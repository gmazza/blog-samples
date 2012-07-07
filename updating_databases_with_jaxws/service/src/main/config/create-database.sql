SET SCHEMA APP;
DROP VIEW EmplByDeptView;
DROP TABLE Employee;
DROP TABLE Department;

CREATE TABLE DEPARTMENT (
  Dept_Id   SMALLINT NOT NULL PRIMARY KEY,
  Name      VARCHAR(20) NOT NULL,
  Location  VARCHAR(20) NOT NULL
);
 
CREATE TABLE EMPLOYEE (
  Empl_Id    SMALLINT NOT NULL PRIMARY KEY
             GENERATED ALWAYS AS IDENTITY,
  Last_Name  VARCHAR(20) NOT NULL,
  First_Name VARCHAR(20) NOT NULL,
  Gender     CHAR(1) NOT NULL 
             CONSTRAINT GENDER_CONSTRAINT 
             CHECK (Gender IN ('M', 'F')),
  Dept_Id    SMALLINT NOT NULL REFERENCES DEPARTMENT(Dept_Id),
  Hiredate   DATE NOT NULL, 
  Salary     DECIMAL(8,2),
  UNIQUE (Last_Name, First_Name)
);

CREATE VIEW EmplByDeptView AS (
  SELECT Empl.Empl_ID, Empl.Last_Name, Empl.First_Name,
	Dept.Name as "Dept Name", Dept.Location, Empl.Hiredate, 
	Empl.Salary
    FROM Employee Empl, Department Dept
  WHERE
    Empl.Dept_Id = Dept.Dept_Id
);

