package at.michaelgrath.owlviewer.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public abstract class Property implements IProperty {

    private static Logger LOG = LoggerFactory.getLogger(Property.class);

    private String name;
    private String localName;
    private String iri;
    private String description;
    private Set<ITypeInfo> typeInfos;
    private boolean literal;

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setLocalName(String localName) {
        this.localName = localName;
    }

    @Override
    public String getLocalName() {
        return this.localName;
    }

    @Override
    public void setIRI(String iri) {
        this.iri = iri;
    }

    @Override
    public String getIRI() {
        return this.iri;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setLiteral(boolean isLiteral) {
        this.literal = isLiteral;
    }

    @Override
    public boolean isLiteral() {
        return this.literal;
    }

    @Override
    public void setTypeInfos(Set<ITypeInfo> fillers) {
        this.typeInfos = fillers;
    }

    @Override
    @JsonProperty("types")
    public Set<ITypeInfo> getTypeInfos() {
        return typeInfos != null ? typeInfos : new HashSet<>();
    }

    @Override
    public void mergeTypeInfo(ITypeInfo filler) {
        if (typeInfos != null) {
            Optional<ITypeInfo> foundFiller = typeInfos.stream()
                    .filter(existingFiller -> existingFiller.getIRI().equals(filler.getIRI()))
                    .findFirst();
            if (foundFiller.isPresent()) {
                if (filler.getMinCardinality() != null &&
                        filler.getMinCardinality().equals(filler.getMaxCardinality())) {
                    if (foundFiller.get().getMinCardinality() != null || foundFiller.get().getMaxCardinality() != null) {
                        LOG.warn("Potentially ambiguous cardinality restrictions for filler \"" + filler.getIRI() + "\"");
                    }
                }
                typeInfos.remove(foundFiller.get());
            }
        }
        else {
            typeInfos = new HashSet<>();
        }
        typeInfos.add(filler);
    }
}
