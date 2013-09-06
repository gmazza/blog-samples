package entities;

import java.util.Set;
import java.util.HashSet;

public class Stamp {
    
    private String id;
    private String description;
    private byte[] image;
    
    public Stamp() {
    }
    
    public Stamp(String id, String description, byte[] image) {
        this.id = id;
        this.description = description;
        this.image = image.clone();
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
     
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getImage() {
        return image.clone();
    }

    public void setImage(byte[] image) {
        this.image = image.clone();
    }

}

