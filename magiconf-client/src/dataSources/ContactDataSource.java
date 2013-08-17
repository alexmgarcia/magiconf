package dataSources;

import java.util.ArrayList;
import java.util.List;
import tables.Author;
import tables.Contact;
import tables.Keynote;
import utils.Triple;

import com.example.magiconf_client.DbHelper;

import dataRetrieving.AuthInfo;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


/**
 * Data source to the Contact table
 */
public class ContactDataSource {
	
	private SQLiteDatabase database;
	private DbHelper dbHelper;
	
	public ContactDataSource(Context context){
		dbHelper = new DbHelper(context);
	}
	
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}

	public void close(){
		dbHelper.close();
	}
	
	/**
	 * Returns the contact with the specified id
	 * @param id id of the contact to get
	 * @return contact with specified id
	 */
	 
	public Contact getContact(Long id) {
		
		Cursor cursor = database.query(DbHelper.TABLE_CONTACTS, null, DbHelper.COLUMN_ID + " = " + id, null, null, null, null);
		boolean cursorExist=cursor.moveToFirst();
		Log.i("existence"," " + cursorExist);
		Contact contact = cursorToContact(cursor);
		cursor.close();
		return contact;
	}
	
	/**
	 * Returns the contact with the specified email
	 * @param email email of the contact to get
	 * @return contact with specified email; null if there is no contact with the specified email
	 */
	 
	public Contact getContact(String email) {
		String whereClause = DbHelper.CONTACT_EMAIL + " = '" + email + "'";
		Cursor cursor = database.query(DbHelper.TABLE_CONTACTS, null, whereClause, null, null, null, null);
		Contact contact = null;
		if (cursor.moveToFirst())
			contact = cursorToContact(cursor);
		cursor.close();
		return contact;
	}
	
	
	/**
	 * Returns all the contacts sorted by group
	 * @return all contacts sorted by group (1.Authors, 2.Keynotes, 3.Others)
	 */ 
	public Triple<List<Author>,List<Keynote>,List<Contact>> getAllContactsSortedByGroup() {
		List<Author> authors = new ArrayList<Author>();
		List<Keynote> keynotes = new ArrayList<Keynote>();
		List<Contact> others = new ArrayList<Contact>();
			
		String whereClause = DbHelper.RELATIONSHIP_PARTICIPANT_AUTHOR_PARTICIPANT_USERNAME +"='"+AuthInfo.username+"'";
		Cursor cursor = database.query(DbHelper.TABLE_RELATIONSHIP_PARTICIPANT_AUTHOR, null, whereClause, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			long id = cursor.getLong(1);
			whereClause = DbHelper.COLUMN_ID +"="+id;
			Cursor c = database.query(DbHelper.TABLE_AUTHORS, null, whereClause, null, null, null, null);
			c.moveToFirst();
			Author a = cursorToAuthor(c);
			authors.add(a);
			c.close();
			cursor.moveToNext();
		}
		
		whereClause = DbHelper.RELATIONSHIP_PARTICIPANT_KEYNOTE_PARTICIPANT_USERNAME +"='"+AuthInfo.username+"'";
		cursor = database.query(DbHelper.TABLE_RELATIONSHIP_PARTICIPANT_KEYNOTE, null, whereClause, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			long id = cursor.getLong(1);
			whereClause = DbHelper.COLUMN_ID +"="+id;
			Cursor c = database.query(DbHelper.TABLE_KEYNOTES, null, whereClause, null, null, null, null);
			c.moveToFirst();
			Keynote a = cursorToKeynote(c);
			keynotes.add(a);
			c.close();
			cursor.moveToNext();
		}
		
		whereClause = DbHelper.RELATIONSHIP_PARTICIPANT_CONTACT_PARTICIPANT_USERNAME +"='"+AuthInfo.username+"'";
		cursor = database.query(DbHelper.TABLE_RELATIONSHIP_PARTICIPANT_CONTACT, null, whereClause, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			long id = cursor.getLong(1);
			whereClause = DbHelper.COLUMN_ID +"="+id;
			Cursor c = database.query(DbHelper.TABLE_CONTACTS, null, whereClause, null, null, null, null);
			c.moveToFirst();
			Contact cont = cursorToContact(c);
			others.add(cont);
			c.close();
			cursor.moveToNext();
		}
		
		cursor.close();
		return new Triple<List<Author>,List<Keynote>,List<Contact>>(authors, keynotes,others);
	}
	
	/**
	 * Inserts a new contact
	 * @param email email of the contact
	 * @param name name of the contact
	 * @param workplace work place of the contact
	 * @param country country of the contact
	 * @param photo photo url of the contact
	 * @param phoneNumber phoneNumber of the contact
	 * @param facebookUrl facebook url of the contact
	 * @param linkedinUrl linkedin url of the contact
	 * @return inserted contact
	 */
public Contact createContact(String email, String name, String country,String workplace,String phoneNumber, String photo, String facebookUrl, String linkedinUrl){
	ContentValues values = new ContentValues();
	
	values.put(DbHelper.CONTACT_EMAIL, email);
	values.put(DbHelper.CONTACT_NAME, name);
	values.put(DbHelper.CONTACT_COUNTRY, country);
	values.put(DbHelper.CONTACT_WORKPLACE, workplace);
	values.put(DbHelper.CONTACT_PHONE, phoneNumber);
	values.put(DbHelper.CONTACT_PHOTO, photo);
	values.put(DbHelper.CONTACT_FACEBOOK, facebookUrl);
	values.put(DbHelper.CONTACT_LINKEDIN, linkedinUrl);
	long insertId = database.insert(DbHelper.TABLE_CONTACTS, null, values);
	
	Cursor cursor = database.query(DbHelper.TABLE_CONTACTS, null, DbHelper.COLUMN_ID + " = " +insertId, null, null, null, null);
	cursor.moveToFirst();
	Contact newContact = cursorToContact(cursor);
	
	/*
	String whereClause = DbHelper.AUTHOR_EMAIL +"='"+email+"'";
	
	cursor = database.query(DbHelper.TABLE_AUTHORS, null, whereClause, null, null, null, null);
	if(cursor.moveToFirst()){
		setContactIDAuthor(cursor, insertId);

	}else{
		whereClause = DbHelper.KEYNOTE_EMAIL + "='"+email+"'";
		cursor = database.query(DbHelper.TABLE_KEYNOTES, null, whereClause, null, null, null, null);
		if(cursor.moveToFirst()){
			setContactIDKeynote(cursor,insertId);
		}
	}
*/
	cursor.close();
	return newContact;

	}
	
	/**
	 * Deletes the specified contact from the local database
	 * @param contact to delete
	 **/
	public void deleteContact(Contact contact){
		long id = contact.getId();
		
		String whereClause = DbHelper.AUTHOR_CONTACT_ID +"="+id;
		
		Cursor cursor = database.query(DbHelper.TABLE_AUTHORS, null, whereClause, null, null, null, null);
		if(cursor.moveToFirst()){
			deleteContactIDAuthor(cursor);
		}else{
			whereClause = DbHelper.KEYNOTE_CONTACT_ID +"="+id;
			cursor = database.query(DbHelper.TABLE_KEYNOTES, null, whereClause, null, null, null, null);
			if(cursor.moveToFirst()){
				deleteContactIDKeynote(cursor);
			}
		}
	
		database.delete(DbHelper.TABLE_CONTACTS, DbHelper.COLUMN_ID +" = "+ id, null);
	
	}
	
	public void updateContactByEmail(String email, String name, String country,String workplace,String phoneNumber, String photo, String facebookUrl, String linkedinUrl) {
		
		String whereClause = DbHelper.CONTACT_EMAIL + " = '" + email + "'";
		ContentValues values = new ContentValues();
		
		//values.put(DbHelper.CONTACT_EMAIL, email);
		values.put(DbHelper.CONTACT_NAME, name);
		values.put(DbHelper.CONTACT_COUNTRY, country);
		values.put(DbHelper.CONTACT_WORKPLACE, workplace);
		values.put(DbHelper.CONTACT_PHONE, phoneNumber);
		values.put(DbHelper.CONTACT_PHOTO, photo);
		values.put(DbHelper.CONTACT_FACEBOOK, facebookUrl);
		values.put(DbHelper.CONTACT_LINKEDIN, linkedinUrl);
		database.update(DbHelper.TABLE_CONTACTS, values, whereClause, null);
	}
	
	/**
	 * Returns all the contacts present in the local database
	 * @return all contact
	 */
	public List<Contact> getAllContacts(boolean sort){
		List<Contact> contacts = new ArrayList<Contact>();
		Cursor cursor;
		if(sort)
			cursor = database.query(DbHelper.TABLE_CONTACTS, null, null, null, null, null, DbHelper.CONTACT_NAME);
		
		else
			cursor = database.query(DbHelper.TABLE_CONTACTS, null, null, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			Contact contact = cursorToContact(cursor);
			contacts.add(contact);
			cursor.moveToNext();
		}
		
		cursor.close();
		return contacts;
	
	}
	
	
	/**
	 * Returns the contact which the current cursor is pointing to
	 * @param cursor current cursor
	 * @return author which the current cursor is pointing to
	**/ 
	private Contact cursorToContact(Cursor cursor){
		Contact contact = new Contact();
		contact.setId(cursor.getLong(0));
		contact.setName(cursor.getString(1));
		contact.setEmail(cursor.getString(2));
		contact.setCountry(cursor.getString(3));
		contact.setWorkPlace(cursor.getString(4));
		contact.setPhoneNumber(cursor.getString(5));
		contact.setPhoto(cursor.getString(6));
		contact.setFacebookUrl(cursor.getString(7));
		contact.setLinkedinUrl(cursor.getString(8));

		return contact;
	}
	
	/**
	 * Returns true if the contact already exists in the local database
	 * @param email from the contact
	 * @return true if the contact already exists, false otherwise
	**/ 
	public boolean hasContact(String email){
		
		String whereClause = DbHelper.CONTACT_EMAIL +" = '"+email+"'";
		Cursor c = database.query(DbHelper.TABLE_CONTACTS, null, whereClause, null, null, null, null);
		boolean result = c.moveToFirst();
		c.close();
		return result;
	}
	


	/**
	 * Sets contact_id value of one author
	 * @param cursor cursor that points to the author
	 * @param contactID id of the contact of the author
	**/
	private void setContactIDAuthor(Cursor cursor, long contactID){
		
		Author a = cursorToAuthor(cursor);		
		String whereClause = DbHelper.COLUMN_ID +"="+a.getId();
		ContentValues values = new ContentValues();
		values.put(DbHelper.AUTHOR_CONTACT_ID, contactID);
		
		database.update(DbHelper.TABLE_AUTHORS, values, whereClause, null);
	}
	
	/**
	 * Sets contact_id value of one keynote
	 * @param cursor cursor that points to the keynote
	 * @param contactID id of the contact of the keynote
	**/
	private void setContactIDKeynote(Cursor cursor, long contactID){
		
		Keynote k = cursorToKeynote(cursor);
		
		String whereClause = DbHelper.COLUMN_ID +"="+k.getId();
		ContentValues values = new ContentValues();
		values.put(DbHelper.KEYNOTE_CONTACT_ID, contactID);
		
		database.update(DbHelper.TABLE_KEYNOTES, values, whereClause, null);
		
	}
	
	/**
	 * Sets contact_id value of one author to null
	 * @param cursor cursor that points to the author
	**/
	private void deleteContactIDAuthor(Cursor cursor){
		
		Author a = cursorToAuthor(cursor);		
		String whereClause = DbHelper.COLUMN_ID +"="+a.getId();
		ContentValues values = new ContentValues();
		values.put(DbHelper.AUTHOR_CONTACT_ID, "");
		
		database.update(DbHelper.TABLE_AUTHORS, values, whereClause, null);
	}
	
	
	/**
	 * Sets contact_id value of one keynote to null
	 * @param cursor cursor that points to the keynote
	**/
	private void deleteContactIDKeynote(Cursor cursor){
		Keynote k = cursorToKeynote(cursor);
		
		String whereClause = DbHelper.COLUMN_ID +"="+k.getId();
		ContentValues values = new ContentValues();
		values.put(DbHelper.KEYNOTE_CONTACT_ID, "");
		
		database.update(DbHelper.TABLE_KEYNOTES, values, whereClause, null);
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
	 * Returns the keynote speaker which the current cursor is pointing to
	 * @param cursor current cursor
	 * @return keynote speaker which the current cursor is pointing to
	 */
	private Keynote cursorToKeynote(Cursor cursor){
		Keynote keynote = new Keynote();
		keynote.setId(cursor.getLong(0));
		keynote.setEmail(cursor.getString(1));
		keynote.setName(cursor.getString(2));
		keynote.setWork_place(cursor.getString(3));
		keynote.setCountry(cursor.getString(4));
		keynote.setPhoto(cursor.getString(5));
		
		return keynote;
	}
	
	
	/*public List<Contact> searchAll(String query){
		
		String lowerQuery = query.toLowerCase();
		String convertedName = "%"+lowerQuery.toLowerCase()+"%";
		String whereClauseQuery = "lower("+DbHelper.CONTACT_EMAIL+ ")='"+lowerQuery+"' OR lower("+DbHelper.CONTACT_NAME+") like '"+convertedName+"'"; 
		
		List<Contact> result = new ArrayList<Contact>();
		
		Cursor cursor = database.query(DbHelper.TABLE_CONTACTS, null, whereClauseQuery, null, null, null, DbHelper.CONTACT_NAME);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			Contact contact = cursorToContact(cursor);
			result.add(contact);
			cursor.moveToNext();
		}
		
		cursor.close();
		return result;
		
	}*/
	
	
	public Triple<List<Author>,List<Keynote>,List<Contact>> search(String query){
		
		String lowerQuery = query.toLowerCase();

		List<Author> authors = new ArrayList<Author>();
		List<Keynote> keynotes = new ArrayList<Keynote>();
		List<Contact> others = new ArrayList<Contact>();
		
		
		
		String whereClause = DbHelper.RELATIONSHIP_PARTICIPANT_CONTACT_PARTICIPANT_USERNAME +"='"+AuthInfo.username+"'";
		Cursor cursor = database.query(DbHelper.TABLE_RELATIONSHIP_PARTICIPANT_CONTACT, null, whereClause, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			long id = cursor.getLong(1);
			whereClause = DbHelper.COLUMN_ID +"="+id;
			Cursor c = database.query(DbHelper.TABLE_CONTACTS, null, whereClause, null, null, null, null);
			c.moveToFirst();
			Contact a = cursorToContact(c);
			boolean sameEmail = (a.getEmail().toLowerCase() == lowerQuery);
			boolean sameName = (a.getName().toLowerCase().contains(lowerQuery));
			
			if(sameEmail || sameName){
				others.add(a);
			}
			c.close();
			cursor.moveToNext();
		}
		
		whereClause = DbHelper.RELATIONSHIP_PARTICIPANT_AUTHOR_PARTICIPANT_USERNAME +"='"+AuthInfo.username+"'";
		cursor = database.query(DbHelper.TABLE_RELATIONSHIP_PARTICIPANT_AUTHOR, null, whereClause, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			long id = cursor.getLong(1);
			whereClause = DbHelper.COLUMN_ID +"="+id;
			Cursor c = database.query(DbHelper.TABLE_AUTHORS, null, whereClause, null, null, null, null);
			c.moveToFirst();
			Author a = cursorToAuthor(c);
			boolean sameEmail = (a.getEmail().toLowerCase() == lowerQuery);
			boolean sameName = (a.getName().toLowerCase().contains(lowerQuery));
			
			if(sameEmail || sameName){
				authors.add(a);
			}
			c.close();
			cursor.moveToNext();
		}
		
		whereClause = DbHelper.RELATIONSHIP_PARTICIPANT_KEYNOTE_PARTICIPANT_USERNAME +"='"+AuthInfo.username+"'";
		cursor = database.query(DbHelper.TABLE_RELATIONSHIP_PARTICIPANT_KEYNOTE, null, whereClause, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			long id = cursor.getLong(1);
			whereClause = DbHelper.COLUMN_ID +"="+id;
			Cursor c = database.query(DbHelper.TABLE_KEYNOTES, null, whereClause, null, null, null, null);
			c.moveToFirst();
			Keynote k = cursorToKeynote(c);
			boolean sameEmail = (k.getEmail().toLowerCase() == lowerQuery);
			boolean sameName = (k.getName().toLowerCase().contains(lowerQuery));
			
			if(sameEmail || sameName){
				keynotes.add(k);
			}
			c.close();
			cursor.moveToNext();
		}
		
		cursor.close();
		return new Triple<List<Author>,List<Keynote>,List<Contact>>(authors, keynotes,others);
		
	}
	
		
}