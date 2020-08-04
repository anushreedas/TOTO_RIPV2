/*
 * RIP.java
 *
 * Version:
 * 1.0
 *
 *Revision:
 *
 */
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 *
 * This program creates RIP packet
 *
 * @author  Anushree Das
 *
 */

// this class implements serializable
class RIPEntry implements Serializable{
    byte[] AddressFamilyIdentifier =  new byte[2];  // AddressFamilyIdentifier field

    byte[] RouteTag = new byte[2];                  // RouteTag field

    byte[] IPaddress = new byte[4];                 // IPaddress field

    byte[] SubnetMask = new byte[4];                // SubnetMask field

    byte[] NextHop = new byte[4];                   // NextHop field

    byte[] Metric = new byte[4];                    // Metric field

    /**
     * converts integer to array of bytes of n length
     * @param integer   integer to be converted
     * @param n         length of byte array
     * @return          array of bytes
     */
    byte[] convertToBytes(int integer, int n){

        // new array of bytes to convert integer to byte array
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(integer & 0xFF);
        byte[] temp = bb.array();
        //new array of bytes of length n to store previous array
        ByteBuffer b2 = ByteBuffer.allocate(n);
        b2.put(temp,4-n,n);
        //return new array
        return b2.array();
    }

    /**
     * converts array of integer to array of bytes of n length
     * @param integer   array of integer to be converted
     * @param n         length of byte array
     * @return          array of bytes
     */
    byte[] convertArrayToBytes(int integer[], int n){
        // new array of bytes to convert integer array to byte array
        byte[] bytes = new byte[n];

        // convert integer at each index to byte and store in bytes array at same index
        for(int i = 0; i < n; i++){
            BigInteger bigInt = BigInteger.valueOf(integer[i]);
            bytes[i] = (byte)(integer[i] & 0xFF);
        }

        //return new array
        return bytes;
    }

    /**
     * Class constructor with 4 parameters
     *
     * @param ipaddress     IP address of pod
     * @param subnetMask    SubnetMask of pod
     * @param nextHop       IP address of Next Hop
     * @param metric        Cost to router
     */
    RIPEntry(InetAddress ipaddress, InetAddress subnetMask, InetAddress nextHop, int metric){
        AddressFamilyIdentifier = convertToBytes(2,2);
        RouteTag = convertToBytes(0,2);
        IPaddress = ipaddress.getAddress();
        SubnetMask = subnetMask.getAddress();
        NextHop = nextHop.getAddress();
        Metric = convertToBytes(metric,4);
    }
}

// this class implements serializable
public class RIP implements Serializable {

    byte[] Command = new byte[1];   // Command field

    byte[] Version = new byte [1];  // Version field

    byte[] Unused = new byte[2];    // Unused field

    RIPEntry[] RIPEntries;          // array of route entries

    /**
     * converts integer to array of bytes of n length
     * @param integer   integer to be converted
     * @param n         length of byte array
     * @return          array of bytes
     */
    byte[] convertToBytes(int integer, int n){
        // new array of bytes to convert integer to byte array
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(integer & 0xFF);
        //new array of bytes of length n to store previous array
        byte[] temp = bb.array();
        ByteBuffer b2 = ByteBuffer.allocate(n);
        b2.put(temp,4-n,n);
        //return new array
        return b2.array();
    }

    /**
     * Class constructor with 5 parameters
     *
     * @param command       RIP command field
     * @param ipaddress     route entries IP addresses of pods
     * @param subnetMask    route entries subnet mask of pods
     * @param nextHop        route entries IP addresses of nexthop for pods
     * @param metric         route entries cost to pods
     * @param n             number of route entries
     */
    RIP(int command,InetAddress ipaddress[],InetAddress subnetMask[], InetAddress nextHop[], int metric[], int n){
        Command = convertToBytes(command,1);
        Version = convertToBytes(2,1);
        Unused = convertToBytes(0,2);
        RIPEntries = new RIPEntry[n];
        for(int i = 0; i < n; i++ ){
            RIPEntries[i] = new RIPEntry(ipaddress[i],subnetMask[i],nextHop[i],metric[i]);
        }
    }

}
