import java.sql.*;
import java.math.BigDecimal;
import java.util.*;

public class BaselineSnapshot {
    record Edge(String id,String parent,String item,String revision,BigDecimal quantity,String unit) {}
    record Line(String path,Edge edge,BigDecimal total) {}
    static void walk(String revision,String prefix,BigDecimal parentQuantity,Set<String> ancestors,
                     Map<String,List<Edge>> graph,List<Line> lines,int depth) {
        // TODO: реализуйте контракт из задания.
        throw new UnsupportedOperationException("Реализуйте методы по контракту задания: BaselineSnapshot.java");
    }
    public static int capture(Connection db,String id) throws Exception {
        // TODO: реализуйте контракт из задания.
        throw new UnsupportedOperationException("Реализуйте методы по контракту задания: BaselineSnapshot.java");
    }
    public static void main(String[] args) throws Exception {
        Lab.args(args,1,1,"Параметры: <идентификатор_снимка>");
        System.out.println("CAPTURED="+Lab.tx(Connection.TRANSACTION_REPEATABLE_READ,db->capture(db,args[0])));
    }
}
