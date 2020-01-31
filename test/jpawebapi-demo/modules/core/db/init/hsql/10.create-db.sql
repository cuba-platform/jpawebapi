create table JPADEMO_CAR (
    ID varchar(255) not null,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    MODEL varchar(255),
    COLOR varchar(255),
    PRODUCED_YEAR integer,
    OWNER_ID varchar(255),
    --
    primary key (ID)
);

create table JPADEMO_DRIVER (
    ID varchar(255) not null,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    FIRST_NAME varchar(255),
    LAST_NAME varchar(255),
    AGE integer,
    --
    primary key (ID)
);