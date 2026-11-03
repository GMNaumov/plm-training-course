import java.sql.*;
import java.util.UUID;

public class OutboxWorker {
    public record Claim(String event,String aggregate,int version,String payload,UUID token) {}
    public static Claim claim(Connection db) throws Exception {
        try(var p=db.prepareStatement("""
            SELECT d.event_id,o.aggregate_id,o.version,o.payload
            FROM lab.java_delivery d JOIN lab.java_outbox o USING(event_id)
            WHERE NOT d.delivered AND (d.lease_until IS NULL OR d.lease_until<=clock_timestamp())
            ORDER BY d.event_id FOR UPDATE OF d SKIP LOCKED LIMIT 1
            """);var r=p.executeQuery()) {
            if(!r.next()) return null;
            var token=UUID.randomUUID();
            var claim=new Claim(r.getString(1),r.getString(2),r.getInt(3),r.getString(4),token);
            Lab.update(db,"UPDATE lab.java_delivery SET token=?,lease_until=clock_timestamp()+interval '10 seconds'," +
                    "attempts=attempts+1 WHERE event_id=?",token,claim.event());
            return claim;
        }
    }
    public static boolean acknowledge(Connection db,Claim c) throws Exception {
        return Lab.update(db,"UPDATE lab.java_delivery SET delivered=true,token=NULL,lease_until=NULL " +
                "WHERE event_id=? AND token=? AND NOT delivered AND lease_until>clock_timestamp()",c.event(),c.token())==1;
    }
    public static String deliver(boolean crash) throws Exception {
        var c=Lab.tx(Connection.TRANSACTION_READ_COMMITTED,OutboxWorker::claim);
        if(c==null) return "IDLE";
        // Отдельная транзакция получателя. На время ввода-вывода блокировка отправителя снята.
        String receipt=OrderedConsumer.consume(c.event(),c.aggregate(),c.version(),c.payload(),false);
        System.out.println("Получатель: "+c.event()+" "+receipt);
        if(crash) throw new IllegalStateException("CRASH_AFTER_RECEIVER_COMMIT:"+c.event());
        boolean ack=Lab.tx(Connection.TRANSACTION_READ_COMMITTED,db->acknowledge(db,c));
        return ack?"ACKNOWLEDGED:"+c.event():"LEASE_LOST:"+c.event();
    }
    public static void main(String[] args) throws Exception {
        Lab.args(args,0,1,"Параметры: [crash — сбой после фиксации у получателя]");
        Lab.require(args.length==0 || args[0].equals("crash"),"BAD_FLAG");
        System.out.println(deliver(args.length==1 && args[0].equals("crash")));
    }
}
