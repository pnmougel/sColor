
# --- !Ups

ALTER TABLE category RENAME TO field;
CREATE TABLE subcategory (
    id BIGSERIAL NOT NULL,
    name VARCHAR(255) NOT NULL,
    category_id BIGINT NOT NULL REFERENCES field(id),
    PRIMARY KEY (id)
);
ALTER TABLE subcategory RENAME TO subfield;
ALTER TABLE subfield RENAME COLUMN category_id TO field_id;

# --- !Downs

ALTER TABLE field RENAME TO category;
DROP TABLE subcategory;
ALTER TABLE subfield RENAME TO subcategory;
ALTER TABLE subcategory RENAME COLUMN field_id TO category_id;
