package cs455.overlay.exceptions;

/**
 * A MessageTypeException class to be used when a given message does not match
 * the requested type.
 *
 * @author ahrtmn, 30 01 2014
 */
public class MessageTypeException extends Exception
{
    public MessageTypeException(String message)
    {
        super(message);
    }
}
