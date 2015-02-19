package cs455.overlay.dijkstra;

import cs455.overlay.wireformats.LinkWeights;

import java.util.LinkedList;
import java.util.List;

/**
 * A Dijkstra class to act as a wrapper class for ShortestPath and its compatriots Graph, Edge,
 * and Vertex. This class will allow a MessagingNode to pass it Data pertaining to the Overlay
 * and have it calculate shortest paths for its message sending.
 *
 * @author ahrtmn, 12 02 2014
 */
public class Dijkstra
{
    /**
     * MessagingNodeList for the ShortestPath to be built from
     */
    private LinkWeights _linkWeights;

    /**
     * List of Vertices to be included in our graph
     */
    private List<Vertex> _vertices;

    /**
     * List of Edges to be included in our graph
     */
    private List<Edge> _edges;

    /**
     * Graph consisting of our vertices and edges
     */
    private Graph _graph;

    /**
     * ShortestPath for calculating the shortest path to various MessagingNodes
     */
    private ShortestPath _shortestPath;

    /**
     * Constructor to construct the Dijkstra object.
     *
     * @param linkWeights <code>LinkWeights</code> message to be used for calculating the shortest paths.
     */
    public Dijkstra(LinkWeights linkWeights)
    {
        // IF the message is null
        if (linkWeights == null)
        {
            System.err.println("Nah ah ah!");
            return;
        }

        _linkWeights = linkWeights;
        _edges = new LinkedList<Edge>();            // create empty list for edges
        _vertices = new LinkedList<Vertex>();       // create empty list for vertices
        _graph = new Graph(_vertices, _edges);      // create empty graph
        _shortestPath = new ShortestPath(_graph);  // create empty shortest path
    }

    /**
     * run method to parse the MessagingNodeList and calculate the shortest path
     */
    public void run()
    {

        /**
         * String array to hold the tokens of our the current link being parsed
         */
        String[] tokens;

        /**
         * String for the sourceID
         */
        String sourceID;

        /**
         * String for the targetID
         */
        String targetID;

        /**
         * int for the weight of the connection between the nodes
         */
        int weight;

        /**
         * Vertex of the source MessagingNode
         */
        Vertex source;

        /**
         * Vertex of the destination MessagingNode
         */
        Vertex destination;

        /**
         * Before creating the new data, clear the current lists in case
         * the user is setting a new link weights message and wanting new
         * shortest paths calculated.
         */
        getEdges().clear();
        getVertices().clear();


        /**
         * For every set of connection info the messaging node list has
         */
        for (String connectionInfo : getLinkWeights().getNodeConnectionInfo())
        {
            /**
             * Get the tokens of the current link
             *
             * tokens[0] = source ID
             * tokens[1] = target ID
             * tokens[2] = link weight
             */
            tokens = connectionInfo.split("\t");
            sourceID = tokens[0];
            targetID = tokens[1];
            weight = Integer.parseInt(tokens[2]);

            source = new Vertex(sourceID);
            destination = new Vertex(targetID);

            // Add our source Vertex to the list of vertices
            _graph.addVertex(source);

            // Add our destination Vertex to the list of vertices
            _graph.addVertex(destination);

            // Add the edge between the two to the edge list, with the weight
            _graph.addEdge(new Edge(source, destination, weight));
//            _graph.addEdge(new Edge(destination, source, weight));

        }

    }


    /**
     * ************************* ACCESSORS AND MUTATORS ******************************
     */
    public LinkWeights getLinkWeights()
    {
        return _linkWeights;
    }

    public List<Vertex> getVertices()
    {
        return _vertices;
    }

    public List<Edge> getEdges()
    {
        return _edges;
    }

    public ShortestPath getShortestPath()
    {
        return _shortestPath;
    }
}
