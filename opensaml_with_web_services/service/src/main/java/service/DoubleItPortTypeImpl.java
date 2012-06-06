package service;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import org.example.contract.doubleit.DoubleItPortType;

@WebService(targetNamespace = "http://www.example.org/contract/DoubleIt", 
            portName="DoubleItPort",
            serviceName="DoubleItService", 
            endpointInterface="org.example.contract.doubleit.DoubleItPortType")
@HandlerChain(file = "/handlers.xml")
public class DoubleItPortTypeImpl implements DoubleItPortType {

    public int doubleIt(int numberToDouble) {
        return numberToDouble * 2;
    }
}
