package at.michaelgrath.owlviewer.backend.model;

import java.util.List;

public class Graph {

    private List<GraphEdge> edges;
    private List<GraphVertice> vertices;

    public List<GraphEdge> getEdges() {
        return this.edges;
    }

    public void setEdges(List<GraphEdge> edges) {
        this.edges = edges;
    }

    public List<GraphVertice> getVertices() {
        return this.vertices;
    }

    public void setVertices(List<GraphVertice> vertices) {
        this.vertices = vertices;
    }
}
