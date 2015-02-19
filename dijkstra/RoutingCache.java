package cs455.overlay.dijkstra;

import cs455.overlay.node.Node;
import cs455.overlay.wireformats.LinkWeights;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Class to contain the information of the shortest paths to each other
 * MessagingNode in the overlay based off the LinkWeights message a
 * MessagingNode receives. This class also facilitates the re-building of
 * the cache with a new LinkWeights message by over-writing the old message
 * and calling buildCache() again.
 */
public class RoutingCache
{
    /**
     * Node Reference to the node to find shortest paths from.
     */
    private Node _node;

    /**
     * LinkWeights message that the routing cash is built off of.
     */
    private LinkWeights _linkWeights;

    /**
     * HashMap to hash node ID's to their shortest path. Shortest path will be stored
     * in the form of  "A C B D" hashed to D; where A,B,C, and D are node ID's, separated by tab
     * characters.
     */
    private HashMap<String, LinkedList<String>> _routing;

    /**
     * Constructor for the RoutingCache class
     *
     * @param linkWeights <code>LinkWeights</code> message for the routing cache to be built from.
     * @param callingNode <code>Node</code> to act as our source when finding shortest paths.
     */
    public RoutingCache(LinkWeights linkWeights, Node callingNode)
    {
        _node = callingNode;
        _linkWeights = linkWeights;
        _routing = new HashMap<String, LinkedList<String>>();


        // build the cache for this link weight
        buildCache();
    }


    /**
     * Method to build the routing cache for the Overlay for message
     * sending from the owning MessagingNode.
     */
    public synchronized void buildCache()
    {
        /**
         * Dijkstra object to facilitate calculating the shortest paths
         */
        Dijkstra dijkstra = new Dijkstra(getLinkWeights());
        dijkstra.run();

        /**
         * LinkedList of vertices containing the shortest path from this node to a target
         * node.
         */
        LinkedList<Vertex> shortestPath;

        /**
         * String array to hold the tokens of the link weights connection info once
         * it has been parsed.
         */
        String[] tokens;

        // find the shortest paths from this node
        dijkstra.getShortestPath().execute(new Vertex(getNode().getID()));


        /**
         * For every node in the overlay we need to construct their shortest paths and hash
         * them to the node's ID for later retrieval. Because the link weights message is generic
         * and constructed:
         *
         * sourceID targetID
         * targetID sourceID
         *
         * We only need to process the first token of the connection info to ensure that we
         * have calculated the shortest path for every node in the overlay.
         */
        for (String nodeInfo : getLinkWeights().getNodeConnectionInfo())
        {
            /**
             * tokens[0] = node1 ID
             * tokens[1] = node2 ID     (unused here)
             * tokens[2] = link weight (unused here)
             */
            tokens = nodeInfo.split("\t");

            // IF the current vertex is not this node
            if ((!tokens[0].equals(getNode().getID())) && (!getRoutingMap().containsKey(tokens[0])))
            {

                shortestPath = dijkstra.getShortestPath().getPath(new Vertex(tokens[0]));

                /**
                 * Here, ONCE after creating the shortest path, we verify that the first "stop" is
                 * us (origin point), and then we remove ourselves from the path.
                 */
                if (shortestPath.getFirst().toString().equals(getNode().getID()))
                    shortestPath.removeFirst();

                /**
                 * Then while we're at it, lets convert this linked list of Vertices to
                 * Strings to make it more clean and convenient for the caller.
                 */
                LinkedList<String> path = new LinkedList<String>();

                for (Vertex v : shortestPath)
                    path.add(v.toString());


                /**
                 * Hash the String version of the shortest path to this node
                 */
                _routing.put(tokens[0], path);

                System.out.println("Node: " + tokens[0]);

                for(String s : path)
                {
                    System.out.println("\t" + s);
                }

            }


        }

    }


    /**
     * Method to return a String containing the shortest path to the given sink node.
     *
     * @param sinkID <code>String</code> denoting the ID of the node a route is requested to.
     * @return <code>LinkedList<Vertex></code> containing the shortest path to the sink node.
     */
    public LinkedList<String> getRoute(String sinkID)
    {

        /**
         * return the linked list shortest path
         */
        return getRoutingMap().get(sinkID);

    }


    /**
     * ************************* ACCESSORS AND MUTATORS ******************************
     */

    public LinkWeights getLinkWeights()
    {
        return _linkWeights;
    }

    public Node getNode()
    {
        return _node;
    }

    public HashMap<String, LinkedList<String>> getRoutingMap()
    {
        return _routing;
    }
}
