package client;

import org.example.contract.doubleit.DoubleItPortType;
import org.example.contract.doubleit.DoubleItService;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.common.gzip.GZIPOutInterceptor;
import javax.xml.ws.BindingProvider;

public class WSClient {
    public static void main (String[] args) {
        DoubleItService service = new DoubleItService();
        DoubleItPortType port = service.getDoubleItPort();           
        BindingProvider portBP = (BindingProvider) port;
        // If calling a Metro web service, uncomment below line 
        // to turn off compression of SOAP request
        // portBP.getRequestContext().put(
        //    "org.apache.cxf.transport.common.gzip.GZIPOutInterceptor.useGzip", "NO");

        Client client = ClientProxy.getClient(port);
        GZIPOutInterceptor gzipOut = new GZIPOutInterceptor(0);

        client.getInInterceptors().add(new org.apache.cxf.transport.common.gzip.GZIPInInterceptor());
        client.getOutInterceptors().add(gzipOut); 

        doubleIt(port, 10);
        doubleIt(port, 15);
        doubleIt(port, -10);
    } 
    
    public static void doubleIt(DoubleItPortType port, 
            int numToDouble) {
        int resp = port.doubleIt(numToDouble);
        System.out.println("The number " + numToDouble + " doubled is " 
            + resp);
    }
}

