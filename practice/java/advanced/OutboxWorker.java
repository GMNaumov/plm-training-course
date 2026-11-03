import java.sql.*;
import java.util.UUID;

public class OutboxWorker {
    public record Claim(String event,String aggregate,int version,String payload,UUID token) {}
    public static Claim claim(Connection db) throws Exception {
        // TODO: реализуйте контракт из задания.
        throw new UnsupportedOperationException("Реализуйте методы по контракту задания: OutboxWorker.java");
    }
    public static boolean acknowledge(Connection db,Claim c) throws Exception {
        // TODO: реализуйте контракт из задания.
        throw new UnsupportedOperationException("Реализуйте методы по контракту задания: OutboxWorker.java");
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
