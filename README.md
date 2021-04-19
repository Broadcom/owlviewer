# OWL Viewer

Limited graph visualization of OWL ontologies

## Build

### Build jar-file
Use Maven to build the application and create a fat jar-file:
```
mvn clean package
```
### Build docker image
Use maven and spring boot buildpacks to create a docker image that gets pushed to the docker registry that is 
configured in the project's pom-file. The password for the registry needs to be passed to the Maven property 
_registry.password_.
```
mvn spring-boot:build-image -Dregistry.password={Password}
```
## Run

### Run application
```
java -jar target/owlviewer-backend-1.0-SNAPSHOT.jar
```
### Run docker container
```
docker run -d -p 8080:8080 owlviewer-backend:1.0-SNAPSHOT
```
## REST API documentation
Please use this link to view the documentation of the provided REST API after building and
running the Sprint Boot application: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Configuration

Configuration properties as well as the corresponding defaults are defined in src/main/resources/application.properties.
The properties can be set by setting the appropriate environment variables before starting the service.

| Property                       | Description                               | Default                         | Environment variable     |
| :----------------------------- | :---------------------------------------- | :------------------------------ | :----------------------- |
| owlviewer.home.directory       | Location of the owlviewer home directory  | %HOMEPATH%/owlviewer            | OWLVIEWER_HOME           |
| owlviewer.ontologies.directory | Path to the owl-files                     | %HOMEPATH%/owlviewer/ontologies | OWLVIEWER_ONTOLOGIES_DIR |
