package service;

import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.example.contract.doubleit.DoubleItPortType;

@WebService(targetNamespace = "http://www.example.org/contract/DoubleIt", 
      portName = "DoubleItPort", serviceName = "DoubleItService", 
      endpointInterface = "org.example.contract.doubleit.DoubleItPortType")
@HandlerChain(file = "/handlers.xml")
public class DoubleItPortTypeImpl implements DoubleItPortType {

   @Resource
   private WebServiceContext context;

   public int doubleIt(int numberToDouble) {
      HandlerUtils.printMessageContext("Web Service Provider", context.getMessageContext());
      // should fail (termOne has HANDLER scope)
      System.out.println("First Word: " + context.getMessageContext().get("termOne"));
      // should succeed (termTwo has APPLICATION scope)
      System.out.println("Second Word: " + context.getMessageContext().get("termTwo"));
      return numberToDouble * 2;
   }
}

