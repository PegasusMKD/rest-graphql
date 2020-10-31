create table post
(
    id      varchar(32) not null
        constraint post_pkey
            primary key,
    content varchar(255),
    user_id varchar(32) not null
        constraint fk4f62kobdc0890vctu2jq5tcvq
            references account
);

alter table post
    owner to admin;

INSERT INTO public.post (id, content, user_id) VALUES ('12A68BAD8413AB0DE0500C0A0E0776A4', 'some', '12A68BAD8413AB0DE0500C0A0E0776A0');
INSERT INTO public.post (id, content, user_id) VALUES ('12A68BAD8413AB0DE0500C0A0E0776A5', 'content', '12A68BAD8413AB0DE0500C0A0E0776A0');
INSERT INTO public.post (id, content, user_id) VALUES ('12A68BAD8413AB0DE0500C0A0E0776A6', 'another', '12A68BAD8413AB0DE0500C0A0E0776A2');