package dataSources;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.magiconf_client.DbHelper;

public class PosterPresentedDataSource {
	private SQLiteDatabase database;
	private DbHelper dbHelper;
	
	public PosterPresentedDataSource(Context context){
		dbHelper = new DbHelper(context);
	}
	
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}

	public void close(){
		dbHelper.close();
	}
	
	
	public void createPosterPresented(Long poster_id, Long postersession_id){
		ContentValues values = new ContentValues();
		values.put(DbHelper.POSTERS_PRESENTED_POSTER_ID, poster_id);
		values.put(DbHelper.POSTERS_PRESENTED_POSTERSESSION_ID, postersession_id);
		database.insert(DbHelper.TABLE_POSTERS_PRESENTED, null, values);
	}
	
	public void deletePosterPresented(Long poster_id, Long postersession_id){
		String whereClause = DbHelper.POSTERS_PRESENTED_POSTER_ID + " = " + poster_id + " and " + DbHelper.POSTERS_PRESENTED_POSTERSESSION_ID + " = " + postersession_id;
		database.delete(DbHelper.TABLE_POSTERS_PRESENTED, whereClause, null);
	}
	
}
