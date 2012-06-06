package client;

import java.io.PrintStream;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml1.core.NameIdentifier;
import org.opensaml.saml2.core.Action;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.AuthzDecisionStatement;
import org.opensaml.saml2.core.DecisionTypeEnumeration;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.impl.ActionBuilder;
import org.opensaml.saml2.core.impl.AssertionBuilder;
import org.opensaml.saml2.core.impl.AttributeBuilder;
import org.opensaml.saml2.core.impl.AttributeStatementBuilder;
import org.opensaml.saml2.core.impl.AuthnContextBuilder;
import org.opensaml.saml2.core.impl.AuthnContextClassRefBuilder;
import org.opensaml.saml2.core.impl.AuthnStatementBuilder;
import org.opensaml.saml2.core.impl.AuthzDecisionStatementBuilder;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml2.core.impl.SubjectBuilder;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSStringBuilder;
import org.w3c.dom.Element;

/*
 * This SOAP protocol handler adds a hardcoded SAML token to the
 * client SOAP request.
 *
 * WARNING: No actual security implemented here, sample just shows
 * using OpenSAML library to create a SAML token.
 */
public class ClientSAMLHandler implements SOAPHandler<SOAPMessageContext> {

   // change this to redirect output if desired
   private static PrintStream out = System.out;

   public static final String WS_SECURITY_NS_URI = 
	  "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

   public Set<QName> getHeaders() {
      return null;
   }

   public boolean handleMessage(SOAPMessageContext smc) {
      Boolean outboundProperty = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

      if (outboundProperty.booleanValue()) {
         out.println("(debug) Adding SAML token to outbound message from client");

         try {
            DefaultBootstrap.bootstrap();
            SOAPMessage message = smc.getMessage();
            SOAPPart soapPart = message.getSOAPPart();
            SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
            Name wsseHeaderName = soapEnvelope.createName("Security",
                  "wsse", WS_SECURITY_NS_URI);
            if (soapEnvelope.getHeader() == null) {
               soapEnvelope.addHeader();
            }
            SOAPHeaderElement securityElement = soapEnvelope.getHeader()
                  .addHeaderElement(wsseHeaderName);

            AssertionBuilder ab = new AssertionBuilder();
            Assertion assertion = ab.buildObject();
            assertion.setVersion(SAMLVersion.VERSION_20);
            assertion.setID("123"); // in reality, must be unique for all assertions
            assertion.setIssueInstant(new DateTime());

            IssuerBuilder ib = new IssuerBuilder();
            Issuer myIssuer = ib.buildObject();
            myIssuer.setValue("http://some.issuer.here");
            assertion.setIssuer(myIssuer);

            SubjectBuilder sb = new SubjectBuilder();
            Subject mySubject = sb.buildObject();
            NameIDBuilder nb = new NameIDBuilder();
            NameID myNameID = nb.buildObject();
            myNameID.setValue("bob");
            myNameID.setFormat(NameIdentifier.X509_SUBJECT);
            mySubject.setNameID(myNameID);
            assertion.setSubject(mySubject);

            // user authenticated via X509 token
            AuthnStatementBuilder asb = new AuthnStatementBuilder();
            AuthnStatement myAuthnStatement = asb.buildObject();
            myAuthnStatement.setAuthnInstant(new DateTime());
            AuthnContextBuilder acb = new AuthnContextBuilder();
            AuthnContext myACI = acb.buildObject();
            AuthnContextClassRefBuilder accrb = new AuthnContextClassRefBuilder();
            AuthnContextClassRef accr = accrb.buildObject();
            accr.setAuthnContextClassRef(AuthnContext.X509_AUTHN_CTX);
            myACI.setAuthnContextClassRef(accr);
            myAuthnStatement.setAuthnContext(myACI);
            assertion.getAuthnStatements().add(myAuthnStatement);

            // user can double even numbers
            AuthzDecisionStatementBuilder adsb = new AuthzDecisionStatementBuilder();
            AuthzDecisionStatement ads = adsb.buildObject();
            ads.setDecision(DecisionTypeEnumeration.PERMIT);
            ads.setResource("DoubleIt");
            ActionBuilder actb = new ActionBuilder();
            Action act = actb.buildObject();
            // arbitrary unique tag to define "namespace" of action
            // note SAML actions not defined in an XSD -- XAMCL normally used instead
            act.setNamespace("urn:doubleit:doubleitactions");
            act.setAction("DoubleEvenNumbers");
            ads.getActions().add(act);
            assertion.getAuthzDecisionStatements().add(ads);

            // user has math degree
            AttributeStatementBuilder attstmtb = new AttributeStatementBuilder();
            AttributeStatement attstmt = attstmtb.buildObject();
            AttributeBuilder attbldr = new AttributeBuilder();
            Attribute attr = attbldr.buildObject();
            attr.setName("degree");
            attr.setNameFormat("http://www.example.org/DoubleIt/Security");
            XSStringBuilder stringBuilder = (XSStringBuilder) Configuration
                  .getBuilderFactory().getBuilder(XSString.TYPE_NAME);
            XSString stringValue = stringBuilder
                  .buildObject(AttributeValue.DEFAULT_ELEMENT_NAME,
                        XSString.TYPE_NAME);
            stringValue.setValue("Mathematics");
            attr.getAttributeValues().add(stringValue);
            attstmt.getAttributes().add(attr);
            assertion.getAttributeStatements().add(attstmt);

            // marshall Assertion Java class into XML
            MarshallerFactory marshallerFactory = Configuration
                  .getMarshallerFactory();
            Marshaller marshaller = marshallerFactory
                  .getMarshaller(assertion);
            Element assertionElement = marshaller.marshall(assertion);
            securityElement.appendChild(soapPart.importNode(
                  assertionElement, true));
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
      return true;
   }

   public boolean handleFault(SOAPMessageContext smc) {
      out.println("Exception in Client handler: ");
      SOAPMessage message = smc.getMessage();
      try {
         message.writeTo(out);
         out.println(""); // just to add a newline
      } catch (Exception e) {
         out.println("Unable to write exception for exception: "
            + e.toString());
      }
      return true;
   }

   // nothing to clean up
   public void close(MessageContext messageContext) {
   }
}
