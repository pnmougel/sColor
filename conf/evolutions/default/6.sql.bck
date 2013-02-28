
# --- !Ups

ALTER TABLE comment DROP CONSTRAINT comment_conference_id_fkey;
ALTER TABLE comment ADD CONSTRAINT comment_conference_id_fkey FOREIGN KEY (conference_id)
REFERENCES "conference" (id) ON DELETE CASCADE;

ALTER TABLE comment DROP CONSTRAINT comment_iuser_id_fkey;
ALTER TABLE comment ADD CONSTRAINT comment_iuser_id_fkey FOREIGN KEY (iuser_id)
REFERENCES "iuser" (id) ON DELETE CASCADE;

ALTER TABLE conference DROP CONSTRAINT conference_category_id_fkey;
ALTER TABLE conference ADD CONSTRAINT conference_category_id_fkey FOREIGN KEY (category_id)
REFERENCES "field" (id) ON DELETE CASCADE;

ALTER TABLE conference DROP CONSTRAINT conference_publisher_id_fkey;
ALTER TABLE conference ADD CONSTRAINT conference_publisher_id_fkey FOREIGN KEY (publisher_id)
REFERENCES "publisher" (id) ON DELETE CASCADE;

ALTER TABLE link DROP CONSTRAINT link_conference_id_fkey;
ALTER TABLE link ADD CONSTRAINT link_conference_id_fkey FOREIGN KEY (conference_id)
REFERENCES "conference" (id) ON DELETE CASCADE;

ALTER TABLE subfield DROP CONSTRAINT subcategory_category_id_fkey;
ALTER TABLE subfield ADD CONSTRAINT subcategory_category_id_fkey FOREIGN KEY (field_id)
REFERENCES "field" (id) ON DELETE CASCADE;

ALTER TABLE scoreiuser DROP CONSTRAINT scoreiuser_iuser_id_fkey;
ALTER TABLE scoreiuser ADD CONSTRAINT scoreiuser_iuser_id_fkey FOREIGN KEY (iuser_id)
REFERENCES "iuser" (id) ON DELETE CASCADE;

ALTER TABLE conferencetag DROP CONSTRAINT conferencetag_conference_id_fkey;
ALTER TABLE conferencetag ADD CONSTRAINT conferencetag_conference_id_fkey FOREIGN KEY (conference_id)
REFERENCES "conference" (id) ON DELETE CASCADE;

ALTER TABLE conferencetag DROP CONSTRAINT conferencetag_tag_id_fkey;
ALTER TABLE conferencetag ADD CONSTRAINT conferencetag_tag_id_fkey FOREIGN KEY (tag)
REFERENCES "tag" (id) ON DELETE CASCADE;

# --- !Downs

ALTER TABLE comment DROP CONSTRAINT comment_conference_id_fkey;
ALTER TABLE comment ADD CONSTRAINT comment_conference_id_fkey FOREIGN KEY (conference_id)
REFERENCES "conference" (id);

ALTER TABLE comment DROP CONSTRAINT comment_iuser_id_fkey;
ALTER TABLE comment ADD CONSTRAINT comment_iuser_id_fkey FOREIGN KEY (iuser_id)
REFERENCES "iuser" (id);

ALTER TABLE conference DROP CONSTRAINT conference_category_id_fkey;
ALTER TABLE conference ADD CONSTRAINT conference_category_id_fkey FOREIGN KEY (category_id)
REFERENCES "field" (id);

ALTER TABLE conference DROP CONSTRAINT conference_publisher_id_fkey;
ALTER TABLE conference ADD CONSTRAINT conference_publisher_id_fkey FOREIGN KEY (publisher_id)
REFERENCES "publisher" (id);

ALTER TABLE link DROP CONSTRAINT link_conference_id_fkey;
ALTER TABLE link ADD CONSTRAINT link_conference_id_fkey FOREIGN KEY (conference_id)
REFERENCES "conference" (id);

ALTER TABLE subfield DROP CONSTRAINT subcategory_category_id_fkey;
ALTER TABLE subfield ADD CONSTRAINT subcategory_category_id_fkey FOREIGN KEY (field_id)
REFERENCES "field" (id);

ALTER TABLE scoreiuser DROP CONSTRAINT scoreiuser_iuser_id_fkey;
ALTER TABLE scoreiuser ADD CONSTRAINT scoreiuser_iuser_id_fkey FOREIGN KEY (iuser_id)
REFERENCES "iuser" (id);

ALTER TABLE conferencetag DROP CONSTRAINT conferencetag_conference_id_fkey;
ALTER TABLE conferencetag ADD CONSTRAINT conferencetag_conference_id_fkey FOREIGN KEY (conference_id)
REFERENCES "conference" (id);

ALTER TABLE conferencetag DROP CONSTRAINT conferencetag_tag_id_fkey;
ALTER TABLE conferencetag ADD CONSTRAINT conferencetag_tag_id_fkey FOREIGN KEY (tag)
REFERENCES "tag" (id);
