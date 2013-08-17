package dataSources;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.example.magiconf_client.DbHelper;

import tables.Article;
import tables.Author;
import tables.NotPresentedPublication;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
import serializers_deserializers.NotPresentedPublicationDeserializer;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

/**
 * @author Alexandre
 * 
 * Data source to the NotPresentedPublication table
 */
@JsonDeserialize(using = NotPresentedPublicationDeserializer.class)
public class NotPresentedPublicationDataSource {
	
	private SQLiteDatabase database;
	private DbHelper dbHelper;

	public NotPresentedPublicationDataSource(Context context){
		dbHelper = new DbHelper(context);
	}
	
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}

	public void close(){
		dbHelper.close();
	}
	
	/**
	 * Inserts a new note presented publication
	 * @param title title of the publication
	 * @param abstract abstract of the publication
	 * @param authors authors of the publication
	 * @return inserted publication
	 */
	public NotPresentedPublication createNotPresentedPublication(String title, String pubAbstract, String authors){
		ContentValues values = new ContentValues();
		
		values.put(DbHelper.PUB_TITLE, title);
		values.put(DbHelper.PUB_ABSTRACT,pubAbstract);
		values.put(DbHelper.LAST_MODIFIED_DATE, new java.util.Date().getTime());

		long insertId = database.insert(DbHelper.TABLE_PUBLICATIONS, null, values);
		
		values = new ContentValues();
		values.put(DbHelper.NOT_PRESENTED_PUBLICATION_PUB_ID, insertId);
		values.put(DbHelper.NOT_PRESENTED_PUBLICATION_AUTHORS, authors);
		
		insertId = database.insert(DbHelper.TABLE_NOT_PRESENTED_PUBLICATIONS, null, values);
		/*
		//Cursor cursor = database.query(DbHelper.TABLE_NOT_PRESENTED_PUBLICATIONS, null, DbHelper.COLUMN_ID + " = " +insertId, null, null, null, null);
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(DbHelper.TABLE_NOT_PRESENTED_PUBLICATIONS + " INNER JOIN " + DbHelper.TABLE_PUBLICATIONS + " ON " + DbHelper.TABLE_NOT_PRESENTED_PUBLICATIONS + "." + DbHelper.NOT_PRESENTED_PUBLICATION_PUB_ID + " = " + DbHelper.TABLE_PUBLICATIONS + "." + DbHelper.COLUMN_ID);
		
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = queryBuilder.query(db, null, DbHelper.TABLE_NOT_PRESENTED_PUBLICATIONS + "." + DbHelper.COLUMN_ID + " = " +insertId, null, null, null, null);
		*/
		
		Cursor cursor = database.query(DbHelper.TABLE_NOT_PRESENTED_PUBLICATIONS, null,
				DbHelper.COLUMN_ID + " = " + insertId, null, null, null, null);

		cursor.moveToFirst();
		NotPresentedPublication newPub = cursorToNotPresentedPublication(cursor);
		cursor.close();
		return newPub;
	}
	
	public void update(Long id, String title, String pubAbstract, String authors, Date lastModifiedDate) {
		String whereClause = DbHelper.COLUMN_ID + " = " + id;
		Cursor cursor = database.query(DbHelper.TABLE_NOT_PRESENTED_PUBLICATIONS, null, whereClause, null, null, null, null);
		cursor.moveToFirst();
		Long publicationID = cursor.getLong(1);
		ContentValues values = new ContentValues();
		values.put(DbHelper.PUB_TITLE, title);
		values.put(DbHelper.PUB_ABSTRACT, pubAbstract);
		values.put(DbHelper.LAST_MODIFIED_DATE, lastModifiedDate.getTime());
		database.update(DbHelper.TABLE_PUBLICATIONS, values, DbHelper.COLUMN_ID + " = " + publicationID, null);
		
		values.clear();
		values.put(DbHelper.NOT_PRESENTED_PUBLICATION_AUTHORS, authors);
		database.update(DbHelper.TABLE_NOT_PRESENTED_PUBLICATIONS, values, DbHelper.COLUMN_ID + " = " + id, null);
	}
	
	public void deleteNotPresentedPublication(NotPresentedPublication pub){
		long id = pub.getId();
		long pubId = pub.getPublicationID();
		database.delete(DbHelper.TABLE_PUBLICATIONS, DbHelper.COLUMN_ID +" = "+ pubId, null);
		database.delete(DbHelper.TABLE_NOT_PRESENTED_PUBLICATIONS, DbHelper.COLUMN_ID +" = "+ id, null);
	}
	
	/**
	 * Returns all not presented publications in the local database
	 * @return all not presented publications in the local database
	 */
	public List<NotPresentedPublication> getAllNotPresentedPublications(){
		List<NotPresentedPublication> notPresentedPublications = new LinkedList<NotPresentedPublication>();
		Cursor cursor = database.query(DbHelper.TABLE_NOT_PRESENTED_PUBLICATIONS, null, null,
				null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			NotPresentedPublication notPresentedPublication = cursorToNotPresentedPublication(cursor);
			notPresentedPublications.add(notPresentedPublication);
			cursor.moveToNext();
		}

		cursor.close();
		return notPresentedPublications;
	
	}
	
	/**
	 * Returns the not presented publication with the specified id
	 * @param id id of the not presented publication
	 * @return not presented publication with the specified id
	 */
	public NotPresentedPublication getNotPresentedPublication(Long id) {
		
		String whereClause = DbHelper.COLUMN_ID + " = " + id;
		Cursor cursor = database.query(DbHelper.TABLE_NOT_PRESENTED_PUBLICATIONS, null, whereClause, null, null, null, null);
		boolean hasData = cursor.moveToFirst();
		if (!hasData){
			cursor.close();
			return null;
		}
		
		NotPresentedPublication p = cursorToNotPresentedPublication(cursor);
		cursor.close();
		return p;
	}
	
	/**
	 * Returns the not presented publication which the current cursor is pointing to
	 * @param cursor current cursor
	 * @return not presented publication which the current cursor is pointing to
	 */
	private NotPresentedPublication cursorToNotPresentedPublication(Cursor cursor){
			long pubID = cursor.getLong(1);
				
			// Get the data from the Event table
			Cursor c = database.query(DbHelper.TABLE_PUBLICATIONS, null, DbHelper.COLUMN_ID + " = " + pubID, null, null, null, null);
			c.moveToFirst();
			
			NotPresentedPublication pub = new NotPresentedPublication();
			
			pub.setId(cursor.getLong(0));
			pub.setPublicationID(pubID);
			pub.setTitle(c.getString(1));
			pub.setAbstract(c.getString(2));
			pub.setAuthors(cursor.getString(2));
			
			Timestamp timestamp = new Timestamp(c.getLong(3));
			pub.setLastModified(new Date(timestamp.getTime()));
			
			c.close();
			return pub;
		
	}
	
	/**
	 * Checks if exists a not presented publication that has the specified pubID as its parent
	 * @param pubID parent's publication id
	 * @return true if exists a not presented publication that has the specified id as its parent; false otherwise
	 */
	public boolean hasNotPresentedPub(long pubID) {
		
		String whereClause = DbHelper.NOT_PRESENTED_PUBLICATION_PUB_ID + " = " + pubID;
		Cursor cursor = database.query(DbHelper.TABLE_NOT_PRESENTED_PUBLICATIONS, null,
				whereClause, null, null, null, null);
		return cursor.moveToFirst();
	}

	public NotPresentedPublication getNotPresentedPublicationByParent(Long id) {
		String whereClause = DbHelper.NOT_PRESENTED_PUBLICATION_PUB_ID + " = " + id;
		Cursor cursor = database.query(DbHelper.TABLE_NOT_PRESENTED_PUBLICATIONS, null, whereClause, null, null, null, null);
		boolean hasData = cursor.moveToFirst();
		
		if (!hasData){
			cursor.close();
			return null;
		}
		NotPresentedPublication p = cursorToNotPresentedPublication(cursor);
		cursor.close();
		Log.i("NOME DE PUB",""+p.getTitle());
		return p;
		
	}

	public NotPresentedPublication getNotPresentedPublicationByTitle(
			String title) {
		String whereClause = DbHelper.PUB_TITLE + " = '" + title+"'";
		Cursor cursor = database.query(DbHelper.TABLE_PUBLICATIONS, null, whereClause, null, null, null, null);
		boolean hasData = cursor.moveToFirst();
		
		if (!hasData){
			cursor.close();
			return null;
		}
		
		//whereClause = DbHelper.NOT_PRESENTED_PUBLICATION_PUB_ID +"="+cursor.getLong(0);
		NotPresentedPublication p = getNotPresentedPublicationByParent(cursor.getLong(0));
		cursor.close();
		return p;
	}
	
	/**
	 * Returns true if already exists a publication with that name
	 */
	public boolean hasPublication(String name){
		String whereClause = DbHelper.PUB_TITLE+"='"+name+"'";
		Cursor c = database.query(DbHelper.TABLE_PUBLICATIONS, null, whereClause, null, null, null, null);
		boolean result = c.moveToFirst();
		c.close();
		return result;
	}
}
