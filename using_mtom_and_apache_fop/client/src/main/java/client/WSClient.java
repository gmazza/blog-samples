package client;

import java.io.File;
import java.io.FileOutputStream;

import javax.activation.DataHandler;

import org.example.contract.doubleit.DoubleItPortType;
import org.example.contract.doubleit.DoubleItService;

public class WSClient {
    public static void main (String[] args) {
        DoubleItService service = new DoubleItService();
        DoubleItPortType port = service.getDoubleItPort();           

        doubleIt(port, 10);
        doubleIt(port, 0);
        doubleIt(port, -10);
    } 
    
    public static void doubleIt(DoubleItPortType port, int numToDouble) {       
        try {
            DataHandler dh =  port.doubleIt(numToDouble);
            String filename = "DoubleIt" + numToDouble + ".pdf";          
            // open pdf, save as a file.
            File file = new File(filename);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            dh.writeTo(fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();                   
            System.out.println(filename + " has been saved locally.");
        } catch (Exception e) {
            System.out.println("System Exception: " +  e.getMessage());
        }

    }
}

