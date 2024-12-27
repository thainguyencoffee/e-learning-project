create table course
(
    id                 bigserial    not null,
    title              varchar(255) not null,
    thumbnail_url      varchar(500),
    published          boolean      not null,
    published_date     timestamp,
    unpublished        boolean      not null,
    unpublished_date   timestamp,
    description        varchar(2000),
    price              varchar(50),
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
    deleted            boolean      not null,
    version            int          not null,
    constraint fk_course primary key (id)
);

CREATE INDEX idx_course_fulltext
    ON course USING GIN (
    to_tsvector(
    'english',
    title || ' ' ||
    teacher || ' ' ||
    description
    )
    );

create table course_request
(
    id              bigserial     not null,
    course          bigint        not null references course (id) on DELETE cascade,
    type            varchar(50)   not null,
    status          varchar(50)   not null,
    resolved        boolean       not null,
    resolved_by     varchar(50),
    requested_by    varchar(50)   not null,
    message         varchar(2000) not null,
    reject_reason   varchar(2000),
    approve_message varchar(2000),
    requested_date  timestamp     not null,
    resolved_date   timestamp,
    constraint fk_course_request primary key (id)
);

create table course_section
(
    id          bigserial    not null,
    title       varchar(255) not null,
    order_index int          not null,
    published   boolean      not null,
    course      bigint       not null references course (id) on DELETE cascade,
    constraint fk_course_section primary key (id)
);

create table lesson
(
    id             bigserial    not null,
    title          varchar(255) not null,
    type           varchar(255) not null,
    link           varchar(255),
    order_index    int          not null,
    course_section bigint       not null references course_section (id) on DELETE cascade,
    constraint fk_lesson primary key (id)
);

create table quiz
(
    id                    bigserial    not null,
    course_section        bigint       not null references course_section (id) on DELETE cascade,
    title                 varchar(255) not null,
    description           varchar(2000),
    after_lesson_id       bigint       not null,
    total_score           int          not null,
    pass_score_percentage int          not null,
    deleted               boolean      not null,
    constraint fk_quiz primary key (id)
);

create table question
(
    id                bigserial     not null,
    quiz              bigint        not null references quiz (id) on DELETE cascade,
    content           varchar(1000) not null,
    type              varchar(50)   not null,
    score             int           not null,
    true_false_answer boolean,
    constraint fk_question primary key (id)
);

create table answer_option
(
    id       bigserial     not null,
    question bigint        not null references question (id) on DELETE cascade,
    content  varchar(1000) not null,
    correct  boolean       not null,
    constraint fk_answer_option primary key (id)
);

create table review
(
    id          bigserial    not null,
    course      bigint       not null references course (id) on DELETE cascade,
    username    varchar(50)  not null,
    rating      int          not null,
    comment     varchar(500) not null,
    review_date timestamp    not null,
    constraint fk_review primary key (id)
);

create table discount
(
    id                 bigserial   not null,
    code               varchar(50) not null,
    type               varchar(50) not null,
    percentage         int,
    max_value          varchar(50),
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
    order_type         varchar(50)                    not null,
    enrollment_id      bigint,
    course_id          bigint,
    additional_price   varchar(50),
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
    price          varchar(50)                    not null,
    payment_date   timestamp,
    payment_method varchar(50)                    not null,
    status         varchar(50)                    not null,
    transaction_id varchar(50),
    receipt_url    varchar(500),
    failure_reason varchar(500),
    constraint fk_payment primary key (id)
);

create table enrollment
(
    id                 bigserial    not null,
    course_id          bigint       not null,
    teacher            varchar(255) not null,
    quiz_ids           bigint[],
    total_lessons      int          not null,
    student            varchar(255) not null,
    enrollment_date    timestamp,
    changed_course     boolean      not null,
    reviewed           boolean      not null,
    completed          boolean      not null,
    created_by         varchar(50)  not null,
    created_date       timestamp    not null,
    completed_date     timestamp,
    last_modified_by   varchar(50)  not null,
    last_modified_date timestamp    not null,
    constraint fk_enrollment primary key (id)
);

