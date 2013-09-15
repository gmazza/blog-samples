package service;

import javax.xml.bind.annotation.XmlRootElement;

import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import entities.Stamp;

@XmlRootElement(name = "stamp")
public class StampResource {

    private Stamp stamp;

    public StampResource() {
        stamp = new Stamp();
    }

    public StampResource(Stamp inStamp) {
        stamp = new Stamp();
        stamp.setId(inStamp.getId());
        stamp.setDescription(inStamp.getDescription());
        stamp.setImage(inStamp.getImage());
    }

    @GET
    @Path("id")
    @Produces("text/plain")
    public String getId() {
        return stamp.getId();
    }

    public void setId(String id) {
        stamp.setId(id);
    }

    @GET
    @Produces("application/xml")
    public StampResource getState() {
        return this;
    }

    @GET
    @Path("description")
    @Produces("text/plain")
    public String getDescription() {
        return stamp.getDescription();
    }

    public void setDescription(String description) {
        stamp.setDescription(description);
    }

    @GET
    @Path("image")
    @Produces("image/jpg")
    public byte[] getImage() {
        return stamp.getImage().clone();
    }

    public void setImage(byte[] image) {
        stamp.setImage(image.clone());
    }
}
