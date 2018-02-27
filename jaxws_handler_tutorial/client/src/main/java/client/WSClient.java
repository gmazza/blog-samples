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

   @SuppressWarnings("unchecked")
   public static void main(String[] args) {
      DoubleItService service = new DoubleItService();
      DoubleItPortType port = service.getDoubleItPort();

      // add Client-side handlers
      ClientHandlers.LogicalHandler lh = new ClientHandlers.LogicalHandler();
      ClientHandlers.SOAPHandler sh = new ClientHandlers.SOAPHandler();

      List<Handler> handlerChain = new ArrayList<Handler>();
      handlerChain.add(lh);
      handlerChain.add(sh);
      ((BindingProvider) port).getBinding().setHandlerChain(handlerChain);

      // add MAX_VALUE to message context, for client LogicalHandler to use
      ((BindingProvider) port).getRequestContext().put("MAX_VALUE", "200");

      // normal case
      doubleIt(port, 100, ((BindingProvider) port).getRequestContext());

      // client LogicalHandler will reduce to MAX_VALUE of 200 (note 
      // answer of 400 will be wrong as a result)
      doubleIt(port, 300, null);

      // client LogicalHandler will raise "can't double 20"
      // SOAPFaultException
      doubleIt(port, 20, null);

      // service LogicalHandler will raise "can't double 30"
      // SOAPFaultException
      doubleIt(port, 30, null);
   }

   public static void doubleIt(DoubleItPortType port, int numToDouble,
         Map<String, Object> propertyMap) {

      if (propertyMap != null)
         HandlerUtils.printMessageContext("SOAP Client", propertyMap);

      try {
         int resp = port.doubleIt(numToDouble);
         System.out.println("The number " + numToDouble + " doubled is " + resp);
      } catch (ProtocolException e) {
         System.out.println("SOAP Call Error: " + e.getMessage());
      }
   }
}
