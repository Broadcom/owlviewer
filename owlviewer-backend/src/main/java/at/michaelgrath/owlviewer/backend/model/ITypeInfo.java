package at.michaelgrath.owlviewer.backend.model;

public interface ITypeInfo {

    void setMinCardinality(Integer minCardinality);

    Integer getMinCardinality();

    void setMaxCardinality(Integer maxCardinality);

    Integer getMaxCardinality();

    void setIRI(String typeIRI);

    String getIRI();

    void setName(String name);

    String getName();

    void setLocalName(String localName);

    String getLocalName();

}
