package cs455.overlay.util;

import cs455.overlay.wireformats.EventFactory;
import cs455.overlay.wireformats.MessagingNodeList;
import cs455.overlay.wireformats.Protocol;
import cs455.overlay.wireformats.RegistrationRequest;

import java.util.HashMap;

/**
 * Class to facilitate the creation of the MessagingNode overlay.
 */
public class OverlayCreator
{
    /**
     * HashMap with all of the RegistrationRequests corresponding to Node's that are to be part of the overlay
     */
    private HashMap<String, RegistrationRequest> _registrationRequestMap;

    /**
     * integer for the number of connections each node in the overlay is to have
     */
    private int _numConnections;

    /**
     * Constructor for the OverlayCreator class.
     *
     * @param registrationRequestMap <code>HashMap</code> of the calling Registry node's registered nodes.
     * @param numConnections         <code>int</code> denoting how many connections the overlay should give each
     *                               MessagingNode.
     */
    public OverlayCreator(HashMap<String, RegistrationRequest> registrationRequestMap, int numConnections)
    {
        _registrationRequestMap = registrationRequestMap;
        _numConnections = numConnections;
    }

    /**
     * Private method to construct the overlay with the nodes that have connected to the
     * registry thus far. Part of this will be sending out the MessagingNodeList of connections
     * that a particular node should make.
     */
    public HashMap<String, MessagingNodeList> setUpOverlay()
    {
        /**
         *  Check that the number of request connections does not equal or exceed the total number of nodes
         *  that are connected to the Registry so far.
         */
//        if (getNumConnections() )
//        {
//            System.err.println("Error! Number of links specified for the overlay creation must not exceed one LESS " +
//                    "than the number\n" +
//                    "of nodes that are connected to the Registry!");
//
//            return null;
//        }


        /**
         * Object array to hold all of the keys (strings) to the registered nodes
         */
        Object[] nodeKeys = getRegisteredNodesMap().keySet().toArray();


        /**
         * HashMap to store our MessagingNodeList
         */
        HashMap<String, MessagingNodeList> nodeListMessageMap = new HashMap<String, MessagingNodeList>();

        /**
         * MessagingNodeList message to be used in the following loops
         */
        MessagingNodeList nodeListMessage;


        /**
         * Create a new MessagingNodeList message for every node we have a registration request for.
         */
        for (Object nodeKey : nodeKeys)
        {
            /**
             * Create a new MessagingNodeList for the current message and add it
             * to the Message map.
             */
            nodeListMessage = (MessagingNodeList) EventFactory.getFactoryInstance().createEvent(Protocol
                    .MESSAGING_NODE_LIST,
                    getRegisteredNodesMap().get(nodeKey.toString())
                            .getIpAddress(),
                    getRegisteredNodesMap().get(nodeKey.toString()).getPort(), nodeKey.toString());

            /**
             * Put the peer messaging list message in the peer list hash map hashed to the recipient of that message.
             * This will allow us to easily add and increment the connections a particular message has.
             */
            nodeListMessageMap.put(nodeListMessage.getID(), nodeListMessage);
        }


        /**
         * MessagingNodeList message to be used instead of re-creating it in loops and
         * calling the lists from the hash maps.
         */
        MessagingNodeList messagingNodeList;

        // FOR every node in the maps key list
        for (int i = 0; i < nodeKeys.length; ++i)
        {

            /**
             * Building the actual overlay. Each node will be linked with its successor, and the node
             * immediately after its successor. ie, i will be connected with i + 1 and i + 2.
             *
             * The 9th node and 10th nodes are special cases, as they will both have links that "wrap"
             * around connecting to the first and second nodes, respectively.
             */


            messagingNodeList = nodeListMessageMap.get(nodeKeys[i].toString());

            // IF we are not to the 9th node yet
            if (i <= 7)
            {

                /**
                 * Add a connection from the current node to the next node
                 */
                messagingNodeList.getNodeConnectionInfo().add(getRegisteredNodesMap().get(nodeKeys[i + 1].toString())
                        .getID());

                messagingNodeList.setNumPeerMessagingNodes(getNumConnections());

                /**
                 * Now add a connection from the current node to the 2nd node after us (the i + 2 node)
                 */

                messagingNodeList.getNodeConnectionInfo().add(getRegisteredNodesMap().get(nodeKeys[i + 2].toString())
                        .getID());

                messagingNodeList.setNumPeerMessagingNodes(getNumConnections());


            }
            // ELSE if we are on the 9th node in the overlay (of 10)
            else if (i == 8)
            {
                /**
                 * Add a connection from the current node to the next node
                 */
                messagingNodeList.getNodeConnectionInfo().add(getRegisteredNodesMap().get(nodeKeys[i + 1].toString())
                        .getID());

                messagingNodeList.setNumPeerMessagingNodes(getNumConnections());


                /**
                 * Now add a connection from the 9th node to the first node
                 */

                messagingNodeList.getNodeConnectionInfo().add(getRegisteredNodesMap().get(nodeKeys[0].toString())
                        .getID());

                messagingNodeList.setNumPeerMessagingNodes(getNumConnections());

            }
            else if (i == 9)    // ELSE we are on the 10th node in the overlay
            {
                /**
                 * Add a connection from the current node to the next node
                 */
                messagingNodeList.getNodeConnectionInfo().add(getRegisteredNodesMap().get(nodeKeys[0].toString())
                        .getID());

                messagingNodeList.setNumPeerMessagingNodes(getNumConnections());


                /**
                 * Now add a connection from the current node to the 2nd node after us (the i + 2 node)
                 */

                messagingNodeList.getNodeConnectionInfo().add(getRegisteredNodesMap().get(nodeKeys[1].toString())
                        .getID());

                messagingNodeList.setNumPeerMessagingNodes(getNumConnections());

            }

        }

        return nodeListMessageMap;
    }


    /**
     * ************************* ACCESSORS AND MUTATORS ******************************
     */


    public HashMap<String, RegistrationRequest> getRegisteredNodesMap()
    {
        return _registrationRequestMap;
    }

    public int getNumConnections()
    {
        return _numConnections;
    }

}

