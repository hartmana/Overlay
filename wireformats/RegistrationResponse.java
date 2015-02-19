package cs455.overlay.wireformats;

import cs455.overlay.exceptions.MessageTypeException;

import java.io.*;
import java.util.Date;

/**
 * A RegistrationResponse class defining what a Registration request response message is.
 * This message will notify the calling MessagingNode whether the registration was successful
 * or not.
 *
 * @author ahrtmn, 27 01 2014
 */
public class RegistrationResponse extends Message implements Event
{

    /**
     * boolean for the status code of registration
     */
    private boolean _status;

    /**
     * String for the description of the register response
     */
    private String _description;


    /**
     * Default constructor
     */
    public RegistrationResponse()
    {
        this("", 0, "");
    }

    /**
     * Standard constructor
     */
    public RegistrationResponse(String ipAddress, int portNumber, String ID)
    {
        setType(Protocol.REGISTRATION_RESPONSE);
        setIpAddress(ipAddress);
        setPort(portNumber);
        setID(ID);

        /**
         * Get the current time stamp of message creation
         */
        Date date = new Date();
        setTimestamp(date.getTime());

        // Set the status to false
        _status = false;

        _description = "";

    }


    /**
     * Overloaded constructor to be used by the TCPReceiverThread. This constructor allows
     * the caller to pass a byte array of the entire message and (assuming its the correct type)
     * have the message construct itself from the byte array.
     *
     * @param data <code>byte[]</code> of all the data belonging to the RegistrationRequest message type
     */
    public RegistrationResponse(byte[] data) throws MessageTypeException
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

            // read the ip addresses bytes
            int ipLength = din.readInt();
            byte[] ipBytes = new byte[ipLength];
            din.readFully(ipBytes);
            setIpAddress(new String(ipBytes));

            // get the port number
            setPort(din.readInt());

            // get the ID
            int idLength = din.readInt();
            byte[] id = new byte[idLength];
            din.readFully(id);
            setID(new String(id));

            // get the status
            _status = din.readBoolean();

            // read the description
            int descLength = din.readInt();
            byte[] desc = new byte[descLength];
            din.readFully(desc);
            setDescription(new String(desc));

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
        if (getType() != Protocol.REGISTRATION_RESPONSE)
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
            dout.writeBoolean(_status);
            dout.writeInt(getDescription().length());
            dout.writeBytes(getDescription());
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

    public boolean getStatus()
    {
        return _status;
    }

    public void setStatus(boolean status)
    {
        _status = status;
    }

    public String getDescription()
    {
        return _description;
    }

    public void setDescription(String description)
    {
        _description = description;
    }
}

