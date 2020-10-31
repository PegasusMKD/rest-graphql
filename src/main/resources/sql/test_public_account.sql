create table account
(
    id        varchar(32) not null
        constraint account_pkey
            primary key,
    username  varchar(255),
    person_id varchar(32) not null
        constraint uk_iajp1nugms7a5wl86ecnjamw2
            unique
        constraint fkd9dhia7smrg88vcbiykhofxee
            references person
);

alter table account
    owner to admin;

INSERT INTO public.account (id, username, person_id) VALUES ('12A68BAD8413AB0DE0500C0A0E0776A0', 'user1', '12A68BAD8413AB0DE0500C0A0E0776A1');
INSERT INTO public.account (id, username, person_id) VALUES ('12A68BAD8413AB0DE0500C0A0E0776A2', 'user2', '12A68BAD8413AB0DE0500C0A0E0776A3');