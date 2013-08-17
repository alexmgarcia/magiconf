package dataSources;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import tables.City;
import tables.Conference;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.magiconf_client.DbHelper;

//TODO: Corrigir datas...

public class ConferenceDataSource {
	private SQLiteDatabase database;
	private DbHelper dbHelper;
		
	public ConferenceDataSource(Context context){
		dbHelper = new DbHelper(context);
		
	}
	
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}

	public void close(){
		dbHelper.close();
	}
	
	
	public Conference createConference(String title,Date beginningDate,Date endingDate, String description,int edition,String place, double latitude, double longitude,String website,String logo) throws ParseException{
		ContentValues values = new ContentValues();
		
		values.put(DbHelper.CONFERENCE_TITLE, title);
		Log.i("CONFERENCE: Title",title);
		values.put(DbHelper.CONFERENCE_BDATE, beginningDate.getTime());
		Log.i("CONFERENCE: BeginDate",""+beginningDate.getTime());
		values.put(DbHelper.CONFERENCE_EDATE, endingDate.getTime());
		Log.i("CONFERENCE: EndingDate",""+endingDate.getTime());
		values.put(DbHelper.CONFERENCE_DESCRIPTION, description);
		values.put(DbHelper.CONFERENCE_EDITION, edition);
		Log.i("CONFERENCE: Edition",""+edition);
		values.put(DbHelper.CONFERENCE_PLACE, place);
		Log.i("CONFERENCE: Place",""+place);
		values.put(DbHelper.CONFERENCE_LATITUDE, latitude);
		Log.i("CONFERENCE: Latitude",""+latitude);
		values.put(DbHelper.CONFERENCE_LONGITUDE, longitude);
		Log.i("CONFERENCE: Longitude",""+longitude);
		values.put(DbHelper.CONFERENCE_WEBSITE, website);
		Log.i("CONFERENCE: Website",website);
		values.put(DbHelper.CONFERENCE_LOGO, logo);
		Log.i("CONFERENCE: Logo",""+logo);
				
		long insertId = database.insert(DbHelper.TABLE_CONFERENCES, null, values);
		
		Cursor cursor = database.query(DbHelper.TABLE_CONFERENCES, null, DbHelper.COLUMN_ID + " = " +insertId, null, null, null, null);
		
		cursor.moveToFirst();
		Conference newConference = cursorToConference(cursor);
		cursor.close();
		return newConference;
	}
	
	public void deleteConference(){
		Cursor cursor = database.query(DbHelper.TABLE_CONFERENCES, null, null, null, null, null, null);
		cursor.moveToFirst();
		long id = cursor.getLong(0);
		database.delete(DbHelper.TABLE_CONFERENCES, DbHelper.COLUMN_ID +" = "+ id, null);
		cursor.close();
	}
	
	public Conference getConference() throws ParseException{
		Cursor cursor = database.query(DbHelper.TABLE_CONFERENCES, null, null, null, null, null, null);
		cursor.moveToFirst();
		Conference conference = null;
		if(!cursor.isAfterLast()){
			conference = cursorToConference(cursor);
		}
		
		cursor.close();
		return conference;
	
	}
	
	private Conference cursorToConference(Cursor cursor) throws ParseException{
		Conference conference = new Conference();
		conference.setId(cursor.getLong(0));
		conference.setTitle(cursor.getString(1));
		    
		Timestamp timestamp = new Timestamp(cursor.getLong(2));
		conference.setBeginningDate(new Date(timestamp.getTime()));
		
		timestamp = new Timestamp(cursor.getLong(3));
		conference.setEndingDate(new Date(timestamp.getTime()));

		conference.setDescription(cursor.getString(4));
		conference.setEdition(cursor.getInt(5));
		conference.setPlace(cursor.getString(6));
		conference.setLatitude(cursor.getDouble(7));
		conference.setLongitude(cursor.getDouble(8));
		conference.setWebsite(cursor.getString(9));
		conference.setLogo(cursor.getString(10));
		
		return conference;
	}
	
	public City getCity(){
		Cursor cursor = database.query(DbHelper.TABLE_CITIES, null, null, null, null, null, null);
		cursor.moveToFirst();
		City city = null;
		if(!cursor.isAfterLast()){
			city = cursorToCity(cursor);
		}
		
		cursor.close();
		return city;
	
	}	
	
	public City createCity(String name, String description, String photo){
		
		ContentValues values = new ContentValues();
		
		values.put(DbHelper.CITY_NAME, name);
		values.put(DbHelper.CITY_DESCRIPTION, description);
		values.put(DbHelper.CITY_PHOTO, photo);

		long insertId = database.insert(DbHelper.TABLE_CITIES, null, values);

		Cursor cursor = database.query(DbHelper.TABLE_CITIES, null, DbHelper.COLUMN_ID + " = " +insertId, null, null, null, null);
		
		cursor.moveToFirst();
		City newCity = cursorToCity(cursor);
		cursor.close();
		return newCity;
		
	}
	
	private City cursorToCity(Cursor cursor){
		City city = new City();
		city.setId(cursor.getLong(0));
		city.setName(cursor.getString(1));
		city.setDescription(cursor.getString(2));
		city.setPhoto(cursor.getString(3));

		return city;
	}
	
}
