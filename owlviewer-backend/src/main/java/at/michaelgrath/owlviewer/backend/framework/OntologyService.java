package at.michaelgrath.owlviewer.backend.framework;

import at.michaelgrath.owlviewer.backend.config.ConfigService;
import at.michaelgrath.owlviewer.backend.model.*;
import com.github.owlcs.ontapi.OntManagers;
import com.github.owlcs.ontapi.Ontology;
import com.github.owlcs.ontapi.OntologyManager;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.query.*;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OntologyService {

    private static final String OWL = ".owl";

    private static final Logger LOG = LoggerFactory.getLogger(OntologyService.class);

    private Ontology ontology;
    private OntologyModel ontologyModel;
    private ConfigService configService;
    private String defaultNamespace;

    public enum Properties {
        DATAPROPERTIES,
        OBJECTPROPERTIES,
        ALL
    }

    @Autowired
    public OntologyService(OntologyModel ontologyModel, ConfigService config) {
        this.ontologyModel = ontologyModel;
        this.configService = config;
        this.defaultNamespace = ontologyModel.getNsPrefixMap().isEmpty() ? "" : ontologyModel.getNsPrefixMap().get("");
    }

    public OntologyModel getModel() {
        return ontologyModel;
    }

    @PostConstruct
    public void init() {
        // Pass Jena OntModel to ONT-API
        addOntology(ontologyModel);
    }

    /**
     * Adds the provided Apache Jena OntModel to the ONT-API ontology
     * @param model the Acpahe Jena OntModel implementation
     */
    private void addOntology(OntModel model) {
        OntologyManager manager = ontology != null ?
                ontology.getOWLOntologyManager() : OntManagers.createManager();
        ontology = manager.addOntology(ontologyModel.getGraph());
    }

    /**
     * Removes all loaded owl files from the underlying Apache Jena OntModel implementation and passes
     * the refreshed Apache Jena OntModel to the cleaned ONT-API ontology.
     */
    private void refreshOntologies() {
        LOG.info("Refresh ontologies");
        ontologyModel.refreshOntologies();
        if (ontology.getOWLOntologyManager() != null) {
            ontology.clearCache();
            ontology.getOWLOntologyManager().clearOntologies();
            addOntology(ontologyModel);
        }
        defaultNamespace = ontologyModel.getNsPrefixMap().isEmpty() ? "" : ontologyModel.getNsPrefixMap().get("");
    }

    public List<String> getOntologyFiles() {
        return ontologyModel.getOntologyFiles().stream()
                .filter(file -> file.getName().toLowerCase().endsWith(".owl"))
                .map(File::getName)
                .collect(Collectors.toList());
    }

    public Optional<File> getOntologyFile(String name) {
        return ontologyModel.getOntologyFile(name);
    }

    public void deleteOntologyFile(String name) throws IOException {
        ontologyModel.delete(name);
        refreshOntologies();
    }

    public Path upload(MultipartFile file, boolean override) throws IOException, IllegalArgumentException {
        String extension = getFileExtension(file.getOriginalFilename());
        if (OWL.equalsIgnoreCase(extension)) {
            Path destinationPath = Paths.get(configService.getOntologiesDirectory() + File.separator + file.getOriginalFilename());
            if (!(ontologyModel.getOntologyFile(file.getOriginalFilename()).isPresent() && !override)) {
                Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
                LOG.info("Uploaded \"" + file.getOriginalFilename() + "\" to \"" + configService.getOntologiesDirectory() + "\"");
                refreshOntologies();
            }
            else {
                String message = "Rejected upload because ontology file \"" + file.getOriginalFilename() + "\" already exists";
                LOG.error(message);
                throw new IllegalArgumentException(message);
            }
        }
        else {
            String message = "Rejected upload of \"" + file.getOriginalFilename() + "\" because its file extension " +
                    "does not match \"" + OWL + "\"";
            LOG.error(message);
            throw new IllegalArgumentException(message);
        }
        return null;
    }

    private String getFileExtension(String filename) {
        int lastIndex = filename.lastIndexOf(".");
        if (lastIndex > -1 && filename.length() > lastIndex + 1) {
            return filename.substring(lastIndex);
        }
        return "";
    }

    public List<OntologyClass> getClasses() {
        List<OntologyClass> classes = new ArrayList<>();
        ontologyModel.listClasses()
                .filterKeep(ontClass -> !ontClass.isRestriction() && ontClass.listSuperClasses()
                        .filterKeep(p -> !p.isRestriction()).toList().size() == 0)
                .forEach(ontClass ->  classes.add(new OntologyClass(ontClass.getLocalName(), ontClass.getURI(),
                        getSubClasses(ontClass))));
        return classes;
    }

    public OntologyFullClass getClassByLocalName(String name, String namespace) throws Exception {
        String uri = (namespace != null ? namespace + "#" : defaultNamespace) + name;
        OntClass ontClass = ontologyModel.getOntClass(uri);
        if (ontClass != null) {
            return new OntologyFullClass(ontClass.getLocalName(), ontClass.getURI(),
                    getProperties(ontClass), getSubClasses(ontClass));
        }
        String message = "Could not find class \"" + uri + "\" in ontology";
        LOG.error(message);
        throw new Exception(message);
    }

    public Graph getGraph() {
        Graph graph = new Graph();
        List<GraphEdge> edges = new ArrayList<>();
        List<GraphVertice> vertices = new ArrayList<>();
        ontologyModel.listClasses()
                .filterKeep(ontClass -> ontClass.getURI() != null)
                .forEach(ontClass -> {
                    // map all object properties that define the current class as their domain to edges
                    ontologyModel.listAllOntProperties()
                        .filterKeep(property -> property.hasDomain(ontClass))
                        .forEach(property ->
                            property.listRange().forEach(domain -> edges.add(new GraphEdge(
                                    property.getURI(),
                                    ontClass.getURI(),
                                    domain.getURI(),
                                    getLabel(property))))
                        );
                    // map superClass relationships to edges
                    if (ontClass.hasSuperClass()) {
                        ontClass.listSuperClasses()
                            .filterKeep(superClass -> superClass.getURI() != null)
                            .forEach(superClass -> edges.add(new GraphEdge(
                                    "http://www.w3.org/2000/01/rdf-schema#subClassOf",
                                    ontClass.getURI(),
                                    superClass.getURI(),
                                    "subClassOf")));
                    }
                    // Map class restrictions to edges
                    vertices.add(new GraphVertice(ontClass.getURI(), getLabel(ontClass)));
                    Set<IProperty> properties = getClassRestrictions(ontClass.getURI(), Properties.OBJECTPROPERTIES);
                    properties
                        .forEach(property -> property.getTypeInfos()
                            .forEach(typeInfo ->
                                edges.add(new GraphEdge(
                                        property.getIRI(),
                                        ontClass.getURI(),
                                        typeInfo.getIRI(),
                                        getLabel(ontologyModel.getOntResource(property.getIRI()))))
                            )
                        );
                });
        graph.setVertices(vertices);
        graph.setEdges(edges);
        return graph;
    }

    public List<OntologyClass> getSubClasses(OntClass parentClass) {
        List<OntologyClass> classes = new ArrayList<>();
        this.ontologyModel.listClasses()
                .filterKeep(predicate -> !predicate.isRestriction() && predicate.hasSuperClass() && predicate.listSuperClasses().toList().contains(parentClass))
                .forEach(ontClass -> classes.add(new OntologyClass(ontClass.getLocalName(), ontClass.getURI(), getSubClasses(ontClass))));
        return classes;
    }

    public Set<IProperty> getProperties(OntClass parentClass) {
        Set<IProperty> properties = new HashSet<>();
        // Get properties of super classes
        parentClass.listSuperClasses()
                .filterKeep(ontClass -> !ontClass.isRestriction())
                .forEach(ontClass -> properties.addAll(getProperties(ontClass)));
        // Get property restrictions
        properties.addAll(getClassRestrictions(parentClass.getURI(), Properties.ALL));
        // Get properties with the parentClass as domain
        this.ontologyModel.listAllOntProperties()
                .filterKeep(property -> property.hasDomain(parentClass))
                .forEach(property -> {
                    IProperty newProperty = null;
                    if (property.isObjectProperty()) {
                        newProperty = new ObjectProperty(getLabel(property), property.getLocalName(), property.getURI(), getDescription(property));
                        properties.add(newProperty);
                    }
                    if (property.isDatatypeProperty()) {
                        newProperty = new DataProperty(getLabel(property), property.getLocalName(), property.getURI(), getDescription(property));
                        properties.add(newProperty);
                    }
                    if (newProperty != null) {
                        Set<ITypeInfo> types = new HashSet<>();
                        property.listRange()
                                .mapWith(ontResource -> new TypeInfo(getLabel(ontResource), ontResource.getLocalName(), ontResource.getURI()))
                                .forEach(types::add);
                        newProperty.setTypeInfos(types);
                    }
                });
        return properties;
    }

    private Set<IProperty> getClassRestrictions(String classIRI, Properties propertiesFilter) {
        Map<String, IProperty> properties = new HashMap<>();
        ontology.classesInSignature()
                .filter(owlClass -> owlClass.getIRI().toString().equals(classIRI))
                .forEach(owlClass -> {
                    ontology.subClassAxiomsForSubClass(owlClass).forEach(subClassOfAxiom ->
                            subClassOfAxiom.getSuperClass().accept(new OWLObjectVisitor() {

                        @Override
                        public void visit(OWLDataExactCardinality ce) {
                            applyCardinalityRestriction(ce);
                        }

                        @Override
                        public void visit(OWLDataMinCardinality ce) {
                            applyCardinalityRestriction(ce);
                        }

                        @Override
                        public void visit(OWLDataMaxCardinality ce) {
                            applyCardinalityRestriction(ce);
                        }

                        @Override
                        public void visit(OWLDataSomeValuesFrom ce) {
                            applyQuantifiedRestriction(ce, ce.getFiller().asOWLDatatype().getIRI().toString());
                        }

                        @Override
                        public void visit(OWLDataAllValuesFrom ce) {
                            applyQuantifiedRestriction(ce, ce.getFiller().asOWLDatatype().getIRI().toString());
                        }

                        @Override
                        public void visit(OWLObjectExactCardinality ce) {
                            applyCardinalityRestriction(ce);
                        }

                        @Override
                        public void visit(OWLObjectMinCardinality ce) {
                            applyCardinalityRestriction(ce);
                        }

                        @Override
                        public void visit(OWLObjectMaxCardinality ce) {
                            applyCardinalityRestriction(ce);
                        }

                        @Override
                        public void visit(OWLObjectSomeValuesFrom ce) {
                            applyQuantifiedRestriction(ce, ce.getFiller().asOWLClass().getIRI().toString());
                        }

                        @Override
                        public void visit(OWLObjectAllValuesFrom ce) {
                            applyQuantifiedRestriction(ce, ce.getFiller().asOWLClass().getIRI().toString());
                        }

                        private void applyQuantifiedRestriction(OWLQuantifiedRestriction<?> ce, String fillerIRI) {
                            if (createPropertyIfNotExists(ce) && getIRIFromRestriction(ce) != null) {
                                IProperty property = properties.get(getIRIFromRestriction(ce).toString());
                                if (property != null && ce.getFiller().isNamed()) {
                                    if (OWLObjectSomeValuesFrom.class.isAssignableFrom(ce.getClass()) ||
                                            OWLDataSomeValuesFrom.class.isAssignableFrom(ce.getClass())) {
                                        property.mergeTypeInfo(new TypeInfo(
                                                getLabel(ontologyModel.getOntResource(fillerIRI)),
                                                ontologyModel.getOntResource(fillerIRI).getLocalName(),
                                                fillerIRI,
                                                1,
                                                null));
                                    }
                                    else if (OWLObjectAllValuesFrom.class.isAssignableFrom(ce.getClass()) ||
                                            OWLDataAllValuesFrom.class.isAssignableFrom(ce.getClass())) {
                                        property.mergeTypeInfo(new TypeInfo(
                                                getLabel(ontologyModel.getOntResource(fillerIRI)),
                                                ontologyModel.getOntResource(fillerIRI).getLocalName(),
                                                fillerIRI,
                                                null,
                                                null));
                                    }
                                }
                            }
                        }

                        private void applyCardinalityRestriction(OWLCardinalityRestriction<?> ce) {
                            if (createPropertyIfNotExists(ce) && getIRIFromRestriction(ce) != null) {
                                IProperty property = properties.get(getIRIFromRestriction(ce).toString());
                                if (property != null && ce.getFiller().isNamed()) {
                                    if (OWLObjectCardinalityRestriction.class.isAssignableFrom(ce.getClass())) {
                                        String fillerIRI = ((OWLObjectCardinalityRestriction) ce).getFiller().asOWLClass().getIRI().toString();
                                        if (OWLObjectMinCardinality.class.isAssignableFrom(ce.getClass())) {
                                            property.mergeTypeInfo(new TypeInfo(
                                                    getLabel(ontologyModel.getOntResource(fillerIRI)),
                                                    ontologyModel.getOntResource(fillerIRI).getLocalName(),
                                                    fillerIRI,
                                                    ce.getCardinality(),
                                                    null));
                                        }
                                        else if (OWLObjectMaxCardinality.class.isAssignableFrom(ce.getClass())) {
                                            property.mergeTypeInfo(new TypeInfo(
                                                    getLabel(ontologyModel.getOntResource(fillerIRI)),
                                                    ontologyModel.getOntResource(fillerIRI).getLocalName(),
                                                    fillerIRI,
                                                    null,
                                                    ce.getCardinality()));
                                        }
                                        else if (OWLObjectExactCardinality.class.isAssignableFrom(ce.getClass())) {
                                            property.mergeTypeInfo(new TypeInfo(
                                                    getLabel(ontologyModel.getOntResource(fillerIRI)),
                                                    ontologyModel.getOntResource(fillerIRI).getLocalName(),
                                                    fillerIRI,
                                                    ce.getCardinality(),
                                                    ce.getCardinality()));
                                        }
                                    }
                                    else if (OWLDataCardinalityRestriction.class.isAssignableFrom(ce.getClass())) {
                                        String fillerIRI = ((OWLDataCardinalityRestriction) ce).getFiller().asOWLDatatype().getIRI().toString();
                                        if (OWLDataMinCardinality.class.isAssignableFrom(ce.getClass())) {
                                            property.mergeTypeInfo(new TypeInfo(
                                                    getLabel(ontologyModel.getOntResource(fillerIRI)),
                                                    ontologyModel.getOntResource(fillerIRI).getLocalName(),
                                                    fillerIRI,
                                                    ce.getCardinality(),
                                                    null));
                                        }
                                        else if (OWLDataMaxCardinality.class.isAssignableFrom(ce.getClass())) {
                                            property.mergeTypeInfo(new TypeInfo(
                                                    getLabel(ontologyModel.getOntResource(fillerIRI)),
                                                    ontologyModel.getOntResource(fillerIRI).getLocalName(),
                                                    fillerIRI,
                                                    null,
                                                    ce.getCardinality()));
                                        }
                                        else if (OWLDataExactCardinality.class.isAssignableFrom(ce.getClass())) {
                                            property.mergeTypeInfo(new TypeInfo(
                                                    getLabel(ontologyModel.getOntResource(fillerIRI)),
                                                    ontologyModel.getOntResource(fillerIRI).getLocalName(),
                                                    fillerIRI,
                                                    ce.getCardinality(),
                                                    ce.getCardinality()));
                                        }
                                    }
                                }
                            }
                        }

                        private boolean createPropertyIfNotExists(OWLRestriction cr) {
                            IRI iri = getIRIFromRestriction(cr);
                            if (iri != null && !properties.containsKey(iri.toString())) {
                                if ((propertiesFilter.equals(Properties.DATAPROPERTIES) ||
                                        propertiesFilter.equals(Properties.ALL)) && cr.isDataRestriction()) {
                                    properties.put(iri.toString(),
                                            new DataProperty(
                                                    getLabel(ontologyModel.getOntResource(iri.toString())),
                                                    iri.getFragment(),
                                                    iri.toString(),
                                                    getDescription(ontologyModel.getOntResource(iri.toString()))));
                                    return true;
                                }
                                if ((propertiesFilter.equals(Properties.OBJECTPROPERTIES) ||
                                        propertiesFilter.equals(Properties.ALL)) && cr.isObjectRestriction()) {
                                    properties.put(iri.toString(),
                                            new ObjectProperty(
                                                    getLabel(ontologyModel.getOntResource(iri.toString())),
                                                    iri.getFragment(),
                                                    iri.toString(),
                                                    getDescription(ontologyModel.getOntResource(iri.toString()))));
                                    return true;
                                }
                                return false;
                            }
                            return true;
                        }

                        private IRI getIRIFromRestriction(OWLRestriction cr) {
                            if (cr.isDataRestriction()) {
                                return cr.getProperty().asOWLDataProperty().getIRI();
                            }
                            else if (cr.isObjectRestriction()) {
                                return cr.getProperty().asOWLObjectProperty().getIRI();
                            }
                            return null;
                        }
                    }));
                });
        return new HashSet<>(properties.values());
    }

    public String getLabel(OntResource classOrProperty) {
        if (classOrProperty.getLabel("EN") != null) {
            return classOrProperty.getLabel("EN");
        }
        else if (classOrProperty.getLabel(null) != null) {
            return classOrProperty.getLabel(null);
        }
        return classOrProperty.getLocalName();
    }

    public String getDescription(OntResource classOrProperty) {
        if (classOrProperty.getComment("EN") != null) {
            return classOrProperty.getComment("EN");
        }
        else if (classOrProperty.getComment(null) != null) {
            return classOrProperty.getComment(null);
        }
        return null;
    }
}
