package dataSources;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.example.magiconf_client.DbHelper;

import tables.Event;
import tables.NotPresentedPublication;
import tables.Publication;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * @author José
 * 
 * Data source to the Publication table
 */
public class PublicationDataSource {
	
	private SQLiteDatabase database;
	private DbHelper dbHelper;

	public PublicationDataSource(Context context){
		dbHelper = new DbHelper(context);
	}
	
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
		Log.i("DbHelper", ""+dbHelper);
		Log.i("Start", ""+database);
	}

	public void close(){
		dbHelper.close();
	}
	
	/**
	 * Inserts a new publication
	 * @param title title of the publication
	 * @param abstract abstract of the publication
	 * @return inserted publication
	 */
	public Publication createPublication(String title, String pubAbstract){
		ContentValues values = new ContentValues();
		
		values.put(DbHelper.PUB_TITLE, title);
		values.put(DbHelper.PUB_ABSTRACT,pubAbstract);
		values.put(DbHelper.LAST_MODIFIED_DATE, new java.util.Date().getTime());

		long insertId = database.insert(DbHelper.TABLE_PUBLICATIONS, null, values);
		
		Cursor cursor = database.query(DbHelper.TABLE_PUBLICATIONS, null, DbHelper.COLUMN_ID + " = " +insertId, null, null, null, null);
		
		cursor.moveToFirst();
		Publication newPub = cursorToPublication(cursor);
		cursor.close();
		return newPub;
	}
	
	public void update(Long id, String title, String pubAbstract, Date lastModifiedDate) {
		String whereClause = DbHelper.COLUMN_ID + " = " + id;
		Log.i("Database publication", ""+database);
		Cursor cursor = database.query(DbHelper.TABLE_PUBLICATIONS, null, whereClause, null, null, null, null);
		cursor.moveToFirst();
		Long pubID = cursor.getLong(0);
		ContentValues values = new ContentValues();
		values.put(DbHelper.PUB_TITLE, title);
		values.put(DbHelper.PUB_ABSTRACT, pubAbstract);
		values.put(DbHelper.LAST_MODIFIED_DATE, lastModifiedDate.getTime());
		database.update(DbHelper.TABLE_PUBLICATIONS, values, DbHelper.COLUMN_ID + " = " + pubID, null);
	}
	
	public void deletePublication(Publication pub){
		long id = pub.getId();
		database.delete(DbHelper.TABLE_PUBLICATIONS, DbHelper.COLUMN_ID +" = "+ id, null);
	}
	
	/**
	 * Returns all publications in the local database
	 * @return all publications in the local database
	 */
	public List<Publication> getAllPublications(){
		List<Publication> pubs = new ArrayList<Publication>();
		Cursor cursor = database.query(DbHelper.TABLE_PUBLICATIONS, null, null, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			Publication pub = cursorToPublication(cursor);
			pubs.add(pub);
			cursor.moveToNext();
		}
		
		cursor.close();
		return pubs;
	
	}
	
	/**
	 * Returns the publication with the specified id
	 * @param id id of the publication
	 * @return publication with the specified id
	 */
	public Publication getPublication(Long id) {
		String whereClause = "_id=" + id;
		Log.i("Database", ""+database);
		Cursor cursor = database.query(DbHelper.TABLE_PUBLICATIONS, null, whereClause, null, null, null, null);
		boolean hasData = cursor.moveToFirst();
		Log.i("Cursor", ""+hasData);
		if (!hasData)
			return null;
		return cursorToPublication(cursor);
	}
	
	/**
	 * Returns the publication which the current cursor is pointing to
	 * @param cursor current cursor
	 * @return publication which the current cursor is pointing to
	 */
	private Publication cursorToPublication(Cursor cursor){
		Publication pub = new Publication();
		pub.setId(cursor.getLong(0));
		pub.setTitle(cursor.getString(1));
		pub.setAbstract(cursor.getString(2));
		Timestamp timestamp = new Timestamp(cursor.getLong(3));
		pub.setLastModified(new Date(timestamp.getTime()));
		
		return pub;
	}
	
	/**
	 * Returns all the publications that match the search query
	 * @param query to search about
	 * @return list of publications that are results of the search
	 */
	public List<Publication> search(String query){
		List<Publication> results = new ArrayList<Publication>();
		
		String lowerQuery = query.toLowerCase();
		String convertedQuery = "%"+lowerQuery.toLowerCase()+"%";
		String whereClause = "lower("+DbHelper.PUB_TITLE+") like '"+convertedQuery+"' OR lower("+DbHelper.PUB_ABSTRACT+") like '"+convertedQuery+"'"; 
		
		Cursor cursor = database.query(DbHelper.TABLE_PUBLICATIONS, null, whereClause, null, null, null, DbHelper.PUB_TITLE);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			Publication pub = cursorToPublication(cursor);
			results.add(pub);
			cursor.moveToNext();
		}
		
		cursor.close();
		return results;
	}
	
	public boolean hasPublication(String name){
		String whereClause = DbHelper.PUB_TITLE+"='"+name+"'";
		Cursor c = database.query(DbHelper.TABLE_PUBLICATIONS, null, whereClause, null, null, null, null);
		boolean result = c.moveToFirst();
		c.close();
		return result;
	}
	
	/**
	 * Inserts a new publication
	 * @param title title of the publication
	 * @param abstract abstract of the publication
	 * @return inserted publication
	 */
	public NotPresentedPublication createNewNotPresentedPublication(String title, String pubAbstract, String authors){
		ContentValues values = new ContentValues();
		
		values.put(DbHelper.PUB_TITLE, title);
		values.put(DbHelper.PUB_ABSTRACT,pubAbstract);
		values.put(DbHelper.LAST_MODIFIED_DATE, new java.util.Date().getTime());

		long insertId = database.insert(DbHelper.TABLE_PUBLICATIONS, null, values);
		
		values = new ContentValues();
		values.put(DbHelper.NOT_PRESENTED_PUBLICATION_PUB_ID, insertId);
		values.put(DbHelper.NOT_PRESENTED_PUBLICATION_AUTHORS, authors);
		
		insertId = database.insert(DbHelper.TABLE_NOT_PRESENTED_PUBLICATIONS, null, values);
		
		Cursor cursor = database.query(DbHelper.TABLE_NOT_PRESENTED_PUBLICATIONS, null, DbHelper.COLUMN_ID + " = " +insertId, null, null, null, null);
		
		cursor.moveToFirst();
		NotPresentedPublication newPub = cursorToNotPresentedPublication(cursor);
		cursor.close();
		return newPub;
	}
	/**
	 * Returns the not presented publication which the current cursor is pointing to
	 * @param cursor current cursor
	 * @return not presented publication which the current cursor is pointing to
	 */
	private NotPresentedPublication cursorToNotPresentedPublication(Cursor cursor){
		long pubID = cursor.getLong(1);
		
		// Get the data from the Publications table
		Cursor c = database.query(DbHelper.TABLE_PUBLICATIONS, null, DbHelper.COLUMN_ID + " = " + pubID, null, null, null, null);
		c.moveToFirst();
		
		NotPresentedPublication p = new NotPresentedPublication();
		
		p.setId(cursor.getLong(0));
		p.setPublicationID(pubID);
		p.setTitle(c.getString(1));
		p.setAbstract(c.getString(2));
		p.setAuthors(cursor.getString(2));
		
		Timestamp timestamp = new Timestamp(c.getLong(3));
		p.setLastModified(new Date(timestamp.getTime()));
		
		c.close();
		return p;
	}
}
