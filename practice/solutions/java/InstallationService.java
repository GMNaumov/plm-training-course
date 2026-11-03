import java.sql.*;
import java.time.OffsetDateTime;
import java.util.*;

public class InstallationService {
    public static String install(Connection db,String id,String parent,String position,String child,
                                 OffsetDateTime from,OffsetDateTime to) throws Exception {
        Lab.require(!id.isBlank() && !position.isBlank() && !parent.equals(child) && (to==null || from.isBefore(to)),"BAD_INSTALLATION");
        // Одноуровневый сервис: сборка -> деталь. Циклы исключены типами.
        // В более общей модели потребуется сериализуемая проверка предков.
        var kinds=new HashMap<String,String>();
        try(var p=Lab.bind(db,"SELECT s.id,i.kind FROM lab.serial_unit s JOIN lab.revision r ON r.id=s.revision_id " +
                "JOIN lab.item i ON i.id=r.item_id WHERE s.id IN (?,?) ORDER BY s.id FOR UPDATE OF s",parent,child);
                var r=p.executeQuery()) {while(r.next()) kinds.put(r.getString(1),r.getString(2));}
        Lab.require("assembly".equals(kinds.get(parent)) && "part".equals(kinds.get(child)),"UNSUPPORTED_SERIAL_KINDS");
        // Блокировки родителя и ребёнка защищают обе области конфликтов.
        try(var p=Lab.bind(db,"""
            SELECT id FROM lab.installation
            WHERE ((parent_serial=? AND position=?) OR child_serial=?)
              AND tstzrange(effective_from,effective_to,'[)') && tstzrange(?,?,'[)')
            """,parent,position,child,from,to);var r=p.executeQuery()) {
            Lab.require(!r.next(),"OVERLAPPING_INSTALLATION");
        }
        Lab.update(db,"INSERT INTO lab.installation VALUES (?,?,?,?,?,?,clock_timestamp(),?)",
                id,parent,position,child,from,to,"J09: контролируемая установка");
        return "INSTALLED";
    }
    public static void main(String[] args) throws Exception {
        Lab.args(args,7,7,"Параметры: <идентификатор> <родитель> <позиция> <компонент> <начало> <конец_или_дефис> training");
        Lab.require("training".equals(args[6]),"EXPECTED_TRAINING_MARKER");
        var from=OffsetDateTime.parse(args[4]);var to=args[5].equals("-")?null:OffsetDateTime.parse(args[5]);
        var result=Lab.tx(Connection.TRANSACTION_READ_COMMITTED,
                db->install(db,args[0],args[1],args[2],args[3],from,to));
        System.out.println(result);
    }
}
