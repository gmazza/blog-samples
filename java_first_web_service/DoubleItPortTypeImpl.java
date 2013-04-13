package service;

import javax.jws.WebService;

@WebService(targetNamespace = "http://www.example.org/contract/DoubleIt", 
            portName="DoubleItPort",
            serviceName="DoubleItService", 
            endpointInterface="service.DoubleItPortType")
public class DoubleItPortTypeImpl implements DoubleItPortType {

    public int doubleIt(int numberToDouble) {
        return numberToDouble * 2;
    }
}