create table quiz_submission
(
    id                 bigserial not null,
    quiz_id            bigint    not null,
    after_lesson_id    bigint    not null,
    enrollment         bigint    not null references enrollment (id) on DELETE cascade,
    score              int       not null,
    passed             boolean   not null,
    bonus              boolean   not null,
    submitted_date     timestamp not null,
    last_modified_date timestamp not null,
    constraint fk_quiz_submission primary key (id)
);

create table quiz_answer
(
    id                   bigserial   not null,
    quiz_submission      bigint      not null references quiz_submission (id) on DELETE cascade,
    question_id          bigint      not null,
    answer_option_ids    bigint[],
    true_false_answer    boolean,
    single_choice_answer bigint,
    type                 varchar(50) not null,
    constraint fk_quiz_answer primary key (id)
);

create table certificate
(
    id           uuid DEFAULT gen_random_uuid() not null,
    enrollment   bigint                         not null references enrollment (id) on DELETE cascade,
    full_name    varchar(255)                   not null,
    email        varchar(255)                   not null,
    student      varchar(255)                   not null,
    teacher      varchar(255)                   not null,
    url          varchar(500)                   not null,
    course_id    bigint                         not null,
    course_title varchar(255)                   not null,
    issued_date  timestamp                      not null,
    certified    boolean                        not null,
    constraint fk_certificate primary key (id)
);

create table lesson_progress
(
    id             bigserial not null,
    enrollment     bigint    not null references enrollment (id) on DELETE cascade,
    lesson_id      bigint    not null,
    lesson_title   varchar(255),
    completed      boolean   not null,
    bonus          boolean   not null,
    completed_date timestamp,
    constraint fk_lesson_progress primary key (id)
);

create table post
(
    id                 bigserial      not null,
    course             bigint         not null references course (id) on DELETE cascade,
    content            varchar(10000) not null,
    first_name         varchar(255)   not null,
    last_name          varchar(255)   not null,
    username           varchar(255)   not null,
    attachment_urls    varchar(255)[],
    created_date       timestamp      not null,
    last_modified_date timestamp      not null,
    deleted            boolean        not null,
    constraint fk_post primary key (id)
);

create table comment
(
    id                 bigserial      not null,
    post               bigint         not null references post (id) on DELETE cascade,
    content            varchar(10000) not null,
    first_name         varchar(255)   not null,
    last_name          varchar(255)   not null,
    username           varchar(255)   not null,
    attachment_urls    varchar(255)[],
    created_date       timestamp      not null,
    last_modified_date timestamp      not null,
    constraint fk_comment primary key (id)
);

create table emotion
(
    id           bigserial   not null,
    post         bigint      not null references post (id) on DELETE cascade,
    username     varchar(50) not null,
    created_date timestamp   not null,
    constraint fk_emotion primary key (id)
);

create table salary
(
    id                 bigserial    not null,
    teacher            varchar(255) not null,
    rank               varchar(50)  not null,
    base_salary        varchar(50)  not null,
    nos_all_time       int          not null,
    noc_all_time       int          not null,
    created_by         varchar(50)  not null,
    created_date       timestamp    not null,
    last_modified_by   varchar(50)  not null,
    last_modified_date timestamp    not null,
    constraint fk_salary primary key (id)
);

create table salary_record
(
    id             bigserial   not null,
    salary         bigint      not null references salary (id) on DELETE cascade,
    type           varchar(50) not null,
    price          varchar(50) not null,
    created_date   timestamp   not null,
    paid_date      timestamp,
    noc_by_month   int         not null,
    nos_by_month   int         not null,
    total_price    varchar(50) not null,
    status         varchar(50) not null,
    failure_reason varchar(500),
    constraint fk_salary_record primary key (id)
);