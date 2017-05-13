package service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.net.URL;

import javax.activation.DataHandler;
import javax.jws.WebService;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingType;
import javax.xml.ws.soap.SOAPFaultException;

import org.example.contract.doubleit.DoubleItPortType;

import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

@WebService(targetNamespace = "http://www.example.org/contract/DoubleIt", 
portName="DoubleItPort",
serviceName="DoubleItService", 
endpointInterface="org.example.contract.doubleit.DoubleItPortType")
// Below annotation activates MTOM, without this the PDF response
// would be inlined as base64Binary within the SOAP response 
@BindingType(value=javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_MTOM_BINDING)
public class DoubleItPortTypeImpl implements DoubleItPortType {

    private static int uploadCount = 0;

    public javax.activation.DataHandler downloadPDF(int numberToDouble) {
        try {
            String requestValue = "<request>" + numberToDouble + "</request>";
            // java.io.BufferedOutputStream can be better than BAOS below
            // for large PDFs (not applicable here).  See the Apache FOP
            // site for more details if you will be generating large PDFs.
            ByteArrayOutputStream pdfBaos = new ByteArrayOutputStream();

            TransformerFactory factory = TransformerFactory.newInstance();
            URL stylesheetURL = 
                    getClass().getClassLoader().getResource("DoubleIt.xsl");
            Transformer transformer = 
                    factory.newTransformer(new StreamSource(stylesheetURL.toString()));

            // Run the results through Apache FOP to get the PDF response
            FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, pdfBaos);
            Result res = new SAXResult(fop.getDefaultHandler());

            transformer.transform(new StreamSource(
                    new StringReader(requestValue)), res);

            ByteArrayDataSource ds = new ByteArrayDataSource(pdfBaos.toByteArray(), 
                    "application/pdf");
            return new DataHandler(ds);
        } catch (Exception e) {
            try {
                SOAPFault fault = SOAPFactory.newInstance().createFault();
                fault.setFaultString(e.getMessage());
                fault.setFaultCode(new QName(SOAPConstants.URI_NS_SOAP_ENVELOPE, "Server"));
                throw new SOAPFaultException(fault);
            } catch (Exception e2) {
                throw new RuntimeException("downloadPDF: Problem processing SOAP Fault on service-side: " +
                        e2.getMessage());
            }
        }
    }

    public int uploadPDF(javax.activation.DataHandler pdfToUpload) {
        try {
            String filename = "ClientUpload" + ++uploadCount + ".pdf";
            File file = new File(filename);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            pdfToUpload.writeTo(fileOutputStream);
            fileOutputStream.flush();
            // Saves to directory where Tomcat was started
            fileOutputStream.close();
            return 500;  // doc size hardcoded for now
        } catch (Exception e) {
            try {
                SOAPFault fault = SOAPFactory.newInstance().createFault();
                fault.setFaultString(e.getMessage());
                fault.setFaultCode(new QName(SOAPConstants.URI_NS_SOAP_ENVELOPE, "Server"));
                throw new SOAPFaultException(fault);
            } catch (Exception e2) {
                throw new RuntimeException("Upload PDF: Problem processing SOAP Fault on service-side: " +
                        e2.getMessage());
            }
        }
    }
}
