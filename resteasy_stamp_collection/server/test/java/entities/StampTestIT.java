package entities;

import javax.persistence.EntityManager;
import org.junit.Test;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class StampTestIT {

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
    public void stampTest() throws Exception {
    	Stamp stampNew = new Stamp();
        em.getTransaction().begin();
        stampNew.setId("4");
        stampNew.setDescription("Stamp Description");
        // stampNew.setImage(...);
        em.persist(stampNew);
        em.getTransaction().commit();
    	int newId = stampNew.getId();
    	
        // test earlier persist
    	Stamp stampLoad1 = em.find(Stamp.class, newId);
        assertEquals(stampLoad1.getId(), newId);
        assertEquals("Description didn't persist correctly", stampLoad1.getDescription(), "Stamp Description");
        // assertEquals("Image didn't persist correctly", stampLoad1.getImage().size(), 5000L);
        
        // test update
        em.getTransaction().begin();
        stampLoad1.setName("New Description");
        // stampLoad1.setImage(...);
        em.getTransaction().commit();
        Stamp stampLoad2 = em.find(Stamp.class, newId);
        assertEquals(stampLoad2.getId(), newId);
        assertEquals("Update to description failed", stampLoad2.getDescription(), "New Description");
//      assertEquals("Update to image failed", stampLoad2.getImage().size(), 8000L);
        
        em.getTransaction().begin();
        em.remove(stampLoad1);
        em.getTransaction().commit();
        
        Stamp shouldBeNull = em.find(Stamp.class, newId);
        assertNull("Delete didn't work", shouldBeNull);
    }
}
