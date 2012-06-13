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

import ebay.apis.eblbasecomponents.FindPopularItemsRequestType;
import ebay.apis.eblbasecomponents.FindPopularItemsResponseType;
import ebay.apis.eblbasecomponents.Shopping;
import ebay.apis.eblbasecomponents.ShoppingInterface;
import ebay.apis.eblbasecomponents.ShoppingInterfaceImpl;
import ebay.apis.eblbasecomponents.SimpleItemArrayType;
import ebay.apis.eblbasecomponents.SimpleItemType;

public class WSClientTest {

   protected static Endpoint ep;
   protected static String address;

   @BeforeClass
   public static void setUp() {
      address = "http://localhost:9000/services/ShoppingService";
      ep = Endpoint.publish(address, new ShoppingInterfaceImpl());
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
      FindPopularItemsRequestType req = new FindPopularItemsRequestType();
      FindPopularItemsResponseType resp = createPort().findPopularItems(req);
      validateObject(resp);
   }

   @Test
   public void testWSClientSOAPCallWorks() {
      WSClient wsc = new WSClient();
      FindPopularItemsRequestType req = new FindPopularItemsRequestType();
      FindPopularItemsResponseType resp = wsc.findPopularItems(createPort(), req);
      validateObject(resp);
   }

   @Test
   public void testWSClientProcessingWorks() {
      WSClient wsc = new WSClient();
      FindPopularItemsRequestType req = new FindPopularItemsRequestType();
      FindPopularItemsResponseType resp = wsc.findPopularItems(createPort(), req);
      SimpleItemArrayType siat = resp.getItemArray();
      List<SimpleItemType> lsit = siat.getItem();
      String test = wsc.formatSimpleItemType(lsit.get(0));
      Assert.assertEquals("WSClient.formatSimpleItemType() not working", 
         "Category: widgets\nItem ID: wid-1234\nURL: http://www.ebay.com/wid-1234", 
         test);
   }

   private void validateObject(FindPopularItemsResponseType resp) {
      SimpleItemArrayType siat = resp.getItemArray();
      List<SimpleItemType> lsit = siat.getItem();
      Assert.assertTrue("Response has only one item", lsit.size() == 1);

      SimpleItemType sit = lsit.get(0);

      Assert.assertEquals("Primary Category Name wrong", "widgets",
            sit.getPrimaryCategoryName());
      Assert.assertEquals("Item ID wrong", "wid-1234", sit.getItemID());
      Assert.assertEquals("View Item URL wrong",
            "http://www.ebay.com/wid-1234",
            sit.getViewItemURLForNaturalSearch());
   }

   private ShoppingInterface createPort() {
      URL wsdlLocation = ClassLoader.getSystemResource("ShoppingService.wsdl");
      QName service = new QName("urn:ebay:apis:eBLBaseComponents", "Shopping");
      ShoppingInterface port = new Shopping(wsdlLocation, service).getShopping();
      System.out.println("******* port is " + port == null ? "null" : "not null");
      BindingProvider bp = (BindingProvider) port;
      bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
            address);
      return port;      
   }

}

