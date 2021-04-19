package at.michaelgrath.owlviewer.backend.model;

public class DataProperty extends Property {

    public DataProperty(String name, String localName, String uri, String description) {
        setName(name);
        setLocalName(localName);
        setIRI(uri);
        setDescription(description);
        setLiteral(true);
    }
}
