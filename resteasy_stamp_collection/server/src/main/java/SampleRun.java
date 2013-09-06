import java.util.Properties;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import entities.Stamp;
import java.io.FileOutputStream;

/* Information on Derby:
   http://www.jroller.com/gmazza/entry/apache_derby_setup

   Create Derby DB via:
   ij> connect 'jdbc:derby:/media/work1/STAMPDB;create=true';
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
//      driver = "org.apache.derby.jdbc.ClientDriver";
        driver ="com.mysql.jdbc.Driver";
//      url = "jdbc:derby://localhost:1527//media/work1/STAMPDB";
        url = "jdbc:mysql://localhost:3306/stampdb";

        emfProps.setProperty("javax.persistence.jdbc.driver", driver);
        emfProps.setProperty("javax.persistence.jdbc.url", url);
//        emfProps.setProperty("javax.persistence.jdbc.user", "APP");
//        emfProps.setProperty("javax.persistence.jdbc.password", "ANYTHING");
        emfProps.setProperty("javax.persistence.jdbc.user", "scott");
        emfProps.setProperty("javax.persistence.jdbc.password", "tiger");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("StampPU", emfProps);
        EntityManager em = emf.createEntityManager();

    	Stamp stampLoad1 = em.find(Stamp.class, "1");
        System.out.println("Stamp ID = " + stampLoad1.getId());
        System.out.println("Stamp Description = " + stampLoad1.getDescription());
        FileOutputStream fileOutputStream = new FileOutputStream("stamp1.png"); 
        fileOutputStream.write(stampLoad1.getImage());
        fileOutputStream.close();

    	stampLoad1 = em.find(Stamp.class, "2");
        System.out.println("Stamp ID = " + stampLoad1.getId());
        System.out.println("Stamp Description = " + stampLoad1.getDescription());
        fileOutputStream = new FileOutputStream("stamp2.png"); 
        fileOutputStream.write(stampLoad1.getImage());
        fileOutputStream.close();
        em.close();
    }
}

