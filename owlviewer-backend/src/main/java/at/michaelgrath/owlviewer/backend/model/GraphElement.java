package at.michaelgrath.owlviewer.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GraphElement {

    private String label;
    private String uri;

    @JsonProperty("id")
    public String getUri() {
        return this.uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
