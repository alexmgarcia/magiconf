package dataSources;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import com.example.magiconf_client.DbHelper;
import tables.Article;
import tables.Author;
import utils.Pair;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * @author José
 * 
 * Data source to the Article table
 */
public class ArticleDataSource {
	
	private SQLiteDatabase database;
	private DbHelper dbHelper;

	public ArticleDataSource(Context context){
		dbHelper = new DbHelper(context);
	}
	
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}

	public void close(){
		dbHelper.close();
	}
	
	
	/**
	 * Inserts a new Article
	 * @param title title of the session
	 * @param time time of the session
	 * @param place place of the session
	 * @param description description of the session
	 * @param keynoteSpeakers keynote speaker present in this session
	 * @return inserted keynote session
	 */
	public Article createArticle(String title, String pubAbstract, List<Author> authors){
		ContentValues values = new ContentValues();
		
		values.put(DbHelper.PUB_TITLE, title);
		values.put(DbHelper.PUB_ABSTRACT, pubAbstract);
		values.put(DbHelper.LAST_MODIFIED_DATE, new java.util.Date().getTime());

		// Insert into Publications table because of inheritance
		long insertId = database.insert(DbHelper.TABLE_PUBLICATIONS, null, values);
		
		values = new ContentValues();
		values.put(DbHelper.ARTICLE_ID, insertId);
		
		// Insert into ArticleSessions table
		insertId = database.insert(DbHelper.TABLE_ARTICLES, null, values);
		
		long authorId;
		
		Iterator<Author> it = authors.iterator(); 
		Author next;
		while (it.hasNext()) {
			next = it.next();
			values = new ContentValues();
			values.put(DbHelper.AUTHOR_NAME, next.getName());
			Log.i("Inserting", next.getName());
			values.put(DbHelper.AUTHOR_EMAIL, next.getEmail());
			values.put(DbHelper.AUTHOR_COUNTRY, next.getCountry());
			values.put(DbHelper.AUTHOR_PHOTO, next.getPhoto());
			values.put(DbHelper.AUTHOR_WORKPLACE, next.getWork_place());
			//TODO: Adicionar Contact e entrada na relação AUtor_Contacto
			
			// Insert the author into the Authors table
			authorId = database.insert(DbHelper.TABLE_AUTHORS, null, values);
			values = new ContentValues();
			values.put(DbHelper.RELATIONSHIP_ARTICLE_AUTHOR_AUTHOR_ID, authorId);
			values.put(DbHelper.RELATIONSHIP_ARTICLE_AUTHOR_ARTICLE_ID, insertId);
			
			// Insert the relationship between the author and the article into article_author table
			database.insert(DbHelper.TABLE_RELATIONSHIP_ARTICLE_AUTHOR, null, values);
		}
		
		Cursor cursor = database.query(DbHelper.TABLE_ARTICLES, null,
				DbHelper.COLUMN_ID + " = " + insertId, null, null, null, null);

		cursor.moveToFirst();
		Article newArticle = cursorToArticle(cursor);
		cursor.close();
		return newArticle;
	}
	
	/**
	 * Deletes the specified article from the local database
	 * @param article article to delete
	 */
	public void deleteArticle(Article art){
		long articleId = art.getArticleId();
		long id = art.getId();
		
		database.delete(DbHelper.TABLE_ARTICLES, DbHelper.COLUMN_ID + " = "
				+ id, null);
		database.delete(DbHelper.TABLE_PUBLICATIONS, DbHelper.COLUMN_ID + " = "
				+ articleId, null);
	}
	
	/**
	 * Returns all the articles present in the local database
	 * @return all the articles present in the local database
	 */
	public List<Article> getAllArticles(){
		List<Article> arts = new LinkedList<Article>();
		Cursor cursor = database.query(DbHelper.TABLE_ARTICLES, null, null, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			Article art = cursorToArticle(cursor);
			arts.add(art);
			cursor.moveToNext();
		}
		
		cursor.close();
		return arts;
	
	}
	
	/**
	 * Returns the author which the current cursor is pointing to
	 * @param cursor current cursor
	 * @return author which the current cursor is pointing to
	 */
	private Author cursorToAuthor(Cursor cursor){
		
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
	
	/**
	 * Returns the article which the current cursor is pointing to
	 * @param cursor current cursor
	 * @return article which the current cursor is pointing to
	 */
	private Article cursorToArticle(Cursor cursor) {
		long pubID = cursor.getLong(1);
			
		// Get the data from the Event table
		Cursor c = database.query(DbHelper.TABLE_PUBLICATIONS, null, DbHelper.COLUMN_ID + " = " + pubID, null, null, null, null);
		c.moveToFirst();
		
		Article article = new Article();
		
		article.setId(cursor.getLong(0));
		article.setArticleId(pubID);
		article.setTitle(c.getString(1));
		article.setAbstract(c.getString(2));
		
		Timestamp timestamp = new Timestamp(c.getLong(3));
		article.setLastModified(new Date(timestamp.getTime()));
		
		
		// Get the authors
		c = database.query(DbHelper.TABLE_RELATIONSHIP_ARTICLE_AUTHOR, null, DbHelper.RELATIONSHIP_ARTICLE_AUTHOR_ARTICLE_ID + " = " + article.getId(), null, null, null, null);
		c.moveToFirst();
		int size = c.getCount();
		Log.i("Size", ""+size);
		ArrayList<Author> authors = new ArrayList<Author>();
		while (!c.isAfterLast()) {
			authors.add(cursorToAuthor(c));
			c.moveToNext();
		}
		article.setAuthors(authors);
		c.close();
		return article;
	}
	
	/**
	 * Returns the article with the specified id
	 * @param id id of the article to get
	 * @return article with specified id
	 */
	public Article getArticle(Long id) {
		String whereClause = DbHelper.COLUMN_ID + " = " + id;
		Cursor cursor = database.query(DbHelper.TABLE_ARTICLES, null, whereClause, null, null, null, null);
		boolean hasData = cursor.moveToFirst();
		if (!hasData){
			cursor.close();
			return null;
		}
		Article a = cursorToArticle(cursor);
		cursor.close();
		return a;
	}
	
	/**
	 * Updates the article with the specified id
	 * @param id id of the article to update
	 * @param title new title of the article
	 * @param pubAbstract new abstract of the article
	 * @param lastModified last date of update
	 */
	public void update(long id, String title, String pubAbstract, Date lastModified) {
		String whereClause = DbHelper.COLUMN_ID + " = " + id;
		Log.i("Database article", "" + database);
		Cursor cursor = database.query(DbHelper.TABLE_ARTICLES, null,
				whereClause, null, null, null, null);
		cursor.moveToFirst();
		Long pubId = cursor.getLong(1);
		ContentValues values = new ContentValues();
		values.put(DbHelper.PUB_TITLE, title);
		values.put(DbHelper.PUB_ABSTRACT, pubAbstract);
		values.put(DbHelper.LAST_MODIFIED_DATE, lastModified.getTime());
		database.update(DbHelper.TABLE_PUBLICATIONS, values, DbHelper.COLUMN_ID
				+ " = " + pubId, null);
	}

	/**
	 * Checks if exists a article that has the specified pubID as its parent
	 * @param pubID parent's publication id
	 * @return true if exists an article that has the specified id as its parent; false otherwise
	 */
	public boolean hasArticle(long pubID) {
		String whereClause = DbHelper.ARTICLE_ID + " = " + pubID;
		Cursor cursor = database.query(DbHelper.TABLE_ARTICLES, null,
				whereClause, null, null, null, null);
		return cursor.moveToFirst();
	}
	
	/**
	 * Checks if exists a article with the specified title
	 * @param pubID parent's publication id
	 * @return true if exists an article that has the specified id as its parent; false otherwise
	 */
	public boolean isArticle(String title) {
		String whereClause = DbHelper.PUB_TITLE +" = '"+title+"'";
		Cursor cursor = database.query(DbHelper.TABLE_PUBLICATIONS, null, whereClause, null, null, null, null);
		if(cursor.moveToFirst()){
			long pubID = cursor.getLong(0);
			whereClause = DbHelper.ARTICLE_ID + " = " + pubID;
			cursor = database.query(DbHelper.TABLE_ARTICLES, null,
					whereClause, null, null, null, null);
			if(cursor.moveToFirst()){
				cursor.close();
				return true;
			}
		}
			
		cursor.close();
		return false;



	}
	
	public Pair<Long,Boolean> isArticleAndGetIDByTitle(String title) {
		String whereClause = DbHelper.PUB_TITLE +" = '"+title+"'";
		Cursor cursor = database.query(DbHelper.PUB_TITLE, null, whereClause, null, null, null, null);
		if(cursor.moveToFirst()){
			long pubID = cursor.getLong(0);
			whereClause = DbHelper.ARTICLE_ID + " = " + pubID;
			cursor = database.query(DbHelper.TABLE_ARTICLES, null,
					whereClause, null, null, null, null);
			if(cursor.moveToFirst()){
				cursor.close();
				return new Pair<Long,Boolean>(pubID, true);
			}
			
			cursor.close();
			return new Pair<Long,Boolean>(pubID, false);

		}
		
		cursor.close();
		return null;

	}

	public Article getArticleByParent(Long id) {
		String whereClause = DbHelper.ARTICLE_ID + " = " + id;
		Cursor cursor = database.query(DbHelper.TABLE_ARTICLES, null, whereClause, null, null, null, null);
		boolean hasData = cursor.moveToFirst();
		if (!hasData){
			cursor.close();
			return null;
		}
		
		Article a = cursorToArticle(cursor);
		cursor.close();
		return a;
	}
	
	
	

}
