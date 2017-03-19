package com.mycompany.datafolder;

import com.exacttarget.wsdl.partnerapi.ClientID;
import com.exacttarget.wsdl.partnerapi.ComplexFilterPart;
import com.exacttarget.wsdl.partnerapi.CreateOptions;
import com.exacttarget.wsdl.partnerapi.CreateRequest;
import com.exacttarget.wsdl.partnerapi.CreateResponse;
import com.exacttarget.wsdl.partnerapi.CreateResult;
import com.exacttarget.wsdl.partnerapi.DataFolder;
import com.exacttarget.wsdl.partnerapi.LogicalOperators;
import com.exacttarget.wsdl.partnerapi.PartnerAPI;
import com.exacttarget.wsdl.partnerapi.RetrieveRequest;
import com.exacttarget.wsdl.partnerapi.RetrieveRequestMsg;
import com.exacttarget.wsdl.partnerapi.RetrieveResponseMsg;
import com.exacttarget.wsdl.partnerapi.SimpleFilterPart;
import com.exacttarget.wsdl.partnerapi.SimpleOperators;
import com.exacttarget.wsdl.partnerapi.Soap;
import com.exacttarget.wsdl.partnerapi.UpdateOptions;
import com.exacttarget.wsdl.partnerapi.UpdateRequest;
import com.exacttarget.wsdl.partnerapi.UpdateResponse;
import com.exacttarget.wsdl.partnerapi.UpdateResult;
import com.mycompany.util.ClientPasswordCallback;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.handler.WSHandlerConstants;

import java.util.HashMap;
import java.util.Map;

public class FolderGenerator {

