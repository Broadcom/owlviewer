package at.michaelgrath.owlviewer.backend.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;
import java.util.Set;

public class OntologyFullClass extends OntologyClass {

    private Set<IProperty> properties;

    public OntologyFullClass() {}

    public OntologyFullClass(String localName, String uri, Set<IProperty> properties, List<OntologyClass> subClasses) {
        super(localName, uri, subClasses);
        setProperties(properties);
    }

    public void setProperties(Set<IProperty> properties) {
        this.properties = properties;
    }

    @XmlElementWrapper(name = "properties")
    @XmlElement(name = "property")
    public Set<IProperty> getProperties() {
        return this.properties;
    }
}
