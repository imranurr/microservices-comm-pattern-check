package com.imranur.microservices.comm.pattern.check;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.imranur.microservices.comm.pattern.check.Models.DockerServices;
import com.imranur.microservices.comm.pattern.check.Utils.DBUtilService;
import com.imranur.microservices.comm.pattern.check.Utils.DockerComposeUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Microservices dependency/communication pattern checking
 */
public class App {
    public static void main(String[] args) throws IOException, ParseException {

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

        //DockerComposeUtils.generateGraphImage(dbName, serviceMappings);


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

        DockerComposeUtils.generateGraphMl(dbName, serviceMappings);

        DockerComposeUtils.generateGraphMl(dbName, serviceMappings);
        ArrayList<Path> servicePaths = new ArrayList<>();
        for (String service : serviceLists){
            String servicePath = directory + "/" + service;
            Path folderPath = Paths.get(servicePath);
            if(Files.exists(folderPath, LinkOption.NOFOLLOW_LINKS)){
                //System.out.println(folderPath + " path found");
                servicePaths.add(folderPath);
            }
        }

        //String pathString = "/home/imran/Thesis_Projects/qbike-master/order/src/main/java/club/newtech/qbike/order/controller/OrderController.java";
        String pathString = "/home/imran/Thesis_Projects/e-commerce-microservices-sample-master/cart-microservice/src/main/java/com/nikhu/ecommerce/cart/CartController.java";
        //String pathString = "/home/imran/Thesis_Projects/cloud-native-microservice-strangler-example-master/microservices/profile-service/src/main/java/demo/api/v1/ProfileControllerV1.java";




//        try (Stream<Path> paths = Files.walk(Paths.get("/home/imran/Thesis_Projects/qbike-master/intention"))) {
//            paths
//                    .filter(Files::isRegularFile)
//                    .forEach(System.out::println);
//        }




        for (Path servicePath : servicePaths) {
            try (Stream<Path> stream = Files.find(servicePath, 10,
                    (path, attr) -> path.getFileName().toString().endsWith(".java"))) {
                stream.forEach(path -> {
                    try {
                        parseClasses(path.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        parseClasses(pathString);


    }

    private static void parseClasses(String pathString) throws IOException {
        //new MethodVisitor().visit(cu, null);
        File f = new File(pathString);
        CompilationUnit cu;
        final FileInputStream in = new FileInputStream(f);
        try {
            cu = StaticJavaParser.parse(in);
        } finally {
            in.close();
        }
        new ClassVisitor().visit(cu, null);
    }


    /**
     * Simple visitor implementation for visiting MethodDeclaration nodes.
     */
    private static class MethodVisitor extends VoidVisitorAdapter {

        @Override
        public void visit(MethodDeclaration n, Object arg) {
            System.out.println(n.getName());
            if (n.getAnnotations() != null) {
                for (AnnotationExpr annotation : n.getAnnotations()) {
                    //System.out.println(annotation.getClass());
                    // MarkerAnnotations, for example @Test
                    if (annotation.getClass().equals(MarkerAnnotationExpr.class)) {
                        System.out.println("MarkerAnnotation:" + ((MarkerAnnotationExpr)annotation).getName());
                    }
                    if (annotation.getClass().equals(NormalAnnotationExpr.class)) {
                        for (MemberValuePair pair : ((NormalAnnotationExpr)annotation).getPairs()) {
                            if (pair.getName().equals("groups"))
                                System.out.println("Group:\"" + pair.getValue() + "\"");
                        }
                    }
                }
            }
        }
    }
    private static class ClassVisitor extends VoidVisitorAdapter {
        @Override
        public void visit(ClassOrInterfaceDeclaration n, Object arg) {

            for (AnnotationExpr ann: n.getAnnotations()) {
                if(ann.toString().equals("@RestController")){
                    n.getMembers().stream().forEach(bodyDeclaration -> {
                        for (AnnotationExpr mappingAnnotation : bodyDeclaration.getAnnotations()){
                            if (mappingAnnotation.toString().startsWith("@") && mappingAnnotation.toString().endsWith(")") && mappingAnnotation.toString().contains("Mapping")) {
                                System.out.println(mappingAnnotation);
                                String regex = "^[a-zA-Z0-9]+$";
                                Pattern pattern = Pattern.compile(regex);
                                int i = mappingAnnotation.toString().indexOf('/');
                                String substring = mappingAnnotation.toString().substring(i + 1);
                                String s = substring.split("\\/")[0];
                                Matcher matcher = pattern.matcher(s);
                                if(matcher.matches()){
                                    System.out.println(s);
                                }
                            }
                        }
                    });
                }
            }
        }
    }
}
