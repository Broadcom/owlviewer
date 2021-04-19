package at.michaelgrath.owlviewer.backend.model;

public class GraphVertice extends GraphElement implements IGraphElement {

    public GraphVertice(String uri, String label) {
        setUri(uri);
        setLabel(label);
    }

}
