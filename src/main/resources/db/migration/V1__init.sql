create table usr (
        id bigserial primary key,
        username text not null,
        password text not null,
        name text not null,
        status text not null,
        created_at timestamptz not null  default now()
);

create unique index users_username_index on usr(username);

create table authorities (
    id serial primary key,
    value text not null
);

create unique index authorities_value_index on authorities(value);

create table user_authorities
(
    user_id      bigint not null,
    authority_id int    not null,
    primary key (user_id, authority_id),
    constraint user_authorities_users_fk foreign key (user_id)
        references usr (id) on delete cascade,
    constraint user_authorities_authorities_fk foreign key (authority_id)
        references authorities (id) on delete cascade
);

insert into authorities (value)
values ('ROLE_USER');
insert into authorities (value)
values ('ROLE_ADMIN');

create table refresh_token
(
    value     uuid        not null primary key,
    user_id   bigint      not null,
    issued_at timestamptz not null,
    expire_at timestamptz not null,
    next      uuid,
    constraint refresh_tokens_user_fk foreign key (user_id)
        references usr (id) on delete cascade,
    constraint refresh_tokens_next_fk foreign key (next)
        references refresh_token (value) on delete cascade
);

create procedure prune_refresh_tokens()
    language SQL
as
$$
delete
from refresh_token rt
where rt.expire_at < current_timestamp
   or rt.user_id in (select u.id from usr u where u.id = rt.user_id and u.status = 'SUSPENDED')
$$;

create table task (
    id bigserial not null primary key,
    text text not null,
    deadline  timestamp without time zone,
    status text not null,
    created_at timestamptz not null  default now(),
    executor_id bigint,
    reviewer_id bigint not null,
    constraint task_executor_fk foreign key (executor_id)
                references usr(id),
    constraint task_reviewer_fk foreign key (reviewer_id)
                references usr(id)
);


