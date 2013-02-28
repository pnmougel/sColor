
# --- !Ups

ALTER TABLE link ADD date timestamp with time zone;
ALTER TABLE rankingscore ALTER COLUMN score TYPE VARCHAR(255);

# --- !Downs

ALTER TABLE link DROP COLUMN date;
ALTER TABLE rankingscore ALTER COLUMN score TYPE INT;

