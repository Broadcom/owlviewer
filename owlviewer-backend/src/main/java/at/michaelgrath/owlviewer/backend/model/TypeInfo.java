package at.michaelgrath.owlviewer.backend.model;

import java.util.Objects;

public class TypeInfo implements ITypeInfo {

    private Integer minCardinality;
    private Integer maxCardinality;
    private String name;
    private String localName;
    private String typeIRI;

    public TypeInfo(String name, String localName, String typeIRI) {
        setName(name);
        setLocalName(localName);
        setIRI(typeIRI);
    }

    public TypeInfo(String name, String localName, String typeIRI, Integer minCardinality, Integer maxCardinality) {
        setName(name);
        setLocalName(localName);
        setIRI(typeIRI);
        setMinCardinality(minCardinality);
        setMaxCardinality(maxCardinality);
    }

    @Override
    public void setMinCardinality(Integer minCardinality) {
        this.minCardinality = minCardinality;
    }

    @Override
    public Integer getMinCardinality() {
        return minCardinality;
    }

    @Override
    public void setMaxCardinality(Integer maxCardinality) {
        this.maxCardinality = maxCardinality;
    }

    @Override
    public Integer getMaxCardinality() {
        return maxCardinality;
    }

    @Override
    public void setIRI(String typeIRI) {
        this.typeIRI = typeIRI;
    }

    @Override
    public String getIRI() {
        return typeIRI;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setLocalName(String localName) {
        this.localName = localName;
    }

    @Override
    public String getLocalName() {
        return localName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minCardinality, maxCardinality, name, localName, typeIRI);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass().isAssignableFrom(obj.getClass())) return false;
        ITypeInfo fillerObj = (ITypeInfo) obj;
        return Objects.equals(minCardinality, fillerObj.getMinCardinality()) &&
                Objects.equals(maxCardinality, fillerObj.getMaxCardinality()) &&
                Objects.equals(name, fillerObj.getMaxCardinality()) &&
                Objects.equals(localName, fillerObj.getMaxCardinality()) &&
                Objects.equals(typeIRI, fillerObj.getIRI());
    }
}
