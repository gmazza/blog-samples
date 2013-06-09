import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import entities.Department;

/* Create Derby database via: 
   connect 'jdbc:derby:/media/work1/EMPLDB;create=true';
   run 'src/main/config/create-database.sql';
*/
public class TestClass {

    // used by standalone Java client
    public static void main (String[] args) throws Exception {
        Properties emfProps = new Properties();
        emfProps.setProperty("javax.persistence.jdbc.driver", "org.apache.derby.jdbc.EmbeddedDriver");
        emfProps.setProperty("javax.persistence.jdbc.url", "jdbc:derby:/media/work1/EMPLDB");
        emfProps.setProperty("javax.persistence.jdbc.user",  "APP");
        emfProps.setProperty("javax.persistence.jdbc.password", "ANYTHING");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("CompanyPU", emfProps);
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Department dp = new Department();
        dp.setId(1);
        dp.setName("Glen's Department");
        dp.setLocation("Buffalo");
        em.persist(dp);
        em.getTransaction().commit();
        em.close();
    } 
    
}
