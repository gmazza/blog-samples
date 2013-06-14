package entities;

import java.util.Set;
import java.util.TreeSet;
import javax.persistence.OneToMany;

public class Department {
    
    private int id;
    private String name;
    private String location;
    private Set employees = new TreeSet();
    
    public Department() {
    }
    
    public Department(int id, String name, String location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
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
    
    @OneToMany
    public Set getEmployees() {
        return employees;
    }

    public void setEmployees(Set employees) {
        this.employees = employees;
    }
    
}

