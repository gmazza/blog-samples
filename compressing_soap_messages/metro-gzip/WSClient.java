package client;

import org.example.contract.doubleit.DoubleItPortType;
import org.example.contract.doubleit.DoubleItService;
import javax.xml.ws.BindingProvider;
import java.util.Map;
import java.util.HashMap;
import com.sun.xml.ws.developer.JAXWSProperties;
import java.util.Collections;
import javax.xml.ws.handler.MessageContext;
import java.util.List;

public class WSClient {
    public static void main (String[] args) {
        DoubleItService service = new DoubleItService();
        DoubleItPortType port = service.getDoubleItPort();           

        BindingProvider bp = (BindingProvider) port;

        Map<String, List<String>> httpHeaders = new HashMap<String, List<String>>();
        // send GZIP on SOAP request (CXF WSP's only, Metro WSP's can't handle compression)
        // httpHeaders.put("Content-Encoding", Collections.singletonList("gzip"));
        // receive GZIP on SOAP response
        httpHeaders.put("Accept-Encoding", Collections.singletonList("gzip"));
        Map<String, Object> requestContext = bp.getRequestContext();
        requestContext.put(MessageContext.HTTP_REQUEST_HEADERS, httpHeaders);

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
