package entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;


@Entity
@Table(name="employee", 
       uniqueConstraints=@UniqueConstraint(name="NAME_CNSTR", 
                                           columnNames={"Last_Name", "First_Name"}))
@NamedQueries({
    @NamedQuery(name="Employee.getAll",
        query="SELECT e FROM Employee e"),
})
public class Employee {
    
    // attributes
    private int id;
    private String lastName;
    private String firstName;
    
    // association
    private Department department;
    
    public Employee() {}
    
    public Employee(int id, String lastName, String firstName) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
    }

    @Id
    @Column(name="Empl_Id",nullable=false,updatable=false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    @Column(name="last_name",nullable=false)
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    @Column(name="first_name",nullable=false)
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }   

    @ManyToOne
    @JoinColumn(name="dept_id",insertable=true,nullable=false,updatable=true)
    public Department getDepartment() {
        return department;
    }
    
    public void setDepartment(Department department) {
        this.department = department;
    }

}

