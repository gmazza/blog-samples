package service;

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.NodeList;

public class SOAPHandler implements javax.xml.ws.handler.soap.SOAPHandler<SOAPMessageContext> {

   @Override
   public Set<QName> getHeaders() {
      return null;
   }

   @Override
   public void close(MessageContext mc) {
   }

   @Override
   public boolean handleFault(SOAPMessageContext mc) {
      return true;
   }

   @Override
   public boolean handleMessage(SOAPMessageContext mc) {
      HandlerUtils.printMessageContext("Service SOAPHandler", mc);
      if (Boolean.FALSE.equals(mc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY))) {
         SOAPMessage sm = mc.getMessage();

         try {
            SOAPHeader sh = sm.getSOAPHeader();

            // Note in real use validity checking should be done
            // (really two terms present? namespaces? etc.)
            NodeList termNodes = sh.getElementsByTagName("term");
            mc.put("termOne", termNodes.item(0).getTextContent());
            mc.put("termTwo", termNodes.item(1).getTextContent());
            // default scope is HANDLER (i.e., not readable by SEI
            // implementation)
            mc.setScope("termTwo", MessageContext.Scope.APPLICATION);
         } catch (SOAPException e) {
            throw new ProtocolException(e);
         }
      }
      return true;
   }
}

