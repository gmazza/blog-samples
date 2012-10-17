package client;

import org.example.contract.doubleit.DoubleItPortType;
import org.example.contract.doubleit.DoubleItService;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;


public class WSClient {
    public static void main (String[] args) {
        DoubleItService service = new DoubleItService();
        DoubleItPortType port = service.getDoubleItPort();           

        Client client = ClientProxy.getClient(port);
        client.getInInterceptors().add(new org.apache.cxf.interceptor.FIStaxInInterceptor());
        client.getOutInterceptors().add(new org.apache.cxf.interceptor.FIStaxOutInterceptor()); 

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
