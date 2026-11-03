import java.sql.*;

public class ChangeRelease {
    public static String release(Connection db,String command,String change,int expected,boolean fail) throws Exception {
        // TODO: реализуйте контракт из задания.
        throw new UnsupportedOperationException("Реализуйте методы по контракту задания: ChangeRelease.java");
    }
    public static void main(String[] args) throws Exception {
        Lab.args(args,3,4,"Параметры: <идентификатор_команды> <изменение> <ожидаемая_версия> [fail — сбой до фиксации]");
        Lab.require(args.length==3 || args[3].equals("fail"),"BAD_FLAG");
        var result=Lab.tx(Connection.TRANSACTION_READ_COMMITTED,
                db->release(db,args[0],args[1],Integer.parseInt(args[2]),args.length==4 && args[3].equals("fail")));
        System.out.println(result);
    }
}
