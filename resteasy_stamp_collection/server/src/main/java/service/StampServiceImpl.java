package service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.ws.rs.core.Response;
import javax.ws.rs.WebApplicationException;

import java.util.concurrent.ConcurrentHashMap;

import entities.Stamp;

/**
* JAX-RS StampService root resource
* Can obtain via Curl using:
* curl http://localhost:8080/stampcollection/stamps/2 -HAccept:text/xml
*/
public class StampServiceImpl implements StampService {
    /*
     * ConcurrentHashMap because StampService is a singleton accessed by multiple requests
     */
    Map<String, StampResource> stamps = new ConcurrentHashMap<String, StampResource>();

    public StampServiceImpl() throws Exception {
        // seed HashMap with initial stamps
        SampleRun.setupDerbyDatabase();
        EntityManager em = SampleRun.getDerbyEntityManager();
        Stamp s = em.find(Stamp.class, "1");
        StampResource sr = new StampResource(s);
        stamps.put(sr.getId(), sr);
        s = em.find(Stamp.class, "2");
        sr = new StampResource(s);
        stamps.put(sr.getId(), sr);
    }

    @Override
    public Collection<StampResource> getAllStamps() {
        return new ArrayList<StampResource>(stamps.values());
    }

    @Override
    public StampResource getStampResource(String id) {
        System.out.println("getStamp called - id = " + id);
        StampResource s = stamps.get(id);
        if (s == null) {
            // will return HTTP 404 "not found" code
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        System.out.println("Stamp ID/Description = " + s.getId() + " / " + s.getDescription());
        return s;
    }
}
