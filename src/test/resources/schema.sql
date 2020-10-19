create table if not exists usr (
                     id  identity primary key,
                     username varchar not null,
                     password varchar not null,
                     name varchar not null,
                     status varchar not null,
                     created_at timestamp with time zone not null  default now()
);

create table if not exists authorities (
                             id integer auto_increment primary key,
                             value varchar not null
);

create table if not exists user_authorities
(
    user_id      bigint not null,
    authority_id integer    not null,
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

create table if not exists refresh_token
(
    value     uuid   not null primary key,
    user_id   bigint      not null,
    issued_at timestamp with time zone not null,
    expire_at timestamp with time zone not null,
    next      uuid,
    constraint refresh_tokens_user_fk foreign key (user_id)
        references usr (id) on delete cascade,
    constraint refresh_tokens_next_fk foreign key (next)
        references refresh_token (value) on delete cascade
);

-- create alias prune_refresh_tokens as
-- $$
-- delete
-- from refresh_token rt
-- where rt.expire_at < current_timestamp
--    or rt.user_id in (select u.id from usr u where u.id = rt.user_id and u.status = 'SUSPENDED')
-- $$;

create table if not exists task (
                      id identity not null primary key,
                      text varchar not null,
                      deadline  timestamp,
                      status varchar not null,
                      created_at timestamp with time zone not null  default now(),
                      executor_id bigint,
                      reviewer_id bigint not null,
                      constraint task_executor_fk foreign key (executor_id)
                          references usr(id),
                      constraint task_reviewer_fk foreign key (reviewer_id)
                          references usr(id)
);





