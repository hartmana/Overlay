package cs455.overlay.util;

import cs455.overlay.wireformats.TrafficSummary;

import java.util.HashMap;

public class StatisticsCollectorAndDisplay
{

    /**
     * Hash map to hold all of the TRAFFIC_SUMMARY messages from MessagingNode's
     */
    private HashMap<String, TrafficSummary> _trafficSummaryMap;

    /**
     * int for total number of messages sent by the overlay
     */
    private int _totalSent;

    /**
     * int for the total number of messages received by the overlay
     */
    private int _totalReceived;

    /**
     * long for the total summation sent by the overlay
     */
    private long _totalSentSummation;

    /**
     * long for the total summation received by the overlay
     */
    private long _totalReceivedSummation;


    public StatisticsCollectorAndDisplay()
    {
        _trafficSummaryMap = new HashMap<String, TrafficSummary>();
    }

    /**
     * Method to add a TRAFFIC_SUMMARY message to the collector and update
     * the statistics.
     *
     * @param trafficSummary <code>TrafficSummary</code> message to be added to the Collector.
     */
    public void add(TrafficSummary trafficSummary)
    {
        _totalSent += trafficSummary.getNumSent();
        _totalReceived += trafficSummary.getNumReceived();
        _totalSentSummation += trafficSummary.getSentSummation();
        _totalReceivedSummation += trafficSummary.getReceivedSummation();

        _trafficSummaryMap.put(trafficSummary.getID(), trafficSummary);
    }

    /**
     * Method to print all of the TRAFFIC_SUMMARY information this class
     * has collected.
     */
    public void print()
    {
        /**
         * Object array of the keys to our message hashmap
         */
        Object[] keys = _trafficSummaryMap.keySet().toArray();

        /**
         * String for the current node ID
         */
        String nodeID;

        /**
         * TrafficSummary of the current node
         */
        TrafficSummary trafficSummary;

        System.out.println("\t\tNumber of Messages Sent\t\tNumber of Messages Received\t\tSummation of Sent " +
                "Messages\t\tSummation of Received Messages\t\tNumber of Messages Relayed");

        // FOR every message in the map
        for (int i = 0; i < keys.length; ++i)
        {
            nodeID = keys[i].toString();
            trafficSummary = _trafficSummaryMap.get(nodeID);

            System.out.print("Node " + (i + 1) + "\t\t");
            System.out.print(trafficSummary.getNumSent());
            System.out.print("\t\t" + trafficSummary.getNumReceived());
            System.out.print("\t\t" + trafficSummary.getSentSummation());
            System.out.print("\t\t" + trafficSummary.getReceivedSummation());
            System.out.print("\t\t" + trafficSummary.getNumRelayed());
            System.out.println();
        }

        System.out.print("Sum\t\t" + _totalSent);
        System.out.print("\t\t" + _totalReceived);
        System.out.print("\t\t" + _totalSentSummation);
        System.out.print("\t\t" + _totalReceivedSummation);
        System.out.println();
    }


    /**
     * Method to return the number of messages we've collected
     *
     * @return <code>int</code> denoting the number of messages we've collected from nodes.
     */
    public int getTotalCollected()
    {
        return _trafficSummaryMap.size();
    }

    /**
     * Method to clear the statistics for this object
     */
    public void clear()
    {
        _totalSent = 0;
        _totalSentSummation = 0;
        _totalReceived = 0;
        _totalReceivedSummation = 0;

        _trafficSummaryMap.clear();
    }
}
