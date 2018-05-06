package service;

import java.net.URL;

import org.eclipse.jetty.server.ServerConnector;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class EmbeddedJettyIT extends DoubleItPortTypeImplTest {
   private static Server server;

   @BeforeClass
   public static void setUp() throws Exception {
      String address = "http://localhost:9001/doubleit/services/doubleit";
      wsdlURL = new URL(address + "?wsdl");
      
      server = new Server();
      ServerConnector connector = new ServerConnector(server);
      connector.setPort(9001);
      server.setConnectors(new Connector[] {connector});

      WebAppContext wactx = new WebAppContext();
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
