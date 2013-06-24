package entities;

import javax.persistence.EntityManager;
import org.junit.Test;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class EmployeeTestIT {

    static EntityManager em;
    
    @BeforeClass
    public static void oneTimeSetUp() {
        em = JPATestUtils.createEntityManager();
    }
 
    @AfterClass
    public static void oneTimeTearDown() {
        em.close();
    }
 
    @Test
    public void employeeTest() throws Exception {
        // sample departments to use
    	Department dpNew1 = new Department();
        em.getTransaction().begin();
        dpNew1.setName("William's Department");
        dpNew1.setLocation("Washington");
        em.persist(dpNew1);
        em.getTransaction().commit();
    	int newDeptId1 = dpNew1.getId();
    	
    	Department dpNew2 = new Department();
        em.getTransaction().begin();
        dpNew2.setName("Nick's Department");
        dpNew2.setLocation("New York");
        em.persist(dpNew2);
        em.getTransaction().commit();
    	int newDeptId2 = dpNew2.getId();

    	Employee empNew = new Employee();
        em.getTransaction().begin();
        empNew.setFirstName("Sam");
        empNew.setLastName("Smith");
        empNew.setDepartment(dpNew1);
        em.persist(empNew);
        em.getTransaction().commit();
    	int newId = empNew.getId();
    	
    	// test earlier persist
    	Employee empLoad1 = em.find(Employee.class, newId);
        assertEquals(empLoad1.getId(), newId);
        assertEquals("Insert didn't work", empLoad1.getFirstName(), "Sam");
        assertEquals("Insert didn't work", empLoad1.getLastName(), "Smith");
        assertEquals("Insert didn't work", empLoad1.getDepartment().getId(), newDeptId1);
        assertEquals("Insert didn't work", empLoad1.getDepartment().getName(), "William's Department");
        assertEquals("Insert didn't work", empLoad1.getDepartment().getLocation(), "Washington");
        
        // test update
        em.getTransaction().begin();
        empLoad1.setFirstName("James");
        empLoad1.setLastName("Jones");
        empLoad1.setDepartment(dpNew2);
        em.getTransaction().commit();
        Employee empLoad2 = em.find(Employee.class, newId);
        assertEquals("Update didn't work", empLoad2.getFirstName(), "James");
        assertEquals("Update didn't work", empLoad2.getLastName(), "Jones");
        assertEquals("Update didn't work", empLoad2.getDepartment().getId(), newDeptId2);
        assertEquals("Update didn't work", empLoad2.getDepartment().getName(), "Nick's Department");
        assertEquals("Update didn't work", empLoad2.getDepartment().getLocation(), "New York");
        
        em.getTransaction().begin();
        em.remove(empLoad1);
        em.getTransaction().commit();
        
        Employee shouldBeNull = em.find(Employee.class, newId);
        assertNull("Employee delete didn't work", shouldBeNull);
        Department shouldNotBeNull1 = em.find(Department.class, newDeptId1);
        assertNotNull("Department #1 delete shouldn't occur", shouldNotBeNull1);
        Department shouldNotBeNull2 = em.find(Department.class, newDeptId2);
        assertNotNull("Department #2 delete shouldn't occur", shouldNotBeNull2);
    }
}
