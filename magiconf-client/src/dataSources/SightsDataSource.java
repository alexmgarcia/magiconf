package dataSources;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import tables.Author;
import tables.Hotel;
import tables.Restaurant;
import tables.Sight;
import utils.Triple;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.magiconf_client.DbHelper;

public class SightsDataSource {

	private SQLiteDatabase database;
	private DbHelper dbHelper;

	public SightsDataSource(Context context){
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
	 * Inserts a new hotel
	 * @return inserted hotel
	 */
	public Hotel createHotel(String name, String address, String description, double latitude, double longitude, String photo, int stars, String phone){
		ContentValues values = new ContentValues();
		
		
		values.put(DbHelper.SIGHT_NAME, name);
		values.put(DbHelper.SIGHT_ADDRESS, address);
		values.put(DbHelper.SIGHT_DESCRIPTION, description);
		values.put(DbHelper.SIGHT_LATITUDE, latitude);
		values.put(DbHelper.SIGHT_LONGITUDE, longitude);
		values.put(DbHelper.SIGHT_PHOTO, photo);
		

		long insertId = database.insert(DbHelper.TABLE_SIGHTS, null, values);
		
		values = new ContentValues();
		values.put(DbHelper.HOTEL_ID, insertId);
		values.put(DbHelper.HOTEL_STARS, stars);
		values.put(DbHelper.HOTEL_PHONE, phone);
		
		insertId = database.insert(DbHelper.TABLE_HOTELS, null, values);
		
		
		Cursor cursor = database.query(DbHelper.TABLE_HOTELS, null, DbHelper.COLUMN_ID + " = " +insertId, null, null, null, null);
		
		cursor.moveToFirst();
		Hotel newHotel = cursorToHotel(cursor);
		cursor.close();
		return newHotel;
	}

	/**
	 * Inserts a new restaurant
	 * @return inserted restaurant
	 */
	public Restaurant createRestaurant(String name, String address, String description, double latitude, double longitude, String photo, String phone){
		ContentValues values = new ContentValues();
		
		
		values.put(DbHelper.SIGHT_NAME, name);
		values.put(DbHelper.SIGHT_ADDRESS, address);
		values.put(DbHelper.SIGHT_DESCRIPTION, description);
		values.put(DbHelper.SIGHT_LATITUDE, latitude);
		values.put(DbHelper.SIGHT_LONGITUDE, longitude);
		values.put(DbHelper.SIGHT_PHOTO, photo);

		long insertId = database.insert(DbHelper.TABLE_SIGHTS, null, values);
		
		values = new ContentValues();
		values.put(DbHelper.RESTAURANT_ID, insertId);
		values.put(DbHelper.RESTAURANT_PHONE, phone);
		
		insertId = database.insert(DbHelper.TABLE_RESTAURANTS, null, values);
		
		
		Cursor cursor = database.query(DbHelper.TABLE_RESTAURANTS, null, DbHelper.COLUMN_ID + " = " +insertId, null, null, null, null);
		
		cursor.moveToFirst();
		Restaurant newRestaurant = cursorToRestaurant(cursor);
		cursor.close();
		return newRestaurant;
	}
	
	/**
	 * Inserts a new sight (that is not a restaurant or an hotel)
	 * @return inserted sight
	 */
	public Sight createNewSight(String name, String address, String description, double latitude, double longitude, String photo){
		ContentValues values = new ContentValues();
		
		
		values.put(DbHelper.SIGHT_NAME, name);
		values.put(DbHelper.SIGHT_ADDRESS, address);
		values.put(DbHelper.SIGHT_DESCRIPTION, description);
		values.put(DbHelper.SIGHT_LATITUDE, latitude);
		values.put(DbHelper.SIGHT_LONGITUDE, longitude);
		values.put(DbHelper.SIGHT_PHOTO, photo);

		long insertId = database.insert(DbHelper.TABLE_SIGHTS, null, values);
		
	
		Cursor cursor = database.query(DbHelper.TABLE_SIGHTS, null, DbHelper.COLUMN_ID + " = " +insertId, null, null, null, null);
		
		cursor.moveToFirst();
		Sight newSight = cursorToSight(cursor);
		cursor.close();
		return newSight;
	}
	
	public Sight getSight(long id){
		
		String whereClause = DbHelper.COLUMN_ID +" = "+id; 
		Cursor cursor = database.query(DbHelper.TABLE_SIGHTS, null, whereClause, null, null, null, null);
		Sight sight=null;
		if(cursor.moveToFirst()){
			sight = cursorToSight(cursor);
		}
		cursor.close();
		
		return sight;
		
	}
	
	public Hotel getHotel(long id){
		
		String whereClause = DbHelper.HOTEL_ID +" = "+id; 
		Cursor cursor = database.query(DbHelper.TABLE_HOTELS, null, whereClause, null, null, null, null);
		Hotel hotel = null;
		if(cursor.moveToFirst()){
			hotel = cursorToHotel(cursor);
		}
		cursor.close();
		
		return hotel;
		
	}
	
	public Restaurant getRestaurant(long id){
		
		String whereClause = DbHelper.RESTAURANT_ID +" = "+id; 
		Cursor cursor = database.query(DbHelper.TABLE_RESTAURANTS, null, whereClause, null, null, null, null);
		Restaurant rest = null;
		if(cursor.moveToFirst()){
			rest = cursorToRestaurant(cursor);
		}
		cursor.close();
		
		return rest;
		
	}
	
	public boolean hasSight(String name, String address){
				
		String whereClause = DbHelper.SIGHT_NAME +" = '"+name+"' AND "+DbHelper.SIGHT_ADDRESS+" = '"+address+"'"; 
		Cursor cursor = database.query(DbHelper.TABLE_SIGHTS, null, whereClause, null, null, null, null);
		boolean result = cursor.moveToFirst();
		cursor.close();
		
		return result;
	}

	/**
	 * Deletes the specified hotel from the local database
	 * @param hotel hotel to delete
	 */
	public void deleteHotel(Hotel hotel){
		long hotelId = hotel.getHotelID();
		long id = hotel.getId();
		
		database.delete(DbHelper.TABLE_HOTELS, DbHelper.COLUMN_ID + " = "
				+ id, null);
		database.delete(DbHelper.TABLE_SIGHTS, DbHelper.COLUMN_ID + " = "
				+ hotelId, null);
	}
	
	/**
	 * Deletes the specified restaurant from the local database
	 * @param res restaurant to delete
	 */
	public void deleteRestaurant(Restaurant res){
		long restaurantId = res.getRestaurantID();
		long id = res.getId();
		
		database.delete(DbHelper.TABLE_RESTAURANTS, DbHelper.COLUMN_ID + " = "
				+ id, null);
		database.delete(DbHelper.TABLE_SIGHTS, DbHelper.COLUMN_ID + " = "
				+ restaurantId, null);
	}
	
	/**
	 * Deletes the specified sight (not hotels nor restaurants) from the local database
	 * @param sight to delete
	 */
	public void deleteSight(Sight sight){

		long id = sight.getId();

		database.delete(DbHelper.TABLE_SIGHTS, DbHelper.COLUMN_ID + " = "
				+ id, null);
	}
	
	/**
	 * Returns all the hotels present in the local database
	 * @return all the hotels present in the local database
	 */
	public List<Hotel> getAllHotels(){
		List<Hotel> hotels = new LinkedList<Hotel>();
		Cursor cursor = database.query(DbHelper.TABLE_HOTELS, null, null, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			Hotel hotel = cursorToHotel(cursor);
			hotels.add(hotel);
			cursor.moveToNext();
		}
		
		cursor.close();
		return hotels;
	
	}
	
	/**
	 * Returns all the restaurants present in the local database
	 * @return all the restaurants present in the local database
	 */
	public List<Restaurant> getAllRestaurants(){
		List<Restaurant> restaurants = new LinkedList<Restaurant>();
		Cursor cursor = database.query(DbHelper.TABLE_RESTAURANTS, null, null, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			Restaurant rest = cursorToRestaurant(cursor);
			restaurants.add(rest);
			cursor.moveToNext();
		}
		
		cursor.close();
		return restaurants;
	
	}
	
	/**
	 * Returns all the sights (no hotels nor restaurants) present in the local database
	 * @return all the sights present in the local database
	 */
	public List<Sight> getAllSights(){
		List<Sight> sights = new LinkedList<Sight>();
		Cursor cursor = database.query(DbHelper.TABLE_SIGHTS, null, null, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			Sight sight = cursorToSight(cursor);
			long sightID = sight.getId();
			
			String whereClause = DbHelper.HOTEL_ID +" = "+ sightID;
			Cursor c = database.query(DbHelper.TABLE_HOTELS, null, whereClause, null, null, null, null);
			boolean hasData = c.moveToFirst();
			if(!hasData){
				whereClause = DbHelper.RESTAURANT_ID + " = "+ sightID;
				c = database.query(DbHelper.TABLE_RESTAURANTS, null, whereClause, null, null, null, null);
				hasData = c.moveToFirst();
				
				if(!hasData){
					sights.add(sight);
				}
			}
			c.close();
			cursor.moveToNext();
		}
		
		cursor.close();
		return sights;
	
	}
	
	
	
	private Sight cursorToSight(Cursor cursor) {
		Sight s = new Sight();
		s.setId(cursor.getLong(0));
		s.setName(cursor.getString(1));
		s.setAddress(cursor.getString(2));
		s.setDescription(cursor.getString(3));
		s.setLatitude(cursor.getDouble(4));
		s.setLongitude(cursor.getDouble(5));
		s.setPhoto(cursor.getString(6));
		
		return s;
	}

	private Restaurant cursorToRestaurant(Cursor cursor) {
		long restID = cursor.getLong(1);
		String phone = cursor.getString(2);
		// Get the data from the Event table
		Cursor c = database.query(DbHelper.TABLE_SIGHTS, null, DbHelper.COLUMN_ID + " = " + restID, null, null, null, null);
		c.moveToFirst();
				
		Restaurant res = new Restaurant();
		
		res.setId(cursor.getLong(0));
		res.setRestaurantID(restID);
		res.setName(c.getString(1));
		res.setAddress(c.getString(2));
		res.setDescription(c.getString(3));
		res.setLatitude(c.getDouble(4));
		res.setLongitude(c.getDouble(5));
		res.setPhoto(c.getString(6));
		res.setPhone(phone);

		c.close();
		return res;
	}

	private Hotel cursorToHotel(Cursor cursor) {
		long hotelID = cursor.getLong(1);
		int stars = cursor.getInt(2);
		String phone = cursor.getString(3);
		// Get the data from the Event table
		Cursor c = database.query(DbHelper.TABLE_SIGHTS, null, DbHelper.COLUMN_ID + " = " + hotelID, null, null, null, null);
		c.moveToFirst();

		Hotel h = new Hotel();
		
		h.setId(cursor.getLong(0));
		h.setHotelID(hotelID);
		h.setName(c.getString(1));
		h.setAddress(c.getString(2));
		h.setDescription(c.getString(3));
		h.setLatitude(c.getDouble(4)); 
		h.setLongitude(c.getDouble(5));
		h.setPhoto(c.getString(6));
		h.setPhone(phone);
		h.setStars(stars);

		c.close();
		return h;
	}

	public Triple<List<Hotel>, List<Restaurant>, List<Sight>> search(String query) {
		String lowerQuery = query.toLowerCase();
		String convertedName = "%"+lowerQuery.toLowerCase()+"%";
		String whereClause = "lower("+DbHelper.SIGHT_NAME+") like '"+convertedName+"'"; 
		
		List<Hotel> hotels = new ArrayList<Hotel>();
		List<Restaurant> rest = new ArrayList<Restaurant>();
		List<Sight> others = new ArrayList<Sight>();
		
		Cursor cursor = database.query(DbHelper.TABLE_SIGHTS, null, whereClause, null, null, null, DbHelper.SIGHT_NAME);
		
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			
			long id = cursor.getLong(0);
			
			String whereHotelClause = DbHelper.HOTEL_ID + " = "+id;
			Cursor c = database.query(DbHelper.TABLE_HOTELS, null, whereHotelClause, null, null, null, null);
			if(c.moveToFirst()){
				Hotel h = cursorToHotel(c);
				hotels.add(h);
			}else{
				
				String whereRestClause = DbHelper.RESTAURANT_ID + " = "+id;
				c = database.query(DbHelper.TABLE_RESTAURANTS, null, whereRestClause, null, null, null, null);
				if(c.moveToFirst()){
					Restaurant r = cursorToRestaurant(c);
					rest.add(r);
				}else{
					Sight s = cursorToSight(cursor);
					others.add(s);
				}
				
			}
			c.close();
			cursor.moveToNext();
		}
		
		cursor.close();
		
		return new Triple<List<Hotel>, List<Restaurant>, List<Sight>>(hotels,rest,others);
	}
	
	public List<Sight> searchAll(String query) {
		String lowerQuery = query.toLowerCase();
		String convertedName = "%"+lowerQuery.toLowerCase()+"%";
		String whereClause = "lower("+DbHelper.SIGHT_NAME+") like '"+convertedName+"'"; 
		
		List<Sight> results = new ArrayList<Sight>();
		
		Cursor cursor = database.query(DbHelper.TABLE_SIGHTS, null, whereClause, null, null, null, DbHelper.SIGHT_NAME);
		
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			Sight s = cursorToSight(cursor);
			results.add(s);
			cursor.moveToNext();
		}
		
		cursor.close();
		
		return results;
	}
	

	

}
