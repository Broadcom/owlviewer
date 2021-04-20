# OWL Viewer

OWL Viewer creates a graph representation of provided OWL ontologies. Named OWL classes are mapped to nodes  whereas a 
property's domain and range as well as property restriction on classes are mapped to edges. By clicking on a node in the
generated graph displays the associated properties in a detail pane.

## Modules

- **owlviewer-backend**: Parses the provided owl-files and provides an abstracted view on the ontology that can be 
consumed using REST API endpoints.
- **owlviewer-frontend**: Wraps a ReactJS frontend application that visualizes the ontologies as a graph that can be
used to browse through major concepts (classes) of the ontologies and its details (properies).
- **owlviewer-doc**: A asciidoc documentation of OWL Viewer (HTML).

## Build

### Build jar-files
Use Maven to build the application and create the appropriate fat jar-files:
```
mvn clean package
```

### Build docker image

Create the docker image of the backend module:
```
cd owlviewer-backend
mvn spring-boot:build-image
```

Create the docker image of the backend module:
```
cd ../owlviewer-frontend
mvn spring-boot:build-image
```

## Run

**Note:** Please configure the location of your owl-files before running the backend service (see the _Configuration_ 
section for more details)!

### Run application

Run the backend service:
```
java -jar owlviewer-backend/target/owlviewer-backend-1.0-SNAPSHOT.jar
```

Run the frontend service:
```
java -Dserver.port="8081" -jar owlviewer-frontend/target/owlviewer-frontend-1.0-SNAPSHOT.jar
```

### Run docker container

Run the backend container:
```
docker run -d -p 8080:8080 owlviewer-backend:1.0-SNAPSHOT
```

Run the frontend container:
```
docker run -d -p 8081:8080 owlviewer-frontened:1.0-SNAPSHOT
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
