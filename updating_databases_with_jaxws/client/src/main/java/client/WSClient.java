package client;

import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.ws.soap.SOAPFaultException;

import org.example.contract.employee.DataProcessingFault;
import org.example.contract.employee.EmployeePortType;
import org.example.contract.employee.EmployeeService;
import org.example.schema.employee.ArrayOfEmployeeRecordType;
import org.example.schema.employee.DepartmentIdType;
import org.example.schema.employee.EmployeeIdType;
import org.example.schema.employee.EmployeeRecordType;
import org.example.schema.employee.GenderType;

public class WSClient {
    public static void main (String[] args) {
        EmployeeService service = new EmployeeService();
        EmployeePortType port = service.getEmployeePort();

        try {
            DatatypeFactory df = DatatypeFactory.newInstance();
            EmployeeRecordType ert = new EmployeeRecordType();
            EmployeeIdType eit = new EmployeeIdType();
            DepartmentIdType dit = new DepartmentIdType();
            GregorianCalendar cal = new GregorianCalendar();
            
            try {
                System.out.println("** getEmployeeByEmpId() - good Empl ID:");
                eit = new EmployeeIdType();
                eit.setEmployeeId(3);
                ert =  port.getEmployeeByEmpId(eit);
                printRecord(ert);
                System.out.println("** getEmployeeByEmpId() - bad Empl ID:");
                eit.setEmployeeId(300);
                ert =  port.getEmployeeByEmpId(eit);
                printRecord(ert);
            } catch (DataProcessingFault dpf) {
                printDataProcessingFault(dpf);
            }
            
            dit.setDepartmentId(1);
            ArrayOfEmployeeRecordType aert =  port.getEmployeesByDeptId(dit);
            List<EmployeeRecordType> lert = aert.getEmployeeRecord();
            System.out.println("\n\n** getEmployeesByDeptId() - Multirow return: ");
            for (EmployeeRecordType ert2 : lert) {
                printRecord(ert2);
            }

            dit.setDepartmentId(300);
            aert =  port.getEmployeesByDeptId(dit);
            lert = aert.getEmployeeRecord();
            System.out.println("** getEmployeesByDeptId() - Zero row return");
            for (EmployeeRecordType ert2 : lert) {
                printRecord(ert2);
            }
                       
            try {
                ert.setFirstName("Alice");
                ert.setLastName("Green");
                ert.setGender(GenderType.fromValue("F"));
                ert.setSalary(62000.50f);
                dit.setDepartmentId(4);
                ert.setDepartmentId(dit.getDepartmentId());
                cal.clear();
                cal.set(2005, 4, 12);
                ert.setHiredate(df.newXMLGregorianCalendar(cal));
                System.out.println("\n\nAdding a new user:"); 
                eit = port.addEmployee(ert);
                ert.setEmployeeId(eit.getEmployeeId());
                // query user
                System.out.println("Querying new user's details:"); 
                ert =  port.getEmployeeByEmpId(eit);
                printRecord(ert);            
            } catch (DataProcessingFault dpf) {
                printDataProcessingFault(dpf);
            }
            
            try {
                ert.setFirstName("Sally");
                ert.setLastName("Brown");
                ert.setGender(GenderType.fromValue("F"));
                ert.setSalary(72000.75f);
                dit.setDepartmentId(2);
                ert.setDepartmentId(dit.getDepartmentId());
                cal.clear();
                cal.set(2006, 5, 13);
                ert.setHiredate(df.newXMLGregorianCalendar(cal));
                System.out.println("\n\nUpdating the user w/empl ID = " 
                        + eit.getEmployeeId() + ":");
                port.updateEmployee(ert);
                System.out.println("Querying the same user's details:");
                ert =  port.getEmployeeByEmpId(eit);
                printRecord(ert);            
            } catch (DataProcessingFault dpf) {
                printDataProcessingFault(dpf);
            }

            try {
                System.out.println("\n\nDeleting the user w/empl ID = " 
                        + eit.getEmployeeId() + ":");
                port.deleteEmployee(eit);
                System.out.println("Trying to requery same user - should fail now:");
                ert =  port.getEmployeeByEmpId(eit);
                printRecord(ert);            
            } catch (DataProcessingFault dpf) {
                printDataProcessingFault(dpf);
            }
                                   
        } catch (SOAPFaultException e) {
            System.out.println("Exception from web service: " +  e.getMessage());
        } catch (Exception e) {
            System.out.println("Basic Exception: " +  e.getMessage());
        }                
    }
    
    private static void printRecord(EmployeeRecordType ert) {
        System.out.println("**** Employee record data: ");
        System.out.println("Empl Id: " + ert.getEmployeeId());
        System.out.println("First Name: " + ert.getFirstName());
        System.out.println("Last Name: " + ert.getLastName());
        System.out.println("Gender: " + ert.getGender().value());
        System.out.println("Dept Id: " + ert.getDepartmentId());
        System.out.println("Hiredate: " + ert.getHiredate().getYear() + "-" + 
                ert.getHiredate().getMonth() + "-" + ert.getHiredate().getDay());
        if (ert.getSalary() != null) {
            System.out.println("Salary: " + ert.getSalary());
        }
    }
    
    private static void printDataProcessingFault(final DataProcessingFault dpf) {
        System.out.println("**** Data Processing Fault: ");
        System.out.println("Message: " + dpf.getMessage());
        System.out.println("Details: " + dpf.getFaultInfo().getErrorDetails());
    }      
    
}
