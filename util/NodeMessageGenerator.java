package cs455.overlay.util;

import cs455.overlay.dijkstra.RoutingCache;
import cs455.overlay.node.Node;
import cs455.overlay.transport.Link;
import cs455.overlay.wireformats.*;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A NodeMessageGenerator class to send random values to random nodes
 * throughout the overlay in a series of messages sent over a period of
 * rounds designated by the Statics final declarations.
 *
 * @author ahrtmn, 16 02 2014
 */
public class NodeMessageGenerator extends Thread
{
    /**
     * HashMap reference to all the registered nodes in the overlay for
     * random selection for message sending.
     */
    private HashMap<String, Link> _registeredNodes;

    /**
     * String of the registry ID to be sent with all outgoing messages
     */
    private String _registryID;

    /**
     * Node reference to the Node wishing to be notified its job has been completed
     */
    private Node _callbackNode;

    /**
     * RoutingCache reference to the routing cache for this node
     */
    private RoutingCache _routing;

    /**
     * int for the message send tracker
     */
    private int _sendTracker;

    /**
     * long int for the send summation
     */
    private long _sendSummation;

    /**
     * EventFactory for messages
     */
    private EventFactory _eventFactory;


    /**
     * Constructor for the NodeMessageGenerator class.
     *
     * @param registeredNodes <code>HashMap<String, Link></code> reference to all registered nodes in the Overlay.
     * @param registryID      <code>String</code> of the Registry that all MessagingNode's in this Overlay connect to.
     * @param callbackNode    <code>Node</code> reference to the node in the overlay that requested this job.
     */
    public NodeMessageGenerator(HashMap<String, Link> registeredNodes, String registryID, Node callbackNode,
                                RoutingCache routing)
    {
        _registeredNodes = registeredNodes;
        _registryID = registryID;
        _callbackNode = callbackNode;
        _routing = routing;
        _sendTracker = 0;
        _sendSummation = 0;
        _eventFactory = EventFactory.getFactoryInstance();
    }

    public void run()
    {
        /**
         * int to hold the random value to send to the random destination node
         */
        int randVal;

        /**
         * int to hold the random node key index to determine our sink node
         */
        int randNodeIndex;

        /**
         * Random object for generating the random value
         */
        Random rand = new Random(new Date().getTime());

        /**
         * String to hold the random sink node
         */
        String randomSinkNode;

        /**
         * Object array to hold the strings of all our node ID's
         */
        Object[] nodeKeys = _routing.getRoutingMap().keySet().toArray();
//        Object[] registeredKeys = _registeredNodes.keySet().toArray();

        /**
         * LinkedList for the shortest path to the sink node
         */
        LinkedList<String> shortestPath;

        /**
         * String containing the sending node's ID
         */
        String nodeID = _callbackNode.getID();

        /**
         * String array for the information in the callback nodes ID
         */
        String tokens[] = nodeID.split(":");

        /**
         * String for the sending node's address
         */
        String nodeAddress = tokens[0];

        /**
         * int for the sending node's port number
         */
        int nodePort = Integer.parseInt(tokens[1]);





        /**
         * int for number of rounds completed
         */
        int numRounds = 0;

        /**
         * int for the number of messages sent during the round
         */
        int numSent = 0;


        System.out.println("generating");
        /**
         * Send 5000 rounds of 5 random messages
         */
//        while (numRounds < Statics.NODE_MESSAGE_ROUNDS)
        while(numRounds < 1000)
        {

            randNodeIndex = rand.nextInt(nodeKeys.length);
            randomSinkNode = nodeKeys[randNodeIndex].toString();


            // WHILE the random node index continues to be the registry or ourselves
            while (randomSinkNode.equals(_registryID) || randomSinkNode.equals(nodeID))
            {
                randNodeIndex = rand.nextInt(nodeKeys.length);
                randomSinkNode = nodeKeys[randNodeIndex].toString();
            }


            shortestPath = _routing.getRoute(randomSinkNode);   // get the shortest path to the chosen node



            /**
             * 5 messages each with different random values to the same random node, 5000 times
             */
            while(numSent < Statics.NODE_MESSAGES_PER_ROUND)
//            while(numSent < 100)
            {
                randVal = rand.nextInt();       // get the random value to send

                /**
                 * PayloadMessage to be sent
                 */
                PayloadMessage payloadMessage = (PayloadMessage) _eventFactory.createEvent(Protocol
                        .PAYLOAD_MESSAGE, nodeAddress, nodePort, nodeID);

                // Add the shortest path to our message
                for (String s : shortestPath)
                    payloadMessage.getPath().add(s);


                payloadMessage.setPayload(randVal);

                _registeredNodes.get(payloadMessage.getPath().getFirst()).send(payloadMessage.getBytes());


                _sendTracker += 1;          // increment the count for total messages sent
                _sendSummation += randVal;  // sum the random value computed with the existing sum

//                payloadMessage = null;

                numSent += 1;

            }

//            try
//            {
//
//                Thread.sleep(20);
//            }
//            catch(InterruptedException ie)
//            {
//                ie.printStackTrace();
//            }

            numSent = 0;
            numRounds += 1;               // increment loop variant
//            System.out.println("round finished");

        }

//        System.out.println("complete");
        /**
         * Notify the calling node we completed our task
         */
        TrafficSummary trafficSummary = (TrafficSummary) _eventFactory.createEvent(Protocol
                .TRAFFIC_SUMMARY);
        trafficSummary.setNumSent(_sendTracker);
        trafficSummary.setSentSummation(_sendSummation);

        try
        {
            _callbackNode.onEvent(null, trafficSummary);
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
}