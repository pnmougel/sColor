# Tasks schema
 
# --- !Ups

CREATE TABLE conference_relation_type (
    id BIGSERIAL NOT NULL,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);


ALTER TABLE conference_relation ADD COLUMN conference_relation_type_id BIGINT REFERENCES conference_relation_type(id);

# --- !Downs
 
DROP TABLE conference_relation_type;
