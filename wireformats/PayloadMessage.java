package cs455.overlay.wireformats;

import cs455.overlay.exceptions.MessageTypeException;

import java.io.*;
import java.util.Date;
import java.util.LinkedList;

/**
 * A PayloadMessage class to carry the actual data we are wanting to send.
 *
 * @author ahrtmn, 12 02 2014
 */
public class PayloadMessage extends Message implements Event
{

    /**
     * String ArrayList to hold the path this message should take to its sink node
     */
    private LinkedList<String> _path;

    /**
     * int for actual payload
     */
    private int _payload;

    /**
     * Default constructor
     */
    public PayloadMessage()
    {
        this("", 0, "");
    }


    /**
     * Standard constructor
     */
    public PayloadMessage(String ipAddress, int portNumber, String id)
    {
        setType(Protocol.PAYLOAD_MESSAGE);
        setIpAddress(ipAddress);
        setPort(portNumber);
        setID(id);
        _payload = 0;

        _path = new LinkedList<String>();

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
     * @param data <code>byte[]</code> of all the data belonging to the PayloadMessage message type
     */
    public PayloadMessage(byte[] data) throws MessageTypeException
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
            int length = din.readInt();

            // read the ip addresses bytes
            byte[] ipBytes = new byte[length];
            din.readFully(ipBytes);

            // convert the ip address to a string
            setIpAddress(new String(ipBytes));

            // get the port number
            setPort(din.readInt());

            // read the ID from the stream
            length = din.readInt();
            byte[] idBytes = new byte[length];
            din.readFully(idBytes);
            setID(new String(idBytes));


            // get the time stamp
            setTimestamp(din.readLong());

            // read the payload
            _payload = din.readInt();


            /**
             * Re-create the path linked list
             */
            int numPaths = din.readInt();  // how many nodes the recipient is to connect with
            _path = new LinkedList<String>();
            int byteLength;
            byte[] stringBytes;

            // FOR every path info we should have
            for (int i = 0; i < numPaths; ++i)
            {
                // read in the length and create a byte array of that size
                byteLength = din.readInt();
                stringBytes = new byte[byteLength];

                // read in the string
                din.readFully(stringBytes);

                _path.add(new String(stringBytes));

            }

            baInputStream.close();
            din.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        // IF the message type isn't appropriate to this message
        if (getType() != Protocol.PAYLOAD_MESSAGE)
            throw new MessageTypeException("Invalid message type! PayloadMessage must be " + Protocol
                    .PAYLOAD_MESSAGE + "; given " + getType() + "!");

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
            dout.writeLong(getTimestamp());
            dout.writeInt(getPayload());
            dout.writeInt(getPath().size());

            // FOR every node in the connection list
            for (String path : getPath())
            {
                // write the length of the string
                dout.writeInt(path.length());

                // write the actual connection info
                dout.writeBytes(path);
            }


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

    public static void main(String[] args)
    {
//        Event E = EventFactory.createEvent(Protocol.PAYLOAD_MESSAGE, "invalid", 4245, "also invalid");
//
//        PayloadMessage p = (PayloadMessage) E;
//
//        p.getPath().add("DOG");
//        p.getPath().add("CAT");
//        p.getPath().add("MOUSE");
//
//
//
//        byte[] bytes = p.getBytes();
//
//
//        Event eTest = null;
//        try
//        {
//            eTest =  EventFactory.getFactoryInstance().createEvent(bytes);
//
//        }
//        catch(IOException ioe)
//        {
//            ioe.printStackTrace();
//        }
//
//        PayloadMessage testMessage = (PayloadMessage) eTest;
//
//
//        System.out.println("IP: " + testMessage.getIpAddress());
//        System.out.println("Port: " + testMessage.getPort());
//        System.out.println("ID: " + testMessage.getID());
//        System.out.println("Type: " + testMessage.getType());
//
//        for(int i = 0; i < testMessage.getPath().size(); ++i)
//        {
//            System.out.println(testMessage.getPath().get(i));
//        }
//
//
//
//        byte[] bytes2 = testMessage.getBytes();
//
//
//        Event eTest2 = null;
//        try
//        {
//            eTest2 =  EventFactory.getFactoryInstance().createEvent(bytes2);
//
//        }
//        catch(IOException ioe)
//        {
//            ioe.printStackTrace();
//        }
//
//        PayloadMessage testMessage2 = (PayloadMessage) eTest2;
//
//
//        System.out.println("IP: " + testMessage2.getIpAddress());
//        System.out.println("Port: " + testMessage2.getPort());
//        System.out.println("ID: " + testMessage2.getID());
//        System.out.println("Type: " + testMessage2.getType());
//
//        for(int i = 0; i < testMessage2.getPath().size(); ++i)
//        {
//            System.out.println(testMessage2.getPath().get(i));
//        }
//
//        testMessage2.getPath().removeFirst();
//
//        System.out.println("IP: " + testMessage2.getIpAddress());
//        System.out.println("Port: " + testMessage2.getPort());
//        System.out.println("ID: " + testMessage2.getID());
//        System.out.println("Type: " + testMessage2.getType());
//
//        for(int i = 0; i < testMessage2.getPath().size(); ++i)
//        {
//            System.out.println(testMessage2.getPath().get(i));
//        }
//
//        PayloadMessage test3 = null;
//        try
//        {
//            test3 = (PayloadMessage) _eventFactory.createEvent(testMessage2.getBytes());
//        }
//        catch(IOException ioe)
//        {
//            ioe.printStackTrace();
//        }
//
//
//        System.out.println("IP: " + test3.getIpAddress());
//        System.out.println("Port: " + test3.getPort());
//        System.out.println("ID: " + test3.getID());
//        System.out.println("Type: " + test3.getType());
//
//        for(int i = 0; i < test3.getPath().size(); ++i)
//        {
//            System.out.println(test3.getPath().get(i));
//        }
//
//        test3.getPath().removeFirst();
//
//        try
//        {
//            test3 = (PayloadMessage) EventFactory.createEvent(test3.getBytes());
//        }
//        catch(IOException ioe)
//        {
//            ioe.printStackTrace();
//        }
//
//
//        System.out.println("IP: " + test3.getIpAddress());
//        System.out.println("Port: " + test3.getPort());
//        System.out.println("ID: " + test3.getID());
//        System.out.println("Type: " + test3.getType());
//
//        for(int i = 0; i < test3.getPath().size(); ++i)
//        {
//            System.out.println(test3.getPath().get(i));
//        }
//
//        System.out.println(test3.getPath().getFirst());
//
//        for(int i = 0; i < test3.getPath().size(); ++i)
//        {
//            System.out.println(test3.getPath().get(i));
//        }
//
//        byte[] TEST = new byte[] {'[','B','@','6','4','9','d','f','6','e','3'};
//
//        PayloadMessage yep = null;
//        try
//        {
//        yep = new PayloadMessage(TEST);
//        }
//        catch(MessageTypeException mte)
//        {
//            mte.printStackTrace();
//        }
//
//        System.out.println("IP: " + test3.getIpAddress());
//        System.out.println("Port: " + test3.getPort());
//        System.out.println("ID: " + test3.getID());
//        System.out.println("Type: " + test3.getType());
//
//        for(int i = 0; i < test3.getPath().size(); ++i)
//        {
//            System.out.println(test3.getPath().get(i));
//        }






    }

    public LinkedList<String> getPath()
    {
        return _path;
    }

    public int getPayload()
    {
        return _payload;
    }

    public void setPayload(int payload)
    {
        _payload = payload;
    }
}
