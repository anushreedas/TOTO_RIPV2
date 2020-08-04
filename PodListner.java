/*
 * PodSender.java
 *
 * Version:
 * 1.0
 *
 *Revision:
 *
 */

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.net.*;
import java.io.*;

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
public class PodListner implements Runnable {

    public int port = 63001; // port to listen on
    public String multicastAddress; // multicast address to send on
    RoutingTable routingTable;  // routing table for current pod

    /**
     * Class constructor with 4 parameters
     *
     * @param portNum       integer value of port number
     * @param multicastIp   multicast IP address as String
     * @param rt            routing table for current pod
     */
    public PodListner(int portNum, String multicastIp, RoutingTable rt)
    {
        port = portNum;
        multicastAddress = multicastIp;
        routingTable = rt;
    }

    /**
     * Update current pod's routing table
     *
     * @param packet            RIP Packet received
     * @param packetFrom        IP address of router from which packet was received
     * @throws UnknownHostException
     */
    void updateRoutingTable(RIP packet, InetAddress packetFrom) throws UnknownHostException {
        // loop through all rout entries in the route table present in the packet received
        for(int i = 0; i < packet.RIPEntries.length; i++ ){
            // add each route information to current pod's routing table accordingly
            InetAddress ip = InetAddress.getByAddress(packet.RIPEntries[i].IPaddress);
            InetAddress subnet = InetAddress.getByAddress(packet.RIPEntries[i].SubnetMask);
            InetAddress next = packetFrom;
            int metric = new BigInteger(1, packet.RIPEntries[i].Metric).intValue();
            routingTable.addRoute(ip,subnet,next,metric);
        }
    }

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
     * Recieve packet and update current pods's routing table accordingly
     *
     * @throws IOException
     */
    public void receiveUDPMessage() throws
            IOException {

        // Create socket to recieve multicast packets
        MulticastSocket socket=new MulticastSocket(port);
        InetAddress group=InetAddress.getByName(multicastAddress);
        socket.joinGroup(group);

        // loop forever
        while(true){
            try {

//                routingTable.removeRoute();

                // Create DatagramPacket to store received packet
                byte[] recvBuf = new byte[5000];
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                // receive packet
                socket.receive(packet);
                byte[] data = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());
                // if packet is received from neighboring pods and not current pod the process packet
                if(!packet.getAddress().equals(InetAddress.getLocalHost()))
                {
                    // create new ByteArrayOutputStream
                    ByteArrayInputStream byteStream = new ByteArrayInputStream(recvBuf);
                    // create new ObjectInputStream
                    ObjectInputStream objectStream = new ObjectInputStream(new BufferedInputStream(byteStream));
                    // convert array of bytes recieved to RIP object
                    RIP o = (RIP) objectStream.readObject();

                    //if RIP command  is request then send response
                    if(new BigInteger(1, o.Command).intValue() == 1)
                        sendUdpMessage(2);
                    //else update routing table
                    else  if(new BigInteger(1, o.Command).intValue()==2)
                    // update current pod's routing table
                        updateRoutingTable(o,packet.getAddress());
                }
            }catch(Exception ex){
                // print exception
                ex.printStackTrace();
            }

            // dummy exit
            if(false) break;
        }

        //close socket
        socket.leaveGroup(group);
        socket.close();
    }

    /**
     * Override run method from Runnable class
     *
     * Recieve packets from neighboring pods and update current pods's routing table accordingly
     */
    @Override
    public void run(){
        try {
            // process received packets
            receiveUDPMessage();
        }catch(IOException ex){
            // print exception
            ex.printStackTrace();
        }
    }
}
