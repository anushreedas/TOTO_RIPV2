/*
 * RoutingTable.java
 *
 * Version:
 * 1.0
 *
 *Revision:
 *
 */
import java.net.InetAddress;
import java.time.LocalTime;
import java.util.ArrayList;
/**
 *
 * This program creates RIP packet
 *
 * @author  Anushree Das
 *
 */

// Route Vector for each pod
class RouteVector{

    InetAddress IPaddress;  // IP address of pod

    InetAddress SubnetMask; // Subnet Mask of pod

    InetAddress NextHop;    // IP address of Next hop for pod

    int Metric;             // cost to pod

    LocalTime EntryTime;    // pod reply time

    /**
     * class constructor with 4 pararmeters
     *
     * @param ip        IP address of pod
     * @param subnet    Subnet Mask of pod
     * @param next      IP address of Next hop for pod
     * @param m         cost to pod
     */
    RouteVector(InetAddress ip, InetAddress subnet, InetAddress next, int m){
        IPaddress = ip;
        SubnetMask = subnet;
        NextHop = next;
        Metric = m;
        // current time
        EntryTime = LocalTime.now();
    }
}

// stores all routing vectors
public class RoutingTable {

    ArrayList<RouteVector> routes = new ArrayList<RouteVector>();   // list of route vector

    /**
     * Convertes network mask to CIDR
     *
     * @param netmask    network mask to be converted
     * @return  CIDR representaion of  network mask
     */
    public int convertToCIDR(InetAddress subnet){
        // get byte array of subnet
        byte[] maskInBytes = subnet.getAddress();
        // initialize cidr number to 0
        int cidr = 0;

        boolean flag = false;
        // loop through byte array of subnet
        for(byte b : maskInBytes){
            // intialize n to 128 in hexadecimal, maximum value possible for cidr
            // binary representation 11111111
            int n = 0x80;

            // loop through each bit in each byte from byte array of subnet
            for(int i = 0; i < 8; i++){
                // perform bitwise and operation on each bit from byte from byte array of subnet
                // with n and get integer value
                int result = b & n;
                // if result is 0
                if(result == 0){
                    // set zero flag true to throw exception
                    flag = true;
                }else if(flag){
                    // if result is 0 throw exception
                    throw new IllegalArgumentException("Invalid netmask.");
                } else {
                    // increase cidr by 1
                    cidr++;
                }
                // right shift n
                // binary representation for first iteration 01111111
                n >>>= 1;
            }
        }
        // return cidr
        return cidr;
    }

    /**
     * Print Route Table
     */
    void printRouteTable(){
        System.out.println("###########Routing Table###########");
        System.out.println("Address\t\tNextHop\t\tCost");
        System.out.println("------------------------------------");
        for (int i = 0; i < routes.size(); i++) {
            RouteVector temp = routes.get(i);
            int subnet=0;
            System.out.println(temp.IPaddress.getHostAddress()+"\\"+convertToCIDR(temp.SubnetMask)+"\t"+temp.NextHop.getHostAddress()+"\t"+temp.Metric);
        }
    }

    /**
     * Add route to table
     *
     * @param ip        IP address of pod
     * @param subnet    Subnet Mask of pod
     * @param next      IP address of Next hop for pod
     * @param m         cost to pod
     */
    void addRoute(InetAddress ip, InetAddress subnet, InetAddress next, int m){
        // The route vector is for sender pod
        // so cost will increase by 1 for current pod
        m++;
        // If cost is greater than 16 keep it 16
        if(m>16)
            m=16;

        // flag for knowing if the route already exits
        boolean exists = false;
        // if routing table is empty add new route
        if(routes.size()==0){
            RouteVector temp = new RouteVector(ip,subnet,next,m);
            routes.add(temp);
            // print routing table
            printRouteTable();
        }
        // if routing table is not empty
        else{
            // loop through all routes
            for (int i = 0; i < routes.size(); i++) {
                // table needs to updated or not
                boolean updated = false;
                RouteVector temp = routes.get(i);
                // if pod ip address is already present in table
                if(temp.IPaddress.equals(ip) && temp.SubnetMask.equals(subnet)) {
//                    route already exits
                    exists = true;
                    // update time for neighbor
                    if(m == 1){
                        temp.EntryTime = LocalTime.now();
                        routes.set(i,temp);
                    }
                    // if next hop is same and pod ip address isn't same as current pod and cost has changed
                    // update route
                    if (temp.NextHop.equals(next) && temp.Metric!= 1 && temp.Metric != m){
                        temp.Metric = m;
                        updated = true;
                    }
                    // if next hop is different and cost is less
                    // update route
                    else if(!(temp.NextHop.equals(next)) && m<temp.Metric){
                        temp.NextHop = next;
                        temp.Metric = m;
                        updated = true;
                    }
                }
                // if table needs to be updated
                if(updated == true){
                    // set new time
                    temp.EntryTime = LocalTime.now();
                    // update route
                    routes.set(i,temp);
                    // print routing table
                    printRouteTable();
                    updated = false;
                }
            }
            // if route doesn't already exist add new route vector to table
            if(exists == false){
                RouteVector temp = new RouteVector(ip,subnet,next,m);
                routes.add(temp);
                // print routing table
                printRouteTable();
            }
        }
    }

    /**
     * Remove route from table
     */
    void removeRoute(){
        // loop through all routes
        for (int i = 0; i < routes.size(); i++) {
            RouteVector temp = routes.get(i);
            // if route entry's last time entered is more than 15 seconds older
            // then set cost to 16
            if(temp.Metric==1 && temp.EntryTime.plusSeconds(15).compareTo(LocalTime.now())<0){
                temp.Metric = 16;
                temp.EntryTime = LocalTime.now();
                routes.set(i,temp);
                // loop through all routes
                for (int j = 0; j < routes.size(); j++) {
                    RouteVector temp2 = routes.get(j);
                    // if routes next hop is the route whose metric was just set 16
                    // then set cost to 16
                    if(temp2.NextHop.equals(temp.NextHop)){
                        temp2.Metric = 16;
                        temp2.EntryTime = LocalTime.now();
                        routes.set(j,temp2);
                    }
                }
                // print rout table
                printRouteTable();
            }
        }
    }

}
