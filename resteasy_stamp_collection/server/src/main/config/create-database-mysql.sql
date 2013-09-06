# run via mysql> source server/src/main/config/createdb-mysql.sql
# TODO: Add country, year, type & link tables
use stampdb;
drop table if exists stamp;

create table stamp (
    id varchar(48) not null primary key,
    description varchar(255) not null,
    image mediumblob not null
);

# for Ubuntu, files must moved to /var/lib/mysql subdirectory, see:
# http://angkatbahu.blogspot.com/2011/03/mysql-loadfile-function-in-ubuntu-it_12.html
# first test LOAD_FILE works by simply running "SELECT LOAD_FILE('...');"
# a non-null return means it can be found.

insert into stamp (id, description, image) values
    ("1", "Turkey",  LOAD_FILE('/var/lib/mysql/images/Turkish_USA_stamp.png'));

insert into stamp (id, description, image) values
    ("2", "Newfoundland", LOAD_FILE('/var/lib/mysql/images/Lord_Bacon_stamp.png'));

# test:
# select image into dumpfile '/var/lib/mysql/test.png' from stamp where id = '2';

