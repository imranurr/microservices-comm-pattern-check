package com.imranur.microservices.comm.pattern.check.Utils;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class DBUtilService {
    public static GraphDatabaseService getGraphDatabaseService(String dbName) {
        GraphDatabaseFactory graphDbFactory = new GraphDatabaseFactory();

        if (dbName.equals("")) {
            dbName = "sampleProject";
            System.out.println("no project name entered, default project name is" + dbName);
        }
        return graphDbFactory.newEmbeddedDatabase(
                new File("neo4jData/" + dbName));
    }

    public static void saveNodesToEmbeddedDb(ArrayList<Map<String, Set<String>>> serviceMappings, GraphDatabaseService graphDb) {
        for (Map<String, Set<String>> entry : serviceMappings) {
            String service = entry.keySet().toString().replace("[", "").replace("]", "");
            graphDb.execute("CREATE (a:Service {name:" + "\"" + service + "\"" + "})");
        }
    }

    public static void makeRelsToEmbeddedDb(ArrayList<Map<String, Set<String>>> serviceMappings, GraphDatabaseService graphDb) {
        for (Map<String, Set<String>> entry : serviceMappings) {
            String service = entry.keySet().toString().replace("[", "").replace("]", "");
            Set<String> strings = entry.get(service);
            strings.forEach(s -> {
                graphDb.execute("MATCH (a:Service {name:" + "\"" + service + "\"" + "})," +
                        "(b:Service {name:" + "\"" + s + "\"" + "})" +
                        "CREATE (a) - [r:depends]-> (b)");
            });
        }
    }
}
