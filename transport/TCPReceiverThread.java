package cs455.overlay.transport;


import cs455.overlay.node.Node;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;


/**
 * This class defines a thread that will continuously listen for new connections to the current Node.
 *
 * @author Aaron Hartman
 */
public class TCPReceiverThread extends Thread
{
    /**
     * Socket connection to receive the data from
     */
    private Socket _socket;

    /**
     * DataInputStream to read data from the socket
     */
    private DataInputStream _din;

    /**
     * Node to be notified when the message is received
     */
    private Node _callbackNode;

    /**
     * EventFactory instance
     */
    private EventFactory _eventFactory = EventFactory.getFactoryInstance();


    /**
     * Constructor
     *
     * @param socket       <code>Socket</code> the message will be sent through.
     * @param callbackNode <code>Node</code> that has interest in the received message.
     * @throws IOException
     */
    public TCPReceiverThread(Socket socket, Node callbackNode) throws IOException
    {
        _socket = socket;
        _din = new DataInputStream(socket.getInputStream());

        _callbackNode = callbackNode;

    }

    /**
     * Receive the message from the sending Node and notify the receiving node of
     * the event.
     */
    public void run()
    {

        // WHILE we want to listen
//        while (!Thread.currentThread().isInterrupted())
        while(true)
        {

            /**
             * Try to read the message data in from the byte array
             */
            try
            {
                int payLoadLength = _din.readInt();

                byte[] payload = new byte[payLoadLength];

                _din.readFully(payload, 0, payLoadLength);

                Event event = _eventFactory.createEvent(payload);

                _callbackNode.onEvent(this, event);

            }
            catch (IOException ioe)
            {
                System.out.println("Receiver belongs to: " + getCallbackNode().getID());
                System.out.println("Message sent from: " + _socket.getInetAddress().getCanonicalHostName());
//                System.out.println("Byte array length: " + payload.)

                ioe.printStackTrace();
                System.exit(0);
            }

        } // END WHILE

    }//ENDRUN


    /**
     * ************************* ACCESSORS AND MUTATORS ******************************
     */

    public DataInputStream getDin()
    {
        return _din;
    }

    public Socket getSocket()
    {
        return _socket;
    }

    public Node getCallbackNode()
    {
        return _callbackNode;
    }

}//ENDTCPRECEIVERTHREAD

