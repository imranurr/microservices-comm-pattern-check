## Microservice Dependency Graph (MicroDepGraph)

MicroDepGraph analyzes the service dependecies of microservices projects based on docker configuration. It produces output as neo4j graph database and also image of dependency graph as SVG format. It analyzes a project which is in local drive. It will create two folders one is "neo4jData" and another one is "output" which holds the generated image of dependency graph.

# Requirements

Java jdk8 or higher.

# How to use it

* clone a git repository containing a java project developed with a micorservice architectural style. 
* execute MicroDepGraph as:     java -jar microservices-dependency-check.jar  <absolute_path_of_the_cloned_repository> <project_name> 

An example command to run the tool from command line is,
 java -jar microservices-dependency-check.jar /home/myuser/ftgo-application-master ftgo-application-master

# List of projects the tool has been currently tested on

| Project name  | Github URL | Dependency Graph 
| ------------- | ------------- | ------------- |
| FTGO example application | [link](https://github.com/microservices-patterns/ftgo-application) | [View](https://raw.githubusercontent.com/clowee/MicroDepGraph/master/resultGraphs/ftgo-application-master.svg)|
| E-Commerce App | [link](https://github.com/venkataravuri/e-commerce-microservices-sample) | [View](https://raw.githubusercontent.com/clowee/MicroDepGraph/master/resultGraphs/ecommerce-microservices.svg) |
| Spring PetClinic Application | [link](https://github.com/spring-petclinic/spring-petclinic-microservices) | [View](https://raw.githubusercontent.com/clowee/MicroDepGraph/master/resultGraphs/Spring-petclinic.svg) |
| QBike | [link](https://github.com/JoeCao/qbike) | [View](https://raw.githubusercontent.com/clowee/MicroDepGraph/master/resultGraphs/qbike.png) |
| Microservice Book Consul Sample | [link](https://github.com/ewolff/microservice-consul) | [View](https://raw.githubusercontent.com/clowee/MicroDepGraph/master/resultGraphs/consul-master.png) |
| Microservices Book | [link](https://github.com/ewolff/microservice) | [View](https://raw.githubusercontent.com/clowee/MicroDepGraph/master/resultGraphs/microservice_sample.png) |
