drop table user if exists;
create table user (
  user_id integer primary key GENERATED BY DEFAULT AS IDENTITY(START WITH 100),
  username varchar(50) not null,
  email varchar(50) not null,
  pw varchar(255) not null
);

drop table follower if exists;
create table follower (
  follower_id integer,
  followee_id integer
);

drop table hashtag if exists;
create table hashtag (
        tag varchar(160)
);

drop table message if exists;
create table message (
  message_id integer primary key GENERATED BY DEFAULT AS IDENTITY(START WITH 100),
  author_id integer not null,
  text varchar(160) not null,
  pub_date timestamp,
  img varchar(160)
);


