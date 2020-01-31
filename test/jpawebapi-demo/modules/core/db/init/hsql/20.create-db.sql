alter table JPADEMO_CAR add constraint FK_JPADEMO_CAR_ON_OWNER foreign key (OWNER_ID) references JPADEMO_DRIVER(ID);
create index IDX_JPADEMO_CAR_ON_OWNER on JPADEMO_CAR (OWNER_ID);