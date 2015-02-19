package cs455.overlay.node;

import cs455.overlay.dijkstra.RoutingCache;
import cs455.overlay.transport.Link;
import cs455.overlay.transport.TCPReceiverThread;
import cs455.overlay.transport.TCPSender;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.util.NodeMessageGenerator;
import cs455.overlay.util.Statics;
import cs455.overlay.wireformats.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;


/**
* This is the parent or base class which the MessagingNode and Registry nodes will be derived from. Basic nodes can
* initiate and receive connections.
*
* @author Aaron Hartman
*/
public class MessagingNode implements Node
{
    /**
     * String for the initial Registry address to connect to
     */
    private String _initialRegistryAddress;

    /**
     * int for the initial Registry port to connect to
     */
    private int _initialRegistryPort;

    /**
     * ServerSocket to be passed to the TCPServerThread
     */
    private ServerSocket _serverSocket;

    /**
     * ServerThread to listen for incoming communications
     */
    private TCPServerThread _serverThread;

    /**
     * Hash map for the links to the current node
     */
    private HashMap<String, Link> _registeredNodesMap;

    /**
     * int to represent the number of connections the node has
     */
    private int _numLinks;

    /**
     * int to represent the max number of connections the node can have to other messaging nodes
     */
    private int _maxMessagingNodes;

    /**
     * String of the IP address of the current node
     */
    private String _listeningIP;

    /**
     * int to represent the port the registry should listen to
     */
    private int _listeningPort;

    /**
     * String to represent the ID of the current node (For our purposes will just be IP:PORT)
     */
    private String _ID;

    /**
     * String to represent the ID of the Registry the current node is connected with. The actual
     * connection will be stored in the hash map (nodeConnections) with the others.
     */
    private String _registryID;

    /**
     * MessagingNodesList containing all of the connections this node is supposed to connect with.
     */
    private MessagingNodeList _messagingNodeList;

    /**
     * LinkWeights message to contain all of the connections in the overlay, and their weights.
     */
    private LinkWeights _linkWeights;

    /**
     * RoutingCache object to build and store the shortest path to all other nodes in the Overlay
     * once we have received the LinkWeights message.
     */
    private RoutingCache _routing;

    /**
     * int for the received message tracker
     */
    private int _receiveTracker;

    /**
     * int for the number of relayed messages
     */
    private int _relayedTracker;

    /**
     * long int for the received summation
     */
    private long _receiveSummation;

    /**
     * TrafficSummary message to be sent to the Registry when it is requested
     */
    private TrafficSummary _trafficSummary;

    /**
     * Constructor for the MessagingNode class
     *
     * @param address   <code>String</code> containing the host name of the registry.
     * @param portNum   <code>int</code> denoting the port number at the host the registry is listening to.
     */
    public MessagingNode(String address, int portNum)
    {
        _initialRegistryAddress = address;
        _initialRegistryPort = portNum;
        _registeredNodesMap = new HashMap<String, Link>();

        _numLinks = 0;

        /**
         *  Create a new TCP server thread to listen for incoming connections
         */

        try
        {
            _serverSocket = new ServerSocket(0);

            _serverThread = new TCPServerThread(_serverSocket, this);




        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }


        _listeningIP = "";
        _listeningPort = _serverSocket.getLocalPort();
        _ID = "";
        _registryID = "";
        _messagingNodeList = null;
        _linkWeights = null;
        _routing = null;
        _trafficSummary = null;

    }


    /**
     * main method
     *
     * @param args command line arguments
     */
    public static void main(String[] args)
    {

        if (args.length != 2)
            System.err.println("Usage: java MessagingNode [host address] [port]");
        else
        {

            int portNum;

            try
            {
                portNum = Integer.parseInt(args[1]);

                MessagingNode node = new MessagingNode(args[0], portNum);
                node.run();
            }
            catch (NumberFormatException e)
            {
                System.err.println("Argument" + " must be an integer");
                System.exit(1);
            }

        }

    }


    /**
     * Run method for the class to initiate the connection to the registry and listen
     * for command at the command line.
     */
    private void run()
    {
        initializer();
        registerWithRegistry(_initialRegistryAddress, _initialRegistryPort);

        /**
         * Scanner object for user input
         */
        Scanner scanner = new Scanner(System.in);

        String input;

        String[] commands;

        /**
         * Continuously loop for user input
         */
        while (true)
        {
            input = scanner.nextLine();

            commands = input.split(" ");

            // IF the user entered 0 or more than 2 words, print an error and continue
            if (commands.length == 0 || commands.length > 2)
            {
                System.err.println("MessagingNode commands must be no more than one word and no less than one word.");
                continue;
            }

            // IF the length is one, process the one word commands
            if (commands.length == 1)
            {
                // IF the command was list-weights
                if (commands[0].equals("print-shortest-path"))
                {
                    // do something
                }
                // ELSE IF the command was start
                else if (commands[0].equals("exit-overlay"))
                {
                    deregisterFromRegistry();
                }
                // TEST
                else if(commands[0].equals("test"))
                {
                    NodeRegistrationRequest nodeRegistrationRequest = (NodeRegistrationRequest) EventFactory.createEvent(Protocol.MESSAGING_NODE_CONNECTION_REQUEST,
                            "129.82.46.214", 5000, "129.82.46.214:5000");

                    try
                    {

                        TCPSender sender = new TCPSender(new Socket("129.82.46.221", 5000));

                        sender.sendData(nodeRegistrationRequest.getBytes());

                    }
                    catch( IOException ioe)
                    {
                        ioe.printStackTrace();
                    }

                }
                // ELSE invalid one word command
                else
                {
                    System.err.println("Single-word MessagingNode commands can only be one of \n" +
                            "[print-shortest-path, exit-overlay].");

                }
            }
        }

    }


