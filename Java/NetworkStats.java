package Java;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class NetworkStats {
    public static void main(String[] args) {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                System.out.println("Interface Name: " + ni.getName());
                System.out.println("Bytes Sent: " + ni.getStatistcs().getTxBytes());
                System.out.println("Bytes Received: " + ni.getStatistcs().getRxBytes());
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}