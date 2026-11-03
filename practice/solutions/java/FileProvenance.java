import java.sql.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;

public class FileProvenance {
    public record Job(String path,String role,String tool) {}
    public record Digest(Job job,String sha256) {}
    public static Digest hash(Path root,Job job) throws Exception {
        Path relative=Path.of(job.path());
        Lab.require(!relative.isAbsolute(),"ABSOLUTE_PATH");
        Path real=root.resolve(relative).toRealPath();
        Lab.require(real.startsWith(root) && Files.isRegularFile(real),"OUTSIDE_ROOT_OR_NOT_FILE");
        var digest=MessageDigest.getInstance("SHA-256");
        // Источники доступны в Compose только для чтения: конкурентное изменение и подмена символических ссылок исключены.
        try(var input=Files.newInputStream(real)) {
            var buffer=new byte[8192];int read;
            while((read=input.read(buffer))!=-1) digest.update(buffer,0,read);
        }
        return new Digest(job,HexFormat.of().formatHex(digest.digest()));
    }
    static List<Job> jobs(Connection db) throws Exception {
        var result=new ArrayList<Job>();
        try(var p=db.prepareStatement("SELECT path,role,tool_version FROM lab.java_file_manifest ORDER BY path");var r=p.executeQuery()) {
            while(r.next()) {Lab.require(result.size()<1000,"MANIFEST_LIMIT");result.add(new Job(r.getString(1),r.getString(2),r.getString(3)));}
        }
        return List.copyOf(result);
    }
    public static List<Digest> calculate(List<Job> jobs) throws Exception {
        var root=Path.of("/workspace/practice/datasets/java-files").toRealPath();
        var permits=new Semaphore(4);var futures=new ArrayList<Future<Digest>>();
        try(var workers=Executors.newVirtualThreadPerTaskExecutor()) {
            try {
                for(var job:jobs) {
                    permits.acquire(); // ограничиваем выдачу работы до submit, чтобы не создавать тысячи ожидающих потоков
                    try {
                        futures.add(workers.submit(()->{try{return hash(root,job);}finally{permits.release();}}));
                    } catch(RuntimeException e) {permits.release();throw e;}
                }
                var result=new ArrayList<Digest>();
                for(var f:futures) result.add(f.get());
                return List.copyOf(result);
            } catch(Exception e) {
                futures.forEach(f->f.cancel(true));throw e;
            }
        }
    }
    public static void main(String[] args) throws Exception {
        Lab.args(args,1,1,"Параметры: <идентификатор_запуска>");Lab.require(!args[0].isBlank(),"EMPTY_RUN");
        var manifest=Lab.tx(Connection.TRANSACTION_REPEATABLE_READ,FileProvenance::jobs);
        var digests=calculate(manifest); // потоки не разделяют Connection
        Lab.tx(Connection.TRANSACTION_READ_COMMITTED,db->{
            for(var d:digests) Lab.update(db,"INSERT INTO lab.java_file_digest VALUES (?,?,?,?,?)",
                    args[0],d.job().path(),d.job().role(),d.job().tool(),d.sha256());
            return null;
        });
        digests.forEach(d->System.out.println(d.job().path()+" "+d.sha256()+" "+d.job().role()+" "+d.job().tool()));
    }
}