    /**
     * Method to be signaled with a received message
     *
     * @param receiverThread <code>TCPReceiverThread</code> of the receiver associated with the message. Contains
     *                       a socket connection to the message sender.
     * @param event          <code>Event</code> of the incoming message.
     */
    public synchronized void onEvent(TCPReceiverThread receiverThread, Event event) throws IOException
    {


        /**
         * Determine the type of message sent so we can do the
         * appropriate action.
         */
        switch (event.getType())
        {
            case Protocol.REGISTRATION_RESPONSE:

                // convert the event to the appropriate type
                RegistrationResponse registrationResponse = (RegistrationResponse) event;

                // IF we have already received a registration response message
                if (getRegisteredNodesMap().containsKey(registrationResponse.getID()))
                {
                    System.err.println("MessagingNode is already registered with the registry!");
                    break;
                }

                // IF the status was successful
                if (registrationResponse.getStatus())
                {
                    // store the ID of the registry
                    _registryID = registrationResponse.
                            getID();

                    // register the connection
                    registerConnection(receiverThread, getID(), registrationResponse.getID());

                }

                break;
            case Protocol.MESSAGING_NODE_LIST:

                // convert the event to the appropriate type
                MessagingNodeList messagingNodeList = (MessagingNodeList) event;

                // connect to the nodes in the message
                processNodeList(messagingNodeList);


                break;
            case Protocol.LINK_WEIGHTS:

                // convert the event to the appropriate type
                LinkWeights linkWeights = (LinkWeights) event;

                processLinkWeights(linkWeights);


                break;
            case Protocol.MESSAGING_NODE_CONNECTION_REQUEST:

                // convert the event to the appropriate type
                NodeRegistrationRequest nodeRegistrationRequest = (NodeRegistrationRequest) event;

                /**
                 * String for the response message
                 */
                String response;

                // IF there is already a connection to the node requesting registration
                if (getRegisteredNodesMap().containsKey(nodeRegistrationRequest.getID()))
                {
                    System.out.println("Should not be here. (node already registered)");
                    response = "Error! Node has been previously registered with this MessagingNode [ID: " + getID() +
                            "]!";
                    registerResponse(nodeRegistrationRequest, false, response);
                }
                else
                {
                    /**
                     * There is no connection to the calling node, Register the node with the current MessagingNode
                     */
                    response = "Registration request successful. The number of messaging nodes currently " +
                            "connected to this node [ID: " + getID() + "] is " + getNumLinks() + ".";

                    registerResponse(nodeRegistrationRequest, true, response);

//                    try
//                    {
//                        Thread.currentThread().sleep(10);
//                    }
//                    catch(InterruptedException ie)
//                    {
//                        ie.printStackTrace();
//                    }
//
//                    test(nodeRegistrationRequest.getID());

                }

                break;
            case Protocol.MESSAGING_NODE_CONNECTION_RESPONSE:

                // convert the event to the appropriate type
                NodeRegistrationResponse nodeRegistrationResponse = (NodeRegistrationResponse) event;

                // IF we have already received a registration response message
                if (getRegisteredNodesMap().containsKey(nodeRegistrationResponse.getID()))
                {
                    System.err.println("MessagingNode is already registered with this MessagingNode [ID: " + getID()
                            + "]");
                    break;
                }

                // IF the status was successful
                if (nodeRegistrationResponse.getStatus())
                {
                    registerConnection(receiverThread, getID(), nodeRegistrationResponse.getID());

//                    try
//                    {
//                        Thread.sleep(100);
//                    }
//                    catch(InterruptedException ie)
//                    {
//                        ie.printStackTrace();
//                    }
//
//                    test(nodeRegistrationResponse.getID());
                }
                else
                    System.err.println(nodeRegistrationResponse.getDescription());


                break;
            case Protocol.TASK_INITIATE:

                // Make sure we have the messaging nodes list first
                if (getMessagingNodeList() == null)
                {
                    System.err.println("Error! Cannot process the TASK_INITIATE command when no MessagingNodeList has" +
                            " been received!");
                    break;
                }


                // begin sending messages
                taskInitiate();

                break;
            case Protocol.PAYLOAD_MESSAGE:

                // convert the event to the appropriate type
                PayloadMessage payloadMessage = (PayloadMessage) event;

                // process the payload message
                processPayload(payloadMessage);


                break;
            case Protocol.TRAFFIC_SUMMARY:

                /**
                 * This message is actually received by an action initiated by this node itself.
                 * This message is sent to us by the NodeMessageGenerator that we create in response
                 * to the TASK_INITIATE message. The contents of this TRAFFIC_SUMMARY message are
                 * the total of sent messages and their summation.
                 */
                TrafficSummary trafficSummary = (TrafficSummary) event;

                trafficSummary.setID(getID());
                trafficSummary.setIpAddress(getRegisteredNodesMap().get(getRegistryID()).getHostname());
                trafficSummary.setPort(getRegisteredNodesMap().get(getRegistryID()).getPort());

                _trafficSummary = trafficSummary;

                /**
                 * Notify the registry we completed our task
                 */
                System.out.println("SENDING TASKCOMPLETE");

                TaskComplete taskComplete = (TaskComplete) EventFactory.getFactoryInstance().createEvent(Protocol
                        .TASK_COMPLETE, getListeningIP(), getListeningPort(), getID());

                getRegisteredNodesMap().get(getRegistryID()).send(taskComplete.getBytes());

                break;
            case Protocol.PULL_TRAFFIC_SUMMARY:

                /**
                 * We know we received this message, so go ahead and send the TrafficSummary message
                 * after we finish filling it out.
                 */
                System.out.println("SENDING TRAFFICSUMMARY");

                // ??????????????????????????????????????????????????????????????????????
                // why is traffic summary a private variable?
                _trafficSummary.setNumReceived(getReceiveTracker());
                _trafficSummary.setReceivedSummation(getReceiveSummation());
                _trafficSummary.setNumRelayed(getRelayedTracker());
                //???????????????????????????????????????????????????????????????????????

                // send the message
                getRegisteredNodesMap().get(getRegistryID()).send(_trafficSummary.getBytes());

                break;
            default:
                System.err.println("Not in OnEvent on MessagingNode");
                break;
        }


    }

    /**
     * Method to register a connection between the current node and another. To register a
     * connection, we must create a new Link and add it to our list of connected nodes.
     *
     * @param receiverThread <code>TCPReceiverThread</code> of the receiver associated with the message. Contains a
     *                       socket connection to the message sender.
     * @param sourceID       <code>String</code> identifier of the owner of this Link (the current Node).
     * @param targetID       <code>String</code> identifier this Link is to.
     */
    public synchronized void registerConnection(TCPReceiverThread receiverThread, String sourceID, String targetID)
    {

        // IF we know how many nodes we should allow connections to, and we haven't reached those
//        if (targetID.equals(getRegistryID()) || getNumLinks() < getMaxMessagingNodes())
        {

            /**
             * Link to the node to be added
             */
            Link nodeLink = new Link(receiverThread, sourceID, targetID);

            // add the connection to our list of connected nodes.
            getRegisteredNodesMap().put(targetID, nodeLink);

            // IF the node getting added is not the Registry (Registry should not count towards our count)
            if (!getRegistryID().equals(targetID))
                setNumLinks(getNumLinks() + 1);

            System.out.println("Total number of links: " + getNumLinks() + " Total allowed: " + getMaxMessagingNodes());


            if(getNumLinks() == getMaxMessagingNodes())
            {
                Object[] keys = _registeredNodesMap.keySet().toArray();

                for(Object key : keys)
                {
                    System.out.println(_registeredNodesMap.get(key.toString()));
                }
            }


        }
//        else
        {
//            System.out.println("Node " + targetID + " not registered.");
            System.out.println("Total number of links: " + getNumLinks() + " Total allowed: " + getMaxMessagingNodes());
        }

    }


    /**
     * Method to deregister a node from the current node (ie., sever the link)
     *
     * @param ID <code>String</code> of the ID to be associated with the link to be deregistered.
     */
    public synchronized void deregisterConnection(String ID)
    {
        // IF the link exists
        if (getRegisteredNodesMap().containsKey(ID))
        {

            // stop the receiver thread
            getRegisteredNodesMap().get(ID).interruptReceiver();

            // remove the Link from our connections
            getRegisteredNodesMap().remove(ID);

            // IF the node getting added is not the Registry (Registry should not count towards our count)
            if (!getRegistryID().equals(ID))
                --_numLinks;

        }
        else
        {
            System.err.println("Node does not exist to de-register!");
        }
    }


