package cs455.overlay.transport;

import cs455.overlay.node.Node;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class defines a thread that will monitor for incoming request connections. When
 * a connection is made, it sends the connection to the TCPReceiverThread to obtain
 * the calling nodes message.
 *
 * @author Aaron Hartman
 */
public class TCPServerThread extends Thread
{

    /**
     * Server socket for accepting connections to the current Node
     */
    private ServerSocket _serverSocket;

    /**
     * Node linked to this server thread. This will be passed to the TCPReceiverThread
     * so it can signal the node when a message is received.
     */
    private Node _callbackNode;

    /**
     * Constructor
     *
     * @param serverSocket <code>ServerSocket</code> for the TCPServerThread to listen to
     */
    public TCPServerThread(ServerSocket serverSocket, Node callbackNode)
    {
        _serverSocket = serverSocket;

        _callbackNode = callbackNode;
    }

    public void run()
    {

        /**
         * Socket for direct communication between the server thread and the 'calling' node
         */
        Socket socket;


        // Keep listening for new connections until we terminate
        while (true)
        {
            try
            {
                socket = _serverSocket.accept();

                /**
                 *  TCPReceiverThread to receive communication from the client
                 */
                TCPReceiverThread receiver = new TCPReceiverThread(socket, _callbackNode);
                receiver.start();

            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
            }

        }// ENDWHILE


    }//ENDRUN


    /**
     * ************************* ACCESSORS AND MUTATORS ******************************
     */

    public ServerSocket getServerSocket()
    {
        return _serverSocket;
    }

}//ENDTCPSERVERTHREAD
