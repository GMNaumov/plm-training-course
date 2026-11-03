import java.sql.*;
import java.time.OffsetDateTime;
import java.util.*;

public class TemporalHistory {
    public static List<String> at(Connection db,OffsetDateTime effective,OffsetDateTime known) throws Exception {
        var result=new ArrayList<String>();
        try(var p=Lab.bind(db,"""
            SELECT fact_id,edition,child_serial FROM lab.java_fact
            WHERE effective_from<=? AND (effective_to IS NULL OR ?<effective_to)
              AND known_from<=? AND (known_to IS NULL OR ?<known_to)
            ORDER BY fact_id,edition
            """,effective,effective,known,known);var r=p.executeQuery()) {
            while(r.next()) result.add(r.getString(1)+"/"+r.getInt(2)+" "+r.getString(3));
        }
        // В исходном учебном наборе все факты относятся к одной позиции DM-100/30.
        Lab.require(result.size()<=1,"CONTRADICTORY_POSITION_HISTORY");
        if(result.isEmpty()) result.add("NO_KNOWN_FACT");
        return result;
    }
    public static String correct(Connection db,String fact,int expected,OffsetDateTime knownAt,OffsetDateTime end) throws Exception {
        String child;OffsetDateTime start,knownFrom;
        try(var p=Lab.bind(db,"SELECT child_serial,effective_from,known_from FROM lab.java_fact " +
                "WHERE fact_id=? AND edition=? AND known_to IS NULL FOR UPDATE",fact,expected);var r=p.executeQuery()) {
            Lab.require(r.next(),"STALE_EDITION");child=r.getString(1);
            start=r.getObject(2,OffsetDateTime.class);knownFrom=r.getObject(3,OffsetDateTime.class);
        }
        Lab.require(knownAt.isAfter(knownFrom) && end.isAfter(start),"BAD_TEMPORAL_BOUNDARY");
        // При конкурентном UPDATE READ COMMITTED повторно проверит known_to IS NULL.
        int closed=Lab.update(db,"UPDATE lab.java_fact SET known_to=? WHERE fact_id=? AND edition=? AND known_to IS NULL",knownAt,fact,expected);
        Lab.require(closed==1,"STALE_EDITION");
        Lab.update(db,"INSERT INTO lab.java_fact VALUES (?,?,?,?,?,?,NULL,?)",fact,expected+1,child,start,end,knownAt,"J10: исправление факта");
        return "CORRECTED:"+(expected+1);
    }
    public static void main(String[] args) throws Exception {
        Lab.args(args,3,5,"Параметры: at <дата_действия> <дата_знания> | correct <факт> <ожидаемая_редакция> <новая_дата_знания> <конец_действия>");
        if(args[0].equals("at") && args.length==3) {
            Lab.tx(Connection.TRANSACTION_REPEATABLE_READ,db->at(db,OffsetDateTime.parse(args[1]),OffsetDateTime.parse(args[2])))
                    .forEach(System.out::println);
        } else {
            Lab.require(args[0].equals("correct") && args.length==5,"BAD_COMMAND");
            var result=Lab.tx(Connection.TRANSACTION_READ_COMMITTED,db->correct(db,args[1],Integer.parseInt(args[2]),
                    OffsetDateTime.parse(args[3]),OffsetDateTime.parse(args[4])));
            System.out.println(result);
        }
    }
}
