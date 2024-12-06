create table course_path (
    id bigserial not null,
    title varchar(255) not null,
    description varchar(2000),
    teacher varchar(50) not null,
    published boolean not null,
    published_date timestamp,
    created_by         varchar(50)  not null,
    created_date       timestamp    not null,
    last_modified_by   varchar(50)  not null,
    last_modified_date timestamp    not null,
    deleted            boolean      not null,
    version            int          not null,
    constraint fk_course_path primary key (id)
);

create table course_order (
    id bigserial not null,
    course_id bigint not null,
    course_path bigint not null references course_path(id) on DELETE cascade,
    order_index int not null,
    constraint fk_course_order primary key (id)
);