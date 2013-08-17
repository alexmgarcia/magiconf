package dataSources;

import com.example.magiconf_client.DbHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;


/**
 * @author José
 *
 * Data source to the Talk - Article relationship
 */
public class ArticlePresentedDataSource {
	
	private SQLiteDatabase database;
	private DbHelper dbHelper;
	
	public ArticlePresentedDataSource(Context context){
		dbHelper = new DbHelper(context);
	}
	
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}

	public void close(){
		dbHelper.close();
	}
	
	
	public void createArticlePresented(Long talk_id, Long article_id){
		ContentValues values = new ContentValues();
		values.put(DbHelper.ARTICLES_PRESENTED_ARTICLE_ID, article_id);
		values.put(DbHelper.ARTICLES_PRESENTED_TALK_ID, talk_id);
		database.insert(DbHelper.TABLE_ARTICLEPRESENTED, null, values);
	}
	
	public void deleteArticlePresented(Long talk_id, Long article_id){
		String whereClause = DbHelper.ARTICLES_PRESENTED_ARTICLE_ID + " = " + article_id + " and " + DbHelper.ARTICLES_PRESENTED_TALK_ID + " = " + talk_id;
		database.delete(DbHelper.TABLE_ARTICLEPRESENTED, whereClause, null);
	}
	
}
