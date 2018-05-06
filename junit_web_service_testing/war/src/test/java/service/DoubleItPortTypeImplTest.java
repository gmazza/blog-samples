package service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;

import org.junit.Test;
import org.w3c.dom.Document;

import org.example.contract.doubleit.DoubleItPortType;
import org.example.contract.doubleit.DoubleItService;
import org.example.schema.doubleit.DoubleIt;
import org.example.schema.doubleit.DoubleItResponse;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

public abstract class DoubleItPortTypeImplTest {
   static URL wsdlURL;
   private static QName serviceName;
   private static QName portName;

   static {
      serviceName = new QName("http://www.example.org/contract/DoubleIt",
         "DoubleItService");
      portName = new QName("http://www.example.org/contract/DoubleIt", "DoubleItPort");
   }

   /*
    * This test uses wsimport/wsdl2java generated artifacts, both service and
    * SEI
    */
   @Test
   public void doubleItWorksWithPositiveNumbers() throws MalformedURLException {
      DoubleItService dis = new DoubleItService(wsdlURL, serviceName);
      DoubleItPortType dipt = dis.getDoubleItPort();
      int resp = dipt.doubleIt(10);
      assertEquals("Double-It not doubling positive numbers", 20, resp);
   }

   /*
    * This test uses raw Service class for service, wsimport/wsdl2java
    * generated SEI
    */
   @Test
   public void testDoubleItWithNegativeNumbers() {
      Service jaxwsService = Service.create(wsdlURL, serviceName);
      DoubleItPortType dipt = jaxwsService.getPort(DoubleItPortType.class);
      int resp = dipt.doubleIt(-10);
      assertEquals("Double-It not doubling negative numbers", -20, resp);
   }

   /*
    * This test uses raw Service class for service, Dispatch<SOAPMessage> for
    * client No wsimport/wsdl2java needed. Note works with full SOAP message
    * (Service.Mode.MESSAGE)
    */
   @Test
   public void doubleItWorksForZero() throws Exception {
      Service jaxwsService = Service.create(wsdlURL, serviceName);
      Dispatch<SOAPMessage> disp = jaxwsService.createDispatch(portName,
            SOAPMessage.class, Service.Mode.MESSAGE);
      InputStream is = getClass().getClassLoader().getResourceAsStream(
            "fullSOAPMessage.xml");
      SOAPMessage reqMsg = MessageFactory.newInstance().createMessage(null,
            is);
      assertNotNull(reqMsg);
      SOAPMessage response = disp.invoke(reqMsg);
      assertEquals("Double-It not doubling zero correctly", "0", response
            .getSOAPBody().getTextContent().trim());
   }

   /*
    * This test uses raw Service class for service, Dispatch<Source> for
    * client. No wsimport/wsdl2java run needed. Uses payload (soap:body contents)
    * only (Service.Mode.PAYLOAD), but can be configured to use MESSAGE. Note
    * CXF supports other options such as Dispatch<DOMSource>, Dispatch<SAXSource>,
    * and Dispatch<StreamSource>, search CXF source code for examples.
    */
   @Test
   public void doubleItWorksForPrimeNumbers() throws Exception {
      Service jaxwsService = Service.create(wsdlURL, serviceName);
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      InputStream is = getClass().getClassLoader().getResourceAsStream(
            "justPayload.xml");
      Document newDoc = builder.parse(is);
      DOMSource request = new DOMSource(newDoc);

      Dispatch<Source> disp = jaxwsService.createDispatch(portName,
            Source.class, Service.Mode.PAYLOAD);
      Source result = disp.invoke(request);
      DOMResult domResponse = new DOMResult();
      Transformer trans = TransformerFactory.newInstance().newTransformer();
      trans.transform(result, domResponse);
      assertEquals("Double-It failing with prime numbers", "14", domResponse
            .getNode().getFirstChild().getTextContent().trim());

      /* Simpler alternative offered by CXF that uses Dispatch<DOMSource>:
      Dispatch<DOMSource> disp = jaxwsService.createDispatch(portName, DOMSource.class,
         Service.Mode.PAYLOAD); 
      DOMSource domResponse = disp.invoke(request);
      assertEquals("Double-It failing with prime numbers", "14",
         domResponse.getNode().getFirstChild().getTextContent().trim()); */
   }

   /*
    * This test uses raw Service class for service, Dispatch<JAXBContext> for
    * client. Conveniently uses JAX-WS generated artifacts.
    */
   @Test
   public void doubleItWorksWithOddNumbers() throws Exception {
      Service jaxwsService = Service.create(wsdlURL, serviceName);
      JAXBContext jaxbContext = JAXBContext
            .newInstance("org.example.schema.doubleit");
      Dispatch<Object> jaxbDispatch = jaxwsService.createDispatch(portName,
            jaxbContext, Service.Mode.PAYLOAD);

      DoubleIt myDoubleIt = new DoubleIt();
      myDoubleIt.setNumberToDouble(3);

      JAXBElement<DoubleIt> doubleItElement = new JAXBElement<>(new QName(
            "http://www.example.org/schema/DoubleIt", "DoubleIt"), DoubleIt.class,
            myDoubleIt);

      DoubleItResponse response = (DoubleItResponse) jaxbDispatch
            .invoke(doubleItElement);
      assertNotNull(response);
      assertEquals("Double-It failing with odd numbers", 6, response
            .getDoubledNumber());
   }
}
