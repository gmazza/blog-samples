package client;

import org.example.contract.doubleit.DoubleItPortType;
import org.example.contract.doubleit.DoubleItService;
import javax.xml.ws.BindingProvider;
import java.util.Map;
import com.sun.xml.ws.developer.JAXWSProperties;


public class WSClient {
    public static void main (String[] args) {
        DoubleItService service = new DoubleItService();
        DoubleItPortType port = service.getDoubleItPort();           

        BindingProvider bp = (BindingProvider) port;
        Map<String, Object> ctxt = bp.getRequestContext();
        ctxt.put("com.sun.xml.ws.client.ContentNegotiation", "pessimistic");

        doubleIt(port, 10);
        doubleIt(port, 15);
//        doubleIt(port, -10);
    } 
    
    public static void doubleIt(DoubleItPortType port, 
            int numToDouble) {
        int resp = port.doubleIt(numToDouble);
        System.out.println("The number " + numToDouble + " doubled is " 
            + resp);
    }
}
