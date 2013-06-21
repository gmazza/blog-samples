package entities;

import javax.persistence.EntityManager;
import org.junit.Test;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DepartmentTestIT {

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
    public void departmentTest() throws Exception {
    	Department dpNew = new Department();
        em.getTransaction().begin();
        dpNew.setName("Bob's Department");
        dpNew.setLocation("Baltimore");
        em.persist(dpNew);
        em.getTransaction().commit();
    	int newId = dpNew.getId();
    	
        // test earlier persist
    	Department dpLoad1 = em.find(Department.class, newId);
        assertEquals(dpLoad1.getId(), newId);
        assertEquals("Insert didn't work", dpLoad1.getName(), "Bob's Department");
        assertEquals("Insert didn't work", dpLoad1.getLocation(), "Baltimore");
        
        // test update
        em.getTransaction().begin();
        dpLoad1.setName("Phil's Department");
        dpLoad1.setLocation("Philadelphia");
        em.getTransaction().commit();
        Department dpLoad2 = em.find(Department.class, newId);
        assertEquals(dpLoad2.getId(), newId);
        assertEquals("Update didn't work", dpLoad2.getName(), "Phil's Department");
        assertEquals("Update didn't work", dpLoad2.getLocation(), "Philadelphia");
        
        em.getTransaction().begin();
        em.remove(dpLoad1);
        em.getTransaction().commit();
        
        Department shouldBeNull = em.find(Department.class, newId);
        assertNull("Delete didn't work", shouldBeNull);
    }
}
