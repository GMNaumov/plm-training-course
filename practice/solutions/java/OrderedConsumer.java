import java.sql.*;

public class OrderedConsumer {
    public static String apply(Connection db,String event,String aggregate,int version,String payload,boolean fail) throws Exception {
        Lab.require(!event.isBlank() && !aggregate.isBlank() && version>0 && !payload.isBlank(),"BAD_EVENT");
        int inserted=Lab.update(db,"INSERT INTO lab.java_event VALUES (?,?,?,?,'PENDING') ON CONFLICT DO NOTHING",
                event,aggregate,version,payload);
        if(inserted==0) {
            // При READ COMMITTED этот SELECT видит результат победившего INSERT.
            try(var p=Lab.bind(db,"SELECT aggregate_id,version,payload,result FROM lab.java_event WHERE event_id=?",event);
                    var r=p.executeQuery()) {
                Lab.require(r.next(),"RETRY_EVENT");
                Lab.require(aggregate.equals(r.getString(1)) && version==r.getInt(2) && payload.equals(r.getString(3)),"EVENT_CONFLICT");
                Lab.require("APPLIED".equals(r.getString(4)),"UNCOMMITTED_PROTOCOL_VIOLATION");
                return "REPLAY:APPLIED";
            }
        }
        // Материализуем строку агрегата, чтобы можно было заблокировать первый приём.
        Lab.update(db,"INSERT INTO lab.java_target(aggregate_id) VALUES (?) ON CONFLICT DO NOTHING",aggregate);
        int current;
        try(var p=Lab.bind(db,"SELECT version FROM lab.java_target WHERE aggregate_id=? FOR UPDATE",aggregate);
                var r=p.executeQuery()) {r.next();current=r.getInt(1);}
        Lab.require(version==current+1,"VERSION_GAP_OR_STALE:current="+current);
        Lab.update(db,"UPDATE lab.java_target SET version=?,payload=? WHERE aggregate_id=?",version,payload,aggregate);
        if(fail) throw new IllegalStateException("FAIL_BEFORE_RECEIPT");
        Lab.update(db,"UPDATE lab.java_event SET result='APPLIED' WHERE event_id=?",event);
        return "APPLIED";
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
