package dataSources;

import java.util.ArrayList;
import java.util.List;

import com.example.magiconf_client.DbHelper;
import com.example.magiconf_client.R;

import tables.Member;
import utils.Triple;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * @Member José
 * 
 * Data source to the Member table
 */
public class MemberDataSource {
	
	private SQLiteDatabase database;
	private DbHelper dbHelper;
	private Context context;
	
	public MemberDataSource(Context context){
		dbHelper = new DbHelper(context);
		this.context=context;
	}
	
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}

	public void close(){
		dbHelper.close();
	}
	
	/**
	 * Returns the Member with the specified id
	 * @param id id of the Member to get
	 * @return Member with specified id
	 */
	public Member getMember(Long id) {
		Cursor cursor = database.query(DbHelper.TABLE_MEMBERS_ORG, null, DbHelper.COLUMN_ID + " = " + id, null, null, null, null);
		cursor.moveToFirst();
		Member m = cursorToMember(cursor);
		cursor.close();
		return m;
	}
	
	/**
	 * Inserts a new Member
	 * @param email email of the Member
	 * @param name name of the Member
	 * @param workplace work place of the Member
	 * @param country country of the Member
	 * @param photo photo url of the Member
	 * @param role role of the Member
	 * @return inserted Member
	 */
public Member createMember(String email, String name, String workplace, String country, String photo, String codRole){

		ContentValues values = new ContentValues();
			
		String role;
		
		if(codRole.equals("CC")){
			role = context.getString(R.string.COORD_COM);
		}else if(codRole.equals("CP")){
			role = context.getString(R.string.PROG_COM);
		} else role = context.getString(R.string.ORG_COM);
		
		values.put(DbHelper.MEMBER_EMAIL, email);
		values.put(DbHelper.MEMBER_NAME,name);
		values.put(DbHelper.MEMBER_WORKPLACE,workplace);
		values.put(DbHelper.MEMBER_COUNTRY,country);
		values.put(DbHelper.MEMBER_PHOTO,photo);
		values.put(DbHelper.MEMBER_ROLE,role);

		long insertId = database.insert(DbHelper.TABLE_MEMBERS_ORG, null, values);
			
		Cursor cursor = database.query(DbHelper.TABLE_MEMBERS_ORG, null, DbHelper.COLUMN_ID + " = " +insertId, null, null, null, null);
			
		cursor.moveToFirst();
		Member newMember = cursorToMember(cursor);
		cursor.close();

		return newMember;
	}
	
	/**
	 * Deletes the specified Member from the local database
	 * @param Member to delete
	 */
	public void deleteMember(Member Member){
		long id = Member.getId();
		database.delete(DbHelper.TABLE_MEMBERS_ORG, DbHelper.COLUMN_ID +" = "+ id, null);
	}
	
	/**
	 * Returns all the Members presents in the local database by role
	 * @return all Members (1. C.Coordenadora; 2. C.Organizadora; 3. C.Programa)
	 */
	public Triple<List<Member>,List<Member>,List<Member>> getAllMembersByRole(){
		List<Member> coord = new ArrayList<Member>();
		List<Member> org = new ArrayList<Member>();
		List<Member> prog = new ArrayList<Member>();
		Cursor cursor;
		cursor = database.query(DbHelper.TABLE_MEMBERS_ORG, null, null, null, null, null, DbHelper.MEMBER_NAME);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			Member m = cursorToMember(cursor);
			String role = m.getRole();
			if(role.equals(context.getString(R.string.COORD_COM))){
				coord.add(m);
			}else if(role.equals(context.getString(R.string.ORG_COM))){
				org.add(m);
			}else prog.add(m);
			
			cursor.moveToNext();
		}
		
		cursor.close();
		return new Triple<List<Member>,List<Member>,List<Member>>(coord,org,prog);
	
	}
	
	/**
	 * Returns all the Members in the local database from the CP
	 */
	public List<Member> getOnlyCPMembers(){
		List<Member> prog = new ArrayList<Member>();
		String whereClause = DbHelper.MEMBER_ROLE +"='"+context.getString(R.string.PROG_COM)+"'";
		Cursor cursor;
		cursor = database.query(DbHelper.TABLE_MEMBERS_ORG, null, whereClause, null, null, null, DbHelper.MEMBER_NAME);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			Member m = cursorToMember(cursor);
			prog.add(m);
			cursor.moveToNext();
		}
		
		cursor.close();
		return prog;
	
	}
	
	/**
	 * Returns all the Members in the local database from the CO
	 */
	public List<Member> getOnlyCOMembers(){
		List<Member> org = new ArrayList<Member>();
		String whereClause = DbHelper.MEMBER_ROLE +"='"+context.getString(R.string.ORG_COM)+"'";
		Cursor cursor;
		cursor = database.query(DbHelper.TABLE_MEMBERS_ORG, null, whereClause, null, null, null, DbHelper.MEMBER_NAME);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			Member m = cursorToMember(cursor);
			org.add(m);
			cursor.moveToNext();
		}
		
		cursor.close();
		return org;
	
	}
	
	/**
	 * Returns all the Members in the local database from the CC
	 */
	public List<Member> getOnlyCCMembers(){
		List<Member> coord = new ArrayList<Member>();
		String whereClause = DbHelper.MEMBER_ROLE +"='"+context.getString(R.string.COORD_COM)+"'";
		Cursor cursor;
		cursor = database.query(DbHelper.TABLE_MEMBERS_ORG, null, whereClause, null, null, null, DbHelper.MEMBER_NAME);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			Member m = cursorToMember(cursor);
			coord.add(m);
			cursor.moveToNext();
		}
		
		cursor.close();
		return coord;
	
	}
	
	
	
	
	
	/**
	 * Returns all the Members presents in the local database
	 * @return all Members
	 */
	public List<Member> getAllMembers(){
		List<Member> members = new ArrayList<Member>();
		Cursor cursor;
		cursor = database.query(DbHelper.TABLE_MEMBERS_ORG, null, null, null, null, null, DbHelper.MEMBER_NAME);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			Member m = cursorToMember(cursor);
			members.add(m);
			cursor.moveToNext();
		}
		
		cursor.close();
		return members;
	
	}
	
	/**
	 * Returns the Member which the current cursor is pointing to
	 * @param cursor current cursor
	 * @return Member which the current cursor is pointing to
	 */
	private Member cursorToMember(Cursor cursor){
		Member m = new Member();
		m.setId(cursor.getLong(0));
		m.setEmail(cursor.getString(1));
		m.setName(cursor.getString(2));
		m.setWork_place(cursor.getString(3));
		m.setCountry(cursor.getString(4));
		m.setPhoto(cursor.getString(5));
		m.setRole(cursor.getString(6));
		
		return m;
	}
	

	/**
	 * Returns if a member already exists in the local database
	 * @param email email that identifies the member
	 * @return true if member already exists, false otherwise
	 */
	public boolean hasMember(String email){
		
		String whereClause = DbHelper.MEMBER_EMAIL +" = '"+email+"'";
		Cursor cursor = database.query(DbHelper.TABLE_MEMBERS_ORG, null, whereClause, null, null, null, null);
		boolean hasMember = cursor.moveToFirst();
		cursor.close();
		return hasMember;
		
	}

	public Member getMemberByEmail(String email) {
		String whereClause = DbHelper.MEMBER_EMAIL +" = '"+email+"'";
		Cursor cursor = database.query(DbHelper.TABLE_MEMBERS_ORG, null, whereClause, null, null, null, null);
		Member m = null;
		if(cursor.moveToFirst()){
			m = cursorToMember(cursor);
		}
		cursor.close();
		return m;
	}
}
