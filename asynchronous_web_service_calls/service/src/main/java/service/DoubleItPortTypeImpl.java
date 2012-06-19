package service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import org.example.contract.doubleit.CorrelationIdNotFoundFault;
import org.example.contract.doubleit.DoubleItPortType;
import org.example.schema.doubleit.BasicFault;
import org.example.schema.doubleit.GetDoubleItResults;
import org.example.schema.doubleit.GetDoubleItResultsResponse;
import org.example.schema.doubleit.SubmitDoubleIt;
import org.example.schema.doubleit.SubmitDoubleItResponse;

@WebService(targetNamespace = "http://www.example.org/contract/DoubleIt", 
portName = "DoubleItPort",
serviceName = "DoubleItService", 
endpointInterface = "org.example.contract.doubleit.DoubleItPortType")
public class DoubleItPortTypeImpl implements DoubleItPortType {

    private static Connection dbConn;

    private static String internalErrorMessage;

    /*
     * This web service operation accepts the time-consuming job request,
     * initiates the job's processing on a separate thread, and returns a
     * correlation ID that the SOAP client. This ID will be used when calling
     * getDoubleItResults() below to check the job's status.
     */
    public SubmitDoubleItResponse submitDoubleIt(SubmitDoubleIt parameters) {
        SubmitDoubleItResponse sdir = new SubmitDoubleItResponse();

        String corrId = null;
        try {
            Statement stmt = dbConn.createStatement();
            stmt
                    .execute("insert into doubleit_results(complete_ind, result) values "
                            + "('N', NULL)");
            dbConn.commit();
            stmt.close();

            // see http://tinyurl.com/35r2oq for info on I_V_L() function
            String sqlStr = "SELECT IDENTITY_VAL_LOCAL() as corr_id "
                    + "from APP.doubleit_results ";

            PreparedStatement pstmt = dbConn.prepareStatement(sqlStr);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            corrId = rs.getString("corr_id"); // I_V_L() returns String

            if (corrId == null || "".equals(corrId)) {
                // see $CATALINA_HOME/logs/catalina.out to view println()
                // messages
                System.out.println("Error Determining Correlation ID");
                throw createSOAPFaultException(internalErrorMessage);
            }
            sdir.setCorrelationId(new Integer(corrId).intValue());
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("SQL Error in submitDoubleIt(): "
                    + e.getMessage());
            throw createSOAPFaultException(internalErrorMessage);
        }

        // initiate separate thread for processing
        DoublerThread dt = new DoublerThread(new Integer(corrId).intValue(),
                parameters);
        dt.start();
        return sdir;
    }

    /*
     * This web service operation is meant to be polled repeatedly until the
     * DoublerThread is complete. It returns the job status, and if complete,
     * the results (sum).
     */
    public GetDoubleItResultsResponse getDoubleItResults(
            GetDoubleItResults parameters) throws CorrelationIdNotFoundFault {
        int corrId = parameters.getCorrelationId();
        int doubledNumber;
        String isComplete;

        try {
            String sqlStr = "SELECT complete_ind, result "
                    + " from APP.doubleit_results "
                    + " where correlation_id = " + corrId;

            PreparedStatement pstmt = dbConn.prepareStatement(sqlStr);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            doubledNumber = rs.getInt("result");
            isComplete = rs.getString("complete_ind");

        } catch (SQLException e) {
            System.out
                    .println("getDoubleItResults(): CorrelationIdNotFoundFault thrown");
            System.out.println("SQL Error message: " + e.getMessage());
            BasicFault bf = new BasicFault();
            bf.setErrorDetails("" + corrId);
            throw new CorrelationIdNotFoundFault("Correlation ID " + corrId
                    + " not found.", bf);
        }

        GetDoubleItResultsResponse garr = new GetDoubleItResultsResponse();
        garr.setCorrelationId(corrId);

        if ("Y".equals(isComplete)) {
            garr.setDoubledNumber(doubledNumber);
            garr.setComplete(true);
        } else {
            garr.setComplete(false);
        }
        return garr;
    }

    /*
     * This separate thread handles the time-consuming processing of the web
     * service request, and updates a row in a database table when complete.
     */
    public static class DoublerThread extends Thread {

        int corrId;

        SubmitDoubleIt vals;

        public DoublerThread(int corrId, SubmitDoubleIt vals) {
            this.corrId = corrId;
            this.vals = vals;
        }

        public void run() {
            // 12 seconds delay to simulate slow processing
            try {
                sleep(12000);
            } catch (Exception e) {
            }
            int answer = vals.getNumberToDouble() * 2;
            try {
                Statement stmt = dbConn.createStatement();
                stmt
                        .execute("update doubleit_results set complete_ind = 'Y', result="
                                + answer + " where correlation_id = " + corrId);
                dbConn.commit();
            } catch (SQLException e) { // should never happen
                System.out.println("SQL Error in DoublerThread.run(): "
                        + e.getMessage());
            }
        }
    }

    /*
     * Convenience function used to generate a generic SOAPFaultException (Used
     * for rare or inexplicable service-side errors not specifically declared
     * via a wsdl:fault in the WSDL.)
     */
    private SOAPFaultException createSOAPFaultException(String faultString) {
        try {
            SOAPFault fault = SOAPFactory.newInstance().createFault();
            fault.setFaultString(faultString);
            fault.setFaultCode(new QName(SOAPConstants.URI_NS_SOAP_ENVELOPE,
                    "Server"));
            return new SOAPFaultException(fault);
        } catch (SOAPException e) {
            // do nothing
        }
        return null;
    }

    /*
     * Static startup method that does a one-time database connection
     * initialization for all web service calls.
     */
    static {
        internalErrorMessage = "Internal error while handling request";

        try {
            DriverManager
                    .registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());

            dbConn = DriverManager.getConnection(
            /*
             * Enter connection string for database directory below. Because service
             * is accessing DB from a webapp, Derby embedded mode (single JVM) URL string
             * needs to be used.
             * Examples:
             * "jdbc:derby:c:\myfolder\STATUSDB" (Windows)
             * "jdbc:derby:dbdir/STATUSDB" (Linux)
             */
            "jdbc:derby:/home/gmazza/STATUSDB", "APP",
                    "PASSWORD"); // Derby ignores password by default
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException(
                    "Database connection cannot be made: " + "Code: "
                            + e.getErrorCode() + "; Message: " + e.getMessage());
        }
    }
}
