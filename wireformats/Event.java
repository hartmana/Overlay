package cs455.overlay.wireformats;

/**
 * A Event interface that all wire format messages will implement
 *
 * @author ahrtmn, 27 01 2014
 */
public interface Event
{
    /**
     * Gets the type of the given message
     *
     * @return <code>int</code> value in the range of 2000-9000 determining the type of message.
     */
    public int getType();

    /**
     * Returns the current message in bytes
     *
     * @return <code>byte[]</code> of the message transformed into a byte array.
     */
    public byte[] getBytes();
}
