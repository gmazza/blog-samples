package client;

import org.example.contract.doubleit.DoubleItPortType;
import org.example.contract.doubleit.DoubleItService;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.common.gzip.GZIPOutInterceptor;

public class WSClient {
    public static void main (String[] args) {
        DoubleItService service = new DoubleItService();
        DoubleItPortType port = service.getDoubleItPort();           

        Client client = ClientProxy.getClient(port);
        GZIPOutInterceptor gzipOut = new GZIPOutInterceptor(0);

        client.getInInterceptors().add(new org.apache.cxf.transport.common.gzip.GZIPInInterceptor());
        client.getOutInterceptors().add(gzipOut); 

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
