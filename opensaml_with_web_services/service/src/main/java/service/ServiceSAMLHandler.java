package service;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPFaultException;

import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Action;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.AuthzDecisionStatement;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.opensaml.common.xml.SAMLConstants;

/*
 * This sample SOAP Protocol Handler for DoubleIt checks for X.509 authentication,
 * attribute of Math degree, and authorization to double even numbers.
 *
 * WARNING: No actual security being coded here, sample 
 * just shows using OpenSAML to read a SAML token.
 */
public class ServiceSAMLHandler implements SOAPHandler<SOAPMessageContext> {

   // change this to redirect output if desired
   private static PrintStream out = System.out;

   private static String WS_SECURITY_URI =
      "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

   public Set<QName> getHeaders() {
      return null;
   }

   public boolean handleMessage(SOAPMessageContext smc) {
      Boolean outboundProperty = (Boolean) smc
            .get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
      if (!outboundProperty.booleanValue()) {
         Element assertionElement;

         try {
            // check for SOAP Header
            SOAPHeader sh = smc.getMessage().getSOAPHeader();
            if (sh == null) {
               throw createSOAPFaultException("Missing SOAP Header", true);
            }

            // check for wsse:security element under SOAP Header
            Node wsseElement = sh.getFirstChild();
            if (wsseElement == null || !"Security".equals(wsseElement.getLocalName())
                  || !WS_SECURITY_URI.equals(wsseElement.getNamespaceURI())) {
               throw createSOAPFaultException("Missing or invalid WS-Security Header",
                     true);
            }

            // check for SAML assertion under wsse:security element
            assertionElement = (Element) wsseElement.getFirstChild();
            if (assertionElement == null
                  || !"Assertion".equals(assertionElement.getLocalName())
                  || !SAMLConstants.SAML20_NS.equals(assertionElement.getNamespaceURI())) {
               throw createSOAPFaultException("Missing or invalid SAML Assertion", true);
            }

            // Unmarshall SAML Assertion into an OpenSAML Java object.
            DefaultBootstrap.bootstrap();
            UnmarshallerFactory unmarshallerFactory = Configuration
                  .getUnmarshallerFactory();
            Unmarshaller unmarshaller = unmarshallerFactory
                  .getUnmarshaller(assertionElement);
            Assertion samlAssertion = (Assertion) unmarshaller
                  .unmarshall(assertionElement);

            /*
             * Below code works with OpenSAML API to check Authentication,
             * Authorization, and attributes. Using the XPath API with the
             * assertionElement above would probably be an easier and more
             * readable option.
             */

            // Check for X509 authentication, error otherwise
            List<AuthnStatement> authStmtList = samlAssertion.getAuthnStatements();
            if (authStmtList == null || authStmtList.size() < 1
                  || authStmtList.size() > 1) {
               throw createSOAPFaultException("Missing Authentication Statement.", true);
            } else {
               AuthnStatement authStmt = authStmtList.get(0);
               if (!AuthnContext.X509_AUTHN_CTX.equals(authStmt.getAuthnContext()
                     .getAuthnContextClassRef().getAuthnContextClassRef())) {
                  throw createSOAPFaultException("Only X.509 authentication supported.",
                        true);
               }
            }

            // Check if math degree, error otherwise
            List<AttributeStatement> asList = samlAssertion.getAttributeStatements();
            if (asList == null || asList.size() == 0) {
               throw createSOAPFaultException("Degree/Major is missing.", true);
            } else {
               boolean hasMathDegree = false;
               for (Iterator<AttributeStatement> it = asList.iterator(); it.hasNext();) {
                  AttributeStatement as = it.next();
                  List<Attribute> attList = as.getAttributes();
                  if (attList == null || attList.size() == 0) {
                     throw createSOAPFaultException("Degree/major is missing.", true);
                  } else {
                     for (Iterator<Attribute> it2 = attList.iterator(); it2.hasNext();) {
                        Attribute att = it2.next();
                        if (!att.getName().equals("degree")) {
                           continue;
                        } else {
                           List<XMLObject> xoList = att.getAttributeValues();
                           if (xoList == null || xoList.size() < 1 || xoList.size() > 1) {
                              throw createSOAPFaultException("Degree/major is missing.",
                                    true);
                           } else {
                              XMLObject xmlObj = xoList.get(0);
                              if (xmlObj.getDOM().getFirstChild().getTextContent()
                                    .equals("Mathematics")) {
                                 hasMathDegree = true;
                              }
                           }
                        }
                     }
                  }
               }
               if (hasMathDegree == false) {
                  throw createSOAPFaultException(
                        "Must have Mathematics degree to run DoubleIt.", true);
               }
            }

            // If even number being doubled, make sure user has permission
            SOAPBody sb = smc.getMessage().getSOAPBody();

            if (sb.getFirstChild() == null || sb.getFirstChild().getFirstChild() == null) {
               throw createSOAPFaultException("Invalid SOAP Body", true);
            } else {
               Integer intValue = new Integer(sb.getFirstChild().getFirstChild()
                     .getTextContent());
               if ((intValue.intValue() % 2) == 0) { // if even
                  List<AuthzDecisionStatement> adsList = samlAssertion
                        .getAuthzDecisionStatements();
                  if (adsList == null || adsList.size() < 1 || adsList.size() > 1) {
                     throw createSOAPFaultException(
                           "Missing or invalid Authorization Decision Statement", true);
                  } else {
                     Boolean canDoubleEven = false;
                     AuthzDecisionStatement ads = adsList.get(0);
                     List<Action> actList = ads.getActions();
                     for (Iterator<Action> it = actList.iterator(); it.hasNext();) {
                        Action action = it.next();
                        if ("DoubleEvenNumbers".equals(action.getAction())) {
                           canDoubleEven = true;
                           break;
                        }
                     }
                     if (canDoubleEven == false) {
                        throw createSOAPFaultException(
                              "Missing authorization to double even numbers.", true);
                     }
                  }
               }
            }
         } catch (Exception e) {
            throw createSOAPFaultException("Internal Error: " + e.getMessage(), false);
         }
      }
      return true;
   }

