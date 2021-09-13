create table comment
(
    id      varchar(32) not null
        constraint comment_pkey
            primary key,
    content varchar(255),
    post_id varchar(32) not null
        constraint fks1slvnkuemjsq2kj4h3vhx7i1
            references post,
    user_id varchar(32) not null
        constraint fkn84216vj612qs1eg5goe6n2lj
            references account
);

alter table comment
    owner to admin;

INSERT INTO public.comment (id, content, post_id, user_id)
VALUES ('12A68BAD8413AB0DE0500C0A0E0776A7', 'some', '12A68BAD8413AB0DE0500C0A0E0776A6',
        '12A68BAD8413AB0DE0500C0A0E0776A2');