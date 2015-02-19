package cs455.overlay.wireformats;

import cs455.overlay.exceptions.MessageTypeException;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * A Singleton EventFactory.getFactoryInstance() class to handle the creation of Messages
 *
 * @author ahrtmn, 26 01 2014
 */
public class EventFactory
{
    private static final EventFactory _factoryInstance = new EventFactory();


    private EventFactory()
    {

    }


    /**
     * createEvent method to return a specific message type determined by the caller
     *
     * @param data <code>byte[]</code> containing the desired message contents
     * @return <code>Event</code> of the desired message type.
     */
    public static Event createEvent(byte[] data) throws IOException
    {

        int type = 0;

        try
        {
            ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
            DataInputStream payloadInputStream = new DataInputStream(new BufferedInputStream(baInputStream));


            type = payloadInputStream.readInt();

        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }

        switch (type)
        {

            case Protocol.REGISTRATION_REQUEST:

                try
                {
                    return new RegistrationRequest(data);
                }
                catch (MessageTypeException mte)
                {
                    mte.getMessage();
                }
                break;
            case Protocol.REGISTRATION_RESPONSE:
                try
                {
                    return new RegistrationResponse(data);
                }
                catch (MessageTypeException mte)
                {
                    mte.getMessage();
                }
                break;
            case Protocol.DEREGISTRATION_REQUEST:
                try
                {
                    return new DeregistrationRequest(data);
                }
                catch (MessageTypeException mte)
                {
                    mte.getMessage();
                }
                break;
            case Protocol.MESSAGING_NODE_LIST:
                try
                {
                    return new MessagingNodeList(data);
                }
                catch (MessageTypeException mte)
                {
                    mte.getMessage();
                }
                break;
            case Protocol.LINK_WEIGHTS:
                try
                {
                    return new LinkWeights(data);
                }
                catch (MessageTypeException mte)
                {
                    mte.getMessage();
                }
                break;
            case Protocol.MESSAGING_NODE_CONNECTION_REQUEST:
                try
                {
                    return new NodeRegistrationRequest(data);
                }
                catch (MessageTypeException mte)
                {
                    mte.getMessage();
                }
                break;
            case Protocol.MESSAGING_NODE_CONNECTION_RESPONSE:
                try
                {
                    return new NodeRegistrationResponse(data);
                }
                catch (MessageTypeException mte)
                {
                    mte.getMessage();
                }
                break;
            case Protocol.TASK_INITIATE:
                try
                {
                    return new TaskInitiate(data);
                }
                catch (MessageTypeException mte)
                {
                    mte.getMessage();
                }
                break;
            case Protocol.PAYLOAD_MESSAGE:
                try
                {
                    return new PayloadMessage(data);
                }
                catch (MessageTypeException mte)
                {
                    mte.getMessage();
                }
                break;
            case Protocol.TASK_COMPLETE:
                try
                {
                    return new TaskComplete(data);
                }
                catch (MessageTypeException mte)
                {
                    mte.printStackTrace();
                }
                break;
            case Protocol.PULL_TRAFFIC_SUMMARY:
                try
                {
                    return new PullTrafficSummary(data);
                }
                catch (MessageTypeException mte)
                {
                    mte.printStackTrace();
                }
                break;
            case Protocol.TRAFFIC_SUMMARY:
                try
                {
                    return new TrafficSummary(data);
                }
                catch (MessageTypeException mte)
                {
                    mte.printStackTrace();
                }
                break;
            default:
                break;
        }


        System.err.println("Error! Should not be here. EventFactories can't make null Events. This is known.");
        System.err.println("Type: " + type);
        return new PayloadMessage("!", 999, "!");
    }

    /**
     * createEvent method to return a specific message type determined by the caller
     *
     * @param data <code>byte[]</code> containing the desired message contents
     * @return <code>Event</code> of the desired message type.
     */
    public static Event createEvent(int type, byte[] data) throws IOException
    {

        switch (type)
        {

            case Protocol.REGISTRATION_REQUEST:

                try
                {
                    return new RegistrationRequest(data);
                }
                catch (MessageTypeException mte)
                {
                    mte.getMessage();
                }
                break;
            case Protocol.REGISTRATION_RESPONSE:
                try
                {
                    return new RegistrationResponse(data);
                }
                catch (MessageTypeException mte)
                {
                    mte.getMessage();
                }
                break;
            case Protocol.DEREGISTRATION_REQUEST:
                try
                {
                    return new DeregistrationRequest(data);
                }
                catch (MessageTypeException mte)
                {
                    mte.getMessage();
                }
                break;
            case Protocol.MESSAGING_NODE_LIST:
                try
                {
                    return new MessagingNodeList(data);
                }
                catch (MessageTypeException mte)
                {
                    mte.getMessage();
                }
                break;
            case Protocol.LINK_WEIGHTS:
                try
                {
                    return new LinkWeights(data);
                }
                catch (MessageTypeException mte)
                {
                    mte.getMessage();
                }
                break;
            case Protocol.MESSAGING_NODE_CONNECTION_REQUEST:
                try
                {
                    return new NodeRegistrationRequest(data);
                }
                catch (MessageTypeException mte)
                {
                    mte.getMessage();
                }
                break;
            case Protocol.MESSAGING_NODE_CONNECTION_RESPONSE:
                try
                {
                    return new NodeRegistrationResponse(data);
                }
                catch (MessageTypeException mte)
                {
                    mte.getMessage();
                }
                break;
            case Protocol.TASK_INITIATE:
                try
                {
                    return new TaskInitiate(data);
                }
                catch (MessageTypeException mte)
                {
                    mte.getMessage();
                }
                break;
            case Protocol.PAYLOAD_MESSAGE:
                try
                {
                    return new PayloadMessage(data);
                }
                catch (MessageTypeException mte)
                {
                    mte.getMessage();
                }
                break;
            case Protocol.TASK_COMPLETE:
                try
                {
                    return new TaskComplete(data);
                }
                catch (MessageTypeException mte)
                {
                    mte.printStackTrace();
                }
                break;
            case Protocol.PULL_TRAFFIC_SUMMARY:
                try
                {
                    return new PullTrafficSummary(data);
                }
                catch (MessageTypeException mte)
                {
                    mte.printStackTrace();
                }
                break;
            case Protocol.TRAFFIC_SUMMARY:
                try
                {
                    return new TrafficSummary(data);
                }
                catch (MessageTypeException mte)
                {
                    mte.printStackTrace();
                }
                break;
            default:
                break;
        }


        System.err.println("Error! Should not be here. EventFactories can't make null Events. This is known.");
        System.err.println("Type: " + type);
        return null;
    }

