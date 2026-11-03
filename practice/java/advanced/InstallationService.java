import java.sql.*;
import java.time.OffsetDateTime;
import java.util.*;

public class InstallationService {
    public static String install(Connection db,String id,String parent,String position,String child,
                                 OffsetDateTime from,OffsetDateTime to) throws Exception {
        // TODO: реализуйте контракт из задания.
        throw new UnsupportedOperationException("Реализуйте методы по контракту задания: InstallationService.java");
    }
    public static void main(String[] args) throws Exception {
        Lab.args(args,7,7,"Параметры: <идентификатор> <родитель> <позиция> <компонент> <начало> <конец_или_дефис> training");
        Lab.require("training".equals(args[6]),"EXPECTED_TRAINING_MARKER");
        var from=OffsetDateTime.parse(args[4]);var to=args[5].equals("-")?null:OffsetDateTime.parse(args[5]);
        var result=Lab.tx(Connection.TRANSACTION_READ_COMMITTED,
                db->install(db,args[0],args[1],args[2],args[3],from,to));
        System.out.println(result);
    }
}
