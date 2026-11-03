import java.sql.*;
import java.util.*;

public class RoutePlanner {
    record Operation(String family,String resource,boolean qualified,String resourceFamily) {}
    public static List<String> plan(Connection db,String route) throws Exception {
        var operations=new TreeMap<String,Operation>();
        try(var p=Lab.bind(db,"SELECT o.id,o.family,o.resource_id,r.qualified,r.family FROM lab.operation o " +
                "LEFT JOIN lab.resource r ON r.id=o.resource_id WHERE route=?",route);var r=p.executeQuery()) {
            while(r.next()) operations.put(r.getString(1),new Operation(r.getString(2),r.getString(3),r.getBoolean(4),r.getString(5)));
        }
        Lab.require(!operations.isEmpty(),"UNKNOWN_OR_EMPTY_ROUTE");
        var degree=new HashMap<String,Integer>();var next=new HashMap<String,List<String>>();
        operations.keySet().forEach(id->degree.put(id,0));
        try(var p=db.prepareStatement("SELECT before_id,after_id FROM lab.precedence ORDER BY before_id,after_id");
                var r=p.executeQuery()) {
            while(r.next()) {
                String a=r.getString(1),b=r.getString(2);
                if(!operations.containsKey(a) && !operations.containsKey(b)) continue;
                Lab.require(operations.containsKey(a) && operations.containsKey(b),"CROSS_ROUTE_DEPENDENCY");
                next.computeIfAbsent(a,k->new ArrayList<>()).add(b);degree.merge(b,1,Integer::sum);
            }
        }
        var ready=new TreeSet<String>();degree.forEach((id,d)->{if(d==0) ready.add(id);});
        var result=new ArrayList<String>();int count=0;
        while(!ready.isEmpty()) {
            // Одна волна: все вершины доступны по предшествованию одновременно.
            var wave=new ArrayList<>(ready);ready.clear();count+=wave.size();
            result.add("WAVE "+String.join(",",wave));
            for(var id:wave) for(var after:next.getOrDefault(id,List.of()))
                if(degree.merge(after,-1,Integer::sum)==0) ready.add(after);
        }
        Lab.require(count==operations.size(),"CYCLE_OR_DEPENDENTS_OF_CYCLE");
        operations.forEach((id,o)->{
            if(!o.qualified() || !o.family().equals(o.resourceFamily())) result.add("BLOCKED_RESOURCE "+id+" "+o.resource());
        });
        result.add("OPERATIONS="+count);
        return result;
    }
    public static void main(String[] args) throws Exception {
        Lab.args(args,1,1,"Параметры: <маршрут>");
        Lab.tx(Connection.TRANSACTION_REPEATABLE_READ,db->plan(db,args[0])).forEach(System.out::println);
    }
}
