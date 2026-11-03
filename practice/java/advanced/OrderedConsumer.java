import java.sql.*;

public class OrderedConsumer {
    public static String apply(Connection db,String event,String aggregate,int version,String payload,boolean fail) throws Exception {
        // TODO: реализуйте контракт из задания.
        throw new UnsupportedOperationException("Реализуйте методы по контракту задания: OrderedConsumer.java");
    }
    public static String consume(String event,String aggregate,int version,String payload,boolean fail) throws Exception {
        return Lab.tx(Connection.TRANSACTION_READ_COMMITTED,db->apply(db,event,aggregate,version,payload,fail));
    }
    public static void main(String[] args) throws Exception {
        Lab.args(args,4,5,"Параметры: <идентификатор_события> <агрегат> <версия> <данные> [fail — сбой до фиксации]");
        Lab.require(args.length==4 || args[4].equals("fail"),"BAD_FLAG");
        System.out.println(consume(args[0],args[1],Integer.parseInt(args[2]),args[3],args.length==5 && args[4].equals("fail")));
    }
}
