package com.imranur.microservices.comm.pattern.check.Utils;

import com.imranur.microservices.comm.pattern.check.Models.DockerServices;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import org.neo4j.driver.Session;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DockerComposeUtils {

    /**
     * TODO: Add javadoc
     *
     * @param dockerServices
     * @param serviceLists
     * @return
     */
    public static ArrayList<Map<String, Set<String>>> getDockerServiceMapping(DockerServices dockerServices, ArrayList<String> serviceLists) {
        ArrayList<Map<String, Set<String>>> serviceMappings = new ArrayList<>();
        for (String entry : serviceLists) {
            dockerServices.getServices().forEach((s, services) -> {
                if (entry.equals(s)) {
                    Map<String, Set<String>> service = new HashMap<>();
                    Set<String> dependencies = new HashSet<>();
                    if (services != null && services.getLinks() != null && !services.getLinks().isEmpty()) {
                        dependencies.addAll(services.getLinks());
                    }
                    if (services != null && services.getDepends_on() != null && !services.getDepends_on().isEmpty()) {
                        dependencies.addAll(services.getDepends_on());
                    }
                    service.put(entry, dependencies);
                    serviceMappings.add(service);
                }
            });
        }
        return serviceMappings;
    }

    /**
     * TODO: Add javadoc
     *
     * @param fileName
     * @param searchDirectory
     * @return
     * @throws IOException
     */
    public static List<Path> find(String fileName, String searchDirectory) throws IOException {
        try (Stream<Path> files = Files.walk(Paths.get(searchDirectory))) {
            return files
                    .filter(f -> f.getFileName().toString().equals(fileName))
                    .collect(Collectors.toList());

        }
    }

    /**
     * TODO: Add javadoc
     *
     * @param serviceMappings
     * @return
     */
    public static StringBuilder getFormattedOutput(ArrayList<Map<String, Set<String>>> serviceMappings) {
        StringBuilder mapping = new StringBuilder();
        for (Map<String, Set<String>> entry : serviceMappings) {
            mapping.append(entry.keySet().toString().replace("[", "").replace("]", ""));
            mapping.append(" --> ");
            entry.values().forEach(mapping::append);
            mapping.append("\n");
        }
        return mapping;
    }

    /**
     * TODO: Add javadoc
     *
     * @param serviceMappings
     * @param session
     */
    public static void makeRelations(ArrayList<Map<String, Set<String>>> serviceMappings, Session session) {
        for (Map<String, Set<String>> entry : serviceMappings) {
            String service = entry.keySet().toString().replace("[", "").replace("]", "");
            Set<String> strings = entry.get(service);
            strings.forEach(s -> {
                session.run("MATCH (a:Service {name:" + "\"" + service + "\"" + "})," +
                        "(b:Service {name:" + "\"" + s + "\"" + "})" +
                        "CREATE (a) - [r:depends]-> (b)");
            });

        }
    }

    /**
     * TODO: Add javadoc
     *
     * @param serviceMappings
     * @param session
     */
    public static void saveNodes(ArrayList<Map<String, Set<String>>> serviceMappings, Session session) {
        for (Map<String, Set<String>> entry : serviceMappings) {
            String service = entry.keySet().toString().replace("[", "").replace("]", "");
            session.run("CREATE (a:Service {name:" + "\"" + service + "\"" + "})");
        }
    }

    public static void generateGraphImage(String dbName, ArrayList<Map<String, Set<String>>> serviceMappings) throws IOException {
        File dotFile = new File("output//" + dbName + ".dot");

        if (!dotFile.getParentFile().exists())
            dotFile.getParentFile().mkdirs();
        if (!dotFile.exists())
            dotFile.createNewFile();

        FileWriter fileWriter = new FileWriter(dotFile);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("digraph {");
        for (Map<String, Set<String>> entry : serviceMappings) {
            String service = entry.keySet().toString().replace("[", "").replace("]", "");
            Set<String> strings = entry.get(service);
            strings.forEach(s -> printWriter.println(service.replace("-","_") + "->" + s.replace("-","_") + "[color=blue]"));

        }
        printWriter.print("}");
        printWriter.close();

        MutableGraph g = Parser.read(new FileInputStream(new File("output/" + dbName + ".dot")));
        Graphviz.fromGraph(g).width(800).render(Format.SVG).toFile(new File("output/" + dbName + ".svg"));

        try {
            Files.deleteIfExists(Paths.get("output/" + dbName + ".dot"));
        } catch (NoSuchFileException e) {
            System.out.println("No such file/directory exists");
        } catch (DirectoryNotEmptyException e) {
            System.out.println("Directory is not empty.");
        } catch (IOException e) {
            System.out.println("Invalid permissions.");
        }


        System.out.println("You can find graph image in output/" + dbName + ".svg");
    }

    public static void generateGraphMl(String dbName, ArrayList<Map<String, Set<String>>> serviceMappings) throws IOException {
        Graph graph = new TinkerGraph();
        for (Map<String, Set<String>> entry : serviceMappings) {
            String service = entry.keySet().toString().replace("[", "").replace("]", "");
            graph.addVertex(service);
        }
        for (Map<String, Set<String>> entry : serviceMappings) {
            String service = entry.keySet().toString().replace("[", "").replace("]", "");
            Set<String> strings = entry.get(service);
            strings.forEach(s -> {
                graph.addEdge(service + "->" + s, graph.getVertex(service), graph.getVertex(s), "depends").setProperty("edgelabel", "depends");
            } );

        }
        OutputStream output = new FileOutputStream("output/"+dbName+".graphml");
        GraphMLWriter mlWriter = new GraphMLWriter(graph);
        mlWriter.setNormalize(true);
        mlWriter.outputGraph(graph, output);
        graph.shutdown();
    }
}
