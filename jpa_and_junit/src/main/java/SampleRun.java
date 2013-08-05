import java.util.Properties;
import java.util.Set;
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
public class SampleRun {

    // used by standalone Java client
    public static void main (String[] args) throws Exception {
        Properties emfProps = new Properties();

        String driver, url;
        // Starting Derby in Network (multi JVM) mode:
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
        System.out.println("****DP Before: " + dp.getId());  // 0
        em.persist(dp);
        // JPA Diff #1: EclipseLink requires a commit to get new dp.id; persist(dp)
        // alone will get the new dp.id value for Hibernate & OpenJPA.
        em.getTransaction().commit();
        System.out.println("****DP After: " + dp.getId());  // non-zero

        em.getTransaction().begin();
        Employee emp1 = new Employee();
        // add dp ID to satisfy uniqueness constraint
        emp1.setLastName("Smith" + dp.getId());
        emp1.setFirstName("Bill");
        emp1.setDepartment(dp);
        em.persist(emp1);

        /*
           JPA Diff #2: With Hibernate, a refresh of the parent after the child is persisted
           updates the parent with the child's info; EL & OpJPA require a commit of the child
           for the parent to be updated.
        */
        System.out.println("After Pst = " + dp.getEmployees().size()); // ALL: 0
        em.refresh(dp);
        System.out.println("After Ref1 = " + dp.getEmployees().size()); // HB: 1; EL & OpJPA: 0
        em.getTransaction().commit();
        System.out.println("After Cmt = " + dp.getEmployees().size()); // HB: 1; EL & OpJPA: 0
        em.refresh(dp);
        System.out.println("After Ref2 = " + dp.getEmployees().size()); // ALL: 1

        /*
        JPA Difference #3:  Hibernate:  If a child entity is removed via EntityManager.remove(child)
        while a copy remains in a managed parent object, the child object will remain in the parent however
        it will no longer be a managed entity instance (EntityManager.contains(parent.getChild()) = false).
        However, upon committing the transaction the child object in the parent becomes persisted
        again, resulting in the object not being deleted from the database.  With Hibernate, it's not
        sufficient to call remove(child), the child must also be manually deleted from the parent
        object prior to calling EntityManager.getTransaction().commit().

        With EclipseLink, upon committing the removal of the child entity from the EntityManager,
        EclipseLink will automatically delete the child from the parent when you call EntityManager.refresh(parent).
        Prior to calling refresh() it remains in the parent object as an unmanaged entity.

        OpenJPA behaves like Hibernate except that it requires the manual removal of the child instance
        from the parent between EntityManager.remove(child) and EntityManager.refresh(parent).  Otherwise it
        will throw an exception when calling the latter about an unmanaged entity remaining in the parent object:
        OpenJPA Exception: Object "entities.Employee@913f000" is not managed by this context
        at org.apache.openjpa.persistence.EntityManagerImpl.processArgument(EntityManagerImpl.java:1433)
        at org.apache.openjpa.kernel.BrokerImpl.processArgument(BrokerImpl.java:2304)
        at org.apache.openjpa.kernel.BrokerImpl.gatherCascadeRefresh(BrokerImpl.java:3025)
        */
        for (Employee e : dp.getEmployees()) {
            // All stacks show the lone employee object to be managed (contains(e) = true)
            System.out.println("Emp1 from Dept = " + e.getId() + "; is Managed: " + em.contains(e));
        }
        // Querying the emp object directly
        System.out.println("Emp1 = " + emp1.getId() + "; is Managed: " + em.contains(emp1));
        em.getTransaction().begin();
        em.remove(emp1);
        // All stacks now show the lone employee object to be unmanaged (contains(e) = false)
        System.out.println("Emp1 after removal, is Managed: " + em.contains(emp1));
        // Below needed only with HB & OpenJPA (see descr above); fine to do but not necessary with EL
        for (Employee e : dp.getEmployees()) {
            dp.getEmployees().remove(e);
        }
        em.getTransaction().commit();
        em.refresh(dp);
        em.close();
    }

}
