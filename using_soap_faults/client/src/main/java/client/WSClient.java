package client;

import org.example.contract.doubleit.DoubleItPortType;
import org.example.contract.doubleit.DoubleItService;
import org.example.contract.doubleit.DoubleNumber316Fault;
import org.example.contract.doubleit.DoubleOddNumberFault;

import javax.xml.ws.soap.SOAPFaultException;

public class WSClient {

    public WSClient() {
    }

    public static void main (String[] args) {
        DoubleItService service = new DoubleItService();
        DoubleItPortType port = service.getDoubleItPort();
        doubleIt(port, 22);
        doubleIt(port, 11);
        doubleIt(port, 316);
        doubleIt(port, 428);
    }

    private static void doubleIt(DoubleItPortType port, int numToDouble) {
        try {
            System.out.println("The number " + numToDouble + " doubled is " + port.doubleIt(numToDouble));
        } catch (DoubleNumber316Fault e) {
            System.out.println("316 Fault: " + e.getMessage() + "; basic fault text: " + e.getFaultInfo().getErrorDetails());
        } catch (DoubleOddNumberFault e) {
            System.out.println("Odd number fault: " + e.getMessage() + "; basic fault text: " + e.getFaultInfo().getErrorDetails());
        } catch (SOAPFaultException e) {
            System.out.println("SOAPFaultException: " + e.getMessage());
        }
    }
}
