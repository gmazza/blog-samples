package client;

import javax.xml.ws.soap.SOAPFaultException;

import org.example.contract.doubleit.DoubleItPortType;
import org.example.contract.doubleit.DoubleItService;
import org.example.contract.doubleit.CorrelationIdNotFoundFault;
import org.example.schema.doubleit.BasicFault;
import org.example.schema.doubleit.GetDoubleItResults;
import org.example.schema.doubleit.GetDoubleItResultsResponse;
import org.example.schema.doubleit.SubmitDoubleIt;
import org.example.schema.doubleit.SubmitDoubleItResponse;

public class WSClient {
    public static void main(String[] args) {
        DoubleItService service = new DoubleItService();
        DoubleItPortType port = service.getDoubleItPort();

        try {
            SubmitDoubleIt sdi = new SubmitDoubleIt();
            sdi.setNumberToDouble(10);
            SubmitDoubleItResponse sdir = port.submitDoubleIt(sdi);
            System.out.println("Submitted DoubleIt request for 10");
            System.out.println("Returned correlation ID is: "
                    + sdir.getCorrelationId());

            GetDoubleItResults gdir = new GetDoubleItResults();
            gdir.setCorrelationId(sdir.getCorrelationId());

            System.out.println("SOAP Client polling for answer:");
            GetDoubleItResultsResponse gdirr = null;
            int TRIES_MAX = 10;
            for (int tries = 1; true; tries++) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                }
                ;
                System.out.println("Polling attempt #" + tries + "...");
                gdirr = port.getDoubleItResults(gdir);
                if (gdirr.isComplete()) {
                    System.out
                            .println("Result is: " + gdirr.getDoubledNumber());
                    break;
                } else if (tries >= TRIES_MAX) {
                    System.out.println("No response.  Attempts halted after "
                            + TRIES_MAX + " tries");
                    break;
                }
            }
        } catch (CorrelationIdNotFoundFault e) {
            BasicFault bf = e.getFaultInfo();
            System.out.println("Exception: Correlation ID "
                    + bf.getErrorDetails() + " not found.");
        } catch (SOAPFaultException e) {
            System.out.println("Exception from web service: " + e.getMessage());
        }
    }
}
