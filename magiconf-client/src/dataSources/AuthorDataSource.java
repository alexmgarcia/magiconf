package dataSources;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.example.magiconf_client.DbHelper;

import tables.Article;
import tables.Author;
import tables.Event;
import tables.NotPresentedPublication;
import tables.Publication;
import utils.EventComparator;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;


/**
 * @author José
 * 
 * Data source to the Author table
 */
public class AuthorDataSource {
	
	private SQLiteDatabase database;
	private DbHelper dbHelper;
	
	public AuthorDataSource(Context context){
		dbHelper = new DbHelper(context);
	}
	
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}

	public void close(){
		dbHelper.close();
	}
	

	public Author getAuthor(String email, long authorId) {
		Cursor cursor = database.query(DbHelper.TABLE_AUTHORS, null, DbHelper.AUTHOR_EMAIL + " = \'" + email +"\' OR "+ DbHelper.COLUMN_ID+ " = " + authorId , null, null, null, null);
		cursor.moveToFirst();
		Author author = cursorToAuthor(cursor);
		cursor.close();
		return author;
	}
	
	/**
	 * Returns all events on which the author's publications will be presented
	 * @param id id of the author to get events
	 * @return events on which the author's publications will be presented
	 */
	public Set<Event> getAuthorEvents(Long id) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		// Get talks
		queryBuilder.setTables(DbHelper.TABLE_RELATIONSHIP_ARTICLE_AUTHOR + " INNER JOIN " + DbHelper.TABLE_ARTICLEPRESENTED + " ON " + 
				DbHelper.TABLE_RELATIONSHIP_ARTICLE_AUTHOR + "." + DbHelper.RELATIONSHIP_ARTICLE_AUTHOR_ARTICLE_ID + " = " + DbHelper.TABLE_ARTICLEPRESENTED + "." + DbHelper.ARTICLES_PRESENTED_ARTICLE_ID +
				" INNER JOIN " + DbHelper.TABLE_TALKS + " ON " + DbHelper.TABLE_ARTICLEPRESENTED + "." + DbHelper.ARTICLES_PRESENTED_TALK_ID + " = " + DbHelper.TABLE_TALKS + "." + DbHelper.COLUMN_ID + 
				" INNER JOIN " + DbHelper.TABLE_EVENTS + " ON " + DbHelper.TABLE_TALKS + "." + DbHelper.EVENT_ID + " = " + DbHelper.TABLE_EVENTS + "." + DbHelper.COLUMN_ID);
		
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		String columns[] = { 	DbHelper.TABLE_EVENTS + "." + DbHelper.COLUMN_ID,
								DbHelper.TABLE_EVENTS + "." + DbHelper.EVENT_TITLE, 
								DbHelper.TABLE_EVENTS + "." + DbHelper.EVENT_TIME,
								DbHelper.TABLE_EVENTS + "." + DbHelper.DJANGO_ID };
		
		Cursor cursorTalks = queryBuilder.query(db, columns, DbHelper.TABLE_RELATIONSHIP_ARTICLE_AUTHOR + "." + DbHelper.RELATIONSHIP_ARTICLE_AUTHOR_AUTHOR_ID + " = " + id, null, null, null, null);
		
		// Get poster sessions
		queryBuilder.setTables(DbHelper.TABLE_RELATIONSHIP_POSTER_AUTHOR + " INNER JOIN " + DbHelper.TABLE_POSTERS_PRESENTED + " ON " + 
				DbHelper.TABLE_RELATIONSHIP_POSTER_AUTHOR + "." + DbHelper.RELATIONSHIP_POSTER_AUTHOR_POSTER_ID + " = " + DbHelper.TABLE_POSTERS_PRESENTED + "." + DbHelper.POSTERS_PRESENTED_POSTER_ID +
				" INNER JOIN " + DbHelper.TABLE_POSTERSESSION + " ON " + DbHelper.TABLE_POSTERS_PRESENTED + "." + DbHelper.POSTERS_PRESENTED_POSTERSESSION_ID + " = " + DbHelper.TABLE_POSTERSESSION + "." + DbHelper.COLUMN_ID + 
				" INNER JOIN " + DbHelper.TABLE_EVENTS + " ON " + DbHelper.TABLE_POSTERSESSION + "." + DbHelper.EVENT_ID + " = " + DbHelper.TABLE_EVENTS + "." + DbHelper.COLUMN_ID);
			
		Cursor cursorPosters = queryBuilder.query(db, columns, DbHelper.TABLE_RELATIONSHIP_POSTER_AUTHOR + "." + DbHelper.RELATIONSHIP_POSTER_AUTHOR_AUTHOR_ID + " = " + id, null, null, null, null);
		
		cursorTalks.moveToFirst();
		cursorPosters.moveToFirst();
		Set<Event> events = new TreeSet<Event>(new EventComparator());
		Event e;
		Timestamp timestamp;
		
		while (!cursorTalks.isAfterLast()) {
			e = new Event();
			e.setId(cursorTalks.getLong(0));
			e.setTitle(cursorTalks.getString(1));
			timestamp = new Timestamp(cursorTalks.getLong(2));
			e.setDjangoId(cursorTalks.getLong(3));
			e.setTime(new Date(timestamp.getTime()));
			
			events.add(e);
			cursorTalks.moveToNext();
		}
		
		while (!cursorPosters.isAfterLast()) {
			e = new Event();
			e.setId(cursorPosters.getLong(0));
			e.setTitle(cursorPosters.getString(1));
			timestamp = new Timestamp(cursorPosters.getLong(2));
			e.setTime(new Date(timestamp.getTime()));
			e.setDjangoId(cursorPosters.getLong(3));
			events.add(e);
			cursorPosters.moveToNext();
		}
		
		cursorTalks.close();
		cursorPosters.close();
		return events;
	}
	
	
	/**
	 * Inserts a new author
	 * @param email email of the author
	 * @param name name of the author
	 * @param workplace work place of the author
	 * @param country country of the author
	 * @param photo photo url of the author
	 * @param string 
	 * @return inserted author
	 */
