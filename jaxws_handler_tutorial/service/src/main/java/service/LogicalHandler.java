package service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

import org.example.schema.doubleit.DoubleIt;
import org.example.schema.doubleit.ObjectFactory;

public class LogicalHandler implements javax.xml.ws.handler.LogicalHandler<LogicalMessageContext> {

   @Override
   public void close(MessageContext mc) {
   }

   @Override
   public boolean handleFault(LogicalMessageContext messagecontext) {
      return true;
   }

   @Override
   public boolean handleMessage(LogicalMessageContext mc) {
      HandlerUtils.printMessageContext("Service LogicalHandler", mc);
      if (Boolean.FALSE.equals(mc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY))) {
         try {
            LogicalMessage msg = mc.getMessage();
            JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
            Object payload = msg.getPayload(jaxbContext);
            if (payload instanceof DoubleIt) {
               DoubleIt req = (DoubleIt) payload;
               if (req.getNumberToDouble() == 30) {
                  throw new ProtocolException(
                        "Doubling 30 is not allowed by the web service provider.");
               }
            }
         } catch (JAXBException ex) {
            throw new ProtocolException(ex);
         }
      }
      return true;
   }

}

