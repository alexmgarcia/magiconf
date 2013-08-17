package dataSources;

import java.util.ArrayList;
import java.util.List;

import com.example.magiconf_client.DbHelper;

import tables.Author;
import tables.Keynote;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


/**
 * @author Alexandre
 * 
 * Data source to the Keynote table
 */
public class KeynoteDataSource {
	
	private SQLiteDatabase database;
	private DbHelper dbHelper;
	
	public KeynoteDataSource(Context context){
		dbHelper = new DbHelper(context);
	}
	
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}

	public void close(){
		dbHelper.close();
	}
	
	/**
	 * Returns the keynote speaker with the specified id
	 * @param id id of the keynote speaker to get
	 * @return keynote speaker with specified id
	 */
	public Keynote getKeynote(Long id) {
		Cursor cursor = database.query(DbHelper.TABLE_KEYNOTES, null, DbHelper.COLUMN_ID + " = " + id, null, null, null, null);
		cursor.moveToFirst();
		Keynote keynote = cursorToKeynote(cursor);
		cursor.close();
		return keynote;
	}
	
	
	/**
	 * Inserts a new keynote speaker
	 * @param email email of the keynotespeaker
	 * @param name name of the keynote speaker
	 * @param workplace work place of the keynote speaker
	 * @param country country of the keynote speaker
	 * @param photo photo url of the keynote speaker
	 * @param string 
	 * @return inserted keynote speaker
	 */
	public Keynote createKeynote(String email, String name, String workplace, String country, String photo, String contact, boolean ignoreAlreadyOnBD){
		boolean insert = true;
		Keynote newKeynote;
		Cursor c = null;
		if(!ignoreAlreadyOnBD){
			c = database.rawQuery("SELECT * FROM " + DbHelper.TABLE_KEYNOTES+ " WHERE " + DbHelper.KEYNOTE_EMAIL + "= '" + email + "'", null);
			insert = c.getCount() == 0;
		}
		if(insert){
			ContentValues values = new ContentValues();
			
			values.put(DbHelper.KEYNOTE_EMAIL, email);
			values.put(DbHelper.KEYNOTE_NAME,name);
			values.put(DbHelper.KEYNOTE_WORKPLACE,workplace);
			values.put(DbHelper.KEYNOTE_COUNTRY,country);
			values.put(DbHelper.KEYNOTE_PHOTO,photo);
			//TODO: Contact

			long insertId = database.insert(DbHelper.TABLE_KEYNOTES, null, values);
			
			Cursor cursor = database.query(DbHelper.TABLE_KEYNOTES, null, DbHelper.COLUMN_ID + " = " +insertId, null, null, null, null);
			
			cursor.moveToFirst();
			newKeynote = cursorToKeynote(cursor);
			cursor.close();
			
		}
		else{
			c.moveToFirst();
			newKeynote = cursorToKeynote(c);
			Log.i("KeynotDataSource", "skipped keynote: "+newKeynote.getEmail());
			c.close();
		}
		
		return newKeynote;	
	}
	
	/**
	 * Deletes the specified keynote speaker from the local database
	 * @param keynote speaker to delete
	 */
	public void deleteKeynote(Keynote keynote){
		long id = keynote.getId();
		database.delete(DbHelper.TABLE_KEYNOTES, DbHelper.COLUMN_ID +" = "+ id, null);
	}
	
	/**
	 * Returns all the keynote speakers present in the local database
	 * @return all keynote speakers
	 */
	public List<Keynote> getAllKeynotes(boolean sort){
		List<Keynote> keynotes = new ArrayList<Keynote>();
		Cursor cursor;
		if(sort)
			cursor = database.query(DbHelper.TABLE_KEYNOTES, null, null, null, null, null, DbHelper.KEYNOTE_NAME);
		
		else
			cursor = database.query(DbHelper.TABLE_KEYNOTES, null, null, null, null, null, null);
		
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			Keynote keynote = cursorToKeynote(cursor);
			keynotes.add(keynote);
			cursor.moveToNext();
		}
		
		cursor.close();
		return keynotes;
	
	}
	
	/**
	 * Returns the keynote speaker which the current cursor is pointing to
	 * @param cursor current cursor
	 * @return keynote speaker which the current cursor is pointing to
	 */
	private Keynote cursorToKeynote(Cursor cursor){
		Keynote keynote = new Keynote();
		keynote.setId(cursor.getLong(0));
		keynote.setEmail(cursor.getString(1));
		keynote.setName(cursor.getString(2));
		keynote.setWork_place(cursor.getString(3));
		keynote.setCountry(cursor.getString(4));
		keynote.setPhoto(cursor.getString(5));
		
		return keynote;
	}
	
	public List<Keynote> search(String query){
		
		String lowerQuery = query.toLowerCase();
		String convertedName = "%"+lowerQuery.toLowerCase()+"%";
		String whereClause = "lower("+DbHelper.KEYNOTE_EMAIL+ ")='"+lowerQuery+"' OR lower("+DbHelper.KEYNOTE_NAME+") like '"+convertedName+"'"; 
		
		List<Keynote> results = new ArrayList<Keynote>();
		
		Cursor cursor = database.query(DbHelper.TABLE_KEYNOTES, null, whereClause, null, null, null, DbHelper.KEYNOTE_NAME);
		
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			
			Keynote a = cursorToKeynote(cursor);
			results.add(a);
			cursor.moveToNext();
		}
		
		cursor.close();
		
		return results;
	}
}
