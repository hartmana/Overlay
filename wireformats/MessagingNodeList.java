package cs455.overlay.wireformats;

import cs455.overlay.exceptions.MessageTypeException;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;

/**
 * A MessagingNodeList class defining what a MessagingNodeList message is.
 * The MessagingNodeList will be sent by the Registry to all listening MessagingNodes informing
 * them of the other nodes in the overlay that they should connect with.
 *
 * @author ahrtmn, 26 01 2014
 */
public class MessagingNodeList extends Message implements Event
{

    /**
     * int to denote the total number of connections the receiver of this message is to initiate
     */
    private int _numPeerMessagingNodes;

    /**
     * ArrayList of Strings specifying the node information for each connection to be made
     */
    private ArrayList<String> _nodeConnectionInfo;

    /**
     * Default constructor
     */
    public MessagingNodeList()
    {
        this("", 0, "");
    }

    /**
     * Standard constructor
     */
    public MessagingNodeList(String ipAddress, int portNumber, String id)
    {
        setType(Protocol.MESSAGING_NODE_LIST);
        setIpAddress(ipAddress);
        setPort(portNumber);
        setID(id);

        /**
         * Get the current time stamp of message creation
         */
        Date date = new Date();
        setTimestamp(date.getTime());

        _numPeerMessagingNodes = 0;

        _nodeConnectionInfo = new ArrayList<String>();


    }


    /**
     * Overloaded constructor to be used by the TCPReceiverThread. This constructor allows
     * the caller to pass a byte array of the entire message and (assuming its the correct type)
     * have the message construct itself from the byte array.
     *
     * @param data <code>byte[]</code> of all the data belonging to the RegistrationRequest message type
     */
    public MessagingNodeList(byte[] data) throws MessageTypeException
    {

        /**
         * Set up appropriate streams to read the fields from the byte array
         * into the private data members.
         */
        ByteArrayInputStream baInputStream =
                new ByteArrayInputStream(data);

        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));


        /**
         * Try to read the message data in from the byte array
         */
        try
        {
            // read the type of the message
            setType(din.readInt());

            // read the length of the ip address
            int ipLength = din.readInt();

            // read the ip addresses bytes
            byte[] ipBytes = new byte[ipLength];
            din.readFully(ipBytes);

            // convert the ip address to a string
            setIpAddress(new String(ipBytes));

            // get the port number
            setPort(din.readInt());

            // read the ID from the stream
            int idLength = din.readInt();
            byte[] idBytes = new byte[idLength];
            din.readFully(idBytes);
            setID(new String(idBytes));

            // get the time stamp
            setTimestamp(din.readLong());

            // Read in the max number of connections the recipient is to have
            _numPeerMessagingNodes = din.readInt();

            /**
             * Re-create the connections ArrayList
             */
            int numConnectionInfo = din.readInt();  // how many nodes the recipient is to connect with
            _nodeConnectionInfo = new ArrayList<String>();
            int byteLength;
            byte[] stringBytes;

            // FOR every connection info we should have
            for (int i = 0; i < numConnectionInfo; ++i)
            {
                // read in the length and create a byte array of that size
                byteLength = din.readInt();
                stringBytes = new byte[byteLength];

                // read in the string
                din.readFully(stringBytes);

                _nodeConnectionInfo.add(new String(stringBytes));

            }

            baInputStream.close();
            din.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        // IF the message type isn't appropriate to this message
        if (getType() != Protocol.MESSAGING_NODE_LIST)
            throw new MessageTypeException("Invalid message type! MessagingNodeList must be " + Protocol
                    .MESSAGING_NODE_LIST + "!");

    }


    /**
     * Event interface method
     * Returns the current message in bytes
     *
     * @return <code>byte[]</code> of the message transformed into a byte array.
     */
    public byte[] getBytes()
    {

        /**
         * Create an empty byte array and get the appropriate streams to be able to
         * write to it.
         */

        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout =
                new DataOutputStream(new BufferedOutputStream(baOutputStream));


        try
        {
            /**
             * Write the message data to the stream
             */
            dout.writeInt(getType());
            dout.writeInt(getIpAddress().length());
            dout.writeBytes(getIpAddress());
            dout.writeInt(getPort());
            dout.writeInt(getID().length());
            dout.writeBytes(getID());
            dout.writeLong(getTimestamp());
            dout.writeInt(getNumConnections());
            dout.writeInt(getNodeConnectionInfo().size());

            // FOR every node in the connection list
            for (String connectionInfo : _nodeConnectionInfo)
            {
                // write the length of the string
                dout.writeInt(connectionInfo.length());

                // write the actual connection info
                dout.writeBytes(connectionInfo);
            }

            dout.flush();

            // get the byte array
            marshalledBytes = baOutputStream.toByteArray();

            baOutputStream.close();
            dout.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }


        return marshalledBytes;


    }

    /**
     * ************************* ACCESSORS AND MUTATORS ******************************
     */

    public int getNumConnections()
    {
        return _numPeerMessagingNodes;
    }

    public void setNumPeerMessagingNodes(int numPeerMessagingNodes)
    {
        _numPeerMessagingNodes = numPeerMessagingNodes;
    }

    public ArrayList<String> getNodeConnectionInfo()
    {
        return _nodeConnectionInfo;
    }

}
