# Tasks schema
 
# --- !Ups

CREATE TABLE IF NOT EXISTS Iuser (
    id BIGSERIAL NOT NULL,
    email VARCHAR(255) NOT NULL,
    key VARCHAR(255) NOT NULL,
    api_key VARCHAR(255) NOT NULL,
    pseudo VARCHAR(255),
    is_admin BOOLEAN NOT NULL,
    is_public BOOLEAN NOT NULL DEFAULT false,
    PRIMARY KEY (id)
);

CREATE INDEX iuser_key_idx ON IUser USING hash (key);

CREATE TABLE IF NOT EXISTS ScoreType (
    id BIGSERIAL NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS ExternalRanking (
    id BIGSERIAL NOT NULL,
    url VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    scoretype_id BIGINT NOT NULL REFERENCES ScoreType(id) ON DELETE CASCADE,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS Publisher (
    id BIGSERIAL NOT NULL,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS Ctype (
    id BIGSERIAL NOT NULL,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS Region (
    id BIGSERIAL NOT NULL,
    name VARCHAR(255) NOT NULL,
    adjective VARCHAR(255),
    is_international BOOLEAN NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS Field (
    id BIGSERIAL NOT NULL,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS Conference (
    id BIGSERIAL NOT NULL,
    name VARCHAR(255) NOT NULL,
    short_name VARCHAR(255) NOT NULL,
    year_since INT,
    description TEXT,
    external_score DOUBLE PRECISION,
    user_score DOUBLE PRECISION,
    avg_score DOUBLE PRECISION,
    name_lower VARCHAR(255) NOT NULL,
    short_name_lower VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL ,
    ctype_id BIGINT NOT NULL REFERENCES Ctype(id) ON DELETE CASCADE,
    publisher_id BIGINT REFERENCES Publisher(id) ON DELETE CASCADE,
    region_id BIGINT NOT NULL REFERENCES Region(id) ON DELETE CASCADE,
    field_id BIGINT NOT NULL REFERENCES Field(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES IUser(id),
    PRIMARY KEY (id)
);

CREATE INDEX conference_name_lower_idx ON Conference USING hash (name_lower);
CREATE INDEX conference_short_name_lower_idx ON Conference USING hash (short_name_lower);

CREATE TABLE IF NOT EXISTS Subfield (
    id BIGSERIAL NOT NULL,
    name VARCHAR(255) NOT NULL,
    field_id BIGINT NOT NULL REFERENCES Field(id) ON DELETE CASCADE,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS Conference_Subfield (
    conference_id BIGINT NOT NULL REFERENCES Conference(id) ON DELETE CASCADE,
    subfield_id BIGINT NOT NULL REFERENCES SubField(id) ON DELETE CASCADE,
    PRIMARY KEY (conference_id,subfield_id)
);

CREATE TABLE IF NOT EXISTS Comment (
    id BIGSERIAL NOT NULL,
    conference_id BIGINT NOT NULL REFERENCES Conference(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    user_id BIGINT NOT NULL REFERENCES IUser(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS Tag (
    id BIGSERIAL NOT NULL,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS Conference_Tag (
    conference_id BIGINT NOT NULL REFERENCES Conference(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES Tag(id) ON DELETE CASCADE,
    PRIMARY KEY (conference_id,tag_id)
);

CREATE TABLE IF NOT EXISTS Conference_ExternalRanking (
    externalranking_id BIGSERIAL NOT NULL REFERENCES ExternalRanking(id) ON DELETE CASCADE,
    conference_id BIGINT NOT NULL REFERENCES Conference(id) ON DELETE CASCADE,
    score DOUBLE PRECISION NOT NULL,
    score_text VARCHAR(255) NOT NULL,
    PRIMARY KEY (externalranking_id,conference_id)
);

CREATE TABLE IF NOT EXISTS Link (
    id BIGSERIAL NOT NULL,
    conference_id BIGINT NOT NULL REFERENCES Conference(id) ON DELETE CASCADE, 
    url VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE,
    user_id BIGINT NOT NULL REFERENCES IUser(id),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS Conference_ScoreUser (
    user_id BIGINT NOT NULL REFERENCES IUser(id) ON DELETE CASCADE,
    conference_id BIGINT NOT NULL REFERENCES Conference(id) ON DELETE CASCADE,
    score DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (user_id,conference_id)
);

CREATE TABLE IF NOT EXISTS ConferenceRelationType (
    id BIGSERIAL NOT NULL,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS Conference_Conference (
    conference_from_id BIGINT NOT NULL REFERENCES Conference(id) ON DELETE CASCADE,
    conference_to_id BIGINT NOT NULL REFERENCES Conference(id) ON DELETE CASCADE,
    conference_relation_type_id BIGINT NOT NULL REFERENCES ConferenceRelationType(id) ON DELETE CASCADE,
    PRIMARY KEY (conference_from_id, conference_to_id)
);

CREATE TABLE IF NOT EXISTS Update (
    id BIGSERIAL NOT NULL,
    conference_id BIGINT NOT NULL REFERENCES Conference(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES IUser(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS Update_Subfield_Added (
    update_id BIGINT NOT NULL REFERENCES Update(id) ON DELETE CASCADE,
    subfield_id BIGINT NOT NULL REFERENCES Subfield(id) ON DELETE CASCADE,
    PRIMARY KEY (update_id,subfield_id)
);

CREATE TABLE IF NOT EXISTS Update_Subfield_Removed (
    update_id BIGINT NOT NULL REFERENCES Update(id) ON DELETE CASCADE,
    subfield_id BIGINT NOT NULL REFERENCES Subfield(id) ON DELETE CASCADE,
    PRIMARY KEY (update_id,subfield_id)
);

CREATE TABLE IF NOT EXISTS Update_Creation_Date (
    update_id BIGINT NOT NULL REFERENCES Update(id) ON DELETE CASCADE,
    before INT NOT NULL,
    after INT NOT NULL,
    PRIMARY KEY (update_id)
);

CREATE TABLE IF NOT EXISTS Update_Name (
    update_id BIGINT NOT NULL REFERENCES Update(id) ON DELETE CASCADE,
    before VARCHAR(255) NOT NULL,
    after VARCHAR(255) NOT NULL,
    PRIMARY KEY (update_id)
);

CREATE TABLE IF NOT EXISTS Update_ShortName (
    update_id BIGINT NOT NULL REFERENCES Update(id) ON DELETE CASCADE,
    before VARCHAR(255) NOT NULL,
    after VARCHAR(255) NOT NULL,
    PRIMARY KEY (update_id)
);

CREATE TABLE IF NOT EXISTS Update_CType (
    update_id BIGINT NOT NULL REFERENCES Update(id) ON DELETE CASCADE,
    before BIGINT NOT NULL REFERENCES CType(id) ON DELETE CASCADE,
    after BIGINT NOT NULL REFERENCES CType(id) ON DELETE CASCADE,
    PRIMARY KEY (update_id)
);

CREATE TABLE IF NOT EXISTS Update_Region (
    update_id BIGINT NOT NULL REFERENCES Update(id) ON DELETE CASCADE,
    before BIGINT NOT NULL REFERENCES Region(id) ON DELETE CASCADE,
    after BIGINT NOT NULL REFERENCES Region(id) ON DELETE CASCADE,
    PRIMARY KEY (update_id)
);

CREATE TABLE IF NOT EXISTS Update_Publisher (
    update_id BIGINT NOT NULL REFERENCES Update(id) ON DELETE CASCADE,
    before BIGINT NOT NULL REFERENCES Publisher(id) ON DELETE CASCADE,
    after BIGINT NOT NULL REFERENCES Publisher(id) ON DELETE CASCADE,
    PRIMARY KEY (update_id)
);

CREATE TABLE IF NOT EXISTS Update_Description (
    update_id BIGINT NOT NULL REFERENCES Update(id) ON DELETE CASCADE,
    before VARCHAR(255) NOT NULL,
    after VARCHAR(255) NOT NULL,
    PRIMARY KEY (update_id)
);

CREATE TABLE IF NOT EXISTS Stem (
    id BIGSERIAL NOT NULL,
    stem VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX stem_stem_idx ON Stem USING hash (stem);
CREATE INDEX region_is_international_idx ON Region USING hash (is_international);
CREATE INDEX conference_subfield_conference_id_idx ON Conference_Subfield USING hash (conference_id);
CREATE INDEX conference_subfield_subfield_id_idx ON Conference_Subfield USING hash (subfield_id);


CREATE TABLE IF NOT EXISTS Conference_Stem (
    stem_id BIGINT NOT NULL REFERENCES Stem(id) ON DELETE CASCADE,
    conference_id BIGINT NOT NULL REFERENCES Conference(id) ON DELETE CASCADE,
    PRIMARY KEY (stem_id, conference_id)
);

CREATE TABLE IF NOT EXISTS BibliometricSource (
    id BIGSERIAL NOT NULL,
    name VARCHAR(255) NOT NULL,
    url VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS Bibliometric (
    id BIGSERIAL NOT NULL,
    name VARCHAR(255) NOT NULL,
    value DOUBLE PRECISION NOT NULL,
    source_id BIGINT NOT NULL REFERENCES BibliometricSource(id),
    conference_id BIGINT NOT NULL REFERENCES Conference(id),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS Idea (
    id BIGSERIAL NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    done  BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    user_id BIGINT NOT NULL REFERENCES IUser(id),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS IdeaVote (
    idea_id BIGINT NOT NULL REFERENCES Idea(id),
    user_id BIGINT NOT NULL REFERENCES IUser(id),
    PRIMARY KEY (idea_id, user_id)
);

CREATE TABLE IF NOT EXISTS IdeaComment (
    id BIGSERIAL NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    idea_id BIGINT NOT NULL REFERENCES Idea(id),
    user_id BIGINT NOT NULL REFERENCES IUser(id),
    PRIMARY KEY (id)
);


# --- !Downs
 
DROP TABLE IF EXISTS Conference_Conference CASCADE;
DROP TABLE IF EXISTS Conference_ScoreUser CASCADE;
DROP TABLE IF EXISTS Link CASCADE;
DROP TABLE IF EXISTS Conference_Tag CASCADE;
DROP TABLE IF EXISTS Comment CASCADE;
DROP TABLE IF EXISTS Tag CASCADE;
DROP TABLE IF EXISTS IUser CASCADE;
DROP TABLE IF EXISTS Conference_Subfield CASCADE;
DROP TABLE IF EXISTS ConferenceRelation CASCADE;
DROP TABLE IF EXISTS ConferenceRelationType CASCADE;
DROP TABLE IF EXISTS Conference_Tag CASCADE;
DROP TABLE IF EXISTS Conference CASCADE;
DROP TABLE IF EXISTS Publisher CASCADE;
DROP TABLE IF EXISTS SubField CASCADE;
DROP TABLE IF EXISTS Field CASCADE;
DROP TABLE IF EXISTS ExternalRanking CASCADE;
DROP TABLE IF EXISTS Conference_ExternalRanking CASCADE;
DROP TABLE IF EXISTS Ctype CASCADE;
DROP TABLE IF EXISTS Region CASCADE;
DROP TABLE IF EXISTS ScoreType CASCADE;
DROP TABLE IF EXISTS Stem CASCADE;
DROP TABLE IF EXISTS Conference_Stem CASCADE;

DROP TABLE IF EXISTS Update CASCADE;
DROP TABLE IF EXISTS Update_Subfield_Added CASCADE;
DROP TABLE IF EXISTS Update_Subfield_Removed CASCADE;
DROP TABLE IF EXISTS Update_Creation_Date CASCADE;
DROP TABLE IF EXISTS Update_Name CASCADE;
DROP TABLE IF EXISTS Update_ShortName CASCADE;
DROP TABLE IF EXISTS Update_CType CASCADE;
DROP TABLE IF EXISTS Update_Region CASCADE;
DROP TABLE IF EXISTS Update_Publisher CASCADE;
DROP TABLE IF EXISTS Update_Description CASCADE;

DROP TABLE IF EXISTS BibliometricSource CASCADE;
DROP TABLE IF EXISTS Bibliometric CASCADE;
DROP TABLE IF EXISTS Idea CASCADE;
DROP TABLE IF EXISTS IdeaVote CASCADE;
DROP TABLE IF EXISTS IdeaComment CASCADE;