    /**
     * Activate SOAP calls by providing necessary login credentials
     * @param businessUnitId - BU/MemberID of Exact Target instance.
     * @param username - Exact Target username
     * @param password - Exact Target password
     */
    public static void generateFolders(int businessUnitId, String username, String password) {
        //Create PartnerAPI stub.
        PartnerAPI service = new PartnerAPI();
        Soap stub = service.getSoap();

        Client client = org.apache.cxf.frontend.ClientProxy.getClient(stub);
        Endpoint cxfEndpoint = client.getEndpoint();
        Map<String, Object> outProps = new HashMap<String, Object>();
        outProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
        outProps.put(WSHandlerConstants.USER, username);
        outProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
        ClientPasswordCallback cbc = new ClientPasswordCallback();
        cbc.setPassword(password);
        outProps.put(WSHandlerConstants.PW_CALLBACK_REF, cbc);
        WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps);
        cxfEndpoint.getOutInterceptors().add(wssOut);
        generateFolders(businessUnitId, stub);

    }

    /**
     * Activate SOAP calls via the given JAX-WS Port object (preconfigured with
     * necessary Username-Token security parameters)
     * @param businessUnitId - BU/MemberID of Exact Target instance
     * @param stub - Port object to make SOAP calls.
     */
    public static void generateFolders(int businessUnitId, Soap stub) {

        try {
            // Apache CXF-specific: if desired to see SOAP messages in console while running
            // Note out interceptor will output readable login credentials
            // org.apache.cxf.endpoint.Client client = org.apache.cxf.frontend.ClientProxy.getClient(stub);
            //client.getInInterceptors().add(new org.apache.cxf.interceptor.LoggingInInterceptor());
            //client.getOutInterceptors().add(new org.apache.cxf.interceptor.LoggingOutInterceptor());

            // Used to specify Business Unit (BU) for subsequent SOAP calls
            ClientID clientID = new ClientID();
            clientID.setID(businessUnitId); // MID value of the BU, passed in from pom.xml file

            // Create two new email folders under a new parent folder (Email Studio Content Tab)
            // Created under the standard "My Emails" folder present on all instances
            String rootFolderName = "My Emails";
            String parentFolderName = "ParentFolder";

            // Can also create folders under Subscription Lists or Data Extensions and others, e.g.:
            //rootFolderName = "My Lists";
            //rootFolderId = getFolderId(stub, clientID, rootFolderName, "list");
            //rootFolderName = "Data Extensions";
            //rootFolderId = getFolderId(stub, clientID, rootFolderName, "dataextension");

            int rootFolderId = getFolderId(stub, clientID, rootFolderName, "email");

            String[] emailFolders = {"TestFolder1", "TestFolder2"};

            if (rootFolderId != -1) {
                // Create parent folder
                int parentFolderId =  createFolder(stub, clientID, rootFolderId, "email", parentFolderName, true);
                if (parentFolderId != -1) {
                    for (String folderName : emailFolders) {
                        createFolder(stub, clientID, parentFolderId, "email", folderName, true);
                    }
                } else {
                    System.out.println("Error creating " + parentFolderName + " on MID: " + clientID.getID());
                }
            } else {
                System.out.println("Error: Could not find " + rootFolderName + " folder on MID " + clientID.getID());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *  Create a folder, returning the Category ID of it.
     *
     *  @param stub - JAX-WS endpoint to make SOAP calls
     *  @param clientID - ClientID of business unit to create the folder in
     *  @param parentFolderId - Category ID of parent where the folder is to be created.
     *  @param contentType - Type of folder (email, list, etc.).  Possible values defined at link below.
     *  @param folderName - Name of new folder, must be unique per parent
     *  @param allowChildren - Allow for child folders to be created under it?
     *
     *  @return category ID of new folder created, or -1 if any error
     *
     *  See https://developer.salesforce.com/docs/atlas.en-us.noversion.mc-apis.meta/mc-apis/datafolder.htm
     */
    static private int createFolder(Soap stub, ClientID clientID, int parentFolderId, String contentType, String folderName,
                                    boolean allowChildren) {

        int newId = -1;

        try {
            DataFolder parentFolder = new DataFolder();
            parentFolder.setID(parentFolderId);

            DataFolder df = new DataFolder();
            df.setClient(clientID);
            df.setParentFolder(parentFolder);
            df.setName(folderName);
            // description oddly required so adding some boilerplate
            df.setDescription(folderName + " desc");
            df.setContentType(contentType);
            df.setIsEditable(true);
            df.setAllowChildren(allowChildren);

            CreateRequest cr = new CreateRequest();
            cr.getObjects().add(df);
            cr.setOptions(new CreateOptions());

            CreateResponse response = stub.create(cr);
            if (response.getResults().size() == 1 && "OK".equals(response.getResults().get(0).getStatusCode())) {
                newId = response.getResults().get(0).getNewID();
                System.out.println("Created folder " + folderName + " (ID: " + newId + ") under folder #" + parentFolderId);
            } else {
                for (CreateResult result : response.getResults()) {
                    System.out.println("Result = " + result.getStatusCode() + " " + result.getStatusMessage() + " " + result.getNewID());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newId;
    }

    /**
     *  Retrieve the ID for a folder by its name.
     *
     *  @param stub - JAX-WS endpoint to make SOAP calls
     *  @param clientID - ClientID of business unit to create the folder in
     *  @param folderName - Name of folder whose ID is being retrieved
     *  @param contentType - Type of folder (email, list, etc.).  Possible values defined at link below.
     *
     *  @return category ID if folder found, -1 if any error
     *
     *  See https://developer.salesforce.com/docs/atlas.en-us.noversion.mc-apis.meta/mc-apis/datafolder.htm
     */
    static int getFolderId(Soap stub, ClientID clientID, String folderName, String contentType) {
        int folderId = -1;

        try {
            RetrieveRequest req = new RetrieveRequest();
            req.getClientIDs().add(clientID);
            req.setObjectType("DataFolder");
            req.getProperties().add("ID");
            SimpleFilterPart sfp = new SimpleFilterPart();
            sfp.setProperty("Name");
            sfp.setSimpleOperator(SimpleOperators.EQUALS);
            sfp.getValue().add(folderName);
            SimpleFilterPart sfp2 = new SimpleFilterPart();
            sfp2.setProperty("ContentType");
            sfp2.setSimpleOperator(SimpleOperators.EQUALS);
            sfp2.getValue().add(contentType);
            ComplexFilterPart cfp = new ComplexFilterPart();
            cfp.setLeftOperand(sfp);
            cfp.setLogicalOperator(LogicalOperators.AND);
            cfp.setRightOperand(sfp2);
            req.setFilter(cfp);

            RetrieveRequestMsg rrMsg = new RetrieveRequestMsg();
            rrMsg.setRetrieveRequest(req);

            RetrieveResponseMsg response = stub.retrieve(rrMsg);
            if (response.getResults().size() == 1) {
                folderId = response.getResults().get(0).getID();
                System.out.println("Folder " + folderName + " has ID " + folderId);
            } else {
                System.out.println("Folder " + folderName + " has ID " + folderId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return folderId;
    }

    /**
     *  Adjust a folder's editable (can move/rename/delete) and/or allow children properties.  Useful for
     *  "locking" a folder after creating it and its child folders.
     *
     *  @param stub - JAX-WS endpoint to make SOAP calls
     *  @param folderId - Folder to adjust
     *  @param editable - Can edit folder?  Leave null to leave property unchanged.
     *  @param allowChildren - Can additional child folders (beyond whatever already present) be created
     *                       for this folder?  Leave null to keep as-is.
     *
     */
    static void updateEmailFolder(Soap stub, int folderId, Boolean editable, Boolean allowChildren) {

        try {
            DataFolder df = new DataFolder();
            df.setID(folderId);
            if (editable != null) {
                df.setIsEditable(editable);
            }
            if (allowChildren != null) {
                df.setAllowChildren(allowChildren);
            }

            UpdateRequest req = new UpdateRequest();
            req.getObjects().add(df);
            req.setOptions(new UpdateOptions());

            UpdateResponse response = stub.update(req);
            if (response.getResults().size() == 1 && "OK".equals(response.getResults().get(0).getStatusCode())) {
                System.out.println("Properties successfully updated on Folder #" + folderId);
            } else {
                for (UpdateResult result : response.getResults()) {
                    System.out.println("Result = " + result.getStatusCode() + " " + result.getStatusMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

