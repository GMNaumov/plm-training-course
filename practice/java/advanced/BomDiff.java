import java.sql.*;
import java.math.BigDecimal;
import java.util.*;

public class BomDiff {
    record Value(String parent,String item,String revision,BigDecimal local,BigDecimal total,String unit) {}
    static Map<String,Value> load(Connection db,String id) throws SQLException {
        // TODO: реализуйте контракт из задания.
        throw new UnsupportedOperationException("Реализуйте методы по контракту задания: BomDiff.java");
    }
    public static List<String> compare(Connection db,String oldId,String newId) throws SQLException {
        // TODO: реализуйте контракт из задания.
        throw new UnsupportedOperationException("Реализуйте методы по контракту задания: BomDiff.java");
    }
    public static void main(String[] args) throws Exception {
        Lab.args(args,2,2,"Параметры: <исходный_снимок> <новый_снимок>");
        var report=Lab.tx(Connection.TRANSACTION_REPEATABLE_READ,db->compare(db,args[0],args[1]));
        report.forEach(System.out::println);System.out.println("CHANGED_PATHS="+report.size());
    }
}
