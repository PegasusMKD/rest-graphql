create table account_friends
(
    account_id varchar(32) not null
        constraint fkthx7lmuewouwom5pk9vpqkg9t
            references account,
    friends_id varchar(32) not null
        constraint fk7p666khgyr4woqglbh2j5di5x
            references account,
    constraint account_friends_pkey
        primary key (account_id, friends_id)
);

alter table account_friends
    owner to admin;

