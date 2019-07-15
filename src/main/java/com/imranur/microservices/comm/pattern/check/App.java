package com.imranur.microservices.comm.pattern.check;

import com.imranur.microservices.comm.pattern.check.Models.DockerServices;
import com.imranur.microservices.comm.pattern.check.Utils.DBUtilService;
import com.imranur.microservices.comm.pattern.check.Utils.DockerComposeUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

/**
 * Microservices dependency/communication pattern checking
 */
public class App {
    public static void main(String[] args) throws IOException {

        String directory = args[0];
        String dbName = args[1];
        if (args[0].equals("")) {
            System.out.println("no file path given");
            System.exit(0);
        }
        //Scanner scan = new Scanner(System.in);
        String fileName = "docker-compose.yml";
        //System.out.println("Enter project directory to search ");
        //String directory = scan.next();
        //String directory = "/home/imran/Thesis_Projects/spring-cloud-microservice-example-master";
        // /home/imran/Thesis_Projects/qbike-master
        List<Path> dockerFiles = null;

        Properties props = System.getProperties();
        props.setProperty("javax.accessibility.assistive_technologies", "");

        dockerFiles = DockerComposeUtils.find(fileName, directory);

        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        Yaml yaml = new Yaml(new Constructor(DockerServices.class), representer);
        DockerServices dockerServices = null;

        if (!dockerFiles.isEmpty()) {
            InputStream inputStream = new FileInputStream(new File(dockerFiles.get(0).toString()));
            dockerServices = yaml.load(inputStream);
        } else {
            System.out.println("no docker files found");
            System.exit(0);
        }

        ArrayList<String> serviceLists = new ArrayList<>();
        ArrayList<Map<String, Set<String>>> serviceMappings = new ArrayList<>();
        if (dockerServices.getServices() != null) {
            serviceLists = new ArrayList<>(dockerServices.getServices().keySet());
        } else {
            System.out.println("Incompatible docker compose file");
        }

        if (!serviceLists.isEmpty()) {
            serviceMappings = DockerComposeUtils.getDockerServiceMapping(dockerServices, serviceLists);
        }

        StringBuilder mapping = DockerComposeUtils.getFormattedOutput(serviceMappings);
        System.out.println(mapping.toString());

        DockerComposeUtils.generateGraphImage(dbName, serviceMappings);


        GraphDatabaseService graphDb = DBUtilService.getGraphDatabaseService(dbName);
        Transaction transaction = graphDb.beginTx();

        DBUtilService.saveNodesToEmbeddedDb(serviceMappings, graphDb);

        DBUtilService.makeRelsToEmbeddedDb(serviceMappings, graphDb);
        transaction.close();
        graphDb.shutdown();

        /*
        // FIXME: This snippet is for saving data to neo4j local db instance
        Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "124"));
        try (Session session = driver.session()) {
            DockerComposeUtils.saveNodes(serviceMappings, session);
            DockerComposeUtils.makeRelations(serviceMappings, session);
            session.close();
            driver.close();
        }
        */
    }


}
