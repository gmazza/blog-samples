package service;

import java.net.URL;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class EmbeddedJettyIT extends DoubleItPortTypeImplTest {
   protected static Server server;
   protected static String address;
  
   @BeforeClass
   public static void setUp() throws Exception {
      address = "http://localhost:9000/doubleit/services/doubleit";
      wsdlURL = new URL(address + "?wsdl");
      
      server = new Server();
      Connector connector = new SelectChannelConnector();
      connector.setPort(9000);
      server.setConnectors(new Connector[] {connector});

      // Below line Metro-only; will provide SOAP request/response info to console
      // com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump=true;      
      
      WebAppContext wactx = new WebAppContext();
      wactx.setParentLoaderPriority(true);
      wactx.setContextPath("/doubleit");
      wactx.setWar("target/doubleit.war");

      HandlerCollection handlers = new HandlerCollection();
      handlers.setHandlers(new Handler[] {wactx, new DefaultHandler()});
      server.setHandler(handlers);

      try {
          server.start();
      } catch (Exception e) {
          e.printStackTrace();
      }     
   }

   @AfterClass
   public static void tearDown() throws Exception {
     if (server != null) {
           server.stop();
           server.destroy();
           server = null;
      }
   }
}
