package cs455.overlay.transport;

import java.io.IOException;
import java.net.Socket;

/**
 * A Link class to represent the connection between a node and another node.
 *
 * @author ahrtmn, 26 01 2014
 */
public class Link
{

    /**
     * TCPReceiverThread to listen for communications from the connected node
     */
    private TCPReceiverThread _receiverThread;      // contains most of the data we need

    /**
     * TCPSender to send data over the given link
     */
    private TCPSender _messageSender;

    /**
     * String of the ID the owner of this Link is.
     */
    private String _ID;

    /**
     * String of the ID connected TO by this link
     */
    private String _targetID;

    /**
     * int of the current links weight (if applicable)
     */
    private int _weight;


    /**
     * Constructor for the Link class.
     *
     * @param receiverThread <code>TCPReceiverThread</code> containing connection data necessary for the
     *                       <code>Link</code>.
     * @param sourceID       <code>String</code> containing the identifier of the Node owning this Link.
     * @param targetID       <code>String</code> containing the identifier to the node the <code>Link</code> is to.
     */
    public Link(TCPReceiverThread receiverThread, String sourceID, String targetID)
    {
        this(receiverThread, sourceID, targetID, 0);
    }

    /**
     * Overloaded Constructor for the Link class. Constructs a link between two nodes.
     *
     * @param receiverThread <code>TCPReceiverThread</code> listening to communications from the target Node.
     * @param sourceID       <code>String</code> ID of the connected <code>Node</code>
     * @param targetID       <code>String</code> ID of the Node this Link is connecting to.
     * @param weight         <code>int</code> denoting the weight of the connection
     */
    public Link(TCPReceiverThread receiverThread, String sourceID, String targetID, int weight)
    {
        _ID = sourceID;
        _targetID = targetID;
        _weight = weight;
        _receiverThread = receiverThread;


        if (_ID == null)
            System.out.println("source id null for target: " + _targetID);
        else if (_targetID == null)
            System.out.println("target id null for source: " + _ID);

        /**
         * Try to create a new TCPSender for the connection to the target node.
         */
        try
        {
            _messageSender = new TCPSender(_receiverThread.getSocket());
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }

    }


    /**
     * Method to close the current link
     */
    public void closeLink()
    {

        // IF the receiver for this link hasn't been interrupted yet
        if (!_receiverThread.isInterrupted())
            interruptReceiver();

        try
        {
            _receiverThread.getDin().close();
            _receiverThread.getSocket().close();
            _ID = "";
            _weight = 0;

        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }


    /**
     * ************************* OVERIDDEN METHODS **********************************
     */
    @Override
    public String toString()
    {
        return getID() + " " + getTargetID() + " " + getWeight();
    }

    @Override
    public boolean equals(Object obj)
    {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;


        Link other = (Link) obj;

        if (getID() == null)
        {
            if (other.getID() != null)
                return false;
        }
        else if (!getID().equals(other.getID()))
            return false;

        return true;
    }

    /**
     * ************************* ACCESSORS AND MUTATORS ******************************
     */

//    public TCPSender getMessageSender()
//    {
//        return _messageSender;
//    }

    public synchronized void send(byte[] data)
    {
        try
        {
            _messageSender.sendData(data);
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
    public String getLocalHostName()
    {
        return _receiverThread.getSocket().getLocalAddress().getCanonicalHostName();
    }

    public String getHostname()
    {
        return _receiverThread.getSocket().getInetAddress().getCanonicalHostName();
    }

    public int getPort()
    {
        return _receiverThread.getSocket().getPort();
    }

    public String getID()
    {
        return _ID;
    }

    public int getWeight()
    {
        return _weight;
    }

    public void setWeight(int weight)
    {
        _weight = weight;
    }

    public Socket getSocketConnection()
    {
        return _receiverThread.getSocket();
    }

    public String getTargetID()
    {
        return _targetID;
    }

    /**
     * Method to interrupt the receiver thread before closing the link
     */
    public void interruptReceiver()
    {
        _receiverThread.interrupt();
    }


}
