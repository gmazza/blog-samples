package entities;

import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class JPATestUtils {
    
    public static EntityManager createEntityManager() {
        Properties emfProps = new Properties();
        String driver, url;
        // Here, connecting to Derby in-memory DB created by Maven inmemdb plugin (see pom.xml)
        // in-memory DB info: http://db.apache.org/derby/docs/10.7/devguide/cdevdvlpinmemdb.html
        driver = "org.apache.derby.jdbc.ClientDriver";
        url = "jdbc:derby://localhost:1527/memory:stampdb";
        
        emfProps.setProperty("javax.persistence.jdbc.driver", driver);
        emfProps.setProperty("javax.persistence.jdbc.url", url); 
        emfProps.setProperty("javax.persistence.jdbc.user", "APP");
        emfProps.setProperty("javax.persistence.jdbc.password", "ANYTHING");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("StampPU", emfProps);
        EntityManager em = emf.createEntityManager();
        return em;
    }
}
