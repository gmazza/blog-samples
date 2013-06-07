package entities;

import java.util.Set;
import java.util.TreeSet;

public class Department {
    
    private String id;
    private String name;
    private String location;
    private Set employees = new TreeSet();
    
    public Department() {
    }
    
    public Department(String id, String name, String location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
     
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public Set getEmployees() {
        return employees;
    }

    public void setEmployees(Set employees) {
        this.employees = employees;
    }
    
}

