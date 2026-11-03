import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class RevisionResolver {
    public record Context(LocalDate date,String series,int serial) {
        public Context { Lab.require(date!=null && series!=null && !series.isBlank() && serial>0,"BAD_CONTEXT"); }
    }
    public record Decision(String status,List<String> candidates) {}
    public static Decision resolve(Connection db,String item,Context c) throws SQLException {
        // TODO: реализуйте контракт из задания.
        throw new UnsupportedOperationException("Реализуйте методы по контракту задания: RevisionResolver.java");
    }
    public static void main(String[] args) throws Exception {
        Lab.args(args,4,4,"Параметры: <изделие> <дата> <серия> <номер>");
        var context=new Context(LocalDate.parse(args[1]),args[2],Integer.parseInt(args[3]));
        var result=Lab.tx(Connection.TRANSACTION_REPEATABLE_READ,db->resolve(db,args[0],context));
        System.out.println(result);
    }
}
