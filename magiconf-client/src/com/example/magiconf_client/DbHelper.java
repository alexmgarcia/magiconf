package com.example.magiconf_client;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {

	public static final String COLUMN_ID="_id";
	public static final String DJANGO_ID="orig_id";
	private static final String DATABASE_NAME = "magic.db";

	private static final int DATABASE_VERSION = 999;

	
	
	
	//Table names
	public static final String TABLE_EVENTS ="events";
	public static final String TABLE_AUTHORS = "authors";
	public static final String TABLE_KEYNOTES = "keynotes";
	public static final String TABLE_KEYSESSIONS = "keynoteSessions";
	public static final String TABLE_WORKSHOPS = "workshops";
	public static final String TABLE_TALKS = "talks";
	public static final String TABLE_PUBLICATIONS = "publications";
	public static final String TABLE_NOT_PRESENTED_PUBLICATIONS = "notPresentedPublications";
	public static final String TABLE_ARTICLES = "articles";
	public static final String TABLE_PARTICIPANTS = "participants";
	public static final String TABLE_POSTERS = "posters";
	public static final String TABLE_CONFERENCES = "conference";
	public static final String TABLE_POSTERSESSION = "posterSessions";
	public static final String TABLE_SOCIALEVENTS= "socialEvents";
	public static final String TABLE_FAVPUBLICATIONS = "favouritePublications";
	public static final String TABLE_CONTACTS = "contacts";
	public static final String TABLE_CITIES = "city";
	public static final String TABLE_SPONSORS = "sponsors";
	public static final String TABLE_MEMBERS_ORG = "members";
	public static final String TABLE_SIGHTS = "sights";
	public static final String TABLE_HOTELS = "hotels";
	public static final String TABLE_RESTAURANTS = "restaurants";
	
	public static final String TABLE_SESSIONS = "sessions";
	public static final String TABLE_NOTIFICATIONS = "notifications";
	
	//Relationship tables
	public static final String TABLE_RELATIONSHIP_KEYNOTES_KEYSESSIONS = TABLE_KEYNOTES+"_"+TABLE_KEYSESSIONS;
	public static final String TABLE_ARTICLEPRESENTED= TABLE_ARTICLES+"_"+TABLE_TALKS;
	public static final String TABLE_POSTERS_PRESENTED = TABLE_POSTERS + "_"+ TABLE_POSTERSESSION; 
	public static final String TABLE_RELATIONSHIP_ARTICLE_AUTHOR = TABLE_ARTICLES+"_"+TABLE_AUTHORS; 
	public static final String TABLE_RELATIONSHIP_POSTER_AUTHOR = TABLE_POSTERS+"_"+TABLE_AUTHORS;
	public static final String TABLE_RELATIONSHIP_PARTICIPANT_EVENT = TABLE_PARTICIPANTS + "_" + TABLE_EVENTS;
	
	public static final String TABLE_RELATIONSHIP_PARTICIPANT_CONTACT = TABLE_PARTICIPANTS+ "_" + TABLE_CONTACTS;
	public static final String TABLE_RELATIONSHIP_PARTICIPANT_KEYNOTE = TABLE_PARTICIPANTS+ "_" + TABLE_KEYNOTES;
	public static final String TABLE_RELATIONSHIP_PARTICIPANT_AUTHOR = TABLE_PARTICIPANTS+ "_" + TABLE_AUTHORS;
	
	public static final String TABLE_RELATIONSHIP_PARTICIPANT_SESSIONS=TABLE_PARTICIPANTS+"_"+TABLE_SESSIONS;

	//Atributtes	
	public static final String LAST_MODIFIED_DATE = "lastmodified";
	public static final String LAST_CHECK = "last_check";
	
	public static final String SESSION_LASTMODIFIED = "lastmodified";
	public static final String SESSION_PASSWORD = "password";
	public static final String SESSION_ID = "session_id";
	
	public static final String EVENT_TITLE = "title";
	public static final String EVENT_TIME = "time";
	public static final String EVENT_PLACE = "place";
	public static final String EVENT_DURATION = "duration";
	public static final String EVENT_ID = "event_id";
	
	public static final String AUTHOR_EMAIL = "email";
	public static final String AUTHOR_WORKPLACE = "workplace";
	public static final String AUTHOR_COUNTRY = "country";
	public static final String AUTHOR_PHOTO = "photo";
	public static final String AUTHOR_NAME = "name";
	public static final String AUTHOR_CONTACT_ID = "contact_id";
	
	public static final String KEYNOTE_EMAIL = "email";
	public static final String KEYNOTE_WORKPLACE = "workplace";
	public static final String KEYNOTE_COUNTRY = "country";
	public static final String KEYNOTE_PHOTO = "photo";
	public static final String KEYNOTE_NAME = "name";
	public static final String KEYNOTE_CONTACT_ID = "contact_id";
	
	public static final String KEYSESSION_TIME = EVENT_TIME;
	public static final String KEYSESSION_TITLE = EVENT_TITLE;
	public static final String KEYSESSION_DESCRIPTION = "description";
	public static final String KEYSESSION_EVENT_ID = EVENT_ID;
	
	public static final String WORKSHOP_DESCRIPTION = "description";
	public static final String WORKSHOP_EVENT_ID = EVENT_ID;
	
	public static final String TALK_TIME = EVENT_TIME;
	public static final String TALK_TITLE = EVENT_TITLE;
	public static final String TALK_EVENT_ID = EVENT_ID;

	
	public static final String PUB_TITLE = "title";
	public static final String PUB_ABSTRACT = "abstract";
	
	public static final String ARTICLE_TITLE = PUB_TITLE;
	public static final String ARTICLE_ID = "pub_id";
		
	public static final String NOT_PRESENTED_PUBLICATION_PUB_ID = "pub_id";
	public static final String NOT_PRESENTED_PUBLICATION_AUTHORS = "authors";
	
	public static final String FAV_PUBLICATION_PUB_ID = "pub_id";
	public static final String FAV_PUBLICATION_USERNAME = "participant_username";
	
	public static final String PARTICIPANT_USERNAME = "username";
	public static final String PARTICIPANT_NAME = "name";
	public static final String PARTICIPANT_EMAIL = "email";
	public static final String PARTICIPANT_COUNTRY ="country";
	public static final String PARTICIPANT_WORKPLACE= "workplace";
	public static final String PARTICIPANT_PHONE ="phone";
	public static final String PARTICIPANT_PHOTO = "photo";
	public static final String PARTICIPANT_QRCODE = "qrcode";
	public static final String PARTICIPANT_LINKEDIN = "linkedin_url";
	public static final String PARTICIPANT_FACEBOOK = "facebook_url";

	public static final String CONTACT_NAME = "name";
	public static final String CONTACT_EMAIL = "email";
	public static final String CONTACT_COUNTRY ="country";
	public static final String CONTACT_WORKPLACE= "workplace";
	public static final String CONTACT_PHONE ="phone";
	public static final String CONTACT_PHOTO = "photo";
	public static final String CONTACT_FACEBOOK = "facebook_url";
	public static final String CONTACT_LINKEDIN = "linkedin_url";
	public static final String CONTACT_ID = "id";
	
	public static final String POSTER_TITLE = "title";
	

	public static final String CONFERENCE_TITLE = "title";
	public static final String CONFERENCE_BDATE = "begginingDate";
	public static final String CONFERENCE_EDATE = "endingDate";
	public static final String CONFERENCE_DESCRIPTION = "description";
	public static final String CONFERENCE_EDITION ="edition";
	public static final String CONFERENCE_PLACE = "place";
	public static final String CONFERENCE_LATITUDE ="latitude";
	public static final String CONFERENCE_LONGITUDE = "longitude";
	public static final String CONFERENCE_WEBSITE = "website";
	public static final String CONFERENCE_LOGO = "logo";
	
	public static final String CITY_NAME = "name";
	public static final String CITY_PHOTO = "photo";
	public static final String CITY_DESCRIPTION = "description";
	
	public static final String SIGHT_NAME = "name";
	public static final String SIGHT_ADDRESS = "address";
	public static final String SIGHT_DESCRIPTION = "description";
	public static final String SIGHT_LONGITUDE = "longitude";
	public static final String SIGHT_LATITUDE = "latitude";
	public static final String SIGHT_PHOTO = "photo";
	
	public static final String HOTEL_ID = "sightID";
	public static final String HOTEL_STARS = "stars";
	public static final String HOTEL_PHONE = "phone";
	
	public static final String RESTAURANT_ID = "sightID";
	public static final String RESTAURANT_PHONE = "phone";
	
	public static final String SOCIALEVENTS_TIME =EVENT_TIME;
	public static final String SOCIALEVENTS_TITLE = EVENT_TITLE;
	public static final String SOCIALEVENTS_EVENT_ID = "event_id";
	
	public static final String POSTER_ID = "poster_id";

	public static final String POSTERSESSION_TIME =EVENT_TIME;
	public static final String POSTERSESSION_TITLE = EVENT_TITLE;
	public static final String POSTERSESSION_ID = "event_id";
	
	
	public static final String SPONSOR_NAME="sponsor_name";
	public static final String SPONSOR_LOGO= "sponsor_logo";
	public static final String SPONSOR_WEBSITE="sponsor_website";
	
	
	public static final String MEMBER_EMAIL = "email";
	public static final String MEMBER_WORKPLACE = "workplace";
	public static final String MEMBER_COUNTRY = "country";
	public static final String MEMBER_PHOTO = "photo";
	public static final String MEMBER_NAME = "name";
	public static final String MEMBER_ROLE = "role";
	
	public static final String NOTIFICATION_TITLE = "title";
	public static final String NOTIFICATION_DESCRIPTION = "description";
	public static final String NOTIFICATION_TIME = "time";
	
		
	public static final String RELATIONSHIP_KEYNOTES_KEYSESSIONS_KEYNOTE_ID = TABLE_KEYNOTES+COLUMN_ID;
	public static final String RELATIONSHIP_KEYNOTES_KEYSESSIONS_KEYNOTESESSION_ID = TABLE_KEYSESSIONS+COLUMN_ID;

	
	public static final String POSTERS_PRESENTED_POSTER_ID= TABLE_POSTERS+COLUMN_ID;
	public static final String POSTERS_PRESENTED_POSTERSESSION_ID= TABLE_POSTERSESSION+COLUMN_ID;
	
	public static final String ARTICLES_PRESENTED_ARTICLE_ID = TABLE_ARTICLES+COLUMN_ID;
	public static final String ARTICLES_PRESENTED_TALK_ID = TABLE_TALKS+COLUMN_ID;
	
	public static final String RELATIONSHIP_ARTICLE_AUTHOR_ARTICLE_ID = TABLE_ARTICLES+COLUMN_ID;
	public static final String RELATIONSHIP_ARTICLE_AUTHOR_AUTHOR_ID = TABLE_AUTHORS+COLUMN_ID;
	public static final String RELATIONSHIP_POSTER_AUTHOR_POSTER_ID = TABLE_POSTERS+COLUMN_ID;
	public static final String RELATIONSHIP_POSTER_AUTHOR_AUTHOR_ID = TABLE_AUTHORS+COLUMN_ID;
	
	public static final String RELATIONSHIP_PARTICIPANT_EVENT_PARTICIPANT_USERNAME = TABLE_PARTICIPANTS+"_"+PARTICIPANT_USERNAME;
	public static final String RELATIONSHIP_PARTICIPANT_EVENT_EVENT_ID = TABLE_EVENTS+COLUMN_ID;
	
	public static final String RELATIONSHIP_PARTICIPANT_CONTACT_PARTICIPANT_USERNAME= TABLE_PARTICIPANTS+"_"+PARTICIPANT_USERNAME;
	public static final String RELATIONSHIP_PARTICIPANT_CONTACT_CONTACT_ID = TABLE_CONTACTS+"_"+COLUMN_ID;

	public static final String RELATIONSHIP_PARTICIPANT_KEYNOTE_PARTICIPANT_USERNAME = TABLE_PARTICIPANTS+"_"+PARTICIPANT_USERNAME;
	public static final String RELATIONSHIP_PARTICIPANT_KEYNOTE_KEYNOTE_ID = TABLE_KEYNOTES+"_"+COLUMN_ID;
	
	public static final String RELATIONSHIP_PARTICIPANT_AUTHOR_PARTICIPANT_USERNAME = TABLE_PARTICIPANTS+"_"+PARTICIPANT_USERNAME;
	public static final String RELATIONSHIP_PARTICIPANT_AUTHOR_AUTHOR_ID = TABLE_AUTHORS+"_"+COLUMN_ID;
	
	public static final String RELATIONSHIP_PARTICIPANT_SESSIONS_USERNAME=TABLE_PARTICIPANTS+"_"+PARTICIPANT_USERNAME;
	public static final String RELATIONSHIP_PARTICIPANT_SESSIONS_ID=TABLE_SESSIONS+COLUMN_ID;
	
	//Instructions to create tables
	
	private static final String CITY_CREATE = 
			"create table "+ TABLE_CITIES + "(" + 
					COLUMN_ID + " integer primary key autoincrement, " +
					CITY_NAME + " text not null unique, " +
					CITY_DESCRIPTION + " text, " +
					CITY_PHOTO + " text);";
	
	private static final String SIGHTS_CREATE =
			"create table "+ TABLE_SIGHTS + "(" + 
					COLUMN_ID + " integer primary key autoincrement, " +
					SIGHT_NAME + " text not null, " +
					SIGHT_ADDRESS + " text not null, " +
					SIGHT_DESCRIPTION + " text not null, " +
					SIGHT_LATITUDE + " text not null, "+
					SIGHT_LONGITUDE + " text not null, "+
					SIGHT_PHOTO + " text, "+
					"unique ("+SIGHT_NAME +","+SIGHT_ADDRESS+"));";
	
	private static final String EVENTS_CREATE = 
			"create table "+ TABLE_EVENTS + "(" + 
					COLUMN_ID + " integer primary key autoincrement, " +
					EVENT_TITLE + " text not null, " +
					EVENT_TIME + " integer not null, " +
					EVENT_PLACE + " text not null, " +
					EVENT_DURATION + " integer not null, " +
					LAST_MODIFIED_DATE + " integer not null, " +
					DJANGO_ID + " integer not null unique, "+
					"unique ("+ EVENT_TIME +","+ EVENT_TITLE+"));";
	
	private static final String AUTHORS_CREATE = 
			"create table "+ TABLE_AUTHORS + "(" + 
					COLUMN_ID + " integer primary key autoincrement, " +
					AUTHOR_EMAIL + " text not null unique, "+
					AUTHOR_NAME + " text not null, " +
					AUTHOR_WORKPLACE + " text not null, " +
					AUTHOR_COUNTRY + " text not null, " +
					AUTHOR_PHOTO + " text, " +
					AUTHOR_CONTACT_ID + " integer," +
					"foreign key (" + AUTHOR_CONTACT_ID + ") references " + TABLE_CONTACTS + "(" + COLUMN_ID + "));";
	
	private static final String PUBLICATIONS_CREATE = 
			"create table "+ TABLE_PUBLICATIONS + "(" + 
					COLUMN_ID + " integer primary key autoincrement, " +
					PUB_TITLE + " text not null unique, "+
					PUB_ABSTRACT + " text, "+
					LAST_MODIFIED_DATE + " integer not null);";
					
	private static final String KEYNOTES_CREATE = 
			"create table "+ TABLE_KEYNOTES + "(" + 
					COLUMN_ID + " integer primary key autoincrement, " +
					KEYNOTE_EMAIL + " text not null unique, "+
					KEYNOTE_NAME + " text not null, " +
					KEYNOTE_WORKPLACE + " text not null, " +
					KEYNOTE_COUNTRY + " text not null, " +
					KEYNOTE_PHOTO + " text," +
					KEYNOTE_CONTACT_ID + " integer," +
					"foreign key (" + KEYNOTE_CONTACT_ID + ") references " + TABLE_CONTACTS + "(" + COLUMN_ID + "));";
					//TABLE_KEYSESSIONS + COLUMN_ID + " integer not null, " +
					//"foreign key ("+KEYSESSION_TIME+","+KEYSESSION_TITLE+") references "+TABLE_KEYSESSIONS+");";
					//"foreign key ("+TABLE_KEYSESSIONS + COLUMN_ID+") references "+TABLE_KEYSESSIONS+");"; // TODO
	
	private static final String CONTACTS_CREATE = 
			"create table "+ TABLE_CONTACTS + "(" + 
					COLUMN_ID + " integer primary key, " +
					CONTACT_NAME + " text not null, "+
					CONTACT_EMAIL + " text not null unique, "+
					CONTACT_COUNTRY + " text not null, "+
					CONTACT_WORKPLACE+ " text not null, "+
					CONTACT_PHONE + " text not null, "+
					CONTACT_PHOTO + " text," +
					CONTACT_FACEBOOK + " text," +
					CONTACT_LINKEDIN + " text);";
	
	
	
					
	private static final String KEYSESSIONS_CREATE = 
			"create table "+ TABLE_KEYSESSIONS + "(" + 
					COLUMN_ID + " integer primary key autoincrement, " +
					KEYSESSION_EVENT_ID + " integer not null unique, " +
					KEYSESSION_DESCRIPTION + " text not null," +
					DJANGO_ID + " integer not null unique, "+
					"foreign key (" + KEYSESSION_EVENT_ID + ") references " + TABLE_EVENTS +"("+COLUMN_ID+"));";
	
	private static final String WORKSHOPS_CREATE = 
			"create table "+ TABLE_WORKSHOPS + "(" + 
					COLUMN_ID + " integer primary key autoincrement, " +
					WORKSHOP_EVENT_ID + " integer not null unique, " +
					WORKSHOP_DESCRIPTION + " text not null," +
					DJANGO_ID + " integer not null unique, "+
					"foreign key (" + WORKSHOP_EVENT_ID + ") references " + TABLE_EVENTS +"("+COLUMN_ID+"));";
	
	private static final String RELATIONSHIP_KEYNOTES_KEYSESSIONS_CREATE =
			"create table " + TABLE_RELATIONSHIP_KEYNOTES_KEYSESSIONS + "(" +
					RELATIONSHIP_KEYNOTES_KEYSESSIONS_KEYNOTE_ID + " integer not null, " +
					RELATIONSHIP_KEYNOTES_KEYSESSIONS_KEYNOTESESSION_ID + " integer not null, " + 
					"primary key (" + RELATIONSHIP_KEYNOTES_KEYSESSIONS_KEYNOTE_ID + ", " + RELATIONSHIP_KEYNOTES_KEYSESSIONS_KEYNOTESESSION_ID + "), " +
					"foreign key (" + RELATIONSHIP_KEYNOTES_KEYSESSIONS_KEYNOTE_ID + ") references " + TABLE_KEYNOTES + "(" + COLUMN_ID + "), " + 
					"foreign key (" + RELATIONSHIP_KEYNOTES_KEYSESSIONS_KEYNOTESESSION_ID + ") references " + TABLE_KEYSESSIONS + "(" + COLUMN_ID + "));"; 
	
	private static final String TALKS_CREATE = 
			"create table "+ TABLE_TALKS + "(" + 
					COLUMN_ID + " integer primary key autoincrement, " +
					TALK_EVENT_ID + " integer not null unique, " +
					DJANGO_ID + " integer not null unique, "+
					"foreign key (" + TALK_EVENT_ID + ") references " + TABLE_EVENTS +"("+COLUMN_ID+"));";
	
	private static final String ARTICLES_CREATE = 
			"create table "+ TABLE_ARTICLES + "(" + 
					COLUMN_ID + " integer primary key autoincrement, " +
					ARTICLE_ID + " integer not null unique, "+
					"foreign key ("+ARTICLE_ID+") references "+TABLE_PUBLICATIONS+"("+COLUMN_ID+"));";
	
	private static final String NOT_PRESENTED_PUBLICATIONS_CREATE = 
			"create table "+ TABLE_NOT_PRESENTED_PUBLICATIONS + "(" + 
					COLUMN_ID + " integer primary key autoincrement, " +
					NOT_PRESENTED_PUBLICATION_PUB_ID + " integer not null unique, "+
					NOT_PRESENTED_PUBLICATION_AUTHORS + " text not null, " +
					"foreign key ("+NOT_PRESENTED_PUBLICATION_PUB_ID+") references "+TABLE_PUBLICATIONS+"("+COLUMN_ID+"));";
	
	private static final String POSTERS_CREATE = 
			"create table "+ TABLE_POSTERS + "(" + 
					COLUMN_ID + " integer primary key autoincrement, " +
					POSTER_TITLE + " text not null, "+
					LAST_MODIFIED_DATE + " integer not null);";

	private static final String SESSIONS_CREATE = 
			"create table "+ TABLE_SESSIONS + "(" + 
					COLUMN_ID + " integer primary key autoincrement, " +
					SESSION_PASSWORD + " text not null, "+
					SESSION_LASTMODIFIED + " integer not null);";
	
	private static final String PARTICIPANTS_CREATE = 
			"create table "+ TABLE_PARTICIPANTS + "(" + 
					PARTICIPANT_USERNAME  + " text primary key, "+
					//PARTICIPANT_PASSWORD + " text not null, "+
					PARTICIPANT_NAME + " text not null, "+
					PARTICIPANT_EMAIL + " text not null unique, "+
					PARTICIPANT_COUNTRY + " text not null, "+
					PARTICIPANT_WORKPLACE+ " text not null, "+
					PARTICIPANT_PHONE + " text not null, "+
					PARTICIPANT_PHOTO + " text, "+
					PARTICIPANT_QRCODE+ " text not null unique, " +
					PARTICIPANT_LINKEDIN + " text, " + 
					PARTICIPANT_FACEBOOK + " text," +
					LAST_CHECK + " integer not null);";			
	
	private static final String CONFERENCES_CREATE =
			"create table "+ TABLE_CONFERENCES + "(" + 
					COLUMN_ID + " integer primary key autoincrement, " +
					
 					 CONFERENCE_TITLE + " text not null unique, "+
					 CONFERENCE_BDATE +  " integer not null, " +
					 CONFERENCE_EDATE +  " integer not null, " +
					 CONFERENCE_DESCRIPTION + " text not null, "+
					 CONFERENCE_EDITION + " integer not null, "+
					 CONFERENCE_PLACE + " text not null, "+
					 CONFERENCE_LATITUDE + " text not null, "+
					 CONFERENCE_LONGITUDE + " text not null, "+
					 CONFERENCE_WEBSITE + " text not null," +
					 CONFERENCE_LOGO + " text);";
	
	
	private static final String SOCIALEVENTS_CREATE = //FALTA ADICIONAR REFERENCIAS
			"create table "+ TABLE_SOCIALEVENTS + "(" + 
					COLUMN_ID + " integer primary key autoincrement, " +
					//SOCIALEVENTS_TITLE + " text not null, "+
					//SOCIALEVENTS_TIME + " date not null, "+
					"event_id integer not null unique, " +
					"foreign key (event_id) references " + TABLE_EVENTS +"("+COLUMN_ID+"));";
					
					//+SOCIALEVENTS_TIME+","+SOCIALEVENTS_TITLE+") references "+TABLE_EVENTS+");";
	
	private static final String POSTERSESSIONS_CREATE = 
			"create table "+ TABLE_POSTERSESSION + "(" + 
					COLUMN_ID + " integer primary key autoincrement, " +
					"event_id integer not null unique, " +
					DJANGO_ID + " integer not null unique, "+
					"foreign key (event_id) references " + TABLE_EVENTS +"("+COLUMN_ID+"));";
	
	private static final String HOTELS_CREATE = 
			"create table "+ TABLE_HOTELS + "(" + 
					COLUMN_ID + " integer primary key autoincrement, " +
					HOTEL_ID + " integer not null unique, "+
					HOTEL_STARS + " integer not null, "+
					HOTEL_PHONE + " text not null, " +
					"foreign key ("+HOTEL_ID+") references " + TABLE_SIGHTS +"("+COLUMN_ID+"));";
	
	private static final String RESTAURANTS_CREATE = 
			"create table "+ TABLE_RESTAURANTS + "(" + 
					COLUMN_ID + " integer primary key autoincrement, " +
					RESTAURANT_ID + " integer not null unique, "+
					RESTAURANT_PHONE + " text not null, " +
					"foreign key ("+RESTAURANT_ID+") references " + TABLE_SIGHTS +"("+COLUMN_ID+"));";
	
	private static final String SPONSORS_CREATE=
			"create table "+ TABLE_SPONSORS + "("+
					COLUMN_ID + " integer primary key autoincrement, "+
					SPONSOR_NAME + " text not null unique, "+
					SPONSOR_LOGO + " text, " +
					SPONSOR_WEBSITE + " text );";
	
	private static final String MEMBERS_CREATE = 
			"create table "+ TABLE_MEMBERS_ORG + "(" + 
					COLUMN_ID + " integer primary key autoincrement, " +
					MEMBER_EMAIL + " text not null unique, "+
					MEMBER_NAME + " text not null, " +
					MEMBER_WORKPLACE + " text not null, " +
					MEMBER_COUNTRY + " text not null, " +
					MEMBER_PHOTO + " text, "+
					MEMBER_ROLE + " text not null);";
	
	private static final String NOTIFICATIONS_CREATE =
			"create table " + TABLE_NOTIFICATIONS + "(" +
					COLUMN_ID + " integer primary key autoincrement, " +
					NOTIFICATION_TITLE + " text not null, " +
					NOTIFICATION_DESCRIPTION + " text not null, " +
					NOTIFICATION_TIME + " integer not null, " +
					"unique ("+ NOTIFICATION_TITLE +","+ NOTIFICATION_TIME+"));";
	
	private static final String ARTICLEPRESENTED_CREATE = 
			"create table "+ TABLE_ARTICLEPRESENTED + "(" + 
					ARTICLES_PRESENTED_TALK_ID + " integer not null, " +
					ARTICLES_PRESENTED_ARTICLE_ID + " integer not null, " + 
					"primary key (" + ARTICLES_PRESENTED_TALK_ID + ", " + ARTICLES_PRESENTED_ARTICLE_ID + "), " +
					"foreign key (" + ARTICLES_PRESENTED_TALK_ID + ") references " + TABLE_TALKS + "(" + COLUMN_ID + "), " + 
					"foreign key (" + ARTICLES_PRESENTED_ARTICLE_ID + ") references " + TABLE_ARTICLES + "(" + COLUMN_ID + "));"; 
	
	private static final String FAVPUBLICATIONS_CREATE = 
			"create table "+ TABLE_FAVPUBLICATIONS + "(" + 
					COLUMN_ID + " integer primary key autoincrement, " +
					FAV_PUBLICATION_PUB_ID + " integer not null, "+
					FAV_PUBLICATION_USERNAME  + " text not null, "+
					"unique (" + FAV_PUBLICATION_PUB_ID + ", " + FAV_PUBLICATION_USERNAME + "), " + 
					"foreign key ("+FAV_PUBLICATION_PUB_ID+") references "+TABLE_PUBLICATIONS + "(" + COLUMN_ID + "), " +
					"foreign key ("+FAV_PUBLICATION_USERNAME+") references "+TABLE_PARTICIPANTS + "(" + PARTICIPANT_USERNAME + "));";
	
	
	/*private static final String FAVPUBLICATIONS_CREATE = // TODO usar o anterior quando ja houver login implementado
			"create table "+ TABLE_FAVPUBLICATIONS + "(" + 
					COLUMN_ID + " integer primary key autoincrement, " +
					FAV_PUBLICATION_PUB_ID + " integer not null, "+
					"foreign key ("+FAV_PUBLICATION_PUB_ID+") references "+TABLE_PUBLICATIONS + "(" + COLUMN_ID + "));";*/
	
	private static final String POSTERS_PRESENTED_CREATE =
			"create table " + TABLE_POSTERS_PRESENTED + "(" +
					POSTERS_PRESENTED_POSTER_ID + " integer not null, " +
					POSTERS_PRESENTED_POSTERSESSION_ID + " integer not null, " + 
					"primary key (" + POSTERS_PRESENTED_POSTER_ID + ", " + POSTERS_PRESENTED_POSTERSESSION_ID + "), " +
					"foreign key (" + POSTERS_PRESENTED_POSTER_ID + ") references " + TABLE_POSTERS + "(" + COLUMN_ID + "), " + 
					"foreign key (" + POSTERS_PRESENTED_POSTERSESSION_ID + ") references " + TABLE_POSTERSESSION + "(" + COLUMN_ID + "));";
	
	private static final String RELATIONSHIP_ARTICLE_AUTHOR_CREATE =
			"create table " + TABLE_RELATIONSHIP_ARTICLE_AUTHOR + "(" +
					RELATIONSHIP_ARTICLE_AUTHOR_ARTICLE_ID + " integer not null, " +
					RELATIONSHIP_ARTICLE_AUTHOR_AUTHOR_ID + " integer not null, " + 
					"primary key (" + RELATIONSHIP_ARTICLE_AUTHOR_ARTICLE_ID + ", " + RELATIONSHIP_ARTICLE_AUTHOR_AUTHOR_ID + "), " +
					"foreign key (" + RELATIONSHIP_ARTICLE_AUTHOR_ARTICLE_ID + ") references " + TABLE_ARTICLES + "(" + COLUMN_ID + "), " + 
					"foreign key (" + RELATIONSHIP_ARTICLE_AUTHOR_AUTHOR_ID + ") references " + TABLE_AUTHORS + "(" + COLUMN_ID + "));"; 
	
	private static final String RELATIONSHIP_POSTER_AUTHOR_CREATE =
			"create table " + TABLE_RELATIONSHIP_POSTER_AUTHOR + "(" +
					RELATIONSHIP_POSTER_AUTHOR_POSTER_ID + " integer not null, " +
					RELATIONSHIP_POSTER_AUTHOR_AUTHOR_ID + " integer not null, " + 
					"primary key (" + RELATIONSHIP_POSTER_AUTHOR_POSTER_ID + ", " + RELATIONSHIP_POSTER_AUTHOR_AUTHOR_ID + "), " +
					"foreign key (" + RELATIONSHIP_POSTER_AUTHOR_POSTER_ID + ") references " + TABLE_POSTERS + "(" + COLUMN_ID + "), " + 
					"foreign key (" + RELATIONSHIP_POSTER_AUTHOR_AUTHOR_ID + ") references " + TABLE_AUTHORS + "(" + COLUMN_ID + "));";
	
	private static final String RELATIONSHIP_PARTICIPANT_EVENT_CREATE =
			"create table " + TABLE_RELATIONSHIP_PARTICIPANT_EVENT + "(" +
					RELATIONSHIP_PARTICIPANT_EVENT_PARTICIPANT_USERNAME + " text not null, " +
					RELATIONSHIP_PARTICIPANT_EVENT_EVENT_ID + " integer not null, " +
					"primary key (" + RELATIONSHIP_PARTICIPANT_EVENT_PARTICIPANT_USERNAME + ", " + RELATIONSHIP_PARTICIPANT_EVENT_EVENT_ID + "), " +
					"foreign key (" + RELATIONSHIP_PARTICIPANT_EVENT_PARTICIPANT_USERNAME + ") references " + TABLE_PARTICIPANTS + "(" + PARTICIPANT_USERNAME + "), " +
					"foreign key (" + RELATIONSHIP_PARTICIPANT_EVENT_EVENT_ID + ") references " + TABLE_EVENTS + "(" + COLUMN_ID + "));";
	
	private static final String RELATIONSHIP_PARTICIPANT_SESSIONS_CREATE =
			"create table " + TABLE_RELATIONSHIP_PARTICIPANT_SESSIONS + "(" +
					RELATIONSHIP_PARTICIPANT_SESSIONS_USERNAME + " text not null, " +
					RELATIONSHIP_PARTICIPANT_SESSIONS_ID + " integer not null, " +
					"primary key (" + RELATIONSHIP_PARTICIPANT_SESSIONS_USERNAME + ", " + RELATIONSHIP_PARTICIPANT_SESSIONS_ID + "), " +
					"foreign key (" + RELATIONSHIP_PARTICIPANT_SESSIONS_USERNAME + ") references " + TABLE_PARTICIPANTS + "(" + PARTICIPANT_USERNAME + "), " +
					"foreign key (" + RELATIONSHIP_PARTICIPANT_SESSIONS_ID + ") references " + TABLE_SESSIONS + "(" + SESSION_ID + "));";
	
	private static final String RELATIONSHIP_PARTICIPANT_CONTACT_CREATE =
			"create table " + TABLE_RELATIONSHIP_PARTICIPANT_CONTACT+ "(" +
					RELATIONSHIP_PARTICIPANT_CONTACT_PARTICIPANT_USERNAME + " text not null, " +
					RELATIONSHIP_PARTICIPANT_CONTACT_CONTACT_ID + " text not null, " +
					"primary key (" + RELATIONSHIP_PARTICIPANT_CONTACT_PARTICIPANT_USERNAME + ", " + RELATIONSHIP_PARTICIPANT_CONTACT_CONTACT_ID + "), " +
					"foreign key (" + RELATIONSHIP_PARTICIPANT_CONTACT_PARTICIPANT_USERNAME + ") references " + TABLE_PARTICIPANTS + "(" + PARTICIPANT_USERNAME + "), " +
					"foreign key (" + RELATIONSHIP_PARTICIPANT_CONTACT_CONTACT_ID + ") references " + TABLE_CONTACTS + "(" + COLUMN_ID + "));";
	
	private static final String RELATIONSHIP_PARTICIPANT_KEYNOTE_CREATE =
			"create table " + TABLE_RELATIONSHIP_PARTICIPANT_KEYNOTE+ "(" +
					RELATIONSHIP_PARTICIPANT_KEYNOTE_PARTICIPANT_USERNAME + " text not null, " +
					RELATIONSHIP_PARTICIPANT_KEYNOTE_KEYNOTE_ID + " text not null, " +
					"primary key (" + RELATIONSHIP_PARTICIPANT_KEYNOTE_PARTICIPANT_USERNAME + ", " + RELATIONSHIP_PARTICIPANT_KEYNOTE_KEYNOTE_ID + "), " +
					"foreign key (" + RELATIONSHIP_PARTICIPANT_KEYNOTE_PARTICIPANT_USERNAME + ") references " + TABLE_PARTICIPANTS + "(" + PARTICIPANT_USERNAME + "), " +
					"foreign key (" + RELATIONSHIP_PARTICIPANT_KEYNOTE_KEYNOTE_ID + ") references " + TABLE_KEYNOTES + "(" + COLUMN_ID + "));";
	
	private static final String RELATIONSHIP_PARTICIPANT_AUTHOR_CREATE =
			"create table " + TABLE_RELATIONSHIP_PARTICIPANT_AUTHOR+ "(" +
					RELATIONSHIP_PARTICIPANT_AUTHOR_PARTICIPANT_USERNAME + " text not null, " +
					RELATIONSHIP_PARTICIPANT_AUTHOR_AUTHOR_ID + " text not null, " +
					"primary key (" + RELATIONSHIP_PARTICIPANT_AUTHOR_PARTICIPANT_USERNAME + ", " + RELATIONSHIP_PARTICIPANT_AUTHOR_AUTHOR_ID + "), " +
					"foreign key (" + RELATIONSHIP_PARTICIPANT_AUTHOR_PARTICIPANT_USERNAME + ") references " + TABLE_PARTICIPANTS + "(" + PARTICIPANT_USERNAME + "), " +
					"foreign key (" + RELATIONSHIP_PARTICIPANT_AUTHOR_AUTHOR_ID + ") references " + TABLE_AUTHORS + "(" + COLUMN_ID + "));";
	
	public DbHelper(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/*
	 * Creates all tables
	*/
	public void onCreate(SQLiteDatabase database) {
		
		database.execSQL(CITY_CREATE);
		database.execSQL(SIGHTS_CREATE);
		database.execSQL(HOTELS_CREATE);
		database.execSQL(RESTAURANTS_CREATE);
		database.execSQL(EVENTS_CREATE);
		database.execSQL(KEYSESSIONS_CREATE);
		database.execSQL(WORKSHOPS_CREATE);
		database.execSQL(TALKS_CREATE);
		database.execSQL(KEYNOTES_CREATE);
		database.execSQL(PUBLICATIONS_CREATE);
		database.execSQL(ARTICLES_CREATE);
		database.execSQL(POSTERS_CREATE);
		database.execSQL(PARTICIPANTS_CREATE);
		database.execSQL(CONTACTS_CREATE);
		database.execSQL(CONFERENCES_CREATE);
		database.execSQL(AUTHORS_CREATE);
		database.execSQL(SOCIALEVENTS_CREATE);
		database.execSQL(SPONSORS_CREATE);
		database.execSQL(MEMBERS_CREATE);
		database.execSQL(NOTIFICATIONS_CREATE);
		database.execSQL(ARTICLEPRESENTED_CREATE);
		database.execSQL(NOT_PRESENTED_PUBLICATIONS_CREATE);
		database.execSQL(FAVPUBLICATIONS_CREATE);
		database.execSQL(POSTERSESSIONS_CREATE);
		database.execSQL(POSTERS_PRESENTED_CREATE);
		database.execSQL(SESSIONS_CREATE);
		database.execSQL(RELATIONSHIP_POSTER_AUTHOR_CREATE);
		database.execSQL(RELATIONSHIP_KEYNOTES_KEYSESSIONS_CREATE);
		database.execSQL(RELATIONSHIP_ARTICLE_AUTHOR_CREATE);
		database.execSQL(RELATIONSHIP_PARTICIPANT_EVENT_CREATE);
		database.execSQL(RELATIONSHIP_PARTICIPANT_SESSIONS_CREATE);
		database.execSQL(RELATIONSHIP_PARTICIPANT_CONTACT_CREATE);
		database.execSQL(RELATIONSHIP_PARTICIPANT_KEYNOTE_CREATE);
		database.execSQL(RELATIONSHIP_PARTICIPANT_AUTHOR_CREATE);
		
		
	}

	/*
	 * Drops all tables and create new ones
	 * TODO: Later this can't delete the tables! But update them instead
	 */
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		Log.w(DbHelper.class.getName(),
				"Upgrading database from version "+ oldVersion + " to " + newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_RESTAURANTS);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_HOTELS);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_SIGHTS);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_KEYNOTES);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_KEYSESSIONS);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_WORKSHOPS);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_ARTICLEPRESENTED);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_ARTICLES);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NOT_PRESENTED_PUBLICATIONS);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_PUBLICATIONS);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_TALKS);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_AUTHORS);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_EVENTS);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_POSTERS);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_PARTICIPANTS);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_CONFERENCES);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_SOCIALEVENTS);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_FAVPUBLICATIONS);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_POSTERSESSION);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_CONTACTS);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_CITIES);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_SPONSORS);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_MEMBERS_ORG);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NOTIFICATIONS);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_POSTERS_PRESENTED);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_SESSIONS);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_RELATIONSHIP_KEYNOTES_KEYSESSIONS);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_RELATIONSHIP_ARTICLE_AUTHOR);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_RELATIONSHIP_POSTER_AUTHOR);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_RELATIONSHIP_PARTICIPANT_EVENT);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_RELATIONSHIP_PARTICIPANT_SESSIONS);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_RELATIONSHIP_PARTICIPANT_CONTACT);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_RELATIONSHIP_PARTICIPANT_KEYNOTE);
		db.execSQL("DROP TABLE IF EXISTS "+ TABLE_RELATIONSHIP_PARTICIPANT_AUTHOR);
		onCreate(db);
	}
	
}
