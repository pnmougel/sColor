
# --- !Ups

ALTER TABLE conference RENAME COLUMN field_id TO subfield_id;

ALTER TABLE conference DROP CONSTRAINT conference_category_id_fkey;
ALTER TABLE conference ADD CONSTRAINT conference_subfield_id_fkey FOREIGN KEY (subfield_id)
REFERENCES "subfield" (id) ON DELETE CASCADE;



# --- !Downs
ALTER TABLE conference RENAME COLUMN subfield_id TO category_id;

ALTER TABLE conference DROP CONSTRAINT conference_subfield_id_fkey;
ALTER TABLE conference ADD CONSTRAINT conference_category_id_fkey FOREIGN KEY (category_id)
REFERENCES "field" (id) ON DELETE CASCADE;

