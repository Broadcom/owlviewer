package at.michaelgrath.owlviewer.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GraphEdge extends GraphElement implements IGraphElement {

    private String sourceUri;
    private String targetUri;

    public GraphEdge(String uri, String sourceUri, String targetUri, String label) {
        setSourceUri(sourceUri);
        setTargetUri(targetUri);
        setLabel(label);
        setUri(uri);
    }

    @JsonProperty("source")
    public String getSourceUri() {
        return this.sourceUri;
    }

    public void setSourceUri(String sourceUri) {
        this.sourceUri = sourceUri;
    }

    @JsonProperty("target")
    public String getTargetUri() {
        return this.targetUri;
    }

    public void setTargetUri(String targetUri) {
        this.targetUri = targetUri;
    }

}
