package dataSources;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.example.magiconf_client.DbHelper;

import tables.Article;
import tables.Author;
import tables.PosterSession;

import tables.Talk;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * @author José
 * 
 * Data source to the Talk table
 */

public class TalkDataSource {
	
	private SQLiteDatabase database;
	private DbHelper dbHelper;

	public TalkDataSource(Context context){
		dbHelper = new DbHelper(context);
	}
	
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}

	public void close(){
		dbHelper.close();
	}
	
	
	/**
	 * Inserts a new Talk
	 * @param title title of the session
	 * @param time time of the session
	 * @param duration duration of the session
	 * @param place place of the session
	 * @param articles articles to be presented in this session
	 * @return inserted talk session
	 */
	public Talk createTalk(Long event_id,String title, Date time, int duration,
			String place, List<Article> articles){
		ContentValues values = new ContentValues();
		
		values.put(DbHelper.DJANGO_ID,event_id);
		values.put(DbHelper.EVENT_TITLE, title);
		values.put(DbHelper.EVENT_PLACE, place);
		values.put(DbHelper.EVENT_TIME, time.getTime());
		values.put(DbHelper.EVENT_DURATION, duration);
		values.put(DbHelper.LAST_MODIFIED_DATE, new java.util.Date().getTime());
		
		// Insert into Events table because of inheritance
		long insertId = database.insert(DbHelper.TABLE_EVENTS, null, values);
		
		values = new ContentValues();
		values.put("event_id", insertId);
		values.put(DbHelper.DJANGO_ID,event_id);
		
		// Insert into Talks table
		insertId = database.insert(DbHelper.TABLE_TALKS, null, values);
		
		
		Iterator<Article> it = articles.iterator();
		Article next;
		while (it.hasNext()) {
			next = it.next();
			values = new ContentValues();
			values.put(DbHelper.PUB_TITLE, next.getTitle());
			Log.i("Inserting", next.getTitle());
			values.put(DbHelper.PUB_ABSTRACT, next.getAbstract());
			values.put(DbHelper.LAST_MODIFIED_DATE, new java.util.Date().getTime());
			
			// Insert into Publications table because of inheritance
			long pubId = database.insert(DbHelper.TABLE_PUBLICATIONS, null, values);
			
			values = new ContentValues();
			values.put(DbHelper.ARTICLE_ID, pubId);
			
			// Insert the article speaker into the Articles table
			long articleId = database.insert(DbHelper.TABLE_ARTICLES, null, values);
			values = new ContentValues();
			values.put(DbHelper.ARTICLES_PRESENTED_ARTICLE_ID, pubId);
			values.put(DbHelper.ARTICLES_PRESENTED_TALK_ID, insertId);
			// Insert the relationship between the talk and the article into articles_presented table
			database.insert(DbHelper.TABLE_ARTICLEPRESENTED, null, values);
			
			long authorId;
			Iterator<Author> itAut = next.getAuthors().iterator();
			Author nextAut; 
			while (itAut.hasNext()) {
				nextAut = itAut.next();
				
				Cursor cAuthors = database.query(DbHelper.TABLE_AUTHORS, null,
						DbHelper.AUTHOR_EMAIL + " = '" + nextAut.getEmail()+"'", null, null, null, null);
				if(cAuthors.getCount()==0){
					Log.i("Autor em TalkSession","Não existia");
				values = new ContentValues();
				values.put(DbHelper.AUTHOR_NAME, nextAut.getName());
				Log.i("Inserting", nextAut.getName());
				values.put(DbHelper.AUTHOR_EMAIL, nextAut.getEmail());
				values.put(DbHelper.AUTHOR_COUNTRY, nextAut.getCountry());
				values.put(DbHelper.AUTHOR_PHOTO, nextAut.getPhoto());
				values.put(DbHelper.AUTHOR_WORKPLACE, nextAut.getWork_place());
				
				// Insert the author into the Authors table
				authorId = database.insert(DbHelper.TABLE_AUTHORS, null, values);
				
				}else{
					Log.i("Autor em TalkSession","Existia");
					cAuthors.moveToFirst();
					authorId= cAuthors.getLong(0);
				}
				
				values = new ContentValues();
				values.put(DbHelper.RELATIONSHIP_ARTICLE_AUTHOR_ARTICLE_ID, articleId);
				values.put(DbHelper.RELATIONSHIP_ARTICLE_AUTHOR_AUTHOR_ID, authorId);
				
				// Insert the relationship between the author and the article into author_article table
				database.insert(DbHelper.TABLE_RELATIONSHIP_ARTICLE_AUTHOR, null, values);
			}

			
		}
		
		
		Cursor cursor = database.query(DbHelper.TABLE_TALKS, null,
				DbHelper.COLUMN_ID + " = " + insertId, null, null, null, null);

		cursor.moveToFirst();
		Talk newTalk = cursorToTalk(cursor);
		cursor.close();
		return newTalk;
	}
	
	/**
	 * Deletes the specified talk session from the local database
	 * @param talk talk session to delete
	 */	
	public void deleteTalk(Talk talk){
		long eventId = talk.getEventId();
		long id = talk.getId();
		
		database.delete(DbHelper.TABLE_TALKS, DbHelper.COLUMN_ID +" = "+ id, null);
		database.delete(DbHelper.TABLE_EVENTS, DbHelper.COLUMN_ID +" = "+ eventId, null);
		
	}
	
	/**
	 * Returns all the talk sessions present in the local database
	 * @return all the talk sessions present in the local database
	 */
	public List<Talk> getAllTalks() {
		List<Talk> talks = new LinkedList<Talk>();
		Cursor cursor = database.query(DbHelper.TABLE_TALKS, null, null,
				null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Talk talk = cursorToTalk(cursor);
			talks.add(talk);
			cursor.moveToNext();
		}

		cursor.close();
		return talks;
	}
	
	/**
	 * Returns the talk which the current cursor is pointing to
	 * @param cursor current cursor
	 * @return talk session which the current cursor is pointing to
	 */
	private Talk cursorToTalk(Cursor cursor) {
		long eventID = cursor.getLong(1);
		
		// Get the data from the Event table
		Cursor c = database.query(DbHelper.TABLE_EVENTS, null, DbHelper.COLUMN_ID + " = " + eventID, null, null, null, null);
		c.moveToFirst();
		
		Talk talk = new Talk();
		
		talk.setId(cursor.getLong(0));
		talk.setEventId(eventID);
		talk.setTitle(c.getString(1));
		talk.setDuration(c.getInt(4));
		talk.setPlace(c.getString(3));
		
		Timestamp timestamp = new Timestamp(c.getLong(2));
		talk.setTime(new Date(timestamp.getTime()));
		timestamp = new Timestamp(c.getLong(5));
		talk.setLastModified(new Date(timestamp.getTime()));
		talk.setDjangoId(c.getLong(6));
		
		// Get the articles
		c = database.query(DbHelper.TABLE_ARTICLEPRESENTED, null, DbHelper.ARTICLES_PRESENTED_TALK_ID + " = " + talk.getId(), null, null, null, null);
		c.moveToFirst();
		int size = c.getCount();
		Log.i("Size", ""+size);
		ArrayList<Article> articles = new ArrayList<Article>();
		while (!c.isAfterLast()) {
			articles.add(cursorToArticle(c));
			c.moveToNext();
		}
		talk.setArticles(articles);
		c.close();
		return talk;
	}
	
	/**
	 * Returns the article which the current cursor is pointing to
	 * @param cursor current cursor
	 * @return article which the current cursor is pointing to
	 */
	//TODO: The order and the cursor may be incorrect 
	private Article cursorToArticle(Cursor cursor) {
		
		long artID = cursor.getLong(1);
		
		Cursor c = database.query(DbHelper.TABLE_ARTICLES, null, DbHelper.COLUMN_ID + " = " + artID, null, null, null, null);
		c.moveToFirst();
		Article art = new Article();
		art.setId(c.getLong(0));
		art.setArticleId(artID);
		
		Cursor cp = database.query(DbHelper.TABLE_PUBLICATIONS, null, DbHelper.COLUMN_ID + " = " + c.getLong(0), null, null, null, null);
		cp.moveToFirst();
		art.setTitle(cp.getString(1));
		art.setAbstract(cp.getString(2));

		return art;
	}
	
	/**
	 * Updates a talk session
	 * @param articles 
	 */
	public void update(long id,long djangoID, String title, Date time, int duration, String place,
			java.util.Date date, List<Article> articles) {
		String whereClause = DbHelper.COLUMN_ID + " = " + id;
		Log.i("Database talk", "" + database);
		Cursor cursor = database.query(DbHelper.TABLE_TALKS, null,
				whereClause, null, null, null, null);
		cursor.moveToFirst();
		Long eventId = cursor.getLong(1);
		ContentValues values = new ContentValues();
		values.put(DbHelper.EVENT_TITLE, title);
		values.put(DbHelper.EVENT_TIME, time.getTime());
		values.put(DbHelper.EVENT_DURATION, duration);
		values.put(DbHelper.EVENT_PLACE, place);
		values.put(DbHelper.LAST_MODIFIED_DATE, date.getTime());
		database.update(DbHelper.TABLE_EVENTS, values, DbHelper.COLUMN_ID
				+ " = " + eventId, null);
		
		
		
		Iterator<Article> it = articles.iterator();
		Article next;
		while (it.hasNext()) {
			next = it.next();
			values = new ContentValues();
			values.put(DbHelper.PUB_TITLE, next.getTitle());
			Log.i("Updating", next.getTitle());
			values.put(DbHelper.PUB_ABSTRACT, next.getAbstract());
			values.put(DbHelper.LAST_MODIFIED_DATE, new java.util.Date().getTime());
			
			String articleWhereClause = DbHelper.ARTICLE_TITLE + " = " + "\""+next.getTitle()+"\"";
			
			// Update Publications table because of inheritance
			int pubsAffected = database.update(DbHelper.TABLE_PUBLICATIONS,values, articleWhereClause, null);
			
			if(pubsAffected > 0){
				//updated then
			}
			else{
				//create new
				
				// Insert into Publications table because of inheritance
				long pubId = database.insert(DbHelper.TABLE_PUBLICATIONS, null, values);
				
				values = new ContentValues();
				values.put(DbHelper.ARTICLE_ID, pubId);
				
				// Update the article speaker into the Articles table
				
				long articleId = database.insert(DbHelper.TABLE_ARTICLES, null, values);
				values = new ContentValues();
				values.put(DbHelper.ARTICLES_PRESENTED_ARTICLE_ID, pubId);
				values.put(DbHelper.ARTICLES_PRESENTED_TALK_ID, id);
				// Insert the relationship between the talk and the article into articles_presented table
				database.insert(DbHelper.TABLE_ARTICLEPRESENTED, null, values);
				
				long authorId;
				Iterator<Author> itAut = next.getAuthors().iterator();
				Author nextAut; 
				while (itAut.hasNext()) {
					nextAut = itAut.next();
					
					Cursor cAuthors = database.query(DbHelper.TABLE_AUTHORS, null,
							DbHelper.AUTHOR_EMAIL + " = '" + nextAut.getEmail()+"'", null, null, null, null);
					if(cAuthors.getCount()==0){
						Log.i("Autor em TalkSession","Não existia");
					values = new ContentValues();
					values.put(DbHelper.AUTHOR_NAME, nextAut.getName());
					Log.i("Inserting", nextAut.getName());
					values.put(DbHelper.AUTHOR_EMAIL, nextAut.getEmail());
					values.put(DbHelper.AUTHOR_COUNTRY, nextAut.getCountry());
					values.put(DbHelper.AUTHOR_PHOTO, nextAut.getPhoto());
					values.put(DbHelper.AUTHOR_WORKPLACE, nextAut.getWork_place());
					
					// Insert the author into the Authors table
					authorId = database.insert(DbHelper.TABLE_AUTHORS, null, values);
					
					}else{
						Log.i("Autor em TalkSession","Existia");
						cAuthors.moveToFirst();
						authorId= cAuthors.getLong(0);
					}
					
					values = new ContentValues();
					values.put(DbHelper.RELATIONSHIP_ARTICLE_AUTHOR_ARTICLE_ID, articleId);
					values.put(DbHelper.RELATIONSHIP_ARTICLE_AUTHOR_AUTHOR_ID, authorId);
					
					// Insert the relationship between the author and the article into author_article table
					database.insert(DbHelper.TABLE_RELATIONSHIP_ARTICLE_AUTHOR, null, values);
				}
			}
		}
			
			
			
		
		
	}
	
	/**
	 * Returns the talk with the specified id
	 * @param id id of the talk session to get
	 * @return talk session with specified id
	 */
	public Talk getTalk(Long id) {
		String whereClause = DbHelper.COLUMN_ID + " = " + id;
		Cursor cursor = database.query(DbHelper.TABLE_TALKS, null, whereClause, null, null, null, null);
		boolean hasData = cursor.moveToFirst();
		if (!hasData)
			return null;
		return cursorToTalk(cursor);
	}
	
	/**
	 * Returns the talk that has the specified event id as its parent
	 * @param eventID parent's event id
	 * @return talk that has the specified event id as its parent
	 */
	public Talk getTalkEvent(Long eventID) {
		String[] columns = { DbHelper.EVENT_ID, DbHelper.COLUMN_ID, DbHelper.DJANGO_ID};
		String whereClause = DbHelper.DJANGO_ID + " = " + eventID;
		Cursor cursor = database.query(DbHelper.TABLE_TALKS, columns,
				whereClause, null, null, null, null);
		boolean hasData = cursor.moveToFirst();
		if (!hasData)
			return null;
		return getTalk(cursor.getLong(1));
	}
	
	/**
	 * Checks if exists a talk that has the specified event id as its parent
	 * @param eventID parent's event id
	 * @return true if exists a talk that has the specified event id as its parent; false otherwise
	 */
	public boolean hasTalkEvent(Long eventID) {
		String[] columns = { DbHelper.EVENT_ID, DbHelper.COLUMN_ID };
		String whereClause = DbHelper.EVENT_ID + " = " + eventID;
		Cursor cursor = database.query(DbHelper.TABLE_TALKS, columns,
				whereClause, null, null, null, null);
		return cursor.moveToFirst();
	}
	
	
	/**
	 * Returns the article with the specified id
	 * @param id id of the article to get
	 * @return article with specified id
	 */
	public Article getArticle(String title) {
		String whereClause = DbHelper.ARTICLE_TITLE + " = \"" + title +"\"";
		Cursor cursor = database.query(DbHelper.TABLE_ARTICLES, null, whereClause, null, null, null, null);
		boolean hasData = cursor.moveToFirst();
		if (!hasData){
			cursor.close();
			return null;
		}
		Article a = cursorToArticle(cursor);
		cursor.close();
		return a;
	}
	
	
	
}