    /**
     * createEvent method to return a specific message type (that is blank) determined by the caller.
     *
     * @param eventType <code>int</code> of the message type ID. For clarity should be called with Protocol.messageType.
     * @return <code>Event</code> of the desired message type.
     */
    public static Event createEvent(int eventType)
    {

        switch (eventType)
        {
            case Protocol.REGISTRATION_REQUEST:
                return new RegistrationRequest();

            case Protocol.REGISTRATION_RESPONSE:
                return new RegistrationResponse();

            case Protocol.DEREGISTRATION_REQUEST:
                return new DeregistrationRequest();

            case Protocol.MESSAGING_NODE_LIST:
                return new MessagingNodeList();

            case Protocol.LINK_WEIGHTS:
                return new LinkWeights();

            case Protocol.MESSAGING_NODE_CONNECTION_REQUEST:
                return new NodeRegistrationRequest();

            case Protocol.MESSAGING_NODE_CONNECTION_RESPONSE:
                return new NodeRegistrationResponse();

            case Protocol.TASK_INITIATE:
                return new TaskInitiate();

            case Protocol.PAYLOAD_MESSAGE:
                return new PayloadMessage();

            case Protocol.TASK_COMPLETE:
                return new TaskComplete();

            case Protocol.PULL_TRAFFIC_SUMMARY:
                return new PullTrafficSummary();

            case Protocol.TRAFFIC_SUMMARY:
                return new TrafficSummary();

            default:
                break;
        }


        return null;
    }


    /**
     * createEvent method to return a specific message type determined by the caller
     *
     * @param eventType <code>int</code> of the message type ID. For clarity should be called with Protocol.messageType
     * @param IP        <code>String</code> containing the IP address of the sender or receiver,
     *                  depending on the message.
     * @param port      <code>int</code> denoting the port number of the sender or receiver.
     * @param ID        <code>String</code> denoting the ID of the sending
     * @return <code>Event</code> of the desired message type.
     */
    public static Event createEvent(int eventType, String IP, int port, String ID)
    {

        switch (eventType)
        {
            case Protocol.REGISTRATION_REQUEST:
                return new RegistrationRequest(IP, port, ID);

            case Protocol.REGISTRATION_RESPONSE:
                return new RegistrationResponse(IP, port, ID);

            case Protocol.DEREGISTRATION_REQUEST:
                return new DeregistrationRequest(IP, port, ID);

            case Protocol.MESSAGING_NODE_LIST:
                return new MessagingNodeList(IP, port, ID);

            case Protocol.LINK_WEIGHTS:
                return new LinkWeights(IP, port, ID);

            case Protocol.MESSAGING_NODE_CONNECTION_REQUEST:
                return new NodeRegistrationRequest(IP, port, ID);

            case Protocol.MESSAGING_NODE_CONNECTION_RESPONSE:
                return new NodeRegistrationResponse(IP, port, ID);

            case Protocol.TASK_INITIATE:
                return new TaskInitiate(IP, port, ID);

            case Protocol.PAYLOAD_MESSAGE:
                return new PayloadMessage(IP, port, ID);

            case Protocol.TASK_COMPLETE:
                return new TaskComplete(IP, port, ID);

            case Protocol.PULL_TRAFFIC_SUMMARY:
                return new PullTrafficSummary(IP, port, ID);

            case Protocol.TRAFFIC_SUMMARY:
                return new TrafficSummary(IP, port, ID);

            default:
                break;
        }

        System.err.println("Error! Should not be here. EventFactories can't make null Events. This is known.");
        System.err.println("Type: " + eventType);
        return null;
    }

    /**
     * createEvent method to specifically for Messaging Node Connection Request messages, which need to include
     * the Registry ID that dictated that connection be made.
     *
     * @param eventType  <code>int</code> of the message type ID. For clarity should be called with Protocol.messageType
     * @param IP         <code>String</code> containing the IP address of the sender or receiver,
     *                   depending on the message.
     * @param port       <code>int</code> denoting the port number of the sender or receiver.
     * @param ID         <code>String</code> denoting the ID of the sending/receiving node.
     * @param registryID <code>String</code> denoting the ID of the Registry who initiated the sending of the
     *                   MessagingNodeRequest.
     * @return <code>NodeRegistrationRequest</code> of the desired message type.
     */
//    public static NodeRegistrationRequest createEvent(int eventType, String IP, int port, String ID, String registryID)
//    {
//
//        switch (eventType)
//        {
//            case Protocol.MESSAGING_NODE_CONNECTION_REQUEST:
//                return new NodeRegistrationRequest(IP, port, ID, registryID);
//
//            default:
//                break;
//        }
//
//        System.err.println("Error! Should not be here. EventFactories can't make null Events. This is known.");
//        return null;
//    }


    public static EventFactory getFactoryInstance()
    {
        return _factoryInstance;
    }

}
