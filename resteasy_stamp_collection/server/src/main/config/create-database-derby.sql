create table stamp (
    id varchar(48) not null primary key,
    description varchar(255) not null,
    image blob(2M)
);
