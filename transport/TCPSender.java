package cs455.overlay.transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * TCPSender class to facilitate the sending of messages to other nodes in the overlay.
 *
 * @author Aaron Hartman
 */
public class TCPSender
{
    private DataOutputStream _dataOutputStream;

    /**
     * Constructor
     *
     * @param socket <code>Socket</code> connection to the node the message should be sent to
     * @throws IOException
     */
    public TCPSender(Socket socket) throws IOException
    {
        _dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    /**
     * Method to send the byte array to the connected node.
     *
     * @param dataToSend <code>byte[]</code> containing the message to the other node.
     * @throws IOException
     */
    public void sendData(byte[] dataToSend) throws IOException
    {
//        System.out.println(dataToSend);
        _dataOutputStream.writeInt(dataToSend.length);
        _dataOutputStream.write(dataToSend, 0, dataToSend.length);
        _dataOutputStream.flush();

    }
}
