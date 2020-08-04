/*
 * PodSender.java
 *
 * Version:
 * 1.0
 *
 *Revision:
 *
 */

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 *
 * This program sends multicast message to neighboring pods in the networks every 5 seconds
 * The message contains the routing table of current pod
 *
 * Code referred from https://github.com/ProfFryer/MulticastTestingEnvironment by Professor Sam Fryer
 * edited by     Anushree Das
 *
 */

// Podsender class implements Runnable class to be able to create threads
public class PodSender implements Runnable  {

    public int port = 63001; // port to send on
    public String multicastAddress; // multicast address to send on
    public int pod = 0; // the current pod number
    RoutingTable routingTable; // routing table for current pod


    /**
     * Class constructor with 4 parameters
     *
     * @param portNum       integer value of port number
     * @param multicastIp   multicast IP address as String
     * @param podNum        number of current pod
     * @param rt            routing table for current pod
     */
    public PodSender(int portNum, String multicastIp, int podNum,RoutingTable rt)
    {
        port = portNum;
        multicastAddress = multicastIp;
        pod = podNum;
        routingTable = rt;
    }

    /**
     * Create a new packet with the current pod's routing table in it
     * and multicast it
     *
     * @param command       RIP packet command field value
     * @throws IOException
     */
    public void sendUdpMessage(int command) throws IOException {
        // Create socket for multicasting
        DatagramSocket socket = new DatagramSocket();
        // multicast address
        InetAddress group = InetAddress.getByName(multicastAddress);

        // get total number of enries in routing table
        int size = routingTable.routes.size();
        // initialize array to store IP adresses from routing table entries
        InetAddress[] ipaddress = new InetAddress[size];
        // initialize array to store subnet masks from routing table entries
        InetAddress[] subnet = new InetAddress[size];
        // initialize array to store next hop IP adresses from routing table entries
        InetAddress[] nexthop = new InetAddress[size];
        // initialize array to store metric values from routing table entries
        int[] metric = new int[size];

        // loop throught all route entries in the routing table and
        // store details from routing table entries to initialized array above
        for (int i = 0; i < size; i++) {
            RouteVector temp = routingTable.routes.get(i);
            ipaddress[i] = temp.IPaddress;
            subnet[i] = temp.SubnetMask;
            nexthop[i] = temp.NextHop;
            metric[i] = temp.Metric;
        }
        // create RIP object
        RIP RIPObject = new RIP(command,ipaddress,subnet,nexthop,metric,size);
        // create new ByteArrayOutputStream
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(5000);
        // create new ObjectOutputStream
        ObjectOutputStream objectStream = new ObjectOutputStream(new BufferedOutputStream(byteStream));
        // convert RIP object to one array of bytes
        objectStream.flush();
        objectStream.writeObject(RIPObject);
        objectStream.flush();
        byte[] sendBuf = byteStream.toByteArray();
        // create new DatagramPacket with that array of bytes
        DatagramPacket packet1 = new DatagramPacket(sendBuf, sendBuf.length,  group, port);
        // send packet
        socket.send(packet1);
        // close socket
        socket.close();
    }


    /**
     * Override run method from Runnable class
     *
     *  Send packets with current pods's routing table to neighboring pods every 5 sec
     */
    @Override
    public void run(){
        try{

            // loop forever
            while (true)
            {
                try {
                    // remove nodes which are expired
                    routingTable.removeRoute();
                    // send multicast message
                    sendUdpMessage(1);
                    // wait 5 seconds
                    Thread.sleep(5000);
                }catch(Exception ex){
                    // print exception
                    ex.printStackTrace();
                }
            }
        }
        // catch and print exception
        catch (Exception e){ e.printStackTrace();}
    }
}
