package cs455.overlay.dijkstra;

/**
 * Class defining what an Edge is for use by our Dijkstra's algorithm
 */
public class Edge
{
    private Vertex _source;
    private Vertex _destination;
    private int _distance;

    public Edge(Vertex source, Vertex destination, int distance)
    {
        setSource(source);
        setDestination(destination);
        setDistance(distance);
    }

    public Vertex getSource()
    {
        return _source;
    }

    public void setSource(Vertex _source)
    {
        this._source = _source;
    }

    public Vertex getDestination()
    {
        return _destination;
    }

    public void setDestination(Vertex _destination)
    {
        this._destination = _destination;
    }

    public int getDistance()
    {
        return _distance;
    }

    public void setDistance(int _distance)
    {
        this._distance = _distance;
    }

    @Override
    public String toString()
    {
        return getSource().toString() + "-->" + getDestination().toString() + "\t" + getDistance();
    }
}
