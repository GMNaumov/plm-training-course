import java.sql.*;

/** L13. Атомарность эффекта и квитанции в одной БД потребителя. */
public class EventConsumer {
    public static String consume(Connection db, String eventId, String aggregate,
                                 int version, String payload, boolean failBeforeCommit) throws SQLException {
        if (eventId.isBlank() || aggregate.isBlank() || payload.isBlank() || version < 1) {
            throw new IllegalArgumentException("INVALID_CONTRACT");
        }
        db.setAutoCommit(false);
        try {
            // В учебной реализации сериализуем потребителей целиком. Это делает
            // обработку отсутствующего агрегата и общего eventId объяснимой.
            // Для нагрузки нужна более узкая, но столь же целостная стратегия.
            try (var lock = db.createStatement()) {
                lock.execute("LOCK TABLE lab.inbox, lab.erp_definition IN SHARE ROW EXCLUSIVE MODE");
            }
            try (var existing = db.prepareStatement(
                    "SELECT aggregate_id,version,payload,result FROM lab.inbox WHERE event_id=?")) {
                existing.setString(1,eventId);
                try (var rows = existing.executeQuery()) {
                    if (rows.next()) {
                        if (!aggregate.equals(rows.getString(1)) || version != rows.getInt(2)
                                || !payload.equals(rows.getString(3))) {
                            throw new SQLException("KEY_CONFLICT");
                        }
                        String result = "REPLAY:" + rows.getString(4);
                        db.commit();
                        return result;
                    }
                }
            }
            int current = 0;
            try (var query = db.prepareStatement("SELECT version FROM lab.erp_definition WHERE aggregate_id=?")) {
                query.setString(1,aggregate);
                try (var rows = query.executeQuery()) { if (rows.next()) current = rows.getInt(1); }
            }
            if (version != current + 1) throw new SQLException("VERSION_GAP_OR_STALE: current=" + current);
            try (var update = db.prepareStatement(
                    "INSERT INTO lab.erp_definition VALUES (?,?,?) ON CONFLICT(aggregate_id) " +
                    "DO UPDATE SET version=excluded.version,payload=excluded.payload")) {
                update.setString(1,aggregate); update.setInt(2,version); update.setString(3,payload);
                update.executeUpdate();
            }
            if (failBeforeCommit) throw new SQLException("SIMULATED_FAILURE_BEFORE_INBOX");
            try (var receipt = db.prepareStatement("INSERT INTO lab.inbox VALUES (?,?,?,?,?)")) {
                receipt.setString(1,eventId); receipt.setString(2,aggregate);
                receipt.setInt(3,version); receipt.setString(4,payload); receipt.setString(5,"APPLIED");
                receipt.executeUpdate();
            }
            db.commit();
            return "APPLIED";
        } catch (SQLException | RuntimeException exc) {
            db.rollback();
            throw exc;
        } finally {
            db.setAutoCommit(true);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 4 || args.length > 5) {
            throw new IllegalArgumentException("Параметры: <идентификатор_события> <агрегат> <версия> <данные> [fail — сбой до фиксации]");
        }
        try (var db = DriverManager.getConnection(System.getenv("JDBC_URL"),
                System.getenv("JDBC_USER"), System.getenv("JDBC_PASSWORD"))) {
            System.out.println(consume(db,args[0],args[1],Integer.parseInt(args[2]),args[3],
                    args.length == 5 && args[4].equals("fail")));
        }
    }
}
