package at.michaelgrath.owlviewer.backend.framework;

import at.michaelgrath.owlviewer.backend.config.ConfigService;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.impl.OntModelImpl;
import org.apache.jena.shared.Lock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OntologyModel extends OntModelImpl implements OntModel {

    private static Logger LOG = LoggerFactory.getLogger(OntologyModel.class);

    private static OntModelSpec MODEL_SPEC = OntModelSpec.OWL_MEM;

    private ConfigService configService;

    @Autowired
    public OntologyModel(ConfigService config) {
        super(MODEL_SPEC);
        this.configService = config;
    }

    @PostConstruct
    public void init() {
        refreshOntologies();
    }

    public void refreshOntologies() {
        enterCriticalSection(Lock.WRITE);
        try {
            removeAll();
            getOntologyFiles().forEach(ontologyFile -> {
                try {
                    this.read(new FileInputStream(ontologyFile), null);
                } catch (FileNotFoundException e) {
                    LOG.error("Failed to load \"" + ontologyFile + "\"");
                }
            });
        } finally {
            leaveCriticalSection();
        }
    }

    public String getNamespace() {
        return getNsPrefixMap().get("");
    }

    public List<File> getOntologyFiles() {
        List<File> ontologyFiles = new ArrayList<>();
        try {
            ontologyFiles = Files.list(Paths.get(configService.getOntologiesDirectory()))
                    .filter(path -> !Files.isDirectory(path) && path.getFileName().toString().toLowerCase().endsWith(".owl"))
                    .map(path -> path.toFile())
                    .peek(file -> LOG.info("Found ontology \"" + file.toString() + "\""))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOG.error("Failed to open \"" + configService.getOntologiesDirectory() + "\"");
        }
        return ontologyFiles;
    }

    public Optional<File> getOntologyFile(String name) {
        Optional<File> ontologyFile = Optional.empty();
        try {
            ontologyFile = Files.list(Paths.get(configService.getOntologiesDirectory()))
                    .filter(path -> !Files.isDirectory(path) &&
                            path.getFileName().toString().equalsIgnoreCase(name) &&
                            path.getFileName().toString().toLowerCase().endsWith(".owl"))
                    .map(path -> path.toFile())
                    .peek(file -> LOG.info("Found ontology \"" + file.toString() + "\""))
                    .findFirst();
        } catch (IOException e) {
            LOG.error("Failed to open \"" + configService.getOntologiesDirectory() + "\"");
        }
        return ontologyFile;
    }

    public void delete(String name) throws IOException {
        Optional<File> file = getOntologyFile(name);
        if (file.isPresent()) {
            LOG.info("Deleting \"" + file.get().getName() + "\"");
            Files.delete(file.get().toPath());
        }
        else {
            String message = "File \"" + name + "\" not found";
            LOG.error(message);
            throw new IllegalArgumentException(message);
        }
    }

}
