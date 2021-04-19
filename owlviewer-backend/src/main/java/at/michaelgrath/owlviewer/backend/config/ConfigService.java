package at.michaelgrath.owlviewer.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConfigService {

    private static final String HOME_DIRECTORY = "owlviewer";
    private static final String ONTOLOGY_DIRECTORY = "ontologies";

    private static Logger LOG = LoggerFactory.getLogger(ConfigService.class);

    private String homeDirectory;
    private String ontologiesDirectory;

    @Autowired
    public ConfigService(
            @Value("${owlviewer.home.directory:#{null}}") String homeDirectory,
            @Value("${owlviewer.ontologies.directory:#{null}}") String ontologiesDirectory) {
        String userHome = System.getProperty("user.home");
        this.homeDirectory = getPath(
                homeDirectory == null || homeDirectory.equals(userHome) ? userHome : homeDirectory,
                homeDirectory == null || homeDirectory.equals(userHome) ? HOME_DIRECTORY : null);
        this.ontologiesDirectory = getPath(
                ontologiesDirectory == null || ontologiesDirectory.equals(userHome) ? this.homeDirectory : ontologiesDirectory,
                ontologiesDirectory == null || ontologiesDirectory.equals(userHome) ? ONTOLOGY_DIRECTORY : null);
        LOG.info("owlviewer.home.directory       = " + (this.homeDirectory != null ? ("\"" + this.homeDirectory + "\"") : ""));
        LOG.info("owlviewer.ontologies.directory = " + (this.ontologiesDirectory != null ? ("\"" + this.ontologiesDirectory + "\"") : ""));
    }

    private String getPath(String configuredDirectory, String defaultRelativeDirectory) {
        if (configuredDirectory == null) {
            configuredDirectory = System.getProperty("user.home");
        }
        try {
            return getDirectoryHierarchy(configuredDirectory, defaultRelativeDirectory);
        } catch (IOException e) {
            LOG.error("Failed to create directory structure \"" +
                    configuredDirectory + File.separator + defaultRelativeDirectory + "\"");
        }
        return configuredDirectory;
    }

    private String getDirectoryHierarchy(String basePath, String newDirectories) throws IOException {
        List<String> directories = newDirectories == null || newDirectories.isEmpty() ? new ArrayList<>() :
                Arrays.asList(newDirectories.split(File.separator.equals("\\") ? "\\\\" : File.separator));
        if (!directories.isEmpty()) {
            String newDirectoryPath = basePath + File.separator + directories.get(0);
            if (!Files.exists(Paths.get(newDirectoryPath))) {
                Files.createDirectory(Paths.get(newDirectoryPath));
            }
            directories = directories.subList(1, directories.size());
            return getDirectoryHierarchy(newDirectoryPath, directories.stream().collect(Collectors.joining(File.separator)));
        }
        return basePath;
    }

    public String getHomeDirectory() {
        return homeDirectory;
    }

    public String getOntologiesDirectory() {
        return ontologiesDirectory;
    }

}
