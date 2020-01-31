create table JPADEMO_TEST_INNER_ENTITY (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    PARAM varchar(255),
    --
    primary key (ID)
)^

create table JPADEMO_TEST_ENTITY (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    F_NAME varchar(255),
    L_NAME varchar(255),
    AGE integer,
    INNER_ENTITY_ID uuid,
    --
    primary key (ID)
)^

-- constraints

alter table JPADEMO_TEST_ENTITY add constraint FK_JPADEMO_TEST_ENTITY_ON_INNER_ENTITY foreign key (INNER_ENTITY_ID) references JPADEMO_TEST_INNER_ENTITY(ID)^


-- indexes

create index IDX_JPADEMO_TEST_ENTITY_ON_INNER_ENTITY on JPADEMO_TEST_ENTITY (INNER_ENTITY_ID)^