import java.sql.*;
import java.math.BigDecimal;
import java.util.*;

public class BaselineSnapshot {
    record Edge(String id,String parent,String item,String revision,BigDecimal quantity,String unit) {}
    record Line(String path,Edge edge,BigDecimal total) {}
    static void walk(String revision,String prefix,BigDecimal parentQuantity,Set<String> ancestors,
                     Map<String,List<Edge>> graph,List<Line> lines,int depth) {
        Lab.require(depth<=128,"DEPTH_LIMIT");
        Lab.require(ancestors.add(revision),"CYCLE:"+prefix);
        for(var e:graph.getOrDefault(revision,List.of())) {
            Lab.require(e.revision()!=null,"MISSING_SELECTION:"+e.id());
            Lab.require(!e.revision().startsWith("!"),"WRONG_ITEM:"+e.id());
            var path=prefix.isEmpty()?e.id():prefix+"/"+e.id();
            var total=parentQuantity.multiply(e.quantity());
            lines.add(new Line(path,e,total));
            Lab.require(lines.size()<=10000,"PATH_LIMIT");
            walk(e.revision(),path,total,ancestors,graph,lines,depth+1);
        }
        ancestors.remove(revision); // повторное использование в другой ветви разрешено
    }
    public static int capture(Connection db,String id) throws Exception {
        Lab.require(id!=null && !id.isBlank(),"EMPTY_SNAPSHOT_ID");
        // Контракт: только BASE, обязательны все occurrence без option_code.
        // Фильтр по выбранным строкам baseline скрыл бы недостающую строку.
        var graph=new HashMap<String,List<Edge>>();
        try(var p=db.prepareStatement("""
            SELECT o.id,o.parent_revision,o.child_item,b.revision_id,r.item_id,
                   o.quantity::text,o.unit,u.dimension
            FROM lab.occurrence o
            LEFT JOIN lab.baseline b ON b.occurrence_id=o.id AND b.name='BASE-01'
            LEFT JOIN lab.revision r ON r.id=b.revision_id
            LEFT JOIN lab.unit u ON u.code=o.unit
            WHERE o.option_code IS NULL ORDER BY o.parent_revision,o.position
            """);var r=p.executeQuery()) {
            while(r.next()) {
                Lab.require("count".equals(r.getString(8)),"UNSUPPORTED_QUANTITY_BASIS");
                String selected=r.getString(4);
                if(selected!=null && !r.getString(3).equals(r.getString(5))) selected="!"+selected;
                var edge=new Edge(r.getString(1),r.getString(2),r.getString(3),selected,
                        Lab.positive(r.getString(6)),r.getString(7));
                graph.computeIfAbsent(edge.parent(),k->new ArrayList<>()).add(edge);
            }
        }
        var lines=new ArrayList<Line>();
        walk("DM-A","",BigDecimal.ONE,new HashSet<>(),graph,lines,0);
        // Контекст каждой включённой строки обязан совпасть. Сохраняем JSON как данные.
        String context=Lab.text(db,"SELECT context::text FROM lab.baseline WHERE name='BASE-01' ORDER BY occurrence_id LIMIT 1");
        try(var p=Lab.bind(db,"SELECT count(*) FROM lab.baseline WHERE name='BASE-01' AND context<>?::jsonb",context);
                var r=p.executeQuery()) { r.next();Lab.require(r.getInt(1)==0,"MIXED_CONTEXT"); }
        Lab.update(db,"INSERT INTO lab.java_snapshot(id,root_revision,source_baseline,rule_version,source_context) " +
                "VALUES (?,'DM-A','BASE-01','snapshot-v1',?::jsonb)",id,context);
        try(var p=db.prepareStatement("INSERT INTO lab.java_snapshot_line VALUES (?,?,?,?,?,?,?,?,?)")) {
            for(var line:lines) {
                var e=line.edge(); Object[] values={id,line.path(),e.parent(),e.id(),e.item(),e.revision(),e.quantity(),line.total(),e.unit()};
                for(int i=0;i<values.length;i++) p.setObject(i+1,values[i]);
                p.addBatch();
            }
            p.executeBatch();
        }
        Lab.update(db,"UPDATE lab.java_snapshot SET sealed=true WHERE id=?",id);
        return lines.size();
    }
    public static void main(String[] args) throws Exception {
        Lab.args(args,1,1,"Параметры: <идентификатор_снимка>");
        System.out.println("CAPTURED="+Lab.tx(Connection.TRANSACTION_REPEATABLE_READ,db->capture(db,args[0])));
    }
}
