package client;

import org.example.contract.doubleit.DoubleItPortType;
import org.example.contract.doubleit.DoubleItService;
import javax.xml.ws.BindingProvider;

public class WSClient {
    public static void main (String[] args) {
        DoubleItService service = new DoubleItService();
        DoubleItPortType port = service.getDoubleItPort();           

        BindingProvider portBP = (BindingProvider) port;
        String urlUsed = (String) portBP.getRequestContext().
            get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
        System.out.println("Using URL: " + urlUsed);
        if (!"https://".equals(urlUsed.substring(0, 8))) {
            throw new IllegalStateException("Endpoint URL must use HTTPS!");
        }
                        
        portBP.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "bob");
        portBP.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "trombone");

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
