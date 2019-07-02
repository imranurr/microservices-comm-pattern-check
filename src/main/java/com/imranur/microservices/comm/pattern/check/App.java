package com.imranur.microservices.comm.pattern.check;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Microservices dependency/communication pattern checking
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        Scanner scan = new Scanner(System.in);
        String name = "docker-compose.yml";
        System.out.println("Enter the directory where to search ");
        String directory = "/home/imran/Thesis_Projects/qbike-master";
        List<Path> dockerFiles = null;
        try {
            dockerFiles = find(name, directory);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        Yaml yaml = new Yaml(new Constructor(DockerServices.class), representer);
        DockerServices dockerServices = null;
        try {
            InputStream inputStream =new FileInputStream(new File(dockerFiles.get(0).toString()));
            dockerServices = yaml.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<String> serviceLists = new ArrayList<>(dockerServices.getServices().keySet());
        ArrayList<Map<String, Set<String>>> serviceMappings = new ArrayList<>();

        if (!serviceLists.isEmpty()) {
            //serviceLists.forEach(System.out::println);
            for (String entry : serviceLists){
                dockerServices.getServices().forEach((s, services) -> {
                    if(entry.equals(s)){
                        Map<String, Set<String>> service = new HashMap<>();
                        Set<String> dependencies = new HashSet<>();
                        if(services != null && services.getLinks()!=null &&!services.getLinks().isEmpty()){
                            dependencies.addAll(services.getLinks());
                        }
                        if(services != null && services.getDepends_on()!=null && !services.getDepends_on().isEmpty()){
                            dependencies.addAll(services.getDepends_on());
                        }
                        service.put(entry, dependencies);
                        serviceMappings.add(service);
                    }
                });
            }
        }
        StringBuilder mapping = new StringBuilder();
        for (Map<String, Set<String>> entry : serviceMappings){
            mapping.append(entry.keySet().toString().replace("[", "").replace("]", ""));
            mapping.append(" --> ");
            entry.values().forEach(mapping::append);
            mapping.append("\n");
        }
        System.out.println(mapping.toString());
    }




    protected static List<Path> find(String fileName, String searchDirectory) throws IOException {
        try (Stream<Path> files = Files.walk(Paths.get(searchDirectory))) {
            return files
                    .filter(f -> f.getFileName().toString().equals(fileName))
                    .collect(Collectors.toList());

        }
    }

}
