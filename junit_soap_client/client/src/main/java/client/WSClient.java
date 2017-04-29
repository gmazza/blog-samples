package client;

import org.example.contract.doubleit.DoubleItPortType;
import org.example.contract.doubleit.DoubleItService;

public class WSClient {

    public WSClient() {
    }

    public static void main (String[] args) {
        DoubleItService service = new DoubleItService();
        DoubleItPortType port = service.getDoubleItPort();
        System.out.println(doubleItMessage(port, 10));
        System.out.println(doubleItMessage(port, 0));
        System.out.println(doubleItMessage(port, -10));
    }

    public static String doubleItMessage(DoubleItPortType port, int numToDouble) {
        int resp = doubleIt(port, numToDouble);
        return "The number " + numToDouble + " doubled is " + resp;
    }

    public static int doubleIt(DoubleItPortType port, int numToDouble) {
        return port.doubleIt(numToDouble);
    }
}
