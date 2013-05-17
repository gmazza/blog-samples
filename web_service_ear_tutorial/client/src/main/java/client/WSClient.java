package client;

import org.example.contract.doubleit.DoubleItPortType;
import org.example.contract.doubleit.DoubleItService;

public class WSClient {

    public WSClient() {
    }

    // used by OSGi-hosted client
    public WSClient(DoubleItPortType portType) {
        makeCalls(portType);
    }

    // used by standalone Java client
    public static void main (String[] args) {
        DoubleItService service = new DoubleItService();
        DoubleItPortType port = service.getDoubleItPort();           
        makeCalls(port);
    } 
    
    private static void makeCalls(DoubleItPortType port) {
        doubleIt(port, 10);
        doubleIt(port, 0);
        doubleIt(port, -10);
    }

    public static void doubleIt(DoubleItPortType port, 
            int numToDouble) {
        int resp = port.doubleIt(numToDouble);
        System.out.println("The number " + numToDouble + " doubled is " 
            + resp);
    }
}
