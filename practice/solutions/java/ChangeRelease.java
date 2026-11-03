import java.sql.*;

public class ChangeRelease {
    public static String release(Connection db,String command,String change,int expected,boolean fail) throws Exception {
        Lab.require(!command.isBlank() && expected>=0,"BAD_COMMAND");
        // Сначала резервируем commandId. UNIQUE защищает и ещё отсутствующую запись.
        int reserved=Lab.update(db,"INSERT INTO lab.java_command VALUES (?,?,?,'PENDING') ON CONFLICT DO NOTHING",
                command,change,expected);
        if(reserved==0) {
            try(var p=Lab.bind(db,"SELECT change_id,expected_version,result FROM lab.java_command WHERE id=?",command);
                    var r=p.executeQuery()) {
                r.next();Lab.require(change.equals(r.getString(1)) && expected==r.getInt(2),"COMMAND_CONFLICT");
                return "REPLAY:"+r.getString(3);
            }
        }
        // Все писатели change_action этого протокола сначала блокируют change_order.
        // FK java_command уже взял KEY SHARE. NO KEY UPDATE совместим с ним:
        // FOR UPDATE здесь привёл бы к взаимной блокировке при одновременном усилении блокировок.
        try(var p=Lab.bind(db,"SELECT state,lock_version FROM lab.change_order WHERE id=? FOR NO KEY UPDATE",change);
                var r=p.executeQuery()) {
            Lab.require(r.next(),"UNKNOWN_CHANGE");
            Lab.require(r.getInt(2)==expected,"STALE_VERSION");
            Lab.require("approved".equals(r.getString(1)),"BAD_STATE");
        }
        Lab.require("0".equals(Lab.text(db,"SELECT count(*) FROM lab.change_action WHERE change_id=? AND status='pending'",change)),
                "PENDING_ACTIONS");
        int updated=Lab.update(db,"UPDATE lab.change_order SET state='implemented',lock_version=lock_version+1 " +
                "WHERE id=? AND lock_version=? AND state='approved'",change,expected);
        Lab.require(updated==1,"STALE_VERSION");
        // Версия потока изменения равна lock_version+1. Отдельный агрегат от MBOM.
        String event="CHANGE:"+change+":"+(expected+1);
        Lab.update(db,"INSERT INTO lab.java_outbox VALUES (?,?,?,?)",event,"CHANGE:"+change,expected+1,"implemented");
        Lab.update(db,"INSERT INTO lab.java_delivery(event_id) VALUES (?)",event);
        if(fail) throw new IllegalStateException("FAIL_BEFORE_COMMIT");
        Lab.update(db,"UPDATE lab.java_command SET result='IMPLEMENTED' WHERE id=?",command);
        return "IMPLEMENTED";
    }
    public static void main(String[] args) throws Exception {
        Lab.args(args,3,4,"Параметры: <идентификатор_команды> <изменение> <ожидаемая_версия> [fail — сбой до фиксации]");
        Lab.require(args.length==3 || args[3].equals("fail"),"BAD_FLAG");
        var result=Lab.tx(Connection.TRANSACTION_READ_COMMITTED,
                db->release(db,args[0],args[1],Integer.parseInt(args[2]),args.length==4 && args[3].equals("fail")));
        System.out.println(result);
    }
}
