package client;

import java.util.List;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;

import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import org.apache.cxf.message.Message;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapOutInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.example.schema.doubleit.DoubleIt;

public class ClientInterceptors {

    public static class ValueCheckOutInterceptor extends AbstractPhaseInterceptor<Message> {

        public ValueCheckOutInterceptor() {
            super(Phase.PRE_PROTOCOL);
        }

        @SuppressWarnings("unchecked")
        public void handleMessage(Message message) throws Fault {
            List<Object> myList = message.getContent(List.class);
            Integer maxValue = new Integer((String) message.get("MAX_VALUE"));

            for (Object item : myList) {
                if (item instanceof DoubleIt) {
                    DoubleIt req = (DoubleIt) item;
                    if (req.getNumberToDouble() > maxValue.intValue()) {
                        req.setNumberToDouble(maxValue.intValue());
                    }
                    if (req.getNumberToDouble() == 20) {
                        throw new Fault(
                                new Exception(
                                        "Doubling 20 is not allowed by the SOAP client."));
                    }
                }
            }
        }
    }

    public static class AddSOAPHeaderOutInterceptor extends AbstractSoapInterceptor { 

        public AddSOAPHeaderOutInterceptor() {
            super(Phase.WRITE);
            this.addAfter(SoapOutInterceptor.class.getName());
        }

        @Override
        public void handleMessage(SoapMessage message) throws Fault {
            SOAPMessage sm = message.getContent(SOAPMessage.class);

            try {
                SOAPFactory sf = SOAPFactory.newInstance();
                SOAPHeader sh = sm.getSOAPHeader();
                if (sh == null) {
                    sh = sm.getSOAPPart().getEnvelope().addHeader();
                }

                Name twoTermName = sf.createName("TwoTerms", "samp", "http://www.example.org");
                SOAPHeaderElement shElement = sh
                        .addHeaderElement(twoTermName);
                SOAPElement firstTerm = shElement.addChildElement("term");
                firstTerm.addTextNode("Apple");
                shElement.addChildElement(firstTerm);
                SOAPElement secondTerm = shElement.addChildElement("term");
                secondTerm.addTextNode("Orange");
                shElement.addChildElement(secondTerm);
            } catch (SOAPException e) {
                throw new Fault(e);
            } 
        }
    }
}

