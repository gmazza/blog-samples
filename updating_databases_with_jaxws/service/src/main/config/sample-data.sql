delete from employee;
delete from department;

INSERT INTO DEPARTMENT (Dept_Id, Name, Location) 
VALUES
(1, 'Accounting', 'Chicago'),
(2, 'Sales', 'Boston'),
(3, 'Training', 'New York'),
(4, 'Research', 'Atlanta');

INSERT INTO EMPLOYEE (Last_Name, First_Name, Gender, Dept_id, 
    Hiredate, Salary)
VALUES
('SMITH', 'BOB', 'M', 1, '2002-10-15', 50000.00),
('JONES', 'TED', 'M', 2, '1998-04-05', 60000.00),
('MILLER', 'SALLY', 'F', 3, '2004-01-12', 70000.00),
('WILSON', 'MARK', 'M', 1, '1997-05-27', 80000.00);

commit;

