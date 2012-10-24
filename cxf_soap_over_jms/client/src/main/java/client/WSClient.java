package client;

import org.example.contract.doubleit.DoubleItPortType;
import org.example.contract.doubleit.DoubleItService;
import java.io.Closeable;

public class WSClient {
    public static void main (String[] args) throws Exception {
        DoubleItService service = new DoubleItService();
        DoubleItPortType port = service.getDoubleItPort();           

        doubleIt(port, 10);
        doubleIt(port, 0);
        doubleIt(port, -10);
        
        // ActiveMQ broker will give EOFException warning
        // if client exits without port being closed
        if (port instanceof Closeable) {
            ((Closeable)port).close();
        }
    } 
    
    public static void doubleIt(DoubleItPortType port, 
            int numToDouble) {
        System.out.println("Attempting to double: " + numToDouble);
        int resp = port.doubleIt(numToDouble);
        System.out.println("The number " + numToDouble + " doubled is " 
            + resp);
    }
}

