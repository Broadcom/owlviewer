package at.michaelgrath.owlviewer.backend.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "class")
public class OntologyClass {

    private String localName;
    private String uri;
    private List<OntologyClass> subClasses;

    public OntologyClass() {}

    public OntologyClass(String localName, String uri, List<OntologyClass> subClasses) {
        setLocalName(localName);
        setUri(uri);
        setSubClasses(subClasses);
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    @XmlAttribute(name = "localName")
    public String getLocalName() {
        return this.localName;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @XmlAttribute(name = "uri")
    public String getUri() {
        return this.uri;
    }

    public void setSubClasses(List<OntologyClass> classes) {
        this.subClasses = classes;
    }

    @XmlElementWrapper(name = "subClasses")
    @XmlElement(name = "class")
    public List<OntologyClass> getSubClasses() {
        return this.subClasses;
    }

}
