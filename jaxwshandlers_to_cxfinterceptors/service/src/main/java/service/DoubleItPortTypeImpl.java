package service;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.example.contract.doubleit.DoubleItPortType;

@WebService(targetNamespace = "http://www.example.org/contract/DoubleIt", 
            portName="DoubleItPort",
            serviceName="DoubleItService", 
            endpointInterface="org.example.contract.doubleit.DoubleItPortType")
@org.apache.cxf.interceptor.InInterceptors (interceptors = {
        "service.ValueCheckInInterceptor",
        "org.apache.cxf.binding.soap.saaj.SAAJInInterceptor", 
        "service.ReadSOAPHeaderInInterceptor"})
public class DoubleItPortTypeImpl implements DoubleItPortType {

    @Resource
    private WebServiceContext context;
    
    public int doubleIt(int numberToDouble) {
        // should succeed (termOne has APPLICATION scope)
        System.out.println("First Word: " + context.getMessageContext().get("termOne"));
        // should succeed (termTwo has APPLICATION scope)
        System.out.println("Second Word: " + context.getMessageContext().get("termTwo"));

        return numberToDouble * 2;
    }
}

