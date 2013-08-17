package dataSources;

import java.util.ArrayList;
import java.util.List;

import com.example.magiconf_client.DbHelper;
import tables.Article;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;



/**
 * @author José
 *
 * Data source to the Article - Author relationship
 */
public class ArticleAuthorDataSource {
	
	private SQLiteDatabase database;
	private DbHelper dbHelper;
	
	public ArticleAuthorDataSource(Context context){
		dbHelper = new DbHelper(context);
	}
	
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}

	public void close(){
		dbHelper.close();
	}
	
	
	public void createArticleAuthor(Long article_id, Long author_id){
		ContentValues values = new ContentValues();
		values.put(DbHelper.RELATIONSHIP_ARTICLE_AUTHOR_ARTICLE_ID, article_id);
		values.put(DbHelper.RELATIONSHIP_ARTICLE_AUTHOR_AUTHOR_ID, author_id);
		database.insert(DbHelper.TABLE_RELATIONSHIP_ARTICLE_AUTHOR, null, values);
	}
	
	public void deleteArticleAuthor(Long article_id, Long author_id){
		String whereClause = DbHelper.RELATIONSHIP_ARTICLE_AUTHOR_ARTICLE_ID + " = " + article_id + " and " + DbHelper.RELATIONSHIP_ARTICLE_AUTHOR_AUTHOR_ID + " = " + author_id;
		database.delete(DbHelper.TABLE_RELATIONSHIP_ARTICLE_AUTHOR, whereClause, null);
	}
	

}
