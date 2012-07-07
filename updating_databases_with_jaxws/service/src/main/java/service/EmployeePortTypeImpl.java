package service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import org.example.contract.employee.DataProcessingFault;
import org.example.contract.employee.EmployeePortType;
import org.example.schema.employee.ArrayOfEmployeeRecordType;
import org.example.schema.employee.BasicFault;
import org.example.schema.employee.DepartmentIdType;
import org.example.schema.employee.EmployeeIdType;
import org.example.schema.employee.EmployeeRecordType;
import org.example.schema.employee.EmptyResponse;
import org.example.schema.employee.GenderType;

@javax.jws.WebService(portName = "EmployeePort", serviceName = "EmployeeService", 
        targetNamespace = "http://www.example.org/contract/Employee", 
        endpointInterface = "org.example.contract.employee.EmployeePortType")
public class EmployeePortTypeImpl implements EmployeePortType {

    private static Connection dbConn;

    private static DatatypeFactory df;

    static {
        try {
            DriverManager
                    .registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());

            dbConn = DriverManager.getConnection(
            /*
             * Enter connection string for database directory below. Examples:
             * "jdbc:derby:c:\myfolder\EMPLDB" (Windows)
             * "jdbc:derby:dbdir/EMPLDB" (Linux)
             * Derby ignores password by default.
             */
            "jdbc:derby:/media/Ext4Data/workspace/blog-samples/updating_databases_with_jaxws/EMPLDB", "APP", "PASSWORD"); 

            df = DatatypeFactory.newInstance();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException(
                    "Database connection cannot be made: " + "Code: "
                            + e.getErrorCode() + "; Message: " + e.getMessage());
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException(
                    "DatatypeFactory cannot be instantiated.");
        }
    }

    public EmployeeRecordType getEmployeeByEmpId(EmployeeIdType eit)
            throws DataProcessingFault {

        checkNotEmpty(eit.getEmployeeId(), "Employee ID");

        String sqlStr = "select last_name, first_name, gender, dept_id,"
                + "hiredate, salary from employee where empl_id = ?";

        try {
            PreparedStatement pstmt = dbConn.prepareStatement(sqlStr);
            pstmt.setInt(1, eit.getEmployeeId());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                EmployeeRecordType ert = new EmployeeRecordType();
                DepartmentIdType dit = new DepartmentIdType();
                dit.setDepartmentId(rs.getInt("dept_id"));

                ert.setEmployeeId(eit.getEmployeeId());
                ert.setDepartmentId(dit.getDepartmentId());
                ert.setFirstName(rs.getString("first_name"));
                ert.setLastName(rs.getString("last_name"));
                ert.setGender(GenderType.fromValue(rs.getString("gender")));
                ert.setHiredate(sqlDateToXMLCal(rs.getDate("hiredate")));
                ert.setSalary(rs.getFloat("salary"));
                rs.close();
                pstmt.close();
                return ert;
            } else {
                BasicFault bf = new BasicFault();
                bf.setErrorDetails("Id attempted to retrieve: "
                        + eit.getEmployeeId());
                throw new DataProcessingFault("Entry Not Found", bf);
            }
        } catch (SQLException e) {
            String errorText = "System error on getEmployeeByEmpId("
                    + eit.getEmployeeId() + ") call: ";
            throw createSOAPFaultException(errorText + e.getMessage());
        }
    }

    public ArrayOfEmployeeRecordType getEmployeesByDeptId(DepartmentIdType dit) {

        ArrayOfEmployeeRecordType aert = new ArrayOfEmployeeRecordType();
        List<EmployeeRecordType> lert = aert.getEmployeeRecord();

        String sqlStr = "select empl_id, last_name, first_name, gender, "
                + "hiredate, salary from employee where dept_id = ?";

        try {
            checkNotEmpty(dit.getDepartmentId(), "Department ID");
            PreparedStatement pstmt = dbConn.prepareStatement(sqlStr);
            pstmt.setInt(1, dit.getDepartmentId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                EmployeeRecordType ert = new EmployeeRecordType();
                EmployeeIdType eit = new EmployeeIdType();
                eit.setEmployeeId(rs.getInt("empl_id"));

                ert.setEmployeeId(eit.getEmployeeId());
                ert.setDepartmentId(dit.getDepartmentId());
                ert.setFirstName(rs.getString("first_name"));
                ert.setLastName(rs.getString("last_name"));
                ert.setGender(GenderType.fromValue(rs.getString("gender")));
                ert.setHiredate(sqlDateToXMLCal(rs.getDate("hiredate")));
                ert.setSalary(rs.getFloat("salary"));
                lert.add(ert);
            }
            rs.close();
            pstmt.close();
            return aert;
        } catch (Exception e) {
            String errorText = "System error on getEmployeesByDeptId("
                    + dit.getDepartmentId() + ") call: ";
            throw createSOAPFaultException(errorText + e.getMessage());
        }
    }

    public EmployeeIdType addEmployee(EmployeeRecordType ert)
            throws DataProcessingFault {

        try {
            checkEmployeeRecordType(ert);

            String sqlStr = "insert into employee(first_name, "
                    + "last_name, gender, dept_id, hiredate, salary) values "
                    + "(?, ?, ?, ?, ?, ?)";

            PreparedStatement pstmt = dbConn.prepareStatement(sqlStr);
            pstmt.setString(1, ert.getFirstName().toUpperCase());
            pstmt.setString(2, ert.getLastName().toUpperCase());
            pstmt.setString(3, ert.getGender().name());
            pstmt.setInt(4, ert.getDepartmentId());

            java.util.Calendar cal = new java.util.GregorianCalendar();
            cal.clear();
            cal.set(ert.getHiredate().getYear(),
                    ert.getHiredate().getMonth() - 1, ert.getHiredate()
                            .getDay());

            pstmt.setDate(5, new java.sql.Date(cal.getTimeInMillis()));
            if (ert.getSalary() != null) {
                pstmt.setFloat(6, ert.getSalary());
            } else {
                pstmt.setNull(6, java.sql.Types.FLOAT);
            }

            pstmt.execute();
            dbConn.commit();

            /*
             * Next, need to retrieve Employee ID to send back to client. Can
             * use "select IDENTITY_VAL_LOCAL() from sysibm.sysdummy1;" with
             * Derby, but that is less threadsafe, so will rely on unique key on
             * {first_name, last_name} to retrieve Employee ID instead.
             */
            sqlStr = "select empl_id from employee where first_name = ? and last_name = ?";
            pstmt = dbConn.prepareStatement(sqlStr);
            pstmt.setString(1, ert.getFirstName().toUpperCase());
            pstmt.setString(2, ert.getLastName().toUpperCase());
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            int emplId = rs.getInt(1);
            EmployeeIdType eit = new EmployeeIdType();
            eit.setEmployeeId(emplId);
            return eit;
        } catch (SQLException e) {
            BasicFault bf = new BasicFault();
            String errorMsg;
            if ("23503".equals(e.getSQLState())) {
                errorMsg = "Unknown Department ID";
            } else if ("22001".equals(e.getSQLState())) {
                errorMsg = "Invalid column value supplied";
            } else if ("23505".equals(e.getSQLState())) {
                errorMsg = "{First name, Last name} must be unique in database";
            } else {
                errorMsg = "SQLState: " + e.getSQLState() + "; Message: "
                        + e.getMessage();
            }
            bf.setErrorDetails(errorMsg);
            throw new DataProcessingFault(
                    "Error while trying to add employee record", bf);
        }
    }

    public EmptyResponse updateEmployee(EmployeeRecordType ert)
            throws DataProcessingFault {
        try {
            checkEmployeeRecordType(ert);
            checkEmployeeIDExists(ert.getEmployeeId());

            String sqlStr = "update employee set first_name = ?, "
                    + "last_name = ?, gender = ?, dept_id = ?, " 
                    + "hiredate = ?, salary = ? "
                    + "where empl_id = ? ";

            PreparedStatement pstmt = dbConn.prepareStatement(sqlStr);
            pstmt.setString(1, ert.getFirstName().toUpperCase());
            pstmt.setString(2, ert.getLastName().toUpperCase());
            pstmt.setString(3, ert.getGender().name());
            pstmt.setInt(4, ert.getDepartmentId());

            java.util.Calendar cal = new java.util.GregorianCalendar();
            cal.clear();
            cal.set(ert.getHiredate().getYear(),
                    ert.getHiredate().getMonth() - 1, ert.getHiredate()
                            .getDay());

            pstmt.setDate(5, new java.sql.Date(cal.getTimeInMillis()));
            if (ert.getSalary() != null) {
                pstmt.setFloat(6, ert.getSalary());
            } else {
                pstmt.setNull(6, java.sql.Types.FLOAT);
            }

            pstmt.setInt(7, ert.getEmployeeId());
            pstmt.execute();
            dbConn.commit();
        } catch (SQLException e) {
            BasicFault bf = new BasicFault();
            String errorMsg;
            if ("23503".equals(e.getSQLState())) {
                errorMsg = "Unknown Department ID";
            } else if ("22001".equals(e.getSQLState())) {
                errorMsg = "Invalid column value supplied";
            } else if ("23505".equals(e.getSQLState())) {
                errorMsg = "{First name, Last name} must be unique in database";
            } else {
                errorMsg = "SQLState: " + e.getSQLState() + "; Message: "
                        + e.getMessage();
            }
            bf.setErrorDetails(errorMsg);
            throw new DataProcessingFault(
                    "Error while trying to update employee record", bf);
        }
        return null;
    }

    public EmptyResponse deleteEmployee(EmployeeIdType eit)
            throws DataProcessingFault {

        checkEmployeeIDExists(eit.getEmployeeId());

        try {
            String sqlStr = "delete from employee where empl_id = ? ";
            PreparedStatement pstmt = dbConn.prepareStatement(sqlStr);
            pstmt.setInt(1, eit.getEmployeeId());
            pstmt.execute();
            dbConn.commit();
        } catch (SQLException e) {
            BasicFault bf = new BasicFault();
            String errorMsg = "SQLState: " + e.getSQLState() + "; Message: "
                    + e.getMessage();
            bf.setErrorDetails(errorMsg);
            throw new DataProcessingFault(
                    "Error while trying to delete employee record", bf);
        }
        return null;
    }

    static XMLGregorianCalendar sqlDateToXMLCal(java.sql.Date sqlDate) {
        java.util.Date jDate = new java.util.Date(sqlDate.getTime());
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(jDate);
        return df.newXMLGregorianCalendar(calendar);
    }

    static void checkEmployeeRecordType(EmployeeRecordType ert)
            throws DataProcessingFault {
        checkNotEmpty(ert.getFirstName(), "First Name");
        checkNotEmpty(ert.getLastName(), "Last Name");
        checkNotEmpty(ert.getDepartmentId(), "Department Id");
        checkNotEmpty(ert.getHiredate(), "Hiredate");
    }

    static void checkEmployeeIDExists(Integer employeeId)
            throws DataProcessingFault {
        try {
            checkNotEmpty(employeeId, "Employee Id");

            String sqlStr = "select count(*) from employee "
                    + "where empl_id = ? ";

            PreparedStatement pstmt = dbConn.prepareStatement(sqlStr);
            pstmt.setInt(1, employeeId.intValue());

            ResultSet rs = pstmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            if (count == 0) {
                BasicFault bf = new BasicFault();
                bf.setErrorDetails("Employee Id provided: "
                        + employeeId.intValue());
                DataProcessingFault dpf = new DataProcessingFault(
                        "No employee record with given ID found", bf);
                throw dpf;
            }
        } catch (SQLException e) {
            BasicFault bf = new BasicFault();
            String errorMsg = "SQLState: " + e.getSQLState() + "; Message: "
                    + e.getMessage();
            bf.setErrorDetails(errorMsg);
            throw new DataProcessingFault("SQL Error: ", bf);
        }
    }

    static void checkNotEmpty(Object valToCheck, String fieldName)
            throws DataProcessingFault {
        if (valToCheck == null) {
            BasicFault bf = new BasicFault();
            DataProcessingFault dpf = new DataProcessingFault(
                    "Required Value Missing", bf);
            bf.setErrorDetails("Missing " + fieldName);
            throw dpf;
        }
    }

    /*
     * Convenience function used to generate a generic SOAPFaultException (Used
     * for SOAP errors not specifically declared with a wsdl:fault.)
     */
    private static SOAPFaultException createSOAPFaultException(
            String faultString) {
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
}
