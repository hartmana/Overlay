package cs455.overlay.util;


/**
 * A Statics interface to contain static variable definitions for use within
 * the Overlay.
 *
 * @author ahrtmn, 16 02 2014
 */
public interface Statics
{
    /**
     * int for the number of rounds a node should generate.
     */
    public static final int NODE_MESSAGE_ROUNDS = 5000;

    /**
     * int for the number of messages to send each round
     */
    public static final int NODE_MESSAGES_PER_ROUND = 5;
}
