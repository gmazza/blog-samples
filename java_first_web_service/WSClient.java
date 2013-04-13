package client;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;
import java.net.URL;

import service.DoubleItPortType;

public class WSClient {

    private static final QName SERVICE_NAME
        = new QName("http://www.example.org/contract/DoubleIt", "DoubleItService");
    private static final QName PORT_NAME
        = new QName("http://www.example.org/contract/DoubleIt", "DoubleItPort");

    public WSClient() {
    }

    // used by OSGi-hosted client
    // portType configured via OSGI-INF/blueprint/client.xml file
    // portType can also be created locally using logic in main() method.
    public WSClient(DoubleItPortType portType) throws Exception {
        makeCalls(portType);
    }

    // used by standalone Java client
    public static void main (String[] args) throws Exception {
        // Use if web service provider hosted on Tomcat
        String endpointAddress = "http://localhost:8080/doubleit/services/doubleit";
        // Use if web service provider hosted on Karaf
        // String endpointAddress = "http://localhost:8181/cxf/doubleit";
        Service service = Service.create(new URL(endpointAddress +"?wsdl"), SERVICE_NAME);
        DoubleItPortType port = service.getPort(DoubleItPortType.class);
        makeCalls(port);
    } 
    
    private static void makeCalls(DoubleItPortType port) {
        doubleIt(port, 10);
        doubleIt(port, 0);
        doubleIt(port, -10);
    }

    public static void doubleIt(DoubleItPortType port, 
            int numToDouble) {
        int resp = port.doubleIt(numToDouble);
        System.out.println("The number " + numToDouble + " doubled is " 
            + resp);
    }
}

