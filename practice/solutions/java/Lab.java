import java.sql.*;
import java.math.BigDecimal;
import java.util.*;

/** Инфраструктура эталонов. Соединение принадлежит одному вызову, не пулу. */
public final class Lab {
    private Lab() {}
    @FunctionalInterface public interface Work<T> { T run(Connection db) throws Exception; }
    public static Connection open() throws SQLException {
        var db = DriverManager.getConnection(System.getenv("JDBC_URL"),
                System.getenv("JDBC_USER"),System.getenv("JDBC_PASSWORD"));
        try (var s=db.createStatement()) {
            s.execute("SET statement_timeout='10s'");
            s.execute("SET lock_timeout='4s'");
        } catch (SQLException e) { db.close(); throw e; }
        return db;
    }
    public static <T> T tx(int isolation, Work<T> work) throws Exception {
        try (var db=open()) {
            db.setTransactionIsolation(isolation);
            db.setAutoCommit(false);
            try {
                T result=work.run(db);
                db.commit();
                return result;
            } catch (Exception e) {
                try { db.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
                throw e;
            }
        }
    }
    public static PreparedStatement bind(Connection db,String sql,Object... args) throws SQLException {
        var p=db.prepareStatement(sql);
        try {
            for(int i=0;i<args.length;i++) p.setObject(i+1,args[i]);
            return p;
        } catch (SQLException e) { p.close(); throw e; }
    }
    public static int update(Connection db,String sql,Object... args) throws SQLException {
        try(var p=bind(db,sql,args)) { return p.executeUpdate(); }
    }
    public static String text(Connection db,String sql,Object... args) throws SQLException {
        try(var p=bind(db,sql,args); var r=p.executeQuery()) {
            if(!r.next()) throw new IllegalArgumentException("MISSING_ROW");
            return r.getString(1);
        }
    }
    public static void require(boolean ok,String message) {
        if(!ok) throw new IllegalArgumentException(message);
    }
    public static BigDecimal positive(String text) {
        require(text!=null && text.length()<=80 && text.matches("[0-9]+(\\.[0-9]+)?"),"INVALID_DECIMAL");
        var n=new BigDecimal(text);
        require(n.signum()>0,"NON_POSITIVE");
        return n;
    }
    public static void args(String[] args,int min,int max,String usage) {
        require(args.length>=min && args.length<=max,usage);
    }
}
