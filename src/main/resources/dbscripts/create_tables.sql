
create sequence seq_message;

create table message
(
    id                numeric(15),
    user_id           numeric(15),
    message_id        numeric(15),
    peer_id           numeric(15),
    text              text,
    subject           varchar(255),
    user_role         varchar(255),
    create_stamp      timestamp,
    parent_message_id numeric(15),
    status            varchar(255),
    message_type varchar(255),
    username varchar(255),

    constraint pk_message primary key (id)
);

alter table message add fio varchar(255);

CREATE TABLE friend_request_counters
(
    id            SERIAL PRIMARY KEY,
    request_date  DATE    NOT NULL UNIQUE,
    requests_sent INTEGER NOT NULL
);

create table users
(
    user_id  numeric(15)  primary key,
    username varchar(255) not null,
    source varchar(255)
);
