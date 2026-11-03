import java.sql.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.*;

public class StreamingExport {
    static String cell(String text) {
        // Учебный TSV: экранируем обратную косую черту, табуляцию и переводы строк.
        return text.replace("\\","\\\\").replace("\t","\\t").replace("\n","\\n").replace("\r","\\r");
    }
    public static long write(Connection db,String snapshot,int copies,Path temporary,boolean fail) throws Exception {
        Lab.require(copies>=1 && copies<=10000,"COPIES_LIMIT");
        Lab.text(db,"SELECT id FROM lab.java_snapshot WHERE id=? AND sealed",snapshot);
        long count=0;
        try(var out=Files.newBufferedWriter(temporary,StandardCharsets.UTF_8);
                var p=Lab.bind(db,"SELECT g.n,l.path,l.revision_id,l.root_quantity::text,l.unit " +
                        "FROM lab.java_snapshot_line l CROSS JOIN generate_series(1,?) g(n) " +
                        "WHERE l.snapshot_id=? ORDER BY g.n,l.path",copies,snapshot)) {
            p.setFetchSize(64); // курсор PostgreSQL требует autoCommit=false
            try(var rows=p.executeQuery()) {
                out.write("copy\tpath\trevision\tquantity\tunit\n");
                while(rows.next()) {
                    for(int i=1;i<=5;i++) {if(i>1) out.write('\t');out.write(cell(rows.getString(i)));}
                    out.write('\n');count++;
                    if(fail && count==3) throw new IOException("FAIL_DURING_EXPORT");
                }
            }
        }
        return count;
    }
    public static void main(String[] args) throws Exception {
        Lab.args(args,2,3,"Параметры: <снимок> <число_копий> [fail — сбой во время выгрузки]");
        Lab.require(args.length==2 || args[2].equals("fail"),"BAD_FLAG");
        var target=Path.of("/artifacts/java-export.tsv");
        var temporary=Files.createTempFile(target.getParent(),"java-export-",".part");
        try {
            long count=Lab.tx(Connection.TRANSACTION_REPEATABLE_READ,
                    db->write(db,args[0],Integer.parseInt(args[1]),temporary,args.length==3 && args[2].equals("fail")));
            // Временный и итоговый файлы на одном томе. Если атомарное переименование не поддерживается,
            // сообщаем ошибку, не ослабляем обещанную гарантию молча.
            Files.move(temporary,target,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);
            System.out.println("EXPORTED="+count+" FILE="+target);
        } finally {Files.deleteIfExists(temporary);}
    }
}
