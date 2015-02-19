package cs455.overlay.wireformats;

/**
 * A Message class
 *
 * @author ahrtmn, 30 01 2014
 */
public abstract class Message implements Event
{
    /**
     * int for the message type
     */
    private int _type;

    /**
     * String for the IP address of the node sending the message
     */
    private String _ipAddress;

    /**
     * int for the port number of the sending node
     */
    private int _portNum;

    /**
     * String for the ID of the sending node
     */
    private String _ID;

    /**
     * long int for the timestamp
     */
    private long _timestamp;

    /**
     * getIpAddress method to return the current message sender's IP address.
     *
     * @return <code>String</code> containing the message senders IP address
     */
    public String getIpAddress()
    {
        return _ipAddress;
    }

    /**
     * setIpAddress method to set the IP address of the message sender.
     *
     * @param ipAddress <code>String</code> containing the sending node's IP address.
     */
    public void setIpAddress(String ipAddress)
    {
        _ipAddress = ipAddress;
    }

    /**
     * getPort method to return the current message sender's port number.
     *
     * @return <code>int</code> of the senders port number
     */
    public int getPort()
    {
        return _portNum;
    }

    /**
     * setPort method to set the port number of the current message sender
     *
     * @param portNum <code>int</code> denoting the port number of the sending node.
     */
    public void setPort(int portNum)
    {
        _portNum = portNum;
    }

    /**
     * getTimestamp method to return the messages creation time stamp
     *
     * @return <code>long</code> of the timestamp in milliseconds
     */
    public long getTimestamp()
    {
        return _timestamp;
    }

    /**
     * setTimestamp method to set the current messages time stamp of creation (used for subclasses)
     *
     * @param timestamp <code>long</code> representing the current time in milliseconds
     */
    public void setTimestamp(long timestamp)
    {
        _timestamp = timestamp;
    }

    /**
     * getType method from the Event interface
     *
     * @return <code>int</code> denoting the messages type
     */
    public int getType()
    {
        return _type;
    }

    /**
     * setType method to set the type of the current message (used for sublasses)
     *
     * @param type <code>int</code> of the message type [specified in Protocol].
     */
    public void setType(int type)
    {
        _type = type;
    }

    /**
     * getBytes method from the Event interface
     *
     * @return <code>byte[]</code> of the current messages contents
     */
    public abstract byte[] getBytes();

    /**
     * getID method to return the ID of the node associated with the current message
     *
     * @return <code>String</code> containing the ID of the node related to this message.
     */
    public String getID()
    {
        return _ID;
    }

    /**
     * setID method to set the ID of the node associated with the current message
     *
     * @param ID <code>String</code> of the nodes ID that is sending the message.
     */
    public void setID(String ID)
    {
        _ID = ID;
    }
}
