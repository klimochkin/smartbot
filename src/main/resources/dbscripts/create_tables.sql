
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

    constraint pk_message primary key (id)
);

alter table message add message_type varchar(255);
alter table message add username varchar(255);


CREATE TABLE friend_request_counters
(
    id            SERIAL PRIMARY KEY,
    request_date  DATE    NOT NULL UNIQUE,
    requests_sent INTEGER NOT NULL
);