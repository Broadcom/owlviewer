# OWL Viewer Frontend

This service wraps the React user interface for the visualization of ontologies.

## Prerequisites
[NodeJS incl. npm](https://nodejs.org/en/) >= 6.14.4

## Build

### Build jar-file
Use the following command to build a executable _jar_-file that contains all required dependencies:
```
mvn clean package
```
### Build docker image
Use maven and spring boot to create a docker image:
```
mvn spring-boot:build-image
```

## Run

### Run application
Use the following command to start the application:
```
java -jar owlviewer-frontend-1.0-SNAPSHOT.jar
```

The default URL for the backend connection is http://localhost:8080. The following command can be used to set the
backend URL to a different value when starting the frontend:

```
java -Dowlviewer.backend.url="http://localhost:8090" -jar owlviewer-frontend-1.0-SNAPSHOT.jar
```

### Run docker container
```
docker run -d -p 8081:8080 owlviewer-frontend:1.0-SNAPSHOT
```
Pass an environment variable in case you would like to specify a specific backend url:
```
docker run -d -p 8080:8080 -e OWLVIEWER_BACKEND_URL="http://localhost:8090" owlviewer-frontend:1.0-SNAPSHOT
```

## Development

### Run development server
1. Switch to the root directory of the React frontend (_/src/main/frontend_)
2. Run the following command:
   ```
   npm start
   ```
   Now you can open the React frontend in your browser using the following URL:
   ```
   http://localhost:3000
   ```  

**Note**: When running the development server, the URL for the backend connection is set to http://localhost:8080 by 
default. In case another URL should be used please adjust the file _src/main/frontend/public/index.html_!

### Build the React frontend
1. Switch to the root directory of the React frontend (_/src/main/frontend_)
2. Run the following command:
   ```
   npm run build
   ```
   The build will result in a new bundle.js file that is located in the _/src/main/frontend/dist_ directory.
   
