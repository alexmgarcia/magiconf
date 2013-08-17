BEGIN;
CREATE TABLE "server_participant_contacts" (
    "id" integer NOT NULL PRIMARY KEY,
    "participant_id" varchar(20) NOT NULL,
    "contact_id" integer NOT NULL,
    UNIQUE ("participant_id", "contact_id")
)
;
CREATE TABLE "server_participant" (
    "username" varchar(20) NOT NULL PRIMARY KEY,
    "work_place" varchar(50) NOT NULL,
    "photo" varchar(100) NOT NULL,
    "name" varchar(30) NOT NULL,
    "country" varchar(20) NOT NULL,
    "phone_number" integer NOT NULL,
    "email" varchar(75) NOT NULL UNIQUE,
    "qrcode" varchar(100),
    "contact_id" integer NOT NULL UNIQUE,
    "user_id" integer NOT NULL UNIQUE REFERENCES "auth_user" ("id")
)
;
CREATE TABLE "server_participantexchangeemailrelationship" (
    "id" integer NOT NULL PRIMARY KEY,
    "participant_sent_request_id" varchar(20) NOT NULL REFERENCES "server_participant" ("username"),
    "participant_received_request_id" varchar(20) NOT NULL REFERENCES "server_participant" ("username"),
    "confirmed" bool NOT NULL
)
;
CREATE TABLE "server_exchangepin" (
    "id" integer NOT NULL PRIMARY KEY,
    "pin" integer NOT NULL,
    "date_created" datetime NOT NULL,
    "owner_id" varchar(20) NOT NULL UNIQUE REFERENCES "server_participant" ("username"),
    UNIQUE ("pin", "date_created")
)
;
CREATE TABLE "server_contact" (
    "id" integer NOT NULL PRIMARY KEY
)
;
CREATE TABLE "server_city" (
    "name" varchar(20) NOT NULL PRIMARY KEY,
    "description" text NOT NULL,
    "photo" varchar(100) NOT NULL
)
;
CREATE TABLE "server_sight" (
    "id" integer NOT NULL PRIMARY KEY,
    "name" varchar(30) NOT NULL,
    "address" varchar(50) NOT NULL,
    "description" text NOT NULL,
    "latitude" decimal NOT NULL,
    "longitude" decimal NOT NULL,
    "city_id" varchar(20) NOT NULL REFERENCES "server_city" ("name"),
    UNIQUE ("name", "address")
)
;
CREATE TABLE "server_hotel" (
    "sight_ptr_id" integer NOT NULL PRIMARY KEY REFERENCES "server_sight" ("id"),
    "stars" integer NOT NULL,
    "phone_number" varchar(9) NOT NULL
)
;
CREATE TABLE "server_restaurant" (
    "sight_ptr_id" integer NOT NULL PRIMARY KEY REFERENCES "server_sight" ("id"),
    "phone_number" varchar(9) NOT NULL
)
;
CREATE TABLE "server_conference" (
    "id" integer NOT NULL PRIMARY KEY,
    "name" varchar(100) NOT NULL,
    "edition" integer NOT NULL,
    "place" text NOT NULL,
    "beginning_date" date NOT NULL,
    "ending_date" date NOT NULL,
    "website" varchar(100) NOT NULL,
    "description" text NOT NULL,
    "latitude" decimal NOT NULL,
    "longitude" decimal NOT NULL,
    "city_id" varchar(20) NOT NULL REFERENCES "server_city" ("name"),
    "photo" varchar(100),
    UNIQUE ("name", "edition", "place")
)
;
CREATE TABLE "server_event" (
    "id" integer NOT NULL PRIMARY KEY,
    "title" varchar(100) NOT NULL,
    "place" varchar(50) NOT NULL,
    "time" datetime NOT NULL,
    "duration" integer NOT NULL,
    "conference_id" integer NOT NULL REFERENCES "server_conference" ("id"),
    UNIQUE ("title", "time")
)
;
CREATE TABLE "server_talksession_articles" (
    "id" integer NOT NULL PRIMARY KEY,
    "talksession_id" integer NOT NULL,
    "article_id" varchar(120) NOT NULL,
    UNIQUE ("talksession_id", "article_id")
)
;
CREATE TABLE "server_talksession" (
    "event_ptr_id" integer NOT NULL PRIMARY KEY REFERENCES "server_event" ("id")
)
;
CREATE TABLE "server_postersession_posters" (
    "id" integer NOT NULL PRIMARY KEY,
    "postersession_id" integer NOT NULL,
    "poster_id" varchar(120) NOT NULL,
    UNIQUE ("postersession_id", "poster_id")
)
;
CREATE TABLE "server_postersession" (
    "event_ptr_id" integer NOT NULL PRIMARY KEY REFERENCES "server_event" ("id")
)
;
CREATE TABLE "server_keynotesession_keynotes" (
    "id" integer NOT NULL PRIMARY KEY,
    "keynotesession_id" integer NOT NULL,
    "keynotespeaker_id" varchar(75) NOT NULL,
    UNIQUE ("keynotesession_id", "keynotespeaker_id")
)
;
CREATE TABLE "server_keynotesession" (
    "event_ptr_id" integer NOT NULL PRIMARY KEY REFERENCES "server_event" ("id"),
    "description" text NOT NULL
)
;
CREATE TABLE "server_workshopsession" (
    "event_ptr_id" integer NOT NULL PRIMARY KEY REFERENCES "server_event" ("id"),
    "description" text NOT NULL
)
;
CREATE TABLE "server_publication" (
    "title" varchar(120) NOT NULL PRIMARY KEY,
    "abstract" text NOT NULL
)
;
CREATE TABLE "server_article_authors" (
    "id" integer NOT NULL PRIMARY KEY,
    "article_id" varchar(120) NOT NULL,
    "author_id" varchar(75) NOT NULL,
    UNIQUE ("article_id", "author_id")
)
;
CREATE TABLE "server_article" (
    "publication_ptr_id" varchar(120) NOT NULL PRIMARY KEY REFERENCES "server_publication" ("title")
)
;
CREATE TABLE "server_notpresentedpublication" (
    "publication_ptr_id" varchar(120) NOT NULL PRIMARY KEY REFERENCES "server_publication" ("title"),
    "authors" text NOT NULL
)
;
CREATE TABLE "server_poster_authors" (
    "id" integer NOT NULL PRIMARY KEY,
    "poster_id" varchar(120) NOT NULL,
    "author_id" varchar(75) NOT NULL,
    UNIQUE ("poster_id", "author_id")
)
;
CREATE TABLE "server_poster" (
    "title" varchar(120) NOT NULL PRIMARY KEY
)
;
CREATE TABLE "server_organizationmember" (
    "email" varchar(75) NOT NULL PRIMARY KEY,
    "name" varchar(20) NOT NULL,
    "photo" varchar(100) NOT NULL,
    "role" varchar(2) NOT NULL,
    "work_place" varchar(50) NOT NULL,
    "country" varchar(20) NOT NULL,
    "conference_id" integer NOT NULL REFERENCES "server_conference" ("id")
)
;
CREATE TABLE "server_notification_participants" (
    "id" integer NOT NULL PRIMARY KEY,
    "notification_id" integer NOT NULL,
    "participant_id" varchar(20) NOT NULL REFERENCES "server_participant" ("username"),
    UNIQUE ("notification_id", "participant_id")
)
;
CREATE TABLE "server_notification" (
    "id" integer NOT NULL PRIMARY KEY,
    "title" varchar(20) NOT NULL,
    "date" datetime NOT NULL,
    "description" text NOT NULL,
    UNIQUE ("title", "date")
)
;
CREATE TABLE "server_author" (
    "email" varchar(75) NOT NULL PRIMARY KEY,
    "name" varchar(30) NOT NULL,
    "work_place" varchar(50) NOT NULL,
    "country" varchar(20) NOT NULL,
    "photo" varchar(100) NOT NULL,
    "participant_id" varchar(20) UNIQUE REFERENCES "server_participant" ("username"),
    "contact_id" integer UNIQUE REFERENCES "server_contact" ("id")
)
;
CREATE TABLE "server_keynotespeaker" (
    "email" varchar(75) NOT NULL PRIMARY KEY,
    "name" varchar(20) NOT NULL,
    "work_place" varchar(50) NOT NULL,
    "country" varchar(20) NOT NULL,
    "photo" varchar(100) NOT NULL,
    "participant_id" varchar(20) UNIQUE REFERENCES "server_participant" ("username"),
    "author_id" varchar(75) UNIQUE REFERENCES "server_author" ("email"),
    "contact_id" integer UNIQUE REFERENCES "server_contact" ("id")
)
;
CREATE TABLE "server_sponsor" (
    "name" varchar(100) NOT NULL PRIMARY KEY,
    "logo" varchar(100) NOT NULL,
    "website" varchar(200) NOT NULL,
    "conference_id" integer NOT NULL REFERENCES "server_conference" ("id")
)
;

COMMIT;
