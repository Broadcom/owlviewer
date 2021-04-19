package at.michaelgrath.owlviewer.backend.api;

import at.michaelgrath.owlviewer.backend.framework.OntologyService;
import at.michaelgrath.owlviewer.backend.model.Graph;
import at.michaelgrath.owlviewer.backend.model.OntologyClass;
import at.michaelgrath.owlviewer.backend.model.OntologyFullClass;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@Tag(name = "Ontology Management")
public class OntologyController {

    Logger LOG = LoggerFactory.getLogger(OntologyController.class);

    private OntologyService ontologyService ;

    @Autowired
    public OntologyController(OntologyService ontologyService) {
        this.ontologyService = ontologyService;
    }

    @RequestMapping(value = {"/ontologies"}, method = RequestMethod.POST)
    @Operation(description = "Upload ontology as OWL file", tags = {"Ontology Management"})
    public void uploadOntology(@RequestParam("file") MultipartFile file) {
        try {
            ontologyService.upload(file, false);
        } catch (IOException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @RequestMapping(value = {"/ontologies"},
            produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    @Operation(description = "Get list of loaded ontology files", tags = {"Ontology Management"})
    public List<String> getOntologyFiles() {
        return ontologyService.getOntologyFiles();
    }

    @RequestMapping(value = {"/ontologies/{name}"},
            produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE}, method = RequestMethod.GET)
    @Operation(description = "Download ontology file", tags = {"Ontology Management"})
    public ResponseEntity<Resource> downloadOntologyFiles(@PathVariable(value = "name", required = false) String name) {
        try {
            Optional<File> file = ontologyService.getOntologyFile(name);
            if (file.isPresent()) {
                LOG.info("Downloading \"" + file.get().getName() + "\"");
                InputStreamResource resource = new InputStreamResource(new FileInputStream(file.get()));
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + name);
                return ResponseEntity.ok()
                        .headers(headers)
                        .contentLength(file.get().length())
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(resource);
            }
            else {
                String message = "File \"" + name + "\" not found";
                LOG.error(message);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @RequestMapping(value = "/ontologies/{name}", method = RequestMethod.DELETE)
    @Operation(description = "Delete ontology file", tags = {"Ontology Management"})
    public void deleteOntology(@PathVariable(value = "name") String name) {
        try {
            ontologyService.deleteOntologyFile(name);
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @RequestMapping(value = {"/ontologies/graph"},
            produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    @Operation(description = "Get classes as directed graph", tags = {"Ontology Management"})
    public Graph getGraph() {
        return ontologyService.getGraph();
    }

    @RequestMapping(value = {"/ontologies/classes"},
            produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    @Operation(description = "Get ontology classes", tags = {"Ontology Management"})
    public List<OntologyClass> getClasses() {
        return ontologyService.getClasses();
    }

    @RequestMapping(value = {"/ontologies/classes/{localName}"},
            produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    @Operation(description = "Get ontology class", tags = {"Ontology Management"})
    public OntologyFullClass getClassByLocalName(@PathVariable(value = "localName", required = false) String name,
                                                 @RequestParam(value = "namespace", required = false) String namespace) {
        try {
            return ontologyService.getClassByLocalName(name, namespace);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
