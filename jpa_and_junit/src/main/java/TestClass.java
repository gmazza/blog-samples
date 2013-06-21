import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import entities.Department;
import entities.Employee;

/* Information on Derby:
   http://www.jroller.com/gmazza/entry/apache_derby_setup

   Create Derby DB via:
   ij> connect 'jdbc:derby:/media/work1/EMPLDB;create=true';
   ij> run 'src/main/config/create-database-derby.sql';

   Before runs, activate Derby DB via:
   % startNetworkServer
   Can access database during runs via:
   ij> connect 'jdbc:derby://localhost:1527//media/work1/EMPLDB';
*/
public class TestClass {

    // used by standalone Java client
    public static void main (String[] args) throws Exception {
        Properties emfProps = new Properties();

        String driver, url;
        /* Starting Derby in Network (multi JVM) mode: */
        driver = "org.apache.derby.jdbc.ClientDriver";
        url = "jdbc:derby://localhost:1527//media/work1/EMPLDB";

        emfProps.setProperty("javax.persistence.jdbc.driver", driver);
        emfProps.setProperty("javax.persistence.jdbc.url", url); 
        emfProps.setProperty("javax.persistence.jdbc.user", "APP");
        emfProps.setProperty("javax.persistence.jdbc.password", "ANYTHING");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("CompanyPU", emfProps);
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Department dp = new Department();
        dp.setName("Glen's Department");
        dp.setLocation("Buffalo");
        System.out.println("****DP Before: " + dp.getId());
        em.persist(dp);
        // EclipseLink requires a commit to get new dp.id; OpenJPA doesn't.
        em.getTransaction().commit();
        System.out.println("****DP After: " + dp.getId());

        em.getTransaction().begin();
        Employee emp = new Employee();
        // add dp ID to satisfy uniqueness constraint
        emp.setLastName("Smith" + dp.getId());
        emp.setFirstName("Bill");
        emp.setDepartment(dp);
        em.persist(emp);

        em.getTransaction().commit();
        em.close();
    } 
    
}
