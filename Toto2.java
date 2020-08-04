/*
 * Toto2.java
 *
 * Version:
 * 1.0
 *
 *Revision:
 *
 */

import java.net.InetAddress;

/**
 *
 * This program creates a pod which multicasts it's routing table and listens for messages from other pods
 *
 * Code originally from https://github.com/ProfFryer/MulticastTestingEnvironment by Professor Sam Fryer
 * edited by     Anushree Das
 *
 */
public class Toto2 {
    static RoutingTable routingTable = new RoutingTable();  // routing table for current pod

    // main program
    public static void main(String args[])
    {
        if (args.length>0) {
            // get pod number from command line arguments
            int podNo = Integer.parseInt(args[0]);
            // printing pod number
            System.out.println("######### TOTO 2 ############");
            System.out.println("Pod number: " + podNo);

            try
            {
                // IP address of the router to which pod is connected
                InetAddress localhost = InetAddress.getLocalHost();
                // Print IP address of the router to which pod is connected
                String address = (localhost.getHostAddress()).trim();
                System.out.println("Pod accessed by router: "+address);
                // add new route entry for current pod for itself with cost as 0
                // current pod's IP address
                InetAddress self = InetAddress.getByName("10.0."+podNo+".0");
                // router IP adress to which the pod is connected to
                InetAddress networkip = InetAddress.getLocalHost();
                // Subnet for pod's IP address
                InetAddress sub = InetAddress.getByName("255.255.0.0");
                // Cost to pod
                routingTable.addRoute(self,sub,networkip,-1);

                // Starting new thread to listen for multicast messages from other pods current pod is connected to
                Thread client=new Thread(new PodListner(63001,"230.230.230.230",routingTable));
                client.start();

                // Starting new thread to send multicast messages to other pods from current pod
                Thread sender=new Thread(new PodSender(63001,"230.230.230.230",podNo,routingTable));
                sender.start();

                // loop to keep current program from terminating
                while(true){Thread.sleep(1000);}
            }
            // catch exceptions
            catch(Exception ex)
            {
                // print exception
                ex.printStackTrace();
            }
        }
        // if no arguments in commandline arguments
        else
            // print message
            System.out.println("Please enter Pod Number as commandline argument");
    }
}
