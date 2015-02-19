package cs455.overlay.dijkstra;

import java.util.List;

/**
 * Class defining what a Graph is
 */
public class Graph
{
    private List<Vertex> _vertices;
    private List<Edge> _edges;

    // constructors
    public Graph(List<Vertex> vertices, List<Edge> edges)
    {
        setVertices(vertices);
        setEdges(edges);
    }

    public void setVertices(List<Vertex> _vertices)
    {
        this._vertices = _vertices;
    }

    public List<Edge> getEdges()
    {
        return _edges;
    }

    public void setEdges(List<Edge> _edges)
    {
        this._edges = _edges;
    }

    public void addVertex(Vertex v)
    {
        _vertices.add(v);
    }

    public void addEdge(Edge e)
    {
        _edges.add(e);
    }


}