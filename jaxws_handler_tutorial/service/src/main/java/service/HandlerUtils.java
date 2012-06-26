package service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.bind.Element;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;

public class HandlerUtils {

   public static void printMessageContext(String whereFrom, Map<String, Object> propertyMap) {
      System.out.println("*** MessageContext from " + whereFrom + ":");
      printMessageContext(propertyMap);
   }

   public static void printMessageContext(Map<String, Object> propertyMap) {
      outputBoolean("Message Outbound Property", (Boolean) propertyMap
            .get(MessageContext.MESSAGE_OUTBOUND_PROPERTY));
      // outputMap("HTTP Request Headers", (Map<String,List<String>>)
      //    propertyMap.get(MessageContext.HTTP_REQUEST_HEADERS));
      outputString("HTTP Request Method", (String) propertyMap
            .get(MessageContext.HTTP_REQUEST_METHOD));
      outputString("Path Info", (String) propertyMap.get(MessageContext.PATH_INFO));
      outputString("Query String", (String) propertyMap.get(MessageContext.QUERY_STRING));
      // outputMap("HTTP Response Headers", (Map<String,List<String>>)
      // propertyMap.get(MessageContext.HTTP_RESPONSE_HEADERS));
      outputInteger("HTTP Response Code", (Integer) propertyMap
            .get(MessageContext.HTTP_RESPONSE_CODE));
      outputArrayList("Reference Parameters", (ArrayList<Element>) propertyMap
            .get(MessageContext.REFERENCE_PARAMETERS));
      outputQName("WSDL Interface", (QName) propertyMap.get(MessageContext.WSDL_INTERFACE));
      outputQName("WSDL Operation", (QName) propertyMap.get(MessageContext.WSDL_OPERATION));
      outputQName("WSDL Port", (QName) propertyMap.get(MessageContext.WSDL_PORT));
      outputQName("WSDL Service", (QName) propertyMap.get(MessageContext.WSDL_SERVICE));
      // outputString("Servlet Context", ( )
      //    propertyMap.get(MessageContext.SERVLET_CONTEXT));
      // outputString("Servlet Request", ( )
      //    propertyMap.get(MessageContext.SERVLET_REQUEST));
      // outputString("Servlet Response", ( )
      //    propertyMap.get(MessageContext.SERVLET_RESPONSE));
   }

   private static void outputString(String key, String value) {
      System.out.println(key + " = " + value);
   }

   private static void outputBoolean(String key, Boolean value) {
      System.out.println(key + " = " + ((value == null) ? "null" : value.toString()));
   }

   private static void outputInteger(String key, Integer value) {
      System.out.println(key + " = " + ((value == null) ? "null" : value.toString()));
   }

   private static void outputURI(String key, URI value) {
      System.out.println(key + " = " + ((value == null) ? "null" : value.toString()));
   }

   private static void outputQName(String key, QName value) {
      System.out.println(key + " = " + ((value == null) ? "null" : value.toString()));
   }

   private static void outputArrayList(String key, ArrayList<Element> list) {
      System.out.println(key + ":" + ((list == null) ? "(null)" : ""));
      if (list != null) {
         for (Element e : list) {
            System.out.println("   " + e.toString());
         }
      }
   }
}
