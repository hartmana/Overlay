package cs455.overlay.node;

import cs455.overlay.transport.TCPReceiverThread;
import cs455.overlay.wireformats.Event;

import java.io.IOException;


/**
 * This is the Node interface that will implement the onEvent(Event) method, used
 * to notify the current Node (MessagingNode or Registry) when an Event (message received)
 * has occurred.
 *
 * @author Aaron Hartman
 */
public interface Node
{
    /**
     * Method to be signaled with a received message
     *
     * @param receiverThread <code>TCPReceiverThread</code> of the receiver associated with the message. Contains
     *                       a socket connection to the message sender.
     * @param data          <code>Event</code> of the incoming message.
     */
    public void onEvent(TCPReceiverThread receiverThread, Event event) throws IOException;

//    public void onEvent(TCPReceiverThread receiverThread, byte[] data) throws IOException;


    /**
     * Method to register a connection between the current node and another. To register a
     * connection, we must create a new Link and add it to our list of connected nodes.
     *
     * @param receiverThread <code>TCPReceiverThread</code> of the receiver thread associated with the node that
     *                       is being registered.
     * @param sourceID       <code>String</code> identifier of the Node creating the Link.
     * @param targetID       <code>String</code> identifier of the Node the created Link is to.
     */
    public void registerConnection(TCPReceiverThread receiverThread, String sourceID, String targetID);

    /**
     * Method to deregister a node from the current node (ie., sever the link)
     *
     * @param ID <<code>String</code> of the ID to be associated with the link to be deregistered.
     */
    public void deregisterConnection(String ID);

    /**
     * Method to return the current Node's ID
     *
     * @return <code>String</code> of the current Node's identifier.
     */
    public String getID();

}



