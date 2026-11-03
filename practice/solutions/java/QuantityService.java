import java.sql.*;
import java.math.BigDecimal;

public class QuantityService {
    public record Result(String status,BigDecimal kilograms) {}
    public static Result convert(String raw,String dimension,BigDecimal factor) {
        if(raw==null) return new Result("UNKNOWN",null);
        if(!"mass".equals(dimension)) return new Result("DIMENSION_MISMATCH",null);
        try { return new Result("KNOWN",Lab.positive(raw).multiply(factor)); }
        catch(IllegalArgumentException e) { return new Result("INVALID",null); }
    }
    public static void run(Connection db) throws Exception {
        // LEFT JOIN сохраняет строку при ошибке справочника; FK не заменяет диагностику.
        try(var p=db.prepareStatement("SELECT m.id,m.raw_value,u.dimension,u.factor " +
                "FROM lab.java_measurement m LEFT JOIN lab.unit u ON u.code=m.unit ORDER BY m.id");
                var r=p.executeQuery()) {
            while(r.next()) {
                var result=convert(r.getString(2),r.getString(3),r.getBigDecimal(4));
                System.out.println(r.getString(1)+" "+result.status()+" "+
                        (result.kilograms()==null?"?":result.kilograms().stripTrailingZeros().toPlainString()));
            }
        }
    }
    public static void main(String[] args) throws Exception {
        Lab.args(args,0,0,"Запуск без аргументов");
        Lab.tx(Connection.TRANSACTION_REPEATABLE_READ,db->{run(db);return null;});
    }
}
