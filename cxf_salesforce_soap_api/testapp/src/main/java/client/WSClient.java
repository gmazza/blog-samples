package client;

import com.exacttarget.wsdl.partnerapi.PartnerAPI;
import com.exacttarget.wsdl.partnerapi.Soap;
import com.mycompany.datafolder.FolderGenerator;

public class WSClient {

    public static void main (String[] args) {

        try {
            PartnerAPI service = new PartnerAPI();
            Soap stub = service.getSoap();
            FolderGenerator.generateFolders(new Integer(args[0]), stub);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
