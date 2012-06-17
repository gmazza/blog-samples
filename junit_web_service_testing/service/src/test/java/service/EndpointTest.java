package service;

import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class EndpointTest extends DoubleItPortTypeImplTest {
   protected static Endpoint ep;
   protected static String address;

   @BeforeClass
   public static void setUp() throws Exception {
      address = "http://localhost:9000/services/DoubleItPortType";
      wsdlURL = new URL(address + "?wsdl");
      ep = Endpoint.publish(address, new DoubleItPortTypeImpl());
   }

   @AfterClass
   public static void tearDown() {
      try {
         ep.stop();
      } catch (Throwable t) {
         System.out.println("Error thrown: " + t.getMessage());
      }
   }
}

