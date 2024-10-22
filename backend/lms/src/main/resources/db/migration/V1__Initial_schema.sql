create table student
(
    id         bigserial    not null,
    first_name varchar(255) not null,
    last_name  varchar(255) not null,
    email      varchar(255) not null
);

create table course
(
    id                 bigserial    not null,
    title              varchar(255) not null,
    thumbnail_url      varchar(500),
    published          boolean      not null,
    description        varchar(2000),
    price              varchar(50),
    discounted_price   varchar(50),
    teacher            varchar(50),
    language           varchar(50),
    subtitles          varchar(50)[],
    benefits           varchar(255)[],
    prerequisites      varchar(255)[],
    approved_by        varchar(50),
    created_by         varchar(50)  not null,
    created_date       timestamp    not null,
    last_modified_by   varchar(50)  not null,
    last_modified_date timestamp    not null,
    discount_code      varchar(50),
    deleted            boolean      not null,
    version            int          not null,
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
    link           varchar(255),
    quiz           bigint,
    course_section bigint       not null references course_section (id) on DELETE cascade,
    constraint fk_lesson primary key (id)
);

create table quiz
(
    id    bigserial    not null,
    title varchar(255) not null,
    constraint fk_quiz primary key (id)
);

create table question
(
    id      bigserial    not null,
    prompt  varchar(255) not null,
    options varchar(255)[] not null,
    correct int          not null,
    quiz    bigint       not null references quiz (id) on DELETE cascade,
    constraint fk_question primary key (id)
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
    percentage         int,
    fixed_price        varchar(50),
    start_date         timestamp   not null,
    end_date           timestamp   not null,
    current_usage      int         not null,
    max_usage          int         not null,
    deleted            boolean     not null,
    created_by         varchar(50) not null,
    created_date       timestamp   not null,
    last_modified_by   varchar(50) not null,
    last_modified_date timestamp   not null,
    constraint fk_discount primary key (id)
);

create table orders
(
    id                 uuid DEFAULT gen_random_uuid() not null,
    order_date         timestamp                      not null,
    total_price        varchar(50)                    not null,
    status             varchar(50)                    not null,
    discount_code      varchar(50),
    discounted_price   varchar(50),
    created_by         varchar(50)                    not null,
    created_date       timestamp                      not null,
    last_modified_by   varchar(50)                    not null,
    last_modified_date timestamp                      not null,
    constraint fk_orders primary key (id)
);

create table order_items
(
    id     bigserial   not null,
    course bigint      not null,
    price  varchar(50) not null,
    orders uuid        not null references orders (id) on DELETE cascade,
    constraint fk_order_item primary key (id)
);

create table payment
(
    id             uuid DEFAULT gen_random_uuid() not null,
    order_id       uuid                           not null,
    amount         varchar(50)                    not null,
    payment_date   timestamp,
    payment_method varchar(50)                    not null,
    status         varchar(50)                    not null,
    transaction_id varchar(50),
    constraint fk_payment primary key (id)
);