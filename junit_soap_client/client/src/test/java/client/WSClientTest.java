package client;

import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.example.contract.doubleit.DoubleItPortImpl;
import org.example.contract.doubleit.DoubleItPortType;
import org.example.contract.doubleit.DoubleItService;

public class WSClientTest {

   protected static Endpoint ep;
   protected static String address;

   @BeforeClass
   public static void setUp() {
      address = "http://localhost:9000/services/DoubleItService";
      ep = Endpoint.publish(address, new DoubleItPortImpl());
   }

   @AfterClass
   public static void tearDown() {
      try {
         ep.stop();
      } catch (Throwable t) {
         System.out.println("Error thrown: " + t.getMessage());
      }
   }

   @Test
   public void testStubWebServiceWorks() {
      int resp = createPort().doubleIt(10);
      validateObject(resp);
   }

   @Test
   public void testWSClientSOAPCallWorks() {
      int response = WSClient.doubleIt(createPort(), 10);
      validateObject(response);
   }

   @Test
   public void testWSClientProcessingWorks() {
      String response = WSClient.doubleItMessage(createPort(), 10);
      
      // mock Service hardcoded to return 24
      Assert.assertEquals("DoubleIt string response not reporting 24", 
         "The number 10 doubled is 24", response);
   }

   private void validateObject(int resp) {
      // mock Service hardcoded to return 24
      Assert.assertTrue("Response is not 24", resp == 24);
   }

   private DoubleItPortType createPort() {
      URL wsdlLocation = ClassLoader.getSystemResource("DoubleIt.wsdl");
      QName service = new QName("http://www.example.org/contract/DoubleIt", "DoubleItService");
      DoubleItPortType port = new DoubleItService(wsdlLocation, service).getDoubleItPort();
      BindingProvider bp = (BindingProvider) port;
      bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, address);
      return port;      
   }

}

