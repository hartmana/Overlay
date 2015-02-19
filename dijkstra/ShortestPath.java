package cs455.overlay.dijkstra;

import java.util.*;

/**
 * ShortestPath is an implementation of Dijkstra's algorithm
 */
public class ShortestPath
{
    Graph _graph;
    HashSet<Vertex> _visitedNodes;
    HashSet<Vertex> _unvisitedNodes;
    HashMap<Vertex, Integer> _distance;
    HashMap<Vertex, Vertex> _predecessors;


    ShortestPath(Graph g)
    {
        _graph = g;
    }

    /**
     * execute method to find the distance to every Node in the graph from the source Node
     *
     * @param source <code>Vertex</code> to find distances from.
     */
    public void execute(Vertex source)
    {
        _visitedNodes = new HashSet<Vertex>();
        _unvisitedNodes = new HashSet<Vertex>();
        _distance = new HashMap<Vertex, Integer>();
        _predecessors = new HashMap<Vertex, Vertex>();

        // Visit the source vertex, setting its distance to 0
        _distance.put(source, 0);
        _unvisitedNodes.add(source);

        // WHILE we still have unvisited nodes
        while (_unvisitedNodes.size() > 0)
        {
            // Get the node with the shortest distance
            Vertex node = getMinimum(_unvisitedNodes);

            // Visit that node
            _visitedNodes.add(node);

            // Remove it from the unvisited nodes
            _unvisitedNodes.remove(node);

            // Find the minimal distance to the node
            findMinimalDistances(node);
        }
    }

    /**
     * Method to getMinimum distance
     *
     * @param vertices <code>Set</code> of vertices
     * @return <code>Vertex</code> with shortest distance
     */
    private Vertex getMinimum(Set<Vertex> vertices)
    {
        Vertex minimum = null;
        for (Vertex vertex : vertices)
        {
            if (minimum == null)
            {
                minimum = vertex;
            }
            else
            {
                if (getShortestDistance(vertex) < getShortestDistance(minimum))
                {
                    minimum = vertex;
                }
            }
        }
        return minimum;
    }

    /**
     * findMinimalDistance method to find shortest distance to that node
     *
     * @param node <code>Vertex</code> of distance to be found
     */
    private void findMinimalDistances(Vertex node)
    {
        List<Vertex> adjacentNodes = getSimpleNeighbors(node);
        for (Vertex target : adjacentNodes)
        {
            if (getShortestDistance(target) > getShortestDistance(node)
                    + getDistance(node, target))
            {
                _distance.put(target, getShortestDistance(node) + getDistance(node, target));
                _predecessors.put(target, node);
                _unvisitedNodes.add(target);
            }
        }

    }

    /**
     * getSimpleNeighbors method to get all neighbors of a particular Vertex
     *
     * @param node <code>Vertex</code> to find the neighbors of
     * @return <code>List</code> of neighbors to given <code>Vertex</code>
     */
    public List<Vertex> getSimpleNeighbors(Vertex node)
    {
        List<Vertex> neighbors = new ArrayList<Vertex>();
        for (Edge edge : _graph.getEdges())
        {
            if (edge.getSource().equals(node))
            {
                neighbors.add(edge.getDestination());
            }
        }
        return neighbors;
    }

    /**
     * getDistance method to find distance between two nodes
     *
     * @param node   <code>Vertex</code> source
     * @param target <code>Vertex</code> target
     * @return distance between source and target
     */
    private int getDistance(Vertex node, Vertex target)
    {
        for (Edge edge : _graph.getEdges())
        {
            if (edge.getSource().equals(node)
                    && edge.getDestination().equals(target))
            {
                return edge.getDistance();
            }
        }

        return Integer.MAX_VALUE;
    }

    /**
     * getShortestDistance method to find the shortest distance to a particular Node.
     *
     * @param destination <code>Vertex</code> of the destination Node
     * @return distance to that Node
     */
    public int getShortestDistance(Vertex destination)
    {
        Integer distance = _distance.get(destination);
        if (distance == null)
        {
            return Integer.MAX_VALUE;
        }
        else
        {
            return distance;
        }
    }

    /**
     * getPath method to find a path to a target Node
     *
     * @param target <code>Vertex</code> Node to find path to
     * @return <code>LinkedList</code> of cities in path, or <code>null</code> if there is no path.
     */
    public LinkedList<Vertex> getPath(Vertex target)
    {
        LinkedList<Vertex> path = new LinkedList<Vertex>();
        Vertex step = target;

        // Check if a path exists
        if (_predecessors.get(step) == null)
        {
            return null;
        }

        path.add(step);

        while (_predecessors.get(step) != null)
        {
            step = _predecessors.get(step);
            path.add(step);
        }
        // Put it into the correct order
        Collections.reverse(path);

        return path;
    }
}