package service;

import javax.jws.WebService;
import org.example.contract.doubleit.DoubleItPortType;
import org.example.contract.doubleit.DoubleNumber316Fault;
import org.example.contract.doubleit.DoubleOddNumberFault;
import org.example.schema.doubleit.DoubleOddFault;
import org.example.schema.doubleit.Double316Fault;

@WebService(targetNamespace = "http://www.example.org/contract/DoubleIt", 
            portName="DoubleItPort",
            serviceName="DoubleItService", 
            endpointInterface="org.example.contract.doubleit.DoubleItPortType")
public class DoubleItPortTypeImpl implements DoubleItPortType {

    public int doubleIt(int numberToDouble) throws DoubleNumber316Fault, DoubleOddNumberFault {
        if (numberToDouble % 2 != 0) {
            DoubleOddFault dof = new DoubleOddFault();
            dof.setErrorDetails(numberToDouble + " is an odd number");
            throw new DoubleOddNumberFault("Don't double odd numbers!", dof);
        }

        if (numberToDouble == 316) {
            Double316Fault d3f =  new Double316Fault();
            d3f.setErrorDetails("Attempt was made to double 316");
            throw new DoubleNumber316Fault("Don't double 316!", d3f);
        }

        // demonstration of throwing an unmapped exception
        if (numberToDouble == 428) {
            throw new RuntimeException("Ha ha!  Didn't tell you 428 can't be doubled either!");
        }

        return numberToDouble * 2;
    }
}
