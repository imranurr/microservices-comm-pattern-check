package com.imranur.microservices.comm.pattern.check.Utils;

import com.imranur.microservices.comm.pattern.check.Models.DockerServices;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DockerComposeUtils {

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

    public static List<Path> find(String fileName, String searchDirectory) throws IOException {
        try (Stream<Path> files = Files.walk(Paths.get(searchDirectory))) {
            return files
                    .filter(f -> f.getFileName().toString().equals(fileName))
                    .collect(Collectors.toList());

        }
    }

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
}
