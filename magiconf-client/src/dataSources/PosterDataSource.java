package dataSources;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import tables.Author;
import tables.Poster;


import com.example.magiconf_client.DbHelper;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * @author Farah
 * 
 * Data source to the Poster table
 */
public class PosterDataSource {
	
	private SQLiteDatabase database;
	private DbHelper dbHelper;
	
	public PosterDataSource(Context context){
		dbHelper = new DbHelper(context);
	}
	
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}

	public void close(){
		dbHelper.close();
	}
	
	/**
	 * Returns the author with the specified id
	 * @param id id of the author to get
	 * @return author with specified id
	 */
	public Poster getPoster(Long id) {
		Cursor cursor = database.query(DbHelper.TABLE_POSTERS, null, DbHelper.COLUMN_ID + " = " + id, null, null, null, null);
		cursor.moveToFirst();
		Poster poster = cursorToPoster(cursor);
		cursor.close();
		return poster;
	}
	
	
	/**
	 * Inserts a new author
	 * @param email email of the author
	 * @param name name of the author
	 * @param workplace work place of the author
	 * @param country country of the author
	 * @param photo photo url of the author
	 * @return inserted author
	 */
	public Poster createPoster(String title,List<Author> authors){
		ContentValues values = new ContentValues();
		
		values.put(DbHelper.POSTER_TITLE, title);
		values.put(DbHelper.LAST_MODIFIED_DATE, new java.util.Date().getTime());
		

		long insertId = database.insert(DbHelper.TABLE_POSTERS, null, values);
		
		
		
		//long articleId;
				Iterator<Author> it = authors.iterator();
				Author next;
				while (it.hasNext()) {
					next = it.next();
					values = new ContentValues();
					values.put(DbHelper.AUTHOR_NAME, next.getName());
					values.put(DbHelper.AUTHOR_EMAIL, next.getEmail());
					values.put(DbHelper.AUTHOR_WORKPLACE, next.getWork_place());
					values.put(DbHelper.AUTHOR_PHOTO, next.getPhoto());
					values.put(DbHelper.AUTHOR_COUNTRY, next.getCountry());
					
					// Insert into Publications table because of inheritance
					long authorId = database.insert(DbHelper.TABLE_AUTHORS, null, values);
				
					// Insert the article speaker into the Articles table
					values = new ContentValues();
					values.put(DbHelper.RELATIONSHIP_POSTER_AUTHOR_POSTER_ID, insertId);
					values.put(DbHelper.RELATIONSHIP_POSTER_AUTHOR_AUTHOR_ID, authorId);
					// Insert the relationship between the talk and the article into articles_presented table
					database.insert(DbHelper.TABLE_RELATIONSHIP_POSTER_AUTHOR, null, values);
				}
				
				
			Cursor cursor = database.query(DbHelper.TABLE_POSTERS, null, DbHelper.COLUMN_ID + " = " +insertId, null, null, null, null);

			cursor.moveToFirst();
			Poster newPoster = cursorToPoster(cursor);
			cursor.close();
		return newPoster;
	}
	
	/**
	 * Deletes the specified author from the local database
	 * @param author to delete
	 */
	public void deletePoster(Poster poster){
		long id = poster.getId();
		database.delete(DbHelper.TABLE_POSTERS, DbHelper.COLUMN_ID +" = "+ id, null);
	}
	
	/**
	 * Returns all the authors present in the local database
	 * @return all author
	 */
	public List<Poster> getAllPosters(){
		List<Poster> posters = new ArrayList<Poster>();
		Cursor cursor = database.query(DbHelper.TABLE_POSTERS, null, null, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			Poster post = cursorToPoster(cursor);
			posters.add(post);
			cursor.moveToNext();
		}
		
		cursor.close();
		return posters;
	
	}
	
	/**
	 * Returns the poster which the current cursor is pointing to
	 * @param cursor current cursor
	 * @return poster which the current cursor is pointing to
	 */
	private Poster cursorToPoster(Cursor cursor) {
		long posterID = cursor.getLong(0);
		
		Log.i("Poster", "CURSOR TO POSTER");
		
		// Get the data from the Event table
		Cursor c = database.query(DbHelper.TABLE_POSTERS, null, DbHelper.COLUMN_ID + " = " + posterID, null, null, null, null);
		c.moveToFirst();
		
		Poster poster = new Poster();
		
		poster.setId(posterID);
		poster.setTitle(c.getString(1));
		
		Timestamp timestamp = new Timestamp(c.getLong(2));
		poster.setLastModified(new Date(timestamp.getTime()));
		
		
		// Get the authors
		c = database.query(DbHelper.TABLE_RELATIONSHIP_POSTER_AUTHOR, null, DbHelper.RELATIONSHIP_POSTER_AUTHOR_POSTER_ID + " = " + posterID, null, null, null, null);
		c.moveToFirst();
		int size = c.getCount();
		Log.i("Size", ""+size);
		ArrayList<Author> authors = new ArrayList<Author>();
		while (!c.isAfterLast()) {
			authors.add(cursorToAuthor(c));
			c.moveToNext();
		}
		poster.setAuthors(authors);
		c.close();
		return poster;
	}
	
	/**
	 * Returns the author which the current cursor is pointing to
	 * @param cursor current cursor
	 * @return author which the current cursor is pointing to
	 */
	private Author cursorToAuthor(Cursor cursor){
		
		Log.i("Poster", "CURSOR TO POSTER");
		Cursor c = database.query(DbHelper.TABLE_AUTHORS, null, DbHelper.COLUMN_ID + " = " + cursor.getLong(1), null, null, null, null);
		c.moveToFirst();
		Author author = new Author();
		author.setId(c.getLong(0));
		author.setEmail(c.getString(1));
		author.setName(c.getString(2));
		author.setWork_place(c.getString(3));
		author.setCountry(c.getString(4));
		author.setPhoto(c.getString(5));
		
		return author;
	}
}
