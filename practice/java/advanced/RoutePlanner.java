import java.sql.*;
import java.util.*;

public class RoutePlanner {
    record Operation(String family,String resource,boolean qualified,String resourceFamily) {}
    public static List<String> plan(Connection db,String route) throws Exception {
        // TODO: реализуйте контракт из задания.
        throw new UnsupportedOperationException("Реализуйте методы по контракту задания: RoutePlanner.java");
    }
    public static void main(String[] args) throws Exception {
        Lab.args(args,1,1,"Параметры: <маршрут>");
        Lab.tx(Connection.TRANSACTION_REPEATABLE_READ,db->plan(db,args[0])).forEach(System.out::println);
    }
}
