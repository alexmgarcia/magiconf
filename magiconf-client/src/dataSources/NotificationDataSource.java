package dataSources;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import com.example.magiconf_client.DbHelper;

import tables.Event;
import tables.Notification;
import utils.Pair;
import utils.ValuesHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.text.format.DateFormat;
import android.util.Log;

/**
 * @author Alexandre
 * 
 * Data source to the Notification table
 */
public class NotificationDataSource {
	
	private SQLiteDatabase database;
	private DbHelper dbHelper;
		
	public NotificationDataSource(Context context){
		dbHelper = new DbHelper(context);
	}
	
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}

	public void close(){
		dbHelper.close();
	}
	
	
	/**
	 * Inserts a new notification
	 * @param title title of the notification
	 * @param description description of the notification
	 * @param time time of the notification
	 * @return inserted notification
	 */
	public Notification createNotification(String title, String description, Date time){
		ContentValues values = new ContentValues();
		
		values.put(DbHelper.NOTIFICATION_TITLE, title);
		values.put(DbHelper.NOTIFICATION_DESCRIPTION, description);
		values.put(DbHelper.NOTIFICATION_TIME, time.getTime());
		long insertId = database.insert(DbHelper.TABLE_NOTIFICATIONS, null, values);
		
		Cursor cursor = database.query(DbHelper.TABLE_NOTIFICATIONS, null, DbHelper.COLUMN_ID + " = " +insertId, null, null, null, null);
		
		cursor.moveToFirst();
		Notification newNotification = cursorToNotification(cursor);
		cursor.close();
		return newNotification;
	}
	
	
	/**
	 * Deletes the specified notification from the local database
	 * @param notification notification to delete
	 */
	public void deleteNotification(Notification notification){
		long id = notification.getId();
		database.delete(DbHelper.TABLE_NOTIFICATIONS, DbHelper.COLUMN_ID +" = "+ id, null);
	}
	
	/**
	 * Returns the notification with the specified id
	 * @param id id of the notification
	 * @return notification with the specified id
	 */
	public Notification getNotification(Long id) {
		String whereClause = "_id=" + id;
		Log.i("Database", ""+database);
		Cursor cursor = database.query(DbHelper.TABLE_NOTIFICATIONS, null, whereClause, null, null, null, null);
		boolean hasData = cursor.moveToFirst();
		Log.i("Cursor", ""+hasData);
		if (!hasData)
			return null;
		return cursorToNotification(cursor);
	}
	
	/**
	 * Returns the number of notifications that are in the database
	 * @return number of notifications that are in the database
	 */
	public int getTotalNotifications() {
		Cursor cursor = database.query(DbHelper.TABLE_NOTIFICATIONS, null, null, null, null, null, null);
		return cursor.getCount();
	}
	
	/**
	 * Returns all notifications
	 * @return all notifications
	 */
	public List<Notification> getNotifications() {
		List<Notification> notificationList = new LinkedList<Notification>();
		Cursor cursor = database.query(DbHelper.TABLE_NOTIFICATIONS, null, null, null, null, null, DbHelper.NOTIFICATION_TIME + " DESC");
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			notificationList.add(cursorToNotification(cursor));
			cursor.moveToNext();
		}
		return notificationList;
	}
			
	/**
	 * Returns the notification which the current cursor is pointing to
	 * @param cursor current cursor
	 * @return notification which the current cursor is pointing to
	 */
	private Notification cursorToNotification(Cursor cursor){
		Notification notification = new Notification();
		notification.setId(cursor.getLong(0));
		notification.setTitle(cursor.getString(1));
		notification.setDescription(cursor.getString(2));
		Timestamp timestamp = new Timestamp(cursor.getLong(3));
		java.sql.Date time = new java.sql.Date(timestamp.getTime());
		notification.setTime(time);
		
		return notification;
	}
}
