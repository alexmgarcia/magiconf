package dataSources;

import java.util.LinkedList;
import java.util.List;

import tables.Author;
import tables.Hotel;
import tables.Sight;
import tables.Sponsor;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.magiconf_client.DbHelper;


/**
 * @author Farah
 * 
 * Data source to the Sponsor table
 */
public class SponsorDataSource {

	private SQLiteDatabase database;
	private DbHelper dbHelper;

	public SponsorDataSource(Context context){
		dbHelper = new DbHelper(context);
	}
	
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}

	public void close(){
		dbHelper.close();
	}
	
	
	/**
	 * Inserts a new sponsor
	 * @param name name of the sponsor
	 * @param logo logo of the sponsor
	 
	 * @return inserted sponsor
	 */
	public Sponsor createSponsor(String name, String logo, String website){
		
		ContentValues values = new ContentValues();
		
		
		values.put(DbHelper.SPONSOR_NAME, name);
		values.put(DbHelper.SPONSOR_LOGO, logo);
		values.put(DbHelper.SPONSOR_WEBSITE, website);
		
		long insertId = database.insert(DbHelper.TABLE_SPONSORS, null, values);
		
		Cursor cursor = database.query(DbHelper.TABLE_SPONSORS, null, DbHelper.COLUMN_ID + " = " +insertId, null, null, null, null);
		
		cursor.moveToFirst();
		Sponsor newSponsor = cursorToSponsor(cursor);
		cursor.close();
		return newSponsor;
	}
	
	/**
	 * Returns the Sponsors with the specified id
	 * @param id id of the sponsor to get
	 * @return sponsor with specified id
	 */
	public Sponsor getSponsor(long id){
		
		Cursor cursor = database.query(DbHelper.TABLE_SPONSORS, null,  DbHelper.COLUMN_ID + " = " + id, null, null, null, null);
		cursor.moveToFirst();
		Sponsor sponsor = cursorToSponsor(cursor);
		cursor.close();
		
		return sponsor;
		
	}
	
	/**
	 * Deletes the specified sponsor from the local database
	 * @param sponsor to delete
	 **/
	public void deleteSponsor(Sponsor sponsor){
		long id = sponsor.getId();
		database.delete(DbHelper.TABLE_SPONSORS, DbHelper.COLUMN_ID + " = " + id, null);
	}
	
	/**
	 * Returns all the sponsors present in the local database
	 * @return all sponsors
	 */
	public List<Sponsor> getAllSponsors(){
		List<Sponsor> sponsors = new LinkedList<Sponsor>();
		Cursor cursor = database.query(DbHelper.TABLE_SPONSORS, null, null, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			Sponsor sponsor = cursorToSponsor(cursor);
			sponsors.add(sponsor);
			cursor.moveToNext();
		}
		
		cursor.close();
		return sponsors;
	
	}
	
	private Sponsor cursorToSponsor(Cursor cursor) {
		Sponsor s = new Sponsor();
		s.setId(cursor.getLong(0));
		s.setName(cursor.getString(1));
		s.setLogo(cursor.getString(2));
		s.setWebSite(cursor.getString(3));
		
		
		return s;
	}
	
	
	public boolean hasSponsor(String name){
		Cursor cursor = database.query(DbHelper.TABLE_SPONSORS, null,  DbHelper.SPONSOR_NAME + " ='" + name+"'", null, null, null, null);
		return cursor.moveToFirst();
	}
	
	
	
	
	
	
}
