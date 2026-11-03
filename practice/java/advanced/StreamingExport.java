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
        // TODO: реализуйте контракт из задания.
        throw new UnsupportedOperationException("Реализуйте методы по контракту задания: StreamingExport.java");
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
