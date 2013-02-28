# Tasks schema
 
# --- !Ups

CREATE TABLE ctype (
    id BIGSERIAL NOT NULL,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);


ALTER TABLE conference ADD COLUMN ctype_id BIGINT NOT NULL REFERENCES ctype(id);

# --- !Downs
 
DROP TABLE conftype;
