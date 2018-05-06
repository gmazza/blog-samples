package service;

import java.net.URL;
import javax.xml.ws.Endpoint;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class EndpointTest extends DoubleItPortTypeImplTest {
   private static Endpoint ep;

   @BeforeClass
   public static void setUp() throws Exception {
      String address = "http://localhost:9000/services/DoubleItPortType";
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