    /**
     * Private method to register the current MessagingNode with the registry.
     *
     * @param hostName <code>String</code> containing the host name of the Registry.
     * @param portNum  <code>int</code> denoting the port number of the Registry.
     */
    private void registerWithRegistry(String hostName, int portNum)
    {
        try
        {

            /**
             *  Socket to the registry
             */
            Socket clientSocket = new Socket(hostName, portNum);


            /**
             * Set the ip address of this node (wasn't able to do via the ServerSocket)
             * as well as initialize the node's ID with this registration request.
             */
            _listeningIP = clientSocket.getLocalAddress().getCanonicalHostName();
            _ID = _listeningIP + ":" + _listeningPort;

            /**
             *  TCPSender to send a message to the client
             */
            TCPSender sender = new TCPSender(clientSocket);


            /**
             * This is sort of confusing, but we are creating the message with the connection info from this socket.
             * The listening IP is the same no matter if its the ServerSocket or a normal Socket, but the port used
             * by the Registry to verify REGISTRATION_REQUEST messages is specific to the current outgoing socket.
             */
            RegistrationRequest registrationMessage = (RegistrationRequest) EventFactory.getFactoryInstance()
                    .createEvent(Protocol
                            .REGISTRATION_REQUEST, getListeningIP(), clientSocket.getLocalPort(), getID());

            /**
             * Set the field in the message for the port that other messaging nodes should initially connect to;
             * to be later used by the Registry when creating MessagingNodeLists to send to the Overlay.
             */
            registrationMessage.setNodeServerPort(getListeningPort());

            // send the message
            sender.sendData(registrationMessage.getBytes());

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    /**
     * Private method to deregister from the registry by sending a deregistration request.
     */
    private void deregisterFromRegistry()
    {

        /**
         * Get the Socket connection to the registry
         */
        Socket socketToRegistry = getRegisteredNodesMap().get(getRegistryID()).getSocketConnection();


        try
        {
            /**
             *  TCPSender to send a message to the registry
             */
            TCPSender sender = new TCPSender(socketToRegistry);

            DeregistrationRequest deregisterMessage = (DeregistrationRequest) EventFactory.getFactoryInstance()
                    .createEvent(Protocol
                            .DEREGISTRATION_REQUEST, socketToRegistry.getLocalAddress().getCanonicalHostName(),
                            socketToRegistry.getLocalPort(),
                            getID());


            // send the message
            sender.sendData(deregisterMessage.getBytes());

            deregisterConnection(_registryID);

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    /**
     * Private method to respond to MessagingNodeList message's from the registry.
     * The method will initiate the connections to the nodes given in the list by
     * sending them all NodeRegistrationRequest messages.
     *
     * @param messagingNodeList <code>MessagingNodeList</code> message containing the list of nodes this node is to
     *                          connect with.
     */
    private void processNodeList(MessagingNodeList messagingNodeList)
    {

        /**
         * Socket for the connection to the fellow messaging node
         */
        Socket socket;

        /**
         * TCPSender object to send the MessagingNodeConnectionRequests to
         * all nodes on the list.
         */
        TCPSender sender;

        /**
         * NodeRegistrationRequest message to be sent to all recipients.
         */
        NodeRegistrationRequest nodeRegistrationRequest = (NodeRegistrationRequest) EventFactory.createEvent(Protocol
                .MESSAGING_NODE_CONNECTION_REQUEST,
                getListeningIP(), getListeningPort(), getID());

        /**
         * byte array of the message contents in bytes, so we aren't doing this for every connection
         */
        byte[] connectionMessageBytes = nodeRegistrationRequest.getBytes();

        /**
         * String array containing the address and port of the node
         */
        String[] tokens;


        // set this messaging node list as the current one
        setMessagingNodeList(messagingNodeList);

        // set the max number of connections this node is to have
        setMaxMessagingNodes(messagingNodeList.getNumConnections());


        // FOR every node in the message node list
        for (String connectionInfo : messagingNodeList.getNodeConnectionInfo())
        {
            // IF a connection to the node doesn't already exist
            if (!getRegisteredNodesMap().containsKey(connectionInfo))
            {

                /**
                 * String array containing the address and port of the node
                 */
                tokens = connectionInfo.split(":");


                try
                {
                    socket = new Socket(tokens[0], Integer.parseInt(tokens[1]));

                    sender = new TCPSender(socket);

                    sender.sendData(connectionMessageBytes);

                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

            }
        }


    }

    /**
     * Private method to send a registration response message to the requesting MessagingNode.
     *
     * @param registrationRequest <code>NodeRegistrationRequest</code> the calling node sent
     * @param status              <code>boolean</code> that signifies if the Link to the node was successfully
     *                            created or
     *                            not
     * @param message             <code>String</code> with a message for the successful/unsuccessful registration.
     */
    private void registerResponse(RegistrationRequest registrationRequest, boolean status, String message)
    {

        /**
         * NodeRegistrationResponse message to be sent to the calling node
         */
        NodeRegistrationResponse nodeRegistrationResponse =
                (NodeRegistrationResponse) EventFactory.getFactoryInstance().createEvent(Protocol
                        .MESSAGING_NODE_CONNECTION_RESPONSE);


        // set the ip address field of the message
        nodeRegistrationResponse.setIpAddress(getListeningIP());

        // set the port number field of the message
        nodeRegistrationResponse.setPort(getListeningPort());

        // set the ID of the registry
        nodeRegistrationResponse.setID(getID());

        // set the status of the registration
        nodeRegistrationResponse.setStatus(status);

        // set the message
        nodeRegistrationResponse.setDescription(message);

        try
        {

            /**
             *  Socket to the client
             */
            Socket clientSocket = new Socket(registrationRequest.getIpAddress(), registrationRequest.getPort());
//            Socket clientSocket = new Socket("129.82.46.214", 5000);

            /**
             *  TCPSender to send a message to the client
             */
            TCPSender sender = new TCPSender(clientSocket);

            // send message
            sender.sendData(nodeRegistrationResponse.getBytes());


            /**
             * Link this socket with the node we just sent the response message to
             * to maintain connection.
             */

            // IF the register response gave a successful registration status
            if (status)
            {
                /**
                 * Create a new receiver for the socket to the registering node,
                 * and register the connection
                 */
                TCPReceiverThread receiver = new TCPReceiverThread(clientSocket, this);
                receiver.start();

                registerConnection(receiver, getID(), registrationRequest.getID()); // listen to that socket

            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }


    /**
     * Private method to process the link weights message and update all of it's node
     * connection information with weights.
     *
     * @param linkWeightsMessage <code>LinkWeights</code> message to be processed.
     */
    private void processLinkWeights(LinkWeights linkWeightsMessage)
    {


        /**
         * String array to hold the tokens from our connection info after
         * parsing.
         */
        String[] infoTokens;

        /**
         * String to hold the source ID
         */
        String sourceID;

        /**
         * String to hold the target ID
         */
        String targetID;

        /**
         * int to hold the weight between the two
         */
        int weight;

        /**
         * Set the private link weights message
         */
        setLinkWeights(linkWeightsMessage);

        /**
         * First assign all the appropriate link weights designated in the message
         * to the Links this node already has.
         */
        for (String info : linkWeightsMessage.getNodeConnectionInfo())
        {
            // Tokens should be:
            // infoTokens[0] is source ID
            // infoTokens[1] is target ID
            // infoTokens[2] is link weight
            infoTokens = info.split("\t");
            sourceID = infoTokens[0];
            targetID = infoTokens[1];
            weight = Integer.parseInt(infoTokens[2]);


            // IF the source ID is ours, we know we have a weight to update
            if (getID().equals(sourceID) && getRegisteredNodesMap().containsKey(targetID))
                getRegisteredNodesMap().get(targetID).setWeight(weight);

        }

    }

    /**
     * Method to respond to the TASK_INITIATE message by initiating the sending of
     * N rounds of sending X number of messages with random values of 2147483647 to
     * -2147483648 to random MessagingNode recipients in the overlay by way of the
     * shortest path. [ N and X static variables set in the Statics interface.
     */
    private void taskInitiate()
    {

        /**
         * Setup a new RoutingCache
         */
        _routing = new RoutingCache(getLinkWeights(), this);

        System.out.println("starting");
        NodeMessageGenerator nodeMessageGenerator = new NodeMessageGenerator(getRegisteredNodesMap(),
                getRegistryID(), this, getRouting());

        nodeMessageGenerator.start();
    }


    /**
     * Method to process a received payload message.
     *
     * @param payloadMessage <code>PayloadMessage</code> to be processed.
     */
    private void processPayload(PayloadMessage payloadMessage) throws IOException
    {

        /**
         * First, remove ourselves from the routing plan and see if the message
         * needs to be relayed or further processed.
         */

        payloadMessage.getPath().removeFirst();



        // IF the size is zero after removing ourselves we know it is ours
        if (payloadMessage.getPath().size() == 0)
        {
            synchronized (this)
            {
            _receiveTracker += 1;   // update our tracker

            _receiveSummation += payloadMessage.getPayload();
            }
        }
        else
        {

            /**
             * The next in the path is a node that we are connected to, so get the connection and send the message
             */
//            if (getRegisteredNodesMap().containsKey(payloadMessage.getPath().getFirst()))
            {
                _registeredNodesMap.get(payloadMessage.getPath().getFirst()).send(payloadMessage.getBytes());

                synchronized (this) {
                _relayedTracker += 1;   // update our tracker
                }
                if(payloadMessage.getType() != Protocol.PAYLOAD_MESSAGE)
                {
                    System.err.println("TYPE OF MESSAGE CHANGED ON RELAY");
                    System.exit(0);
                }
            }
//            else
//            {
//
//
//                System.out.println("Node not registered with this node: " + payloadMessage.getPath().getFirst());
//
//                Object[] keys = getRegisteredNodesMap().keySet().toArray();
//
//
//                System.out.println("Nodes registered:");
//                for (int i = 0; i < keys.length; ++i)
//                    System.out.println("\t" + getRegisteredNodesMap().get(keys[i].toString()));
//
//                System.out.println("Path left on message [" + payloadMessage.getPath().size() + "]:");
//                for (int i = 0; i < payloadMessage.getPath().size(); ++i)
//                    System.out.println("\t" + payloadMessage.getPath().get(i));
//
//
//
//            }

        }
    }

    private void test(String nodeID)
    {
        /**
         * int for number of messages sent
         */
        int numRounds = 0;


        /**
         * Send 5000 rounds of 5 random messages
         */
        while (numRounds < Statics.NODE_MESSAGE_ROUNDS)
//        while (numRounds < 4000)
        {



            /**
             * 5 messages each with different random values to the same random node, 5000 times
             */
            for (int i = 0; i < Statics.NODE_MESSAGES_PER_ROUND; ++i)
            {

                PayloadMessage payloadMessage = (PayloadMessage) EventFactory.createEvent(Protocol.PAYLOAD_MESSAGE, _listeningIP,
                        _listeningPort, _ID);

               payloadMessage.getPath().add(nodeID);

                payloadMessage.setPayload(17);


                _registeredNodesMap.get(payloadMessage.getPath().getFirst()).send(payloadMessage.getBytes());


                payloadMessage = null;


            }


            numRounds += 1;               // increment loop variant

            /**
             * Slight pause between rounds
             */
//            try
//            {
//                Thread.sleep(5);
//            }
//            catch (InterruptedException ie)
//            {
//                ie.printStackTrace();
//            }

        }
//        for(int i = 0; i < 2000; ++i)
//        {
////            for(int j = 0; j < 5; ++j)
//            {
//                PayloadMessage payloadMessage = (PayloadMessage) EventFactory.createEvent(Protocol.PAYLOAD_MESSAGE, _listeningIP,
//                        _listeningPort, _ID);
//
//                payloadMessage.getPath().add(nodeID);
//
//                payloadMessage.setPayload(5);
//
//                try
//                {
//                    _registeredNodesMap.get(nodeID).getMessageSender().sendData(payloadMessage.getBytes());
//                }
//                catch(IOException ioe)
//                {
//                    ioe.printStackTrace();
//                }
//            }
//
////            try
////            {
////                Thread.sleep(10);
////            }
////            catch(InterruptedException ie)
////            {
////                ie.printStackTrace();
////            }
//        }

        System.out.println("all messages sent");

    }

    private void initializer()
    {
        _serverThread.start();
        _listeningIP = _serverThread.getServerSocket().getInetAddress().toString();
    }



    /**
     * ************************* ACCESSORS AND MUTATORS ******************************
     */

    public HashMap<String, Link> getRegisteredNodesMap()
    {
        return _registeredNodesMap;
    }

    public int getNumLinks()
    {
        return _numLinks;
    }

    public void setNumLinks(int numLinks)
    {
        _numLinks = numLinks;
    }

    public int getMaxMessagingNodes()
    {
        return _maxMessagingNodes;
    }

    public void setMaxMessagingNodes(int maxMessagingNodes)
    {
        _maxMessagingNodes = maxMessagingNodes;
    }

    public String getListeningIP()
    {
        return _listeningIP;
    }

    public int getListeningPort()
    {
        return _listeningPort;
    }

    public String getRegistryID()
    {
        return _registryID;
    }

    public MessagingNodeList getMessagingNodeList()
    {
        return _messagingNodeList;
    }

    public void setMessagingNodeList(MessagingNodeList messagingNodeList)
    {
        _messagingNodeList = messagingNodeList;
    }

    public String getID()
    {
        return _ID;
    }

    public LinkWeights getLinkWeights()
    {
        return _linkWeights;
    }

    private void setLinkWeights(LinkWeights linkWeights)
    {
        _linkWeights = linkWeights;
    }

    public RoutingCache getRouting()
    {
        return _routing;
    }

    public int getReceiveTracker()
    {
        return _receiveTracker;
    }

    public int getRelayedTracker()
    {
        return _relayedTracker;
    }

    public long getReceiveSummation()
    {
        return _receiveSummation;
    }

}



//
//
//
//
//
//package cs455.overlay.node;
//
//        import cs455.overlay.dijkstra.RoutingCache;
//        import cs455.overlay.transport.Link;
//        import cs455.overlay.transport.TCPReceiverThread;
//        import cs455.overlay.transport.TCPSender;
//        import cs455.overlay.transport.TCPServerThread;
//        import cs455.overlay.util.NodeMessageGenerator;
//        import cs455.overlay.wireformats.*;
//
//        import java.io.IOException;
//        import java.net.ServerSocket;
//        import java.net.Socket;
//        import java.util.HashMap;
//        import java.util.Scanner;
//        import java.util.concurrent.ConcurrentHashMap;
//
///**
//* This is the parent or base class which the MessagingNode and Registry nodes will be derived from. Basic nodes can
//* initiate and receive connections.
//*
//* @author Aaron Hartman
//*/
//public class MessagingNode implements Node
//{
//    /**
//     * String for the initial Registry address to connect to
//     */
//    private String _initialRegistryAddress;
//
//    /**
//     * int for the initial Registry port to connect to
//     */
//    private int _initialRegistryPort;
//
//    /**
//     * ServerSocket to be passed to the TCPServerThread
//     */
//    private ServerSocket _serverSocket;
//
//    /**
//     * TCPServerThread to listen for incomming communications
//     */
//    private TCPServerThread _serverThread;
//
//    /**
//     * Hash map for the links to the current node
//     */
//    private HashMap<String, Link> _registeredNodesMap;
//
//    /**
//     * int to represent the number of connections the node has
//     */
//    private int _numLinks;
//
//    /**
//     * int to represent the max number of connections the node can have to other messaging nodes
//     */
//    private int _maxMessagingNodes;
//
//    /**
//     * String of the IP address of the current node
//     */
//    private String _listeningIP;
//
//    /**
//     * int to represent the port the registry should listen to
//     */
//    private int _listeningPort;
//
//    /**
//     * String to represent the ID of the current node (For our purposes will just be IP:PORT)
//     */
//    private String _ID;
//
//    /**
//     * String to represent the ID of the Registry the current node is connected with. The actual
//     * connection will be stored in the hash map (nodeConnections) with the others.
//     */
//    private String _registryID;
//
//    /**
//     * MessagingNodesList containing all of the connections this node is supposed to connect with.
//     */
//    private MessagingNodeList _messagingNodeList;
//
//    /**
//     * LinkWeights message to contain all of the connections in the overlay, and their weights.
//     */
//    private LinkWeights _linkWeights;
//
//    /**
//     * RoutingCache object to build and store the shortest path to all other nodes in the Overlay
//     * once we have received the LinkWeights message.
//     */
//    private RoutingCache _routing;
//
//    /**
//     * int for the received message tracker
//     */
//    private int _receiveTracker;
//
//    /**
//     * int for the number of relayed messages
//     */
//    private int _relayedTracker;
//
//    /**
//     * long int for the received summation
//     */
//    private long _receiveSummation;
//
//    /**
//     * TrafficSummary message to be sent to the Registry when it is requested
//     */
//    private TrafficSummary _trafficSummary;
//
//    /**
//     * Constructor for the MessagingNode class
//     *
//     * @param address   <code>String</code> containing the host name of the registry.
//     * @param portNum   <code>int</code> denoting the port number at the host the registry is listening to.
//     */
//    public MessagingNode(String address, int portNum)
//    {
//        _initialRegistryAddress = address;
//        _initialRegistryPort = portNum;
//        _registeredNodesMap = new HashMap<String, Link>();
//
//        _numLinks = 0;
//
//        /**
//         *  Create a new TCP server thread to listen for incoming connections
//         */
//
//        try
//        {
//            _serverSocket = new ServerSocket(0);
//
//            _serverThread = new TCPServerThread(_serverSocket, this);
//
//
//        }
//        catch (IOException ioe)
//        {
//            ioe.printStackTrace();
//        }
//
//
//        _listeningIP = "";
//        _listeningPort = _serverSocket.getLocalPort();
//        _ID = "";
//        _registryID = "";
//        _messagingNodeList = null;
//        _linkWeights = null;
//         _routing = null;
//        _trafficSummary = null;
//
//    }
//
//
//    /**
//     * main method
//     *
//     * @param args command line arguments
//     */
//    public static void main(String[] args)
//    {
//
//        if (args.length != 2)
//            System.err.println("Usage: java MessagingNode [host address] [port]");
//        else
//        {
//
//            int portNum;
//
//            try
//            {
//                portNum = Integer.parseInt(args[1]);
//
//                MessagingNode node = new MessagingNode(args[0], portNum);
//                node.run();
//            }
//            catch (NumberFormatException e)
//            {
//                System.err.println("Argument" + " must be an integer");
//                System.exit(1);
//            }
//
//        }
//
//    }
//
//
//    /**
//     * Run method for the class to initiate the connection to the registry and listen
//     * for command at the command line.
//     */
//    private void run()
//    {
//        initializer();
//        registerWithRegistry(_initialRegistryAddress, _initialRegistryPort);
//
//        /**
//         * Scanner object for user input
//         */
//        Scanner scanner = new Scanner(System.in);
//
//        String input;
//
//        String[] commands;
//
//        /**
//         * Continuously loop for user input
//         */
//        while (true)
//        {
//            input = scanner.nextLine();
//
//            commands = input.split(" ");
//
//            // IF the user entered 0 or more than 2 words, print an error and continue
//            if (commands.length == 0 || commands.length > 1)
//            {
//                System.err.println("MessagingNode commands must be no more than one word and no less than one word.");
//                continue;
//            }
//
//            // IF the length is one, process the one word commands
//            if (commands.length == 1)
//            {
//                // IF the command was list-weights
//                if (commands[0].equals("print-shortest-path"))
//                {
//                    // do something
//                }
//                // ELSE IF the command was start
//                else if (commands[0].equals("exit-overlay"))
//                {
//                    deregisterFromRegistry();
//                }
//                // ELSE invalid one word command
//                else
//                {
//                    System.err.println("Single-word MessagingNode commands can only be one of \n" +
//                            "[print-shortest-path, exit-overlay].");
//
//                }
//            }
//        }
//
//    }
//
//
//    /**
//     * Method to be signaled with a received message
//     *
//     * @param receiverThread <code>TCPReceiverThread</code> of the receiver associated with the message. Contains
//     *                       a socket connection to the message sender.
//     * @param event          <code>Event</code> of the incoming message.
//     */
//    public synchronized void onEvent(TCPReceiverThread receiverThread, Event event) throws IOException
//    {
//
//
//        /**
//         * Determine the type of message sent so we can do the
//         * appropriate action.
//         */
//        switch (event.getType())
//        {
//            case Protocol.REGISTRATION_RESPONSE:
//
//                // convert the event to the appropriate type
//                RegistrationResponse registrationResponse = (RegistrationResponse) event;
//
//                // IF we have already received a registration response message
//                if (getRegisteredNodesMap().containsKey(registrationResponse.getID()))
//                {
//                    System.err.println("MessagingNode is already registered with the registry!");
//                    break;
//                }
//
//                // IF the status was successful
//                if (registrationResponse.getStatus())
//                {
//                    // store the ID of the registry
//                    _registryID = registrationResponse.
//                            getID();
//
//                    // register the connection
//                    registerConnection(receiverThread, getID(), registrationResponse.getID());
//
//                }
//
//                break;
//            case Protocol.MESSAGING_NODE_LIST:
//
//                // convert the event to the appropriate type
//                MessagingNodeList messagingNodeList = (MessagingNodeList) event;
//
//                // connect to the nodes in the message
//                processNodeList(messagingNodeList);
//
//
//                break;
//            case Protocol.LINK_WEIGHTS:
//
//                // convert the event to the appropriate type
//                LinkWeights linkWeights = (LinkWeights) event;
//
//                processLinkWeights(linkWeights);
//
//
//                break;
//            case Protocol.MESSAGING_NODE_CONNECTION_REQUEST:
//
//                // convert the event to the appropriate type
//                NodeRegistrationRequest nodeRegistrationRequest = (NodeRegistrationRequest) event;
//
//                /**
//                 * String for the response message
//                 */
//                String response;
//
//                // IF there is already a connection to the node requesting registration
//                if (getRegisteredNodesMap().containsKey(nodeRegistrationRequest.getID()))
//                {
//                    System.out.println("Should not be here. (node already registered)");
//                    response = "Error! Node has been previously registered with this MessagingNode [ID: " + getID() +
//                            "]!";
//                    registerResponse(nodeRegistrationRequest, false, response);
//                }
//                else
//                {
//                    /**
//                     * There is no connection to the calling node, Register the node with the current MessagingNode
//                     */
//                    response = "Registration request successful. The number of messaging nodes currently " +
//                            "connected to this node [ID: " + getID() + "] is " + getNumLinks() + ".";
//
//                    registerResponse(nodeRegistrationRequest, true, response);
//
//                }
//
//                break;
//            case Protocol.MESSAGING_NODE_CONNECTION_RESPONSE:
//
//                // convert the event to the appropriate type
//                NodeRegistrationResponse nodeRegistrationResponse = (NodeRegistrationResponse) event;
//
//                // IF we have already received a registration response message
//                if (getRegisteredNodesMap().containsKey(nodeRegistrationResponse.getID()))
//                {
//                    System.err.println("MessagingNode is already registered with this MessagingNode [ID: " + getID()
//                            + "]");
//                    break;
//                }
//
//                // IF the status was successful
//                if (nodeRegistrationResponse.getStatus())
//                    registerConnection(receiverThread, getID(), nodeRegistrationResponse.getID());
//                else
//                    System.err.println(nodeRegistrationResponse.getDescription());
//
//
//                break;
//            case Protocol.TASK_INITIATE:
//
//                // Make sure we have the messaging nodes list first
//                if (getMessagingNodeList() == null)
//                {
//                    System.err.println("Error! Cannot process the TASK_INITIATE command when no MessagingNodeList has" +
//                            " been received!");
//                    break;
//                }
//
//
//                // begin sending messages
//                taskInitiate();
//
//                break;
//            case Protocol.PAYLOAD_MESSAGE:
//
//                // convert the event to the appropriate type
//                PayloadMessage payloadMessage = (PayloadMessage) event;
//
//                // process the payload message
//                processPayload(payloadMessage);
//
//                break;
//            case Protocol.TRAFFIC_SUMMARY:
//
//                /**
//                 * This message is actually received by an action initiated by this node itself.
//                 * This message is sent to us by the NodeMessageGenerator that we create in response
//                 * to the TASK_INITIATE message. The contents of this TRAFFIC_SUMMARY message are
//                 * the total of sent messages and their summation.
//                 */
//                TrafficSummary trafficSummary = (TrafficSummary) event;
//
//                trafficSummary.setID(getID());
//                trafficSummary.setIpAddress(getRegisteredNodesMap().get(getRegistryID()).getHostname());
//                trafficSummary.setPort(getRegisteredNodesMap().get(getRegistryID()).getPort());
//
//                _trafficSummary = trafficSummary;
//
//                /**
//                 * Notify the registry we completed our task
//                 */
//                System.out.println("SENDING TASKCOMPLETE");
//
//                TaskComplete taskComplete = (TaskComplete) EventFactory.getFactoryInstance().createEvent(Protocol
//                        .TASK_COMPLETE, getListeningIP(), getListeningPort(), getID());
//
//                getRegisteredNodesMap().get(getRegistryID()).getMessageSender().sendData(taskComplete.getBytes());
//
//                break;
//            case Protocol.PULL_TRAFFIC_SUMMARY:
//
//                /**
//                 * We know we received this message, so go ahead and send the TrafficSummary message
//                 * after we finish filling it out.
//                 */
//                System.out.println("SENDING TRAFFICSUMMARY");
//
//                // ??????????????????????????????????????????????????????????????????????
//                // why is traffic summary a private variable?
//                _trafficSummary.setNumReceived(getReceiveTracker());
//                _trafficSummary.setReceivedSummation(getReceiveSummation());
//                _trafficSummary.setNumRelayed(getRelayedTracker());
//                //???????????????????????????????????????????????????????????????????????
//
//                // send the message
//                getRegisteredNodesMap().get(getRegistryID()).getMessageSender().sendData(_trafficSummary.getBytes());
//
//                break;
//            default:
//                break;
//        }
//
//
//    }
//
//    /**
//     * Method to register a connection between the current node and another. To register a
//     * connection, we must create a new Link and add it to our list of connected nodes.
//     *
//     * @param receiverThread <code>TCPReceiverThread</code> of the receiver associated with the message. Contains a
//     *                       socket connection to the message sender.
//     * @param sourceID       <code>String</code> identifier of the owner of this Link (the current Node).
//     * @param targetID       <code>String</code> identifier this Link is to.
//     */
//    public synchronized void registerConnection(TCPReceiverThread receiverThread, String sourceID, String targetID)
//    {
//
//        // IF we know how many nodes we should allow connections to, and we haven't reached those
//        if (targetID.equals(getRegistryID()) || getNumLinks() < getMaxMessagingNodes())
//        {
//
//            /**
//             * Link to the node to be added
//             */
//            Link nodeLink = new Link(receiverThread, sourceID, targetID);
//
//            // add the connection to our list of connected nodes.
//            getRegisteredNodesMap().put(targetID, nodeLink);
//
//            // IF the node getting added is not the Registry (Registry should not count towards our count)
//            if (!getRegistryID().equals(targetID))
//                setNumLinks(getNumLinks() + 1);
//
//            System.out.println("Total number of links: " + getNumLinks() + " Total allowed: " + getMaxMessagingNodes());
//
//
//            if(getNumLinks() == getMaxMessagingNodes())
//            {
//                Object[] keys = _registeredNodesMap.keySet().toArray();
//
//                for(Object key : keys)
//                {
//                    System.out.println(_registeredNodesMap.get(key.toString()));
//                }
//            }
//
//
//        }
//        else
//        {
//            System.out.println("Node " + targetID + " not registered.");
//            System.out.println("Total number of links: " + getNumLinks() + " Total allowed: " + getMaxMessagingNodes());
//        }
//
//    }
//
//
//    /**
//     * Method to deregister a node from the current node (ie., sever the link)
//     *
//     * @param ID <code>String</code> of the ID to be associated with the link to be deregistered.
//     */
//    public synchronized void deregisterConnection(String ID)
//    {
//        // IF the link exists
//        if (getRegisteredNodesMap().containsKey(ID))
//        {
//
//            // stop the receiver thread
//            getRegisteredNodesMap().get(ID).interruptReceiver();
//
//            // remove the Link from our connections
//            getRegisteredNodesMap().remove(ID);
//
//            // IF the node getting added is not the Registry (Registry should not count towards our count)
//            if (!getRegistryID().equals(ID))
//                --_numLinks;
//
//        }
//        else
//        {
//            System.err.println("Node does not exist to de-register!");
//        }
//    }
//
//
//    /**
//     * Private method to register the current MessagingNode with the registry.
//     *
//     * @param hostName <code>String</code> containing the host name of the Registry.
//     * @param portNum  <code>int</code> denoting the port number of the Registry.
//     */
//    private void registerWithRegistry(String hostName, int portNum)
//    {
//        try
//        {
//
//            /**
//             *  Socket to the registry
//             */
//            Socket clientSocket = new Socket(hostName, portNum);
//
//
//            /**
//             * Set the ip address of this node (wasn't able to do via the ServerSocket)
//             * as well as initialize the node's ID with this registration request.
//             */
//            _listeningIP = clientSocket.getLocalAddress().getCanonicalHostName();
//            _ID = _listeningIP + ":" + _listeningPort;
//
//            /**
//             *  TCPSender to send a message to the client
//             */
//            TCPSender sender = new TCPSender(clientSocket);
//
//
//            /**
//             * This is sort of confusing, but we are creating the message with the connection info from this socket.
//             * The listening IP is the same no matter if its the ServerSocket or a normal Socket, but the port used
//             * by the Registry to verify REGISTRATION_REQUEST messages is specific to the current outgoing socket.
//             */
//            RegistrationRequest registrationMessage = (RegistrationRequest) EventFactory.getFactoryInstance()
//                    .createEvent(Protocol
//                            .REGISTRATION_REQUEST, getListeningIP(), clientSocket.getLocalPort(), getID());
//
//            /**
//             * Set the field in the message for the port that other messaging nodes should initially connect to;
//             * to be later used by the Registry when creating MessagingNodeLists to send to the Overlay.
//             */
//            registrationMessage.setNodeServerPort(getListeningPort());
//
//            // send the message
//            sender.sendData(registrationMessage.getBytes());
//
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//
//    }
//
//    /**
//     * Private method to deregister from the registry by sending a deregistration request.
//     */
//    private void deregisterFromRegistry()
//    {
//
//        /**
//         * Get the Socket connection to the registry
//         */
//        Socket socketToRegistry = getRegisteredNodesMap().get(getRegistryID()).getSocketConnection();
//
//
//        try
//        {
//            /**
//             *  TCPSender to send a message to the registry
//             */
//            TCPSender sender = new TCPSender(socketToRegistry);
//
//            DeregistrationRequest deregisterMessage = (DeregistrationRequest) EventFactory.getFactoryInstance()
//                    .createEvent(Protocol
//                            .DEREGISTRATION_REQUEST, socketToRegistry.getLocalAddress().getCanonicalHostName(),
//                            socketToRegistry.getLocalPort(),
//                            getID());
//
//
//            // send the message
//            sender.sendData(deregisterMessage.getBytes());
//
//            deregisterConnection(_registryID);
//
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//
//    }
//
//    /**
//     * Private method to respond to MessagingNodeList message's from the registry.
//     * The method will initiate the connections to the nodes given in the list by
//     * sending them all NodeRegistrationRequest messages.
//     *
//     * @param messagingNodeList <code>MessagingNodeList</code> message containing the list of nodes this node is to
//     *                          connect with.
//     */
//    private void processNodeList(MessagingNodeList messagingNodeList)
//    {
//
//        /**
//         * Socket for the connection to the fellow messaging node
//         */
//        Socket socket;
//
//        /**
//         * TCPSender object to send the MessagingNodeConnectionRequests to
//         * all nodes on the list.
//         */
//        TCPSender sender;
//
//        /**
//         * NodeRegistrationRequest message to be sent to all recipients.
//         */
//        NodeRegistrationRequest nodeRegistrationRequest = (NodeRegistrationRequest) EventFactory.createEvent(Protocol
//                .MESSAGING_NODE_CONNECTION_REQUEST,
//                getListeningIP(), getListeningPort(), getID());
//
//        /**
//         * byte array of the message contents in bytes, so we aren't doing this for every connection
//         */
//        byte[] connectionMessageBytes = nodeRegistrationRequest.getBytes();
//
//        /**
//         * String array containing the address and port of the node
//         */
//        String[] tokens;
//
//
//        // set this messaging node list as the current one
//        setMessagingNodeList(messagingNodeList);
//
//        // set the max number of connections this node is to have
//        setMaxMessagingNodes(messagingNodeList.getNumConnections());
//
//
//        // FOR every node in the message node list
//        for (String connectionInfo : messagingNodeList.getNodeConnectionInfo())
//        {
//            // IF a connection to the node doesn't already exist
//            if (!getRegisteredNodesMap().containsKey(connectionInfo))
//            {
//
//                /**
//                 * String array containing the address and port of the node
//                 */
//                tokens = connectionInfo.split(":");
//
//
//                try
//                {
//                    socket = new Socket(tokens[0], Integer.parseInt(tokens[1]));
//
//                    sender = new TCPSender(socket);
//
//                    sender.sendData(connectionMessageBytes);
//
//                }
//                catch (IOException e)
//                {
//                    e.printStackTrace();
//                }
//
//            }
//        }
//
//
//    }
//
//    /**
//     * Private method to send a registration response message to the requesting MessagingNode.
//     *
//     * @param registrationRequest <code>NodeRegistrationRequest</code> the calling node sent
//     * @param status              <code>boolean</code> that signifies if the Link to the node was successfully
//     *                            created or
//     *                            not
//     * @param message             <code>String</code> with a message for the successful/unsuccessful registration.
//     */
//    private void registerResponse(RegistrationRequest registrationRequest, boolean status, String message)
//    {
//
//        /**
//         * NodeRegistrationResponse message to be sent to the calling node
//         */
//        NodeRegistrationResponse nodeRegistrationResponse =
//                (NodeRegistrationResponse) EventFactory.getFactoryInstance().createEvent(Protocol
//                        .MESSAGING_NODE_CONNECTION_RESPONSE);
//
//
//        // set the ip address field of the message
//        nodeRegistrationResponse.setIpAddress(getListeningIP());
//
//        // set the port number field of the message
//        nodeRegistrationResponse.setPort(getListeningPort());
//
//        // set the ID of the registry
//        nodeRegistrationResponse.setID(getID());
//
//        // set the status of the registration
//        nodeRegistrationResponse.setStatus(status);
//
//        // set the message
//        nodeRegistrationResponse.setDescription(message);
//
//        try
//        {
//
//            /**
//             *  Socket to the client
//             */
//            Socket clientSocket = new Socket(registrationRequest.getIpAddress(), registrationRequest.getPort());
////            Socket clientSocket = new Socket("129.82.46.214", 5000);
//
//            /**
//             *  TCPSender to send a message to the client
//             */
//            TCPSender sender = new TCPSender(clientSocket);
//
//            // send message
//            sender.sendData(nodeRegistrationResponse.getBytes());
//
//
//            /**
//             * Link this socket with the node we just sent the response message to
//             * to maintain connection.
//             */
//
//            // IF the register response gave a successful registration status
//            if (status)
//            {
//                /**
//                 * Create a new receiver for the socket to the registering node,
//                 * and register the connection
//                 */
//                TCPReceiverThread receiver = new TCPReceiverThread(clientSocket, this);
//                receiver.start();
//
//                registerConnection(receiver, getID(), registrationRequest.getID()); // listen to that socket
//
//            }
//
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//
//    }
//
//
//    /**
//     * Private method to process the link weights message and update all of it's node
//     * connection information with weights.
//     *
//     * @param linkWeightsMessage <code>LinkWeights</code> message to be processed.
//     */
//    private void processLinkWeights(LinkWeights linkWeightsMessage)
//    {
//
//
//        /**
//         * String array to hold the tokens from our connection info after
//         * parsing.
//         */
//        String[] infoTokens;
//
//        /**
//         * String to hold the source ID
//         */
//        String sourceID;
//
//        /**
//         * String to hold the target ID
//         */
//        String targetID;
//
//        /**
//         * int to hold the weight between the two
//         */
//        int weight;
//
//        /**
//         * Set the private link weights message
//         */
//        setLinkWeights(linkWeightsMessage);
//
//        /**
//         * First assign all the appropriate link weights designated in the message
//         * to the Links this node already has.
//         */
//        for (String info : linkWeightsMessage.getNodeConnectionInfo())
//        {
//            // Tokens should be:
//            // infoTokens[0] is source ID
//            // infoTokens[1] is target ID
//            // infoTokens[2] is link weight
//            infoTokens = info.split("\t");
//            sourceID = infoTokens[0];
//            targetID = infoTokens[1];
//            weight = Integer.parseInt(infoTokens[2]);
//
//
//            // IF the source ID is ours, we know we have a weight to update
//            if (getID().equals(sourceID) && getRegisteredNodesMap().containsKey(targetID))
//                getRegisteredNodesMap().get(targetID).setWeight(weight);
//
//        }
//
//    }
//
//    /**
//     * Method to respond to the TASK_INITIATE message by initiating the sending of
//     * N rounds of sending X number of messages with random values of 2147483647 to
//     * -2147483648 to random MessagingNode recipients in the overlay by way of the
//     * shortest path. [ N and X static variables set in the Statics interface.
//     */
//    private void taskInitiate()
//    {
//
//        /**
//         * Setup a new RoutingCache
//         */
//        _routing = new RoutingCache(getLinkWeights(), this);
//
//        NodeMessageGenerator nodeMessageGenerator = new NodeMessageGenerator(getRegisteredNodesMap(),
//                getRegistryID(), this, getRouting());
//
//        nodeMessageGenerator.start();
//    }
//
//
//    /**
//     * Method to process a received payload message.
//     *
//     * @param payloadMessage <code>PayloadMessage</code> to be processed.
//     */
//    private void processPayload(PayloadMessage payloadMessage) throws IOException
//    {
//
//        /**
//         * First, remove ourselves from the routing plan and see if the message
//         * needs to be relayed or further processed.
//         */
//
//        payloadMessage.getPath().removeFirst();
//
//
//
//        // IF the size is zero after removing ourselves we know it is ours
//        if (payloadMessage.getPath().size() == 0)
//        {
//            _receiveTracker += 1;   // update our tracker
//
//            _receiveSummation += payloadMessage.getPayload();
//
//        }
//        else
//        {
//
//            /**
//             * The next in the path is a node that we are connected to, so get the connection and send the message
//             */
////            if (getRegisteredNodesMap().containsKey(payloadMessage.getPath().getFirst()))
//
//                _registeredNodesMap.get(payloadMessage.getPath().getFirst()).getMessageSender().sendData(payloadMessage.getBytes());
//
//                _relayedTracker += 1;   // update our tracker
//
//                if(payloadMessage.getType() != Protocol.PAYLOAD_MESSAGE)
//                {
//                    System.err.println("TYPE OF MESSAGE CHANGED ON RELAY");
//                    System.exit(0);
//                }
//
////            else
////            {
////
////
////                System.out.println("Node not registered with this node: " + payloadMessage.getPath().getFirst());
////
////                Object[] keys = getRegisteredNodesMap().keySet().toArray();
////
////
////                System.out.println("Nodes registered:");
////                for (int i = 0; i < keys.length; ++i)
////                    System.out.println("\t" + getRegisteredNodesMap().get(keys[i].toString()));
////
////                System.out.println("Path left on message [" + payloadMessage.getPath().size() + "]:");
////                for (int i = 0; i < payloadMessage.getPath().size(); ++i)
////                    System.out.println("\t" + payloadMessage.getPath().get(i));
////
////
////
////            }
//
//        }
//    }
//
//    private void initializer()
//    {
//        _serverThread.start();
//        _listeningIP = _serverThread.getServerSocket().getInetAddress().toString();
//    }
//
//
//    /**
//     * ************************* ACCESSORS AND MUTATORS ******************************
//     */
//
//    public HashMap<String, Link> getRegisteredNodesMap()
//    {
//        return _registeredNodesMap;
//    }
//
//    public int getNumLinks()
//    {
//        return _numLinks;
//    }
//
//    public void setNumLinks(int numLinks)
//    {
//        _numLinks = numLinks;
//    }
//
//    public int getMaxMessagingNodes()
//    {
//        return _maxMessagingNodes;
//    }
//
//    public void setMaxMessagingNodes(int maxMessagingNodes)
//    {
//        _maxMessagingNodes = maxMessagingNodes;
//    }
//
//    public String getListeningIP()
//    {
//        return _listeningIP;
//    }
//
//    public int getListeningPort()
//    {
//        return _listeningPort;
//    }
//
//    public String getRegistryID()
//    {
//        return _registryID;
//    }
//
//    public MessagingNodeList getMessagingNodeList()
//    {
//        return _messagingNodeList;
//    }
//
//    public void setMessagingNodeList(MessagingNodeList messagingNodeList)
//    {
//        _messagingNodeList = messagingNodeList;
//    }
//
//    public String getID()
//    {
//        return _ID;
//    }
//
//    public LinkWeights getLinkWeights()
//    {
//        return _linkWeights;
//    }
//
//    private void setLinkWeights(LinkWeights linkWeights)
//    {
//        _linkWeights = linkWeights;
//    }
//
//    public RoutingCache getRouting()
//    {
//        return _routing;
//    }
//
//    public int getReceiveTracker()
//    {
//        return _receiveTracker;
//    }
//
//    public int getRelayedTracker()
//    {
//        return _relayedTracker;
//    }
//
//    public long getReceiveSummation()
//    {
//        return _receiveSummation;
//    }
//
//}
