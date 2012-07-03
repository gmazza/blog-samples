package client;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;

import org.example.contract.doubleit.DoubleItPortType;
import org.example.contract.doubleit.DoubleItService;

import org.apache.cxf.binding.soap.saaj.SAAJOutInterceptor;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;

public class WSClient {
    public static void main(String[] args) {
        DoubleItService service = new DoubleItService();
        DoubleItPortType port = service.getDoubleItPort();

        Client client = ClientProxy.getClient(port);
        client.getOutInterceptors().add(
                new ClientInterceptors.ValueCheckOutInterceptor());
        client.getOutInterceptors().add(
                new SAAJOutInterceptor());
        client.getOutInterceptors().add(
                new ClientInterceptors.AddSOAPHeaderOutInterceptor());

        // add MAX_VALUE to message context, for client LogicalHandler to use
        ((BindingProvider) port).getRequestContext().put("MAX_VALUE", "200");

        // normal case
        doubleIt(port, 100);

        // ValueCheckOutInterceptor will reduce to MAX_VALUE of 200 (note
        // answer of 400 will be wrong as a result)
        doubleIt(port, 300);

        // ValueCheckOutInterceptor will raise "can't double 20" Fault
        doubleIt(port, 20);

        // service in interceptor will raise "can't double 30" Fault
        doubleIt(port, 30);

    }

    public static void doubleIt(DoubleItPortType port, int numToDouble) {
        try {
            int resp = port.doubleIt(numToDouble);
            System.out.println("The number " + numToDouble + " doubled is "
                    + resp);
        } catch (SOAPFaultException e) {
            System.out.println("SOAP Call Error: " + e.getMessage());
        }
    }
}