public Author createAuthor(String email, String name, String workplace, String country, String photo, String contact, boolean ignoreAlreadyOnBD){
		
		boolean insert = true;
		Author newAuthor;
		Cursor c = null;
		if(!ignoreAlreadyOnBD){
			c = database.rawQuery("SELECT * FROM " + DbHelper.TABLE_AUTHORS+ " WHERE " + DbHelper.AUTHOR_EMAIL + "= '" + email + "'", null);
			insert = c.getCount() == 0;
		}
		if(insert){
			ContentValues values = new ContentValues();
			
			values.put(DbHelper.AUTHOR_EMAIL, email);
			values.put(DbHelper.AUTHOR_NAME,name);
			values.put(DbHelper.AUTHOR_WORKPLACE,workplace);
			values.put(DbHelper.AUTHOR_COUNTRY,country);
			values.put(DbHelper.AUTHOR_PHOTO,photo);
			//TODO: Contact

			long insertId = database.insert(DbHelper.TABLE_AUTHORS, null, values);
			
			Cursor cursor = database.query(DbHelper.TABLE_AUTHORS, null, DbHelper.COLUMN_ID + " = " +insertId, null, null, null, null);
			
			cursor.moveToFirst();
			newAuthor = cursorToAuthor(cursor);
			cursor.close();
		}
		else{
			c.moveToFirst();
			newAuthor = cursorToAuthor(c);
			Log.i("AuthorDataSource", "skipped author: "+newAuthor.getEmail());
			c.close();
		}
		return newAuthor;
	}
	
	/**
	 * Deletes the specified author from the local database
	 * @param author to delete
	 */
	public void deleteAuthor(Author author){
		long id = author.getId();
		database.delete(DbHelper.TABLE_AUTHORS, DbHelper.COLUMN_ID +" = "+ id, null);
	}
	
	/**
	 * Returns all the authors present in the local database
	 * @return all author
	 */
	public List<Author> getAllAuthors(boolean sort){
		List<Author> authors = new ArrayList<Author>();
		Cursor cursor;
		if(sort)
			cursor = database.query(DbHelper.TABLE_AUTHORS, null, null, null, null, null, DbHelper.AUTHOR_NAME);
		
		else
			cursor = database.query(DbHelper.TABLE_AUTHORS, null, null, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			Author aut = cursorToAuthor(cursor);
			authors.add(aut);
			cursor.moveToNext();
		}
		
		cursor.close();
		return authors;
	
	}
	
	/**
	 * Returns the author which the current cursor is pointing to
	 * @param cursor current cursor
	 * @return author which the current cursor is pointing to
	 */
	private Author cursorToAuthor(Cursor cursor){
		Author author = new Author();
		author.setId(cursor.getLong(0));
		author.setEmail(cursor.getString(1));
		author.setName(cursor.getString(2));
		author.setWork_place(cursor.getString(3));
		author.setCountry(cursor.getString(4));
		author.setPhoto(cursor.getString(5));
		
		return author;
	}
	
	/**
	 * Returns the articles from the author with the specified id
	 * @param authorID id of the author
	 * @return list of articles from the author with the specified id
	 */
	public List<Article> getArticlesFromAuthor(String email) {
		Log.i("Author ID",""+email);
		Author a = getAuthor(email,-1);
		Log.i("Author NAME",""+ a.getName());
		String whereClause = DbHelper.RELATIONSHIP_ARTICLE_AUTHOR_AUTHOR_ID + " = " + a.getId();
		Cursor cursor = database.query(DbHelper.TABLE_RELATIONSHIP_ARTICLE_AUTHOR, null, whereClause, null, null, null, null);
		List<Article> list = new ArrayList<Article>();
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			
			Article art = cursorToArticle(cursor);
			list.add(art);
			Log.i("Artigo NOME",""+art.getTitle());
			cursor.moveToNext();
		}
		
		cursor.close();
		
		Log.i("Tamanho da lista",""+list.size());
		return list;
	}
	
	/**
	 * Returns the article which the current cursor is pointing to
	 * @param cursor current cursor
	 * @return article which the current cursor is pointing to
	 */
	private Article cursorToArticle(Cursor cursor){
		
		
		Article art = new Article();
		art.setId(cursor.getLong(0));
		
		String whereClause = DbHelper.COLUMN_ID + " = " + cursor.getLong(0);
		
		Cursor c = database.query(DbHelper.TABLE_ARTICLES, null, whereClause, null, null, null, null);
		c.moveToFirst();
		art.setArticleId(c.getLong(1));
				
		whereClause = DbHelper.COLUMN_ID + " = " + c.getLong(1);
		Cursor c2 = database.query(DbHelper.TABLE_PUBLICATIONS, null, whereClause, null, null, null, null);
		c2.moveToFirst();
		art.setTitle(c2.getString(1));

		c.close();
		c2.close();
		
		return art;
	}
	
	public List<Author> search(String query){
		
		String lowerQuery = query.toLowerCase();
		String convertedName = "%"+lowerQuery.toLowerCase()+"%";
		String whereClause = "lower("+DbHelper.AUTHOR_EMAIL+ ")='"+lowerQuery+"' OR lower("+DbHelper.AUTHOR_NAME+") like '"+convertedName+"'"; 
		
		List<Author> results = new ArrayList<Author>();
		
		Cursor cursor = database.query(DbHelper.TABLE_AUTHORS, null, whereClause, null, null, null, DbHelper.AUTHOR_NAME);
		
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			
			Author a = cursorToAuthor(cursor);
			results.add(a);
			cursor.moveToNext();
		}
		
		cursor.close();
		
		return results;
	}

	public List<NotPresentedPublication> getNotPresentedPublicationsFromAuthor(String author) {

		String whereClause = DbHelper.NOT_PRESENTED_PUBLICATION_AUTHORS+" like '%"+author+"%'";
		Cursor cursor = database.query(DbHelper.TABLE_NOT_PRESENTED_PUBLICATIONS, null, whereClause, null, null, null, null);
		List<NotPresentedPublication> list = new ArrayList<NotPresentedPublication>();
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			
			NotPresentedPublication p = cursorToNotPresentedPublication(cursor);
			list.add(p);
			Log.i("Artigo NOME",""+p.getTitle());
			cursor.moveToNext();
		}
		
		cursor.close();
		
		Log.i("Tamanho da lista",""+list.size());
		return list;
	}

	private NotPresentedPublication cursorToNotPresentedPublication(
			Cursor cursor) {
		NotPresentedPublication p = new NotPresentedPublication();
		p.setId(cursor.getLong(0));
		p.setPublicationID(cursor.getLong(1));
		p.setAuthors(cursor.getString(2));
		
		String whereClause = DbHelper.COLUMN_ID + " = " + cursor.getLong(1);
		Cursor c = database.query(DbHelper.TABLE_PUBLICATIONS, null, whereClause, null, null, null, null);
		c.moveToFirst();
		p.setTitle(c.getString(1));
		
		c.close();
		
		return p;
	}

	public List<Publication> getPublicationsFromAuthor(long authorId, String name) {

		String whereClause = DbHelper.RELATIONSHIP_ARTICLE_AUTHOR_AUTHOR_ID + " = " + authorId;
		Cursor cursor = database.query(DbHelper.TABLE_RELATIONSHIP_ARTICLE_AUTHOR, null, whereClause, null, null, null, null);
		List<Publication> list = new ArrayList<Publication>();
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			
			Article art = cursorToArticle(cursor);
			art.setId(art.getArticleId());
			list.add(art);
			Log.i("Artigo NOME",""+art.getTitle());
			Log.i("Pub ID",""+art.getId());
			cursor.moveToNext();
		}
		
		whereClause = DbHelper.NOT_PRESENTED_PUBLICATION_AUTHORS+" like '%"+name+"%'";
		cursor = database.query(DbHelper.TABLE_NOT_PRESENTED_PUBLICATIONS, null, whereClause, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			
			NotPresentedPublication p = cursorToNotPresentedPublication(cursor);
			p.setId(p.getPublicationID());
			list.add(p);
			Log.i("Pub NOME",""+p.getTitle());
			Log.i("Pub ID",""+p.getId());
			cursor.moveToNext();
		}
		
		cursor.close();		
		
		return list;
		
	}
	
}
