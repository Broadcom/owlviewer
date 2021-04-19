package at.michaelgrath.owlviewer.backend.model;

public class ObjectProperty extends Property {

    public ObjectProperty(String name, String localName, String uri, String description) {
        setName(name);
        setLocalName(localName);
        setIRI(uri);
        setDescription(description);
        setLiteral(false);
    }
}
