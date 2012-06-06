package client;

import org.example.contract.doubleit.DoubleItPortType;
import org.example.contract.doubleit.DoubleItService;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.soap.SOAPFaultException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WSClient {
    public static void main (String[] args) {
        DoubleItService service = new DoubleItService();
        DoubleItPortType port = service.getDoubleItPort();

        ClientSAMLHandler csh = new ClientSAMLHandler();
        List<Handler> handlerChain = new ArrayList<Handler>();
        handlerChain.add(csh);           
        ((BindingProvider) port).getBinding().setHandlerChain(handlerChain);

        doubleIt(port, 10);
        doubleIt(port, 7);
        doubleIt(port, -10);
    } 
    
    public static void doubleIt(DoubleItPortType port, 
            int numToDouble) {
        try {
           int resp = port.doubleIt(numToDouble);
           System.out.println("The number " + numToDouble + " doubled is " + resp);
        } catch (ProtocolException e) { 
           System.out.println("SOAP Call Error: " + e.getMessage());
        }
    }
}
