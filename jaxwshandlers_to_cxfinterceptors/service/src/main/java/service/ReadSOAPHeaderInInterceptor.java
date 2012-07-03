package service;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.w3c.dom.NodeList;

public class ReadSOAPHeaderInInterceptor extends AbstractSoapInterceptor { 

    public ReadSOAPHeaderInInterceptor() {
        super(Phase.USER_PROTOCOL);
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        SOAPMessage sm = message.getContent(SOAPMessage.class);

        try {
            SOAPHeader sh = sm.getSOAPHeader();
            /* Note in real use validity checking should be done
               (really two terms present? namespaces? etc.) */
            NodeList termNodes = sh.getElementsByTagName("term");
            message.put("termOne", termNodes.item(0).getTextContent());
            message.put("termTwo", termNodes.item(1).getTextContent());
            /* JAX-WS Handler "setScope()" (HANDLER/APPLICATION) 
               not available with interceptors, APPLICATION is standard
               meaning both properties readable by service bean */
        } catch (SOAPException e) {
            throw new Fault(e);
        } 
    }
}

