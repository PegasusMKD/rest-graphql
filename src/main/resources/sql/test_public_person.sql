create table person
(
    id           varchar(32) not null
        constraint person_pkey
            primary key,
    full_name    varchar(255),
    phone_number varchar(255)
);

alter table person
    owner to admin;

INSERT INTO public.person (id, full_name, phone_number) VALUES ('12A68BAD8413AB0DE0500C0A0E0776A1', 'filip jovanov', null);
INSERT INTO public.person (id, full_name, phone_number) VALUES ('12A68BAD8413AB0DE0500C0A0E0776A3', 'martin', null);