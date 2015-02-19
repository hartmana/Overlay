package cs455.overlay.util;

import cs455.overlay.wireformats.EventFactory;
import cs455.overlay.wireformats.LinkWeights;
import cs455.overlay.wireformats.MessagingNodeList;
import cs455.overlay.wireformats.Protocol;

import java.util.Date;
import java.util.HashMap;
import java.util.Random;

/**
 * A LinkWeightsMessageCreator class
 *
 * @author ahrtmn, 10 02 2014
 */
public class LinkWeightsMessageCreator
{

    /**
     * HashMap containing the messaging node list for every node a LinkWeightMessage is to be created
     * for.
     */
    private HashMap<String, MessagingNodeList> _messagingNodesListMap;

    /**
     * EventFactory.getFactoryInstance() for message creation
     */


    /**
     * Variable to hold the max link weight
     */
    private final int _MAX_LINK_WEIGHT;


    /**
     * Constructor
     *
     * @param messagingNodesListMap <code>HashMap</code> of all the Node's currently registered with the registry;
     *                              hashed
     *                              by their ID.
     */
    public LinkWeightsMessageCreator(HashMap<String, MessagingNodeList> messagingNodesListMap, int maxLinkWeight)
    {
        _messagingNodesListMap = messagingNodesListMap;
        _MAX_LINK_WEIGHT = maxLinkWeight + 1;
    }


    /**
     * Method to create the LinkWeights message for the overlay.
     *
     * @return <code>LinkWeights</code> message containing the link information for all Node's in the Overlay.
     */
    public LinkWeights createLinkWeightMessage()
    {

        /**
         * Object array to hold all of the keys (strings) to the registered nodes
         */
        Object[] nodeKeys = getMessagingNodesListMap().keySet().toArray();


        /**
         * Random object to generate the random Link Weights
         */
        Random random = new Random(new Date().getTime());

        /**
         * LinkWeights message to be used instead of re-creating it in loops and
         * calling the lists from the hash maps.
         */
        LinkWeights weightsMessage = (LinkWeights) EventFactory.getFactoryInstance().createEvent(Protocol.LINK_WEIGHTS);

        /**
         * MessagingNodeList for the current node connection info
         */
        MessagingNodeList nodesListMessage;

        /**
         * int for the link weight of a given connection
         */
        int linkWeight;

        /**
         * Add connection info for every node that has a MessagingNodeList
         */
        for (Object nodeKey : nodeKeys)
        {

            /**
             * Add the current nodes info to the list and generate its Link Weight
             */

            nodesListMessage = getMessagingNodesListMap().get(nodeKey.toString());

            // FOR all of the current Node's connections
            for (int j = 0; j < nodesListMessage.getNodeConnectionInfo().size(); ++j)
            {

                // generate random link weight
                linkWeight = random.nextInt(getMaxLinkWeight());

                // IF the link weight is 0, it is invalid
                if (linkWeight == 0) ++linkWeight;


                // add the link weight for the connection from the current messages node to the target node
                weightsMessage.addConnectionInfo(nodesListMessage.getID(), nodesListMessage.getNodeConnectionInfo()
                        .get(j), linkWeight);


                // add the link weight for the connection from the target node to the current node
                weightsMessage.addConnectionInfo(nodesListMessage.getNodeConnectionInfo().get(j),
                        nodesListMessage.getID(), linkWeight);


            }


        }

        return weightsMessage;
    }

    /**
     * ************************* ACCESSORS AND MUTATORS ******************************
     */

    public HashMap<String, MessagingNodeList> getMessagingNodesListMap()
    {
        return _messagingNodesListMap;
    }

    public int getMaxLinkWeight()
    {
        return _MAX_LINK_WEIGHT;
    }

}
