import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class RevisionResolver {
    public record Context(LocalDate date,String series,int serial) {
        public Context { Lab.require(date!=null && series!=null && !series.isBlank() && serial>0,"BAD_CONTEXT"); }
    }
    public record Decision(String status,List<String> candidates) {}
    public static Decision resolve(Connection db,String item,Context c) throws SQLException {
        // Приоритет выше лучше; rev_order служит вторым ключом, id НЕ снимает ничью.
        Lab.text(db,"SELECT id FROM lab.item WHERE id=?",item);
        var winners=new ArrayList<String>();
        try(var p=Lab.bind(db,"""
            SELECT id,priority,rev_order FROM lab.revision
            WHERE item_id=? AND state='released' AND valid_from<=?
              AND (valid_to IS NULL OR ?<valid_to) AND series=?
              AND serial_from<=? AND (serial_to IS NULL OR ?<=serial_to)
            ORDER BY priority DESC,rev_order DESC,id
            """,item,c.date(),c.date(),c.series(),c.serial(),c.serial());var r=p.executeQuery()) {
            Integer priority=null,order=null;
            while(r.next()) {
                if(priority==null) {priority=r.getInt(2);order=r.getInt(3);}
                if(priority!=r.getInt(2) || order!=r.getInt(3)) break;
                winners.add(r.getString(1));
            }
        }
        return new Decision(winners.isEmpty()?"UNRESOLVED":winners.size()==1?"RESOLVED":"AMBIGUOUS",List.copyOf(winners));
    }
    public static void main(String[] args) throws Exception {
        Lab.args(args,4,4,"Параметры: <изделие> <дата> <серия> <номер>");
        var context=new Context(LocalDate.parse(args[1]),args[2],Integer.parseInt(args[3]));
        var result=Lab.tx(Connection.TRANSACTION_REPEATABLE_READ,db->resolve(db,args[0],context));
        System.out.println(result);
    }
}
