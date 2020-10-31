create table user_friends
(
    user_id    varchar(32) not null,
    friends_id varchar(32) not null,
    constraint user_friends_pkey
        primary key (user_id, friends_id)
);

alter table user_friends
    owner to admin;

