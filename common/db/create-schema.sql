drop table if exists wf_task;
drop table if exists wf_task_state;
drop table if exists wf_token_string_attr;
drop table if exists wf_node_token_parent;
drop table if exists wf_arc_token;
drop table if exists wf_node_token;
drop table if exists wf_arc;
drop table if exists wf_node_ref;
drop table if exists wf_node_task;
drop table if exists wf_node;
drop table if exists wf_process;
drop table if exists wf_graph;

create table wf_graph
(
  id      serial       NOT NULL PRIMARY KEY,
  name    varchar(255) NOT NULL,
  version int          NOT NULL
);

ALTER TABLE wf_graph
  ADD CONSTRAINT wf_graph_unique
    UNIQUE(name,version);

create table wf_process
(
  id       serial NOT NULL PRIMARY KEY,
  graph_id int    NOT NULL
);

create table wf_node
(
  id              serial       NOT NULL PRIMARY KEY,
  graph_id        int          NOT NULL REFERENCES wf_graph,
  name            varchar(255) NOT NULL,
  is_join         char(1)      NOT NULL,
  type            varchar(255) NOT NULL,
  guard           varchar(255) NOT NULL
);

ALTER TABLE wf_node
  ADD CONSTRAINT wf_node_unique
    UNIQUE(graph_id, name);

create table wf_node_ref
(
  id        serial       NOT NULL PRIMARY KEY,
  node_id   int          NOT NULL REFERENCES wf_node,
  graph_id  int          NOT NULL REFERENCES wf_graph,
  instance  varchar(255) NOT NULL
);

create table wf_arc
(
  id            serial       NOT NULL PRIMARY KEY,
  graph_id      int          NOT NULL REFERENCES wf_graph,
  a_node_ref_id int          NOT NULL REFERENCES wf_node_ref,
  z_node_ref_id int          NOT NULL REFERENCES wf_node_ref,
  name          varchar(255) NOT NULL
);

create table wf_node_token
(
  id            serial    NOT NULL PRIMARY KEY,
  process_id    int       NOT NULL REFERENCES wf_process,
  node_ref_id   int       NOT NULL REFERENCES wf_node_ref,
  attr_set_id   int       NULL     REFERENCES wf_node_token,
  create_date   timestamp NOT NULL DEFAULT current_timestamp,
  complete_date timestamp NULL
);

create table wf_arc_token
(
  id              serial    NOT NULL PRIMARY KEY,
  process_id      int       NOT NULL REFERENCES wf_process,
  arc_id          int       NOT NULL REFERENCES wf_arc,
  parent_token_id int       NOT NULL REFERENCES wf_node_token,
  create_date     timestamp NOT NULL DEFAULT current_timestamp,
  complete_date   timestamp NULL
);

create table wf_node_token_parent
(
   node_token_id INT NOT NULL REFERENCES wf_node_token,
   arc_token_id  INT NOT NULL REFERENCES wf_arc_token
);

create table wf_token_string_attr
(
  attr_set_id  int          NOT NULL REFERENCES wf_node_token,
  name         varchar(64)  NOT NULL,
  value        varchar(255) NOT NULL
);

ALTER TABLE wf_token_string_attr
  ADD PRIMARY KEY (attr_set_id, name);


create table wf_node_task
(
  id          int          NOT NULL PRIMARY KEY REFERENCES wf_node,
  name        varchar(255) NOT NULL,
  description text         NOT NULL
);

create table wf_task_state
(
   id          int         NOT NULL  PRIMARY KEY,
   description varchar(10) NOT NULL
);

insert into wf_task_state values ( 0, 'Open' );
insert into wf_task_state values ( 1, 'Complete' );
insert into wf_task_state values ( 2, 'Rejected' );

create table wf_task
(
  id            serial       NOT NULL PRIMARY KEY,
  node_token_id int          NOT NULL REFERENCES wf_node_token,
  name          varchar(255) NOT NULL,
  description   varchar(255) NOT NULL,
  state         int          NOT NULL REFERENCES wf_task_state
);