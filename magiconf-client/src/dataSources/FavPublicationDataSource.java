package dataSources;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import tables.FavPublication;
import tables.Publication;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.magiconf_client.DbHelper;

/**
 * @author Alexandre
 * 
 * Data source to the FavouritePublications table
 */
public class FavPublicationDataSource {

	private SQLiteDatabase database;
	private DbHelper dbHelper;
	
	public FavPublicationDataSource(Context context){
		dbHelper = new DbHelper(context);
	}
	
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}

	public void close(){
		dbHelper.close();
	}
	
	
	/**
	 * Inserts a new FavPublication
	 * @param pubID publication id
	 * @param username username which is adding the publication to his favorites
	 * @return inserted FavPublication
	 */
	public FavPublication createFavPublication(long pubID, String username){
		ContentValues values = new ContentValues();
		
		values.put(DbHelper.FAV_PUBLICATION_PUB_ID, pubID);
		values.put(DbHelper.FAV_PUBLICATION_USERNAME, username);

		long insertId = database.insert(DbHelper.TABLE_FAVPUBLICATIONS, null, values);
		
		Cursor cursor = database.query(DbHelper.TABLE_FAVPUBLICATIONS, null, DbHelper.COLUMN_ID + " = " +insertId, null, null, null, null);
		
		cursor.moveToFirst();
		FavPublication pub = cursorToFavPublication(cursor);
		cursor.close();
		return pub;
	}
	
	/**
	 * Deletes the specified favpublication from the local database
	 * @param pub favpublication to delete
	 */
	public void deleteFavPublication(FavPublication pub){
		long id = pub.getId();
		database.delete(DbHelper.TABLE_FAVPUBLICATIONS, DbHelper.COLUMN_ID +" = "+ id, null);
	}
	
	/**
	 * Deletes the specified favpublication from the local database
	 * @param pub favpublication to delete
	 */
	public void deleteFavPublicationByPubID(long pubId){

		database.delete(DbHelper.TABLE_FAVPUBLICATIONS, DbHelper.FAV_PUBLICATION_PUB_ID +" = "+ pubId, null);
	}
	
	/**
	 * Returns the favpublication with the specified id
	 * @param pubID id of the favpublication
	 * @return favpublication with the specified id
	 */
	public FavPublication getFavoritePublication(Long pubID) {
		
		String whereClause = DbHelper.FAV_PUBLICATION_PUB_ID + " = " + pubID;
		Cursor cursor = database.query(DbHelper.TABLE_FAVPUBLICATIONS, null, whereClause, null, null, null, null);
		boolean hasData = cursor.moveToFirst();
		
		if (!hasData){
			cursor.close();
			return null;
		}
		FavPublication pub = cursorToFavPublication(cursor);
		
		return pub;
	}
	
	/**
	 * Checks if the favorite publication is an article
	 * @param pub favorite publication to check
	 * @return true if the favorite publication is an article; false otherwise
	 */
	public boolean checkifIsArticle(FavPublication pub) {
		Log.i("PUB ID in CheckifisArticle",""+pub.getPublicationID());
		String whereClause = DbHelper.ARTICLE_ID + " = " + pub.getPublicationID();
		Cursor cursor = database.query(DbHelper.TABLE_ARTICLES, null, whereClause, null, null, null, null);
		boolean hasData = cursor.moveToFirst();
		cursor.close();
		return hasData;
	}
	
	/**
	 * Returns all the favpublications of a user
	 * @param username username to return favpublications
	 * @return all favpublications of a user
	 */
	public List<FavPublication> getAllFavPublications(String username){
		String whereClause =  DbHelper.FAV_PUBLICATION_USERNAME + " = '" + username + "'";
		List<FavPublication> pubs = new ArrayList<FavPublication>();
		Cursor cursor = database.query(DbHelper.TABLE_FAVPUBLICATIONS, null, whereClause, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			FavPublication pub = cursorToFavPublication(cursor);
			pubs.add(pub);
			cursor.moveToNext();
		}
		
		cursor.close();
		return pubs;
	
	}
	
	/**
	 * Returns the favpublication which the current cursor is pointing to
	 * @param cursor current cursor
	 * @return favpublication which the current cursor is pointing to
	 */
	private FavPublication cursorToFavPublication(Cursor cursor){
		FavPublication pub = new FavPublication();
		pub.setId(cursor.getLong(0));
		pub.setPublicationID(cursor.getLong(1));
		pub.setUsername(cursor.getString(2));
		String whereClause = DbHelper.COLUMN_ID + " = " + pub.getPublicationID();
		Cursor c = database.query(DbHelper.TABLE_PUBLICATIONS, null, whereClause, null, null, null, null);
		c.moveToFirst();
		pub.setTitle(c.getString(1));
		c.close();
		
		return pub;
	}

	public List<FavPublication> search(String username, String query) {
		String favPubWhereClause = DbHelper.FAV_PUBLICATION_USERNAME + " = '" + username + "'";
		List<FavPublication> pubs = new ArrayList<FavPublication>();
		Cursor cursor = database.query(DbHelper.TABLE_FAVPUBLICATIONS, null, favPubWhereClause, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			FavPublication pub = cursorToFavPublication(cursor);
			long pubID = pub.getPublicationID();
			String whereClause = DbHelper.COLUMN_ID +" = "+pubID;
			Cursor c = database.query(DbHelper.TABLE_PUBLICATIONS, null, whereClause, null, null, null, null);
			c.moveToFirst();
			Publication p = cursorToPublication(c);
			if(p.getTitle().toLowerCase().contains(query.toLowerCase()) || p.getAbstract().toLowerCase().contains(query.toLowerCase())){
				pubs.add(pub);
			}
			c.close();
			cursor.moveToNext();
		}
		
		cursor.close();
		return pubs;
	}
	
	/**
	 * Returns the publication which the current cursor is pointing to
	 * @param cursor current cursor
	 * @return publication which the current cursor is pointing to
	 */
	private Publication cursorToPublication(Cursor cursor){
		Publication pub = new Publication();
		pub.setId(cursor.getLong(0));
		pub.setTitle(cursor.getString(1));
		pub.setAbstract(cursor.getString(2));
		Timestamp timestamp = new Timestamp(cursor.getLong(3));
		pub.setLastModified(new Date(timestamp.getTime()));
		
		return pub;
	}
}
