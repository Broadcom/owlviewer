package at.michaelgrath.owlviewer.backend.model;

import java.util.Set;

public interface IProperty {

    void setName(String name);

    String getName();

    void setLocalName(String localName);

    String getLocalName();

    void setIRI(String iri);

    String getIRI();

    void setLiteral(boolean isLiteral);

    boolean isLiteral();

    void setDescription(String description);

    String getDescription();

    void setTypeInfos(Set<ITypeInfo> fillers);

    Set<ITypeInfo> getTypeInfos();

    void mergeTypeInfo(ITypeInfo filler);

}
