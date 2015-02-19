package cs455.overlay.node;

import cs455.overlay.transport.Link;
import cs455.overlay.transport.TCPReceiverThread;
import cs455.overlay.transport.TCPSender;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.util.LinkWeightsMessageCreator;
import cs455.overlay.util.OverlayCreator;
import cs455.overlay.util.StatisticsCollectorAndDisplay;
import cs455.overlay.wireformats.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Scanner;



/**
 * Registry class to define the functionality of the registry. The registry is
 * responsible for the overlay creation, assigning of link weights, notifying the
 * MessagingNodes in the overlay who they should connect to, as well as listening
 * for incoming connections from MessagingNodes and satisfying valid requests.
 *
 * @author Aaron Hartman
 */
public class Registry implements Node
{

    /**
     * ServerSocket to be passed to the TCPServerThread
     */
    private ServerSocket _serverSocket;

    /**
     * TCPServerThread to listen for incoming communication
     */
    private TCPServerThread _serverThread;

    /**
     * Hash map for the links to each node registered with the registry
     */
    private HashMap<String, Link> _registeredNodesMap;

    /**
     * Hash map for storing the RegistrationRequest messages of all the nodes
     * registered with the Registry. This information will be used later to generate
     * the MessagingNodeList messages with the appropriate info.
     */
    private HashMap<String, RegistrationRequest> _registrationRequestMap;

    /**
     * Hash map for building the overlay connections by hashing each nodes ID
     * to its corresponding MessagingNodeList message. The node list message
     * also contains the total number of connections for referencing during the
     * overlay creation.
     */
    private HashMap<String, MessagingNodeList> _nodeListMessageMap;

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
     * int to keep track of how many TASK_COMPLETE messages have been received from registered
     * nodes in the overlay.
     */
    private int _taskCompleteReceived;

    /**
     * StatisticsCollectorAndDisplay object to gather the data we receive from nodes who have finished their task
     */
    private StatisticsCollectorAndDisplay _statisticsCollectorAndDisplay;

    EventFactory _eventFactory;


    /**
     * Constructor for the Registry class
     */
    public Registry(int listeningPort, int maxLinks)
    {

        _registeredNodesMap = new HashMap<String, Link>();
        _registrationRequestMap = new HashMap<String, RegistrationRequest>();
        _nodeListMessageMap = new HashMap<String, MessagingNodeList>();
        _numLinks = 0;
        _listeningPort = listeningPort;

        /**
         *  Create a new TCP server thread to listen for incoming connections
         */
        try
        {
            _serverSocket = new ServerSocket(_listeningPort, 0);

            _serverThread = new TCPServerThread(_serverSocket, this);

        }
        catch(UnknownHostException uhe)
        {
            uhe.printStackTrace();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }


        _listeningIP = "";
        _ID = "";
        _maxMessagingNodes = maxLinks;
        _statisticsCollectorAndDisplay = new StatisticsCollectorAndDisplay();
        _eventFactory = EventFactory.getFactoryInstance();


    }


    /**
     * main method
     *
     * @param args command line arguments
     */
    public static void main(String[] args)
    {
        if (args.length > 0)
        {

            int portNum;

            try
            {
                portNum = Integer.parseInt(args[0]);

                Registry registry = new Registry(portNum, 10);
                registry.run();
            }
            catch (NumberFormatException e)
            {
                System.err.println("Argument" + " must be an integer");
                System.exit(1);
            }

        }

    }

    private void run()
    {
        try
        {
            initializer();
        }
        catch(UnknownHostException uhe)
        {
            uhe.printStackTrace();
        }



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
                System.err.println("Registry commands must be no more than two words and no less than one.");
                continue;
            }

