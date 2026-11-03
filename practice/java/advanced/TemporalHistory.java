import java.sql.*;
import java.time.OffsetDateTime;
import java.util.*;

public class TemporalHistory {
    public static List<String> at(Connection db,OffsetDateTime effective,OffsetDateTime known) throws Exception {
        // TODO: реализуйте контракт из задания.
        throw new UnsupportedOperationException("Реализуйте методы по контракту задания: TemporalHistory.java");
    }
    public static String correct(Connection db,String fact,int expected,OffsetDateTime knownAt,OffsetDateTime end) throws Exception {
        // TODO: реализуйте контракт из задания.
        throw new UnsupportedOperationException("Реализуйте методы по контракту задания: TemporalHistory.java");
    }
    public static void main(String[] args) throws Exception {
        Lab.args(args,3,5,"Параметры: at <дата_действия> <дата_знания> | correct <факт> <ожидаемая_редакция> <новая_дата_знания> <конец_действия>");
        if(args[0].equals("at") && args.length==3) {
            Lab.tx(Connection.TRANSACTION_REPEATABLE_READ,db->at(db,OffsetDateTime.parse(args[1]),OffsetDateTime.parse(args[2])))
                    .forEach(System.out::println);
        } else {
            Lab.require(args[0].equals("correct") && args.length==5,"BAD_COMMAND");
            var result=Lab.tx(Connection.TRANSACTION_READ_COMMITTED,db->correct(db,args[1],Integer.parseInt(args[2]),
                    OffsetDateTime.parse(args[3]),OffsetDateTime.parse(args[4])));
            System.out.println(result);
        }
    }
}
