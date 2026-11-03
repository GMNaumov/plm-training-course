import java.sql.*;

/** L13. Обвязка CLI готова; реализуйте транзакционный метод consume. */
public class EventConsumer {
    public static String consume(Connection db, String eventId, String aggregate,
                                 int version, String payload, boolean failBeforeCommit) throws SQLException {
        // TODO: транзакция; блокировка агрегата, проверка ключа/содержимого,
        // текущей версии; запись эффекта и inbox; rollback при любом отказе.
        throw new UnsupportedOperationException("Реализуйте consume по контракту L13");
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 4 || args.length > 5) {
            throw new IllegalArgumentException("eventId aggregate version payload [fail]");
        }
        try (var db = DriverManager.getConnection(System.getenv("JDBC_URL"),
                System.getenv("JDBC_USER"), System.getenv("JDBC_PASSWORD"))) {
            System.out.println(consume(db,args[0],args[1],Integer.parseInt(args[2]),args[3],
                    args.length == 5 && args[4].equals("fail")));
        }
    }
}
