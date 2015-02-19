package cs455.overlay.wireformats;

import cs455.overlay.exceptions.MessageTypeException;

import java.io.*;
import java.util.Date;

/**
 * A DeregistrationRequest class defining what a Registration request message is. MessagingNode's
 * in the overlay will send the Registry register-requests with their IP Address and
 * port number.
 *
 * @author ahrtmn, 26 01 2014
 */
public class RegistrationRequest extends Message implements Event
{
    /**
     * int of the port number that the MessagingNode sending the REGISTRATION_REQUEST message
     * has a server thread listening to.
     */
    private int _nodeServerPort;

    /**
     * Default constructor
     */
    public RegistrationRequest()
    {
        this("", 0, "");
    }

    /**
     * Standard constructor
     */
    public RegistrationRequest(String ipAddress, int portNumber, String id)
    {
        setType(Protocol.REGISTRATION_REQUEST);
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
     * @param data <code>byte[]</code> of all the data belonging to the RegistrationRequest message type
     */
    public RegistrationRequest(byte[] data) throws MessageTypeException
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

            // set the messaging nodes listening port number
            setNodeServerPort(din.readInt());

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
        if (getType() != Protocol.REGISTRATION_REQUEST)
            throw new MessageTypeException("Invalid message type! RegistrationRequest must be " + Protocol
                    .REGISTRATION_REQUEST + "!");

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
            dout.writeInt(getNodeServerPort());
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


    /**
     * ************************* ACCESSORS AND MUTATORS ******************************
     */
    public int getNodeServerPort()
    {
        return _nodeServerPort;
    }

    public void setNodeServerPort(int nodeServerPort)
    {
        _nodeServerPort = nodeServerPort;
    }
}
