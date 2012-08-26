package client;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

import javax.activation.DataHandler;

import org.example.contract.doubleit.DoubleItPortType;
import org.example.contract.doubleit.DoubleItService;

public class WSClient {
    public static void main(String[] args) {
        DoubleItService service = new DoubleItService();
        DoubleItPortType port = service.getDoubleItPort();

        downloadPDF(port, 10);
        downloadPDF(port, 0);
        downloadPDF(port, -10);

        uploadPDF(port, "MySamplePDF.pdf");
    }

    public static void downloadPDF(DoubleItPortType port, int numToDouble) {
        try {
            DataHandler dh = port.downloadPDF(numToDouble);
            String filename = "DoubleIt" + numToDouble + ".pdf";
            // open pdf, save as a file.
            File file = new File(filename);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            dh.writeTo(fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            System.out.println(filename + " has been saved locally.");
        } catch (Exception e) {
            System.out.println("System Exception during PDF download: " + e.getMessage());
        }
    }

    public static void uploadPDF(DoubleItPortType port, String filename) {
        try {
            URL fileURL = WSClient.class.getClassLoader().getResource(filename);
            int pdfSize = port.uploadPDF(new DataHandler(fileURL));
            System.out.println("Web service reported upload file size as " + pdfSize + " bytes.");
        } catch (Exception e) {
            System.out.println("System Exception during PDF upload: " + e.getMessage());
        }
    }
}