   /*
    * Convenience function used to generate a generic SOAPFaultException
    */
   private SOAPFaultException createSOAPFaultException(String faultString,
         Boolean clientFault) {
      try {
         String faultCode = clientFault ? "Client" : "Server";
         SOAPFault fault = SOAPFactory.newInstance().createFault();
         fault.setFaultString(faultString);
         fault.setFaultCode(new QName(SOAPConstants.URI_NS_SOAP_ENVELOPE, faultCode));
         return new SOAPFaultException(fault);
      } catch (SOAPException e) {
         throw new RuntimeException("Error creating SOAP Fault message, faultString: "
               + faultString);
      }
   }

   public boolean handleFault(SOAPMessageContext smc) {
      logToSystemOut(smc);
      return true;
   }

   // nothing to clean up
   public void close(MessageContext messageContext) {
   }

   /*
    * Check the MESSAGE_OUTBOUND_PROPERTY in the context to see if this is an
    * outgoing or incoming message. Write a brief message to the print stream
    * and output the message. The writeTo() method can throw SOAPException or
    * IOException
    */
   private void logToSystemOut(SOAPMessageContext smc) {
      Boolean outboundProperty = (Boolean) smc
            .get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

      if (outboundProperty.booleanValue()) {
         out.println("\nIncoming message to web service provider:");
      } else {
         out.println("\nOutgoing message from web service provider:");
      }

      SOAPMessage message = smc.getMessage();
      try {
         message.writeTo(out);
         out.println(""); // just to add a newline
      } catch (Exception e) {
         out.println("Exception in handler: " + e);
      }
   }
}
