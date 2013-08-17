package dataSources;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import com.example.magiconf_client.DbHelper;

import tables.Keynote;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;


/**
 * @author Alexandre
 *
 * Data source to the Keynote - KeynoteSession relationship
 */
public class KeynoteKeynoteSessionDataSource {
	
	private SQLiteDatabase database;
	private DbHelper dbHelper;
	
	public KeynoteKeynoteSessionDataSource(Context context){
		dbHelper = new DbHelper(context);
	}
	
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}

	public void close(){
		dbHelper.close();
	}
	
	
	public void createKeynoteKeynoteSession(Long keynote_id, Long keynotesession_id){
		ContentValues values = new ContentValues();
		values.put(DbHelper.RELATIONSHIP_KEYNOTES_KEYSESSIONS_KEYNOTE_ID, keynote_id);
		values.put(DbHelper.RELATIONSHIP_KEYNOTES_KEYSESSIONS_KEYNOTESESSION_ID, keynotesession_id);
		database.insert(DbHelper.TABLE_RELATIONSHIP_KEYNOTES_KEYSESSIONS, null, values);
	}
	
	public void deleteKeynoteKeynoteSession(Long keynote_id, Long keynotesession_id){
		String whereClause = DbHelper.RELATIONSHIP_KEYNOTES_KEYSESSIONS_KEYNOTE_ID + " = " + keynote_id + " and " + DbHelper.RELATIONSHIP_KEYNOTES_KEYSESSIONS_KEYNOTESESSION_ID + " = " + keynotesession_id;
		database.delete(DbHelper.TABLE_RELATIONSHIP_KEYNOTES_KEYSESSIONS, whereClause, null);
	}
	
}
