package client;

import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.BindingProvider;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.example.contract.doubleit.DoubleItPortType;
import org.example.contract.doubleit.DoubleItService;

public class WSClient {
   
    public static void main(String[] args) {
        DoubleItService service = new DoubleItService();
        DoubleItPortType port = service.getDoubleItPort();

        Map outProps = new HashMap();
        Client client = org.apache.cxf.frontend.ClientProxy.getClient(port);
        Endpoint cxfEndpoint = client.getEndpoint();

        // WS-SecurityPolicy method
        Map ctx = ((BindingProvider)port).getRequestContext();
//        ctx.put("ws-security.username", "joe");
//        ctx.put("ws-security.password", "joespassword");

        doubleIt(port, 10);
        doubleIt(port, 0);
        doubleIt(port, -10);
    }

    public static void doubleIt(DoubleItPortType port, int numToDouble) {
        int resp = port.doubleIt(numToDouble);
        System.out.println("The number " + numToDouble + " doubled is "
                + resp);
    }
}

