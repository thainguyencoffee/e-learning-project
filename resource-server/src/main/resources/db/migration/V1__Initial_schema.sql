create table student
(
    id         bigserial    not null,
    first_name varchar(255) not null,
    last_name  varchar(255) not null,
    email      varchar(255) not null
);

create table course
(
    id                 bigserial   not null,
    title              varchar(50) not null,
    thumbnail_url      varchar(50) not null,
    status             varchar(50) not null,
    description        text,
    is_public          boolean     not null,
    email_authorities  varchar(255)[],
    price              varchar(50) not null,
    discounted_price   varchar(50),
    teacher_id         varchar(50) not null,
    term               varchar(50) not null,
    language           varchar(50) not null,
    subtitles          varchar(50)[],
    benefits           text[],
    prerequisites      text[],
    approved_by        varchar(50),
    created_by         varchar(50) not null,
    created_date       timestamp   not null,
    last_modified_by   varchar(50) not null,
    last_modified_date timestamp   not null,
    discount_id        bigint,
    constraint fk_course primary key (id)
);


create table course_section
(
    id     bigserial    not null,
    title  varchar(255) not null,
    course bigint       not null references course (id) on DELETE cascade,
    constraint fk_course_section primary key (id)
);


create table lesson
(
    id             bigserial    not null,
    title          varchar(255) not null,
    type           varchar(255) not null,
    link           varchar(255) not null,
    course_section bigint       not null references course_section (id) on DELETE cascade,
    constraint fk_lesson primary key (id)
);


create table course_student
(
    course     bigint       not null,
    student    bigint       not null,
    first_name varchar(255) not null,
    last_name  varchar(255) not null,
    email      varchar(255) not null,
    constraint fk_course_student primary key (course, student)
);

create table discount
(
    id                 bigserial   not null,
    code               varchar(50) not null,
    type               varchar(50) not null,
    percentage         int         not null,
    fixed_amount       varchar(50),
    start_date         timestamp   not null,
    end_date           timestamp   not null,
    created_by         varchar(50) not null,
    created_date       timestamp   not null,
    last_modified_by   varchar(50) not null,
    last_modified_date timestamp   not null,
    constraint fk_discount primary key (id)
);
