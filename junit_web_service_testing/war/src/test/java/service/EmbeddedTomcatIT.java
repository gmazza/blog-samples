package service;

import java.io.File;
import java.net.URL;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.startup.Tomcat;

public class EmbeddedTomcatIT extends DoubleItPortTypeImplTest {
   private static Tomcat tomcat;

   @BeforeClass
   public static void setUp() throws Exception {
      String address = "http://localhost:9002/doubleit/services/doubleit";
      wsdlURL = new URL(address + "?wsdl");

      String appBase = "./target";
      tomcat = new Tomcat();
      tomcat.setPort(9002);
      tomcat.setBaseDir(".");
      tomcat.getHost().setAppBase(appBase);
      
      // Add AprLifecycleListener
      StandardServer server = (StandardServer)tomcat.getServer();
      AprLifecycleListener listener = new AprLifecycleListener();
      server.addLifecycleListener(listener);

      tomcat.addWebapp("/doubleit", new File("target/doubleit").getAbsolutePath());
      tomcat.start();
   }

   @AfterClass
   public static void tearDown() throws Exception {
      if (tomcat != null) {
         tomcat.stop();
         tomcat.destroy();
         tomcat = null;
      }
   }
}
