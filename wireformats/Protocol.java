package cs455.overlay.wireformats;

/**
 * A Protocol interface
 *
 * @author ahrtmn, 27 01 2014
 */
public interface Protocol
{
    /**
     * Message types and their corresponding integer values
     */
    public static final int REGISTRATION_REQUEST = 2000;
    public static final int REGISTRATION_RESPONSE = 2500;
    public static final int TASK_COMPLETE = 3000;
    public static final int TRAFFIC_SUMMARY = 3500;
    public static final int MESSAGING_NODE_CONNECTION_REQUEST = 4000;
    public static final int MESSAGING_NODE_CONNECTION_RESPONSE = 4500;
    public static final int MESSAGING_NODE_LIST = 5000;
    public static final int LINK_WEIGHTS = 6000;
    public static final int TASK_INITIATE = 7000;
    public static final int PAYLOAD_MESSAGE = 7500;
    public static final int PULL_TRAFFIC_SUMMARY = 8000;
    public static final int DEREGISTRATION_REQUEST = 9000;

}
