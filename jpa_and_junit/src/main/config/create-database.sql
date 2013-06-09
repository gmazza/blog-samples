# For Derby, see here: http://www.jroller.com/gmazza/entry/apache_derby_setup
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
  Dept_Id    SMALLINT NOT NULL REFERENCES DEPARTMENT(Dept_Id),
  UNIQUE (Last_Name, First_Name)
);

CREATE VIEW EmplByDeptView AS (
  SELECT Empl.Empl_ID, Empl.Last_Name, Empl.First_Name,
	Dept.Name as "Dept Name", Dept.Location
    FROM Employee Empl, Department Dept
  WHERE
    Empl.Dept_Id = Dept.Dept_Id
);

