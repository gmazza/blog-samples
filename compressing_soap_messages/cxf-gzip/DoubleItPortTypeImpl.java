package service;

import javax.jws.WebService;
import org.example.contract.doubleit.DoubleItPortType;

@WebService(targetNamespace = "http://www.example.org/contract/DoubleIt", 
            portName="DoubleItPort",
            serviceName="DoubleItService", 
            endpointInterface="org.example.contract.doubleit.DoubleItPortType")
@org.apache.cxf.annotations.GZIP(threshold=0)
public class DoubleItPortTypeImpl implements DoubleItPortType {

    public int doubleIt(int numberToDouble) {
        return numberToDouble * 2;
    }
}

