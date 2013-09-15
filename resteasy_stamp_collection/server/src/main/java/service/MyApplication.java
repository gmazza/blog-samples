package service;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;
import javax.ws.rs.ApplicationPath;

/* @ApplicationPath annotation causes embedded Glassfish
container option (given in README) to try to activate
Jersey servlet separately and complain of a mapping conflict
with this project's WAR. This problem doesn't
occur if using embedded Jetty option. For this CXF
conversion, commenting out this annotation and relying
instead on path config info in web.xml.
*/
@ApplicationPath("/resources")
public class MyApplication extends Application {
    private Set<Object> singletons = new HashSet();

    public MyApplication() throws Exception {
        this.singletons.add(new StampServiceImpl());
    }

    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<Class<?>>();
        // register root resource
        // classes.add(StampServiceImpl.class);
        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        return this.singletons;
    }
}

