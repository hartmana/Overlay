package cs455.overlay.wireformats;

import cs455.overlay.exceptions.MessageTypeException;

import java.io.*;
import java.util.Date;

/**
 * A TrafficSummary class defining what a TrafficSummary message is. This message
 * will be sent by MessagingNodes in response to the Registry PullTrafficSummary request
 * message and will contain statistics on that particular node's transmissions.
 *
 * @author ahrtmn, 27 01 2014
 */
public class TrafficSummary extends Message implements Event
{
    /**
     * int for total number of message sent by the sending node
     */
    private int _numSent;

    /**
     * long for the total summation of sent messages
     */
    private long _sentSummation;

    /**
     * int for total number messages received
     */
    private int _numReceived;

    /**
     * long for the total summation of received messages
     */
    private long _receivedSummation;

    /**
     * int for the total number of messages relayed
     */
    private int _numRelayed;

    /**
     * Default constructor
     */
    public TrafficSummary()
    {
        this("", 0, "");
    }

    /**
     * Standard constructor
     */
    public TrafficSummary(String ipAddress, int portNumber, String id)
    {
        setType(Protocol.TRAFFIC_SUMMARY);
        setIpAddress(ipAddress);
        setPort(portNumber);
        setID(id);

        /**
         * Get the current time stamp of message creation
         */
        Date date = new Date();
        setTimestamp(date.getTime());

    }


    /**
     * Overloaded constructor to be used by the TCPReceiverThread. This constructor allows
     * the caller to pass a byte array of the entire message and (assuming its the correct type)
     * have the message construct itself from the byte array.
     *
     * @param data <code>byte[]</code> of all the data belonging to the TrafficSummary message type
     */
    public TrafficSummary(byte[] data) throws MessageTypeException
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

            // read in the total number of messages sent
            setNumSent(din.readInt());

            // read in the total summation of the sent messages
            setSentSummation(din.readLong());

            // read in the total number of received messages
            setNumReceived(din.readInt());

            // read in the total summation of the received messages
            setReceivedSummation(din.readLong());

            // read in the total number of relayed messages
            setNumRelayed(din.readInt());

            // get the time stamp
            setTimestamp(din.readLong());

            baInputStream.close();
            din.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        // IF the message type isn't appropriate to this message
        if (getType() != Protocol.TRAFFIC_SUMMARY)
            throw new MessageTypeException("Invalid message type! TrafficSummary must be " + Protocol
                    .TRAFFIC_SUMMARY + "!");

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
            dout.writeInt(getNumSent());
            dout.writeLong(getSentSummation());
            dout.writeInt(getNumReceived());
            dout.writeLong(getReceivedSummation());
            dout.writeInt(getNumRelayed());
            dout.writeLong(getTimestamp());

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

    public int getNumSent()
    {
        return _numSent;
    }

    public void setNumSent(int numSent)
    {
        _numSent = numSent;
    }

    public long getSentSummation()
    {
        return _sentSummation;
    }

    public void setSentSummation(long sentSummation)
    {
        _sentSummation = sentSummation;
    }

    public int getNumReceived()
    {
        return _numReceived;
    }

    public void setNumReceived(int received)
    {
        _numReceived = received;
    }

    public long getReceivedSummation()
    {
        return _receivedSummation;
    }

    public void setReceivedSummation(long receivedSummation)
    {
        _receivedSummation = receivedSummation;
    }

    public int getNumRelayed()
    {
        return _numRelayed;
    }

    public void setNumRelayed(int numRelayed)
    {
        _numRelayed = numRelayed;
    }
}
