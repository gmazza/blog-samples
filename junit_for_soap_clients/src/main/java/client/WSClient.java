package client;

import java.util.List;
import javax.xml.ws.BindingProvider;
import ebay.apis.eblbasecomponents.FindPopularItemsRequestType;
import ebay.apis.eblbasecomponents.FindPopularItemsResponseType;
import ebay.apis.eblbasecomponents.SimpleItemArrayType;
import ebay.apis.eblbasecomponents.SimpleItemType;
import ebay.apis.eblbasecomponents.Shopping;
import ebay.apis.eblbasecomponents.ShoppingInterface;

public class WSClient {
   public static void main(String[] args) {
      if (args.length != 1) {
         System.out.println("Usage: WSClient &lt;search item>");
         System.exit(-1);
      }

      WSClient wsc = new WSClient();
      wsc.run(args[0]);
   }

   private void run(String searchItem) {
      try {
         ShoppingInterface port = new Shopping().getShopping();
         BindingProvider bp = (BindingProvider) port;

         // retrieve the URL stub from the WSDL
         String ebayURL = (String) bp.getRequestContext().get(
               BindingProvider.ENDPOINT_ADDRESS_PROPERTY);

         // add eBay-required parameters to the URL
         // Important: add your app ID and check WSDL service
         // section for correct version number
         String endpointURL = ebayURL + "?callname=FindPopularItems&siteid=0"
               + "&appid=????"
               + "&version=767&requestencoding=SOAP";

         // replace the endpoint address with the new value
         bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
               endpointURL);

         FindPopularItemsRequestType req = new FindPopularItemsRequestType();
         req.setQueryKeywords(searchItem);

         FindPopularItemsResponseType resp = findPopularItems(port, req);
         SimpleItemArrayType siat = resp.getItemArray();
         List<SimpleItemType> lsit = siat.getItem();

         for (SimpleItemType sit : lsit) {
            System.out.println("\n\n" + formatSimpleItemType(sit));
         }

      } catch (Exception e) {
         System.out.println("Exception: " + e.getMessage());
      }
   }

   protected FindPopularItemsResponseType findPopularItems(ShoppingInterface port,
         FindPopularItemsRequestType req) {
      return port.findPopularItems(req);
   }

   protected String formatSimpleItemType(SimpleItemType sit) {
      StringBuffer sb = new StringBuffer();
      sb.append("Category: " + sit.getPrimaryCategoryName());
      sb.append("\nItem ID: " + sit.getItemID());
      sb.append("\nURL: " + sit.getViewItemURLForNaturalSearch());
      return sb.toString();
   }

}