            // IF the length is one, process the one word commands
            if (commands.length == 1)
            {
                // IF the command was list-weights
                if (commands[0].equals("list-weights"))
                {
                    // do something
                }
                // ELSE IF the command was start
                else if (commands[0].equals("start"))
                {
                    // reset our statistics
                    _statisticsCollectorAndDisplay.clear();

                    TaskInitiate taskInitiate = (TaskInitiate) _eventFactory.createEvent(Protocol.TASK_INITIATE);

                    broadcastMessage(taskInitiate, 5000);
                }
                // ELSE IF te command was send-overlay-link-weights
                else if (commands[0].equals("send-overlay-link-weights"))
                {
                    LinkWeightsMessageCreator linkWeightsCreator = new LinkWeightsMessageCreator
                            (getNodeListMessageMap(), 10);

                    broadcastMessage(linkWeightsCreator.createLinkWeightMessage(), 0);


                }
                // ELSE invalid one word command
                else
                {
                    System.err.println("Single-word Registry commands can only be one of \n" +
                            "[list-weights, send-overlay-link-weights, start].");

                }
            }
            // ELSE IF the length is two, process the two word commands
            else if (commands.length == 2)
            {
                // IF the first word of the command is list-messaging
                if (commands[0].equals("list-messaging"))
                {
                    // IF the second word of the command is nodes
                    if (commands[1].equals("nodes"))
                    {
                        // do something
                    }
                    else
                    {
                        System.err.println("Argument [" + commands[1] + "] to list-messaging invalid!");

                    }
                }
                // ELSE IF the first word of the command is setup-overlay
                else if (commands[0].equals("setup-overlay"))
                {
                    // number of connections each node should have
                    int numConnections = Integer.parseInt(commands[1]);


                    // create an OverlayCreator with the appropriate info
                    OverlayCreator overlay = new OverlayCreator(getRegistrationRequestMap(), numConnections);


                    // generate and overlay and pass the MessagingNodeLists off to be broad-casted
                    _nodeListMessageMap = overlay.setUpOverlay();
                    broadcastMessagingNodeLists(getNodeListMessageMap());


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
//    public synchronized void onEvent(TCPReceiverThread receiverThread, Event event) throws IOException
//    {
//
//
//
//        /**
//         * Determine the type of message sent so we can do the
//         * appropriate action.
//         *
//         */
//        switch (event.getType())
//        {
//            case Protocol.REGISTRATION_REQUEST:
//
//                /**
//                 * Convert the event to its appropriate type
//                 */
//                RegistrationRequest registrationRequest = (RegistrationRequest) event;
//
//                /**
//                 * String for the response message
//                 */
//                String response;
//
//                // IF there is already a connection to the node requesting registration
//                if (getRegisteredNodesMap().containsKey(registrationRequest.getID()))
//                {
//                    response = "Error! Node has been previously registered with the registry!";
//                    registerResponse(registrationRequest, false, response);
//                }
//                else
//                {
//                    // IF the socket info matches the message info
//                    if (verifyAddress(registrationRequest, receiverThread.getSocket()))
//                    {
//                        /**
//                         * There is no connection to the calling node, Register the node with the registry
//                         */
//                        response = "Registration request successful. The number of messaging nodes currently " +
//                                "constituting the overlay is " + getNumLinks() + ".";
//
//                        registerResponse(registrationRequest, true, response);
//                    }
//                    else
//                        System.err.println("Error! Message IP and/or Port number do not match the open socket " +
//                                            "connection! Failed to register.");
//
//                }
//
//                break;
//
//            case Protocol.DEREGISTRATION_REQUEST:
//
//                /**
//                 * Convert the event to its appropriate type
//                 */
//                DeregistrationRequest deregistrationRequest = (DeregistrationRequest) event;
//
//                // IF the socket info matches the message info
//                if (verifyAddress(deregistrationRequest, receiverThread.getSocket()))
//                    deregisterConnection(deregistrationRequest.getID());
//                else
//                    System.err.println("Error! Message IP and/or Port number do not match the open socket " +
//                                        "connection! Failed to deregister.");
//
//
//                break;
//            case Protocol.TASK_COMPLETE:
//
//                /**
//                 * Convert the event to its appropriate type
//                 */
//                TaskComplete taskComplete = (TaskComplete) event;
//                System.out.println("Task complete received: " + taskComplete.getType());
//
//                processTaskComplete(taskComplete);
//
//                break;
//            case Protocol.TRAFFIC_SUMMARY:
//
//                /**
//                 * Convert the event to its appropriate type
//                 */
//                TrafficSummary trafficSummary = (TrafficSummary) event;
//
//                _statisticsCollectorAndDisplay.add(trafficSummary);
//
//                // IF we have received a summary from every node
//                if (_statisticsCollectorAndDisplay.getTotalCollected() == getMaxMessagingNodes())
//                    _statisticsCollectorAndDisplay.print();
//
//                break;
//            default:
//                break;
//
//
//        }
//
//
//    }


    /**
     * Method to be signaled with a received message
     *
     * @param receiverThread <code>TCPReceiverThread</code> of the receiver associated with the message. Contains
     *                       a socket connection to the message sender.
     * @param event          <code>Event</code> of the incoming message.
     */
    public synchronized void onEvent(TCPReceiverThread receiverThread, Event event)
    {

        /**
         * Determine the type of message sent so we can do the
         * appropriate action.
         */
        switch (event.getType())
        {
            case Protocol.REGISTRATION_REQUEST:

                /**
                 * Convert the event to its appropriate type
                 */
                RegistrationRequest registrationRequest = (RegistrationRequest) event;

                /**
                 * String for the response message
                 */
                String response;

                // IF there is already a connection to the node requesting registration
                if (getRegisteredNodesMap().containsKey(registrationRequest.getID()))
                {
                    response = "Error! Node has been previously registered with the registry!";
                    registerResponse(registrationRequest, false, response);
                }
                else
                {
                    /**
                     * There is no connection to the calling node, Register the node with the registry
                     */
                    response = "Registration request successful. The number of messaging nodes currently " +
                            "constituting the overlay is " + getNumLinks() + ".";

                    registerResponse(registrationRequest, true, response);

                }

                break;

            case Protocol.DEREGISTRATION_REQUEST:

                /**
                 * Convert the event to its appropriate type
                 */
                DeregistrationRequest deregistrationRequest = (DeregistrationRequest) event;

                deregisterConnection(deregistrationRequest.getID());

                break;
            case Protocol.TASK_COMPLETE:

                /**
                 * Convert the event to its appropriate type
                 */
                TaskComplete taskComplete = (TaskComplete) event;

                processTaskComplete(taskComplete);

                break;
            case Protocol.TRAFFIC_SUMMARY:

                /**
                 * Convert the event to its appropriate type
                 */
                TrafficSummary trafficSummary = (TrafficSummary) event;

                _statisticsCollectorAndDisplay.add(trafficSummary);

                // IF we have received a summary from every node
                if (_statisticsCollectorAndDisplay.getTotalCollected() == getMaxMessagingNodes())
                    _statisticsCollectorAndDisplay.print();

                break;
            default:
                System.err.println("Not in OnEvent for Registry");
                break;


        }
    }




    /**
     * Private method to send a registration response message to the requesting MessagingNode.
     *
     * @param registrationRequest <code>RegistrationRequest</code> the calling node sent
     * @param status              <code>boolean</code> that signifies if the Link to the node was successfully
     *                            created or
     *                            not
     * @param message             <code>String</code> with a message for the successful/unsuccessful registration.
     */
    private void registerResponse(RegistrationRequest registrationRequest, boolean status, String message)
    {

        /**
         * RegistrationResponse message to be sent to the calling node
         */
        RegistrationResponse registrationResponse = (RegistrationResponse) EventFactory.getFactoryInstance()
                .createEvent(Protocol
                        .REGISTRATION_RESPONSE);

        // set the ip address field of the message
        registrationResponse.setIpAddress(_listeningIP);

        // set the port number field of the message
        registrationResponse.setPort(_listeningPort);

        // set the ID of the registry
        registrationResponse.setID(_ID);

        // set the status of the registration
        registrationResponse.setStatus(status);

        // set the message
        registrationResponse.setDescription(message);

        try
        {

            /**
             *  Socket to the client
             */
            Socket clientSocket = new Socket(registrationRequest.getIpAddress(),
                    registrationRequest.getNodeServerPort());

            /**
             *  TCPSender to send a message to the client
             */
            TCPSender sender = new TCPSender(clientSocket);

            // send message
            sender.sendData(registrationResponse.getBytes());


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

                /**
                 * Add the RegistrationRequest message to our tracker
                 */
                getRegistrationRequestMap().put(registrationRequest.getID(), registrationRequest);
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    /**
     * Method to register a connection between the current node and another. To register a
     * connection, we must create a new Link and add it to our list of connected nodes.
     *
     * @param receiverThread <code>TCPReceiverThread</code> of the receiver associated with the message. Contains a
     *                       socket connection to the message sender.
     * @param sourceID       <code>String</code> identifier of the owner of this Link (the current Node)
     * @param targetID       <code>String</code> identifier this Link is to.
     */
    public synchronized void registerConnection(TCPReceiverThread receiverThread, String sourceID, String targetID)
    {

        // IF the max nodes connected hasn't been reached
        if (_numLinks < _maxMessagingNodes)
        {

            /**
             * Link to the node to be added
             */
            Link nodeLink = new Link(receiverThread, sourceID, targetID);

            // add the connection to our list of connected nodes.
            getRegisteredNodesMap().put(targetID, nodeLink);

            ++_numLinks;
        }
        else    // print an error
            System.err.println("You promised us no more than 10 connections!");

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
            // interupt the receiver
            getRegisteredNodesMap().get(ID).interruptReceiver();

            getRegisteredNodesMap().remove(ID);
            --_numLinks;

        }
        else
        {
            System.err.println("Node does not exist to de-register!");
        }
    }


    /**
     * Private method to send out a node list message to every node who has a MessagingNodeList
     * created for them.
     *
     * @param nodeListMessageMap <code>HashMap</code> containing each <code>MessagingNode</code>'s
     *                           <code>MessagingNodeList</code>.
     */
    private void broadcastMessagingNodeLists(HashMap<String, MessagingNodeList> nodeListMessageMap)
    {
        /**
         * Object array to hold all of the keys (strings) for our MessagingNodeList map
         */
        Object[] nodeKeys = nodeListMessageMap.keySet().toArray();

        /**
         * String to be used for the current Node's ID
         */
        String nodeID;

        /**
         * Link to be used for the current node's link
         */
        Link currLink;


        /**
         * For every messaging list in the hash map, send the message to the recipient.
         */
        for (Object node : nodeKeys)
        {

            // get the current node's ID
            nodeID = node.toString();

            // get that node's link
            currLink = getRegisteredNodesMap().get(nodeID);

            /**
             * Try to send the message
             */
            currLink.send(nodeListMessageMap.get(nodeID).getBytes());


        }

    }

    /**
     * Private method to send out a messages to every node in the Overlay and set the
     * appropriate IP and port number for each node.
     *
     * @param message <code>Message</code> to be broadcast to the overlay.
     */
    private void broadcastMessage(Message message, int sleepTime)
    {
        /**
         * Object array to hold all of the keys (strings) to the registered nodes
         */
        Object[] nodeKeys = getRegisteredNodesMap().keySet().toArray();


        /**
         * String of the current node's ID
         */
        String nodeID;

        /**
         * Link to the current node
         */
        Link currLink;

//        int started = 0;

        /**
         * Send the LinkWeights message to every node registered with the Registry
         */
        for (Object node : nodeKeys)
        {
//            if(started == 2)
//                break;

            // get the current node's ID for the hash map
            nodeID = node.toString();

            // get the link of the current node
            currLink = getRegisteredNodesMap().get(nodeID);

            // set the message's info with the appropriate socket info
            message.setID(getID());
            message.setIpAddress(currLink.getHostname());
            message.setPort(currLink.getPort());

            currLink.send(message.getBytes());



            // IF we were asked to sleep
            if(sleepTime > 0)
            {
                try
                {
                    Thread.sleep(sleepTime);
                }
                catch (InterruptedException ie)
                {
                    ie.printStackTrace();
                }
            }

        }
//        started += 1;


    }


    /**
     * Private method to respond to TaskComplete messages. This method will keep track
     * of how many nodes have sent us TaskComplete messages. Once a TaskComplete message
     * has been received for every registered MessagingNode it will send a PullTrafficSummary
     * to the Overlay.
     *
     * @param taskComplete <code>TaskComplete</code> message from a registered <code>MessagingNode</code>.
     */
    private void processTaskComplete(TaskComplete taskComplete)
    {
        /**
         * If the message was sent from a registered node in the Overlay, increment
         * the tracker.
         */
        if (getRegisteredNodesMap().containsKey(taskComplete.getID()))
            setTaskCompleteReceived(getTaskCompleteReceived() + 1);
        else
        {
            System.out.println("TASK COMPLETE FROM UNREGISTERED NODE");
            System.out.println("NodeID: " + taskComplete.getID());

        }


        /**
         * If the registry has received a connection for every registered node in
         * the Overlay, broadcast a PULL_TRAFFIC_SUMMARY message.
         */
        if (getTaskCompleteReceived() == getRegisteredNodesMap().size())
        {
            /**
             * Slight pause before sending the PULL_TRAFFIC_SUMMARY message
             */
            try
            {
                Thread.sleep(2000);
            }
            catch (InterruptedException ie)
            {
                ie.printStackTrace();
            }


            PullTrafficSummary pullTrafficSummary = (PullTrafficSummary) _eventFactory.createEvent(Protocol.PULL_TRAFFIC_SUMMARY);

            broadcastMessage(pullTrafficSummary, 0);

            System.out.println("Sent a PULL_TRAFFIC_SUMMARY");
        }
    }

    /**
     * Method to verify that the IP address given in a MessagingNode's registration/deregistration request
     * and the IP of the actual socket connection match.
     *
     * @param message <code>Message</code> to be checked.
     * @return <code>true</code> if the sent address and actual address match, <code>false</code> if otherwise.
     */
    private boolean verifyAddress(Message message, Socket socket)
    {
        // IF the messages IP and port
        if (message.getIpAddress().equals(socket.getInetAddress().getCanonicalHostName()) && message.getPort() ==
                socket.getPort())
            return true;
        else
        {
            System.out.println("message.getIPAddress(): " + message.getIpAddress() +
                    "\ngetInetAddress().getCanonicalHostName(): " + socket.getInetAddress().getCanonicalHostName());

            return false;
        }
    }

    /**
     * Private method to start our server thread and set our listening address from it.
     * @throws UnknownHostException
     */
    private void initializer() throws UnknownHostException
    {
        _serverThread.start();
        _listeningIP = _serverThread.getServerSocket().getInetAddress().getLocalHost().getCanonicalHostName();

        _ID = _listeningIP + ":" + _listeningPort;
    }


    /**
     * ************************* ACCESSORS AND MUTATORS ******************************
     */

    public HashMap<String, Link> getRegisteredNodesMap()
    {
        return _registeredNodesMap;
    }

    public HashMap<String, RegistrationRequest> getRegistrationRequestMap()
    {
        return _registrationRequestMap;
    }

    public HashMap<String, MessagingNodeList> getNodeListMessageMap()
    {
        return _nodeListMessageMap;
    }

    public String getID()
    {
        return _ID;
    }

    public void setID(String ID)
    {
        _ID = ID;
    }

    public int getNumLinks()
    {
        return _numLinks;
    }

    public int getMaxMessagingNodes()
    {
        return _maxMessagingNodes;
    }

    public String getListeningIP()
    {
        return _listeningIP;
    }

    public int getListeningPort()
    {
        return _listeningPort;
    }

    public int getTaskCompleteReceived()
    {
        return _taskCompleteReceived;
    }

    public void setTaskCompleteReceived(int taskCompleteReceived)
    {
        _taskCompleteReceived = taskCompleteReceived;
    }

}
