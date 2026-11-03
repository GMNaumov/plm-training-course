import java.sql.*;
import java.math.BigDecimal;
import java.util.*;

public class BomDiff {
    record Value(String parent,String item,String revision,BigDecimal local,BigDecimal total,String unit) {}
    static Map<String,Value> load(Connection db,String id) throws SQLException {
        Lab.text(db,"SELECT id FROM lab.java_snapshot WHERE id=? AND sealed",id); // отсутствующий != пустой
        var lines=new TreeMap<String,Value>();
        try(var p=Lab.bind(db,"SELECT path,parent_revision,child_item,revision_id,local_quantity,root_quantity,unit " +
                "FROM lab.java_snapshot_line WHERE snapshot_id=?",id);var r=p.executeQuery()) {
            while(r.next()) lines.put(r.getString(1),new Value(r.getString(2),r.getString(3),r.getString(4),
                    r.getBigDecimal(5),r.getBigDecimal(6),r.getString(7)));
        }
        return lines;
    }
    public static List<String> compare(Connection db,String oldId,String newId) throws SQLException {
        var old=load(db,oldId);var next=load(db,newId);
        var paths=new TreeSet<>(old.keySet());paths.addAll(next.keySet());
        var result=new ArrayList<String>();
        for(var path:paths) {
            var a=old.get(path);var b=next.get(path);
            if(a==null) {result.add(path+" ADDED");continue;}
            if(b==null) {result.add(path+" REMOVED");continue;}
            var changes=new ArrayList<String>();
            if(!a.parent().equals(b.parent()) || !a.item().equals(b.item())) changes.add("IDENTITY");
            if(!a.revision().equals(b.revision())) changes.add("REVISION");
            if(!a.unit().equals(b.unit())) changes.add("UNIT_REVIEW");
            // compareTo игнорирует scale, equals отличил бы 2.0 от 2.00.
            if(a.local().compareTo(b.local())!=0) changes.add("LOCAL_QUANTITY");
            if(a.total().compareTo(b.total())!=0) changes.add("ROOT_QUANTITY");
            if(!changes.isEmpty()) result.add(path+" "+String.join(",",changes));
        }
        return result;
    }
    public static void main(String[] args) throws Exception {
        Lab.args(args,2,2,"Параметры: <исходный_снимок> <новый_снимок>");
        var report=Lab.tx(Connection.TRANSACTION_REPEATABLE_READ,db->compare(db,args[0],args[1]));
        report.forEach(System.out::println);System.out.println("CHANGED_PATHS="+report.size());
    }
}
