package dataRetrieving;

import java.sql.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import dataSources.ContactDataSource;
import dataSources.SightsDataSource;
import tables.Contact;
import utils.RestClient;
import utils.ValuesHelper;
import android.content.Context;
import android.util.Log;


public class ContactData {

	//TODO: Endereço correcto?

	public static String RESOURCE_CONTACT = "/participant/";

	//http://192.168.1.33:8000/api/v1/participant/?format=json&user_name=joseandrade&password=662eaa47199461d01a623884080934ab
	/*

	public static Contact downloadContactData(Long id,String parcitipantUsername,String password,
			ContactDataSource contactsDB){

		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_CONTACT  + "?"+
				ValuesHelper.FormatJsonParam + "&"+
				ValuesHelper.UsernameParam + parcitipantUsername+ "&"+
				ValuesHelper.PasswordParam + password;

		Log.i("CONTACTDATA",request);


		JSONObject jData = RestClient.makeRequest(request,false,0);
		Contact aux=null;
		contactsDB.open();
		try {
			//We will always get One or none! Filter applied is the resource PK
			if(jData.length() > 0){
				contactsDB.open();
				JSONObject k = jData;

				Log.i("CONTACT","JSON: "+k.toString());

				String email= k.getString("email");
				if(!contactsDB.hasContact(email)){
					aux = contactsDB.createContact(email, k.getString("name"), k.getString("country"), k.getString("work_place"),k.getString("phone_number"),k.getString("photo").substring(1),k.getString("facebook_url"),k.getString("linkedin_url"));			
					Log.i("CREATE CONTACT", "Creating Contact"+aux.toString());
				}
				contactsDB.close();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		return aux;
	}	
	 */

	public static List<JSONObject> downloadContacts(String parcitipantUsername,String password){
		List<JSONObject> list = new LinkedList<JSONObject>();

		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_CONTACT + "?"+
				ValuesHelper.FormatJsonParam + "&"+
				ValuesHelper.UsernameParam + parcitipantUsername+ "&"+
				ValuesHelper.PasswordParam + password;

		JSONObject jData = RestClient.makeRequest(request,false,0);
		int total = 0;
		int limit= 0;
		try {
			JSONObject aux =(JSONObject) jData.get("meta");
			total = aux.getInt("total_count");
			limit = aux.getInt("limit");

			int offset = 0;
			list.add(jData);
			if(limit < total){
				offset +=limit;
				while(offset < total ){
					jData = RestClient.makeRequest(request,true,offset);
					offset+=limit;
					list.add(jData);
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		Log.i("CONTACTS", "Number requests: "+list.size());
		return list;
	}


	public static void createContacts(JSONArray data, ContactDataSource contactsDB) {
		Contact aux;
		Log.i("CREATE CONTACT", "Start adding");
		contactsDB.open();
		Log.i("CREATE CONTACT", ""+data.length());
		for(int i = 0 ; i < data.length() ; i++){
			try {
				JSONObject k = data.getJSONObject(i);
				String email= k.getString("email");
				if(!contactsDB.hasContact(email)){
					aux = contactsDB.createContact(email, k.getString("name"), k.getString("country"), k.getString("work_place"),k.getString("phone_number"),k.getString("photo").substring(1),k.getString("facebook_url"),k.getString("linkedin_url"));			
					Log.i("CREATE CONTACT", "Creating Contact"+aux.toString());
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		contactsDB.close();
	}

	public static void downloadAllContacts(String parcitipantUsername,String password,
			ContactDataSource contactDB){
		List<JSONObject> list = getAllContacts(parcitipantUsername, password, contactDB);
		Iterator<JSONObject> it = list.iterator();
		try {
			contactDB.open();
			while(it.hasNext()){
				JSONObject obj = it.next();
				JSONArray data;

				data = obj.getJSONArray("objects");

				createContacts(data,contactDB);

			}	
			contactDB.close();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	private static List<JSONObject> getAllContacts(String parcitipantUsername,String password,
			ContactDataSource contactDB){
		List<JSONObject> list = new LinkedList<JSONObject>();

		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_CONTACT  + "?"+
				ValuesHelper.FormatJsonParam + "&"+
				ValuesHelper.UsernameParam + parcitipantUsername+ "&"+
				ValuesHelper.PasswordParam + password;

		JSONObject jData = RestClient.makeRequest(request,false,0);

		int total = 0;
		int limit= 0;
		try {
			JSONObject aux =(JSONObject) jData.get("meta");
			total = aux.getInt("total_count");
			limit = aux.getInt("limit");

			int offset = 0;
			list.add(jData);
			if(limit < total){
				offset +=limit;
				while(offset < total ){
					jData = RestClient.makeRequest(request,true,offset);
					offset+=limit;
					list.add(jData);
				}
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Log.i("CONTACT", "Number requests: "+list.size());
		return list;

	}

	public static void checkForContactUpdates(String parcitipantUsername,String password,Date lastCheck, 
			ContactDataSource contactDB,Context context){
		List<JSONObject> list = downloadUpdatedContacts(parcitipantUsername, password,lastCheck, contactDB);
		Iterator<JSONObject> it = list.iterator();
		try {
			contactDB.open();
			while(it.hasNext()){
				JSONObject obj = it.next();
				JSONArray data;

				data = obj.getJSONArray("objects");

				updateContacts(data,contactDB,context);

			}	
			contactDB.close();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	public static void updateContacts(JSONArray data, ContactDataSource contactsDB,Context context) {
		Contact aux;
		Log.i("CREATE CONTACT", "Start adding");
		contactsDB.open();
		Log.i("CREATE CONTACT", ""+data.length());
		for(int i = 0 ; i < data.length() ; i++){
			try {
				JSONObject k = data.getJSONObject(i);
				String email= k.getString("email");
				String photo = k.getString("photo").substring(1);
				
				contactsDB.updateContactByEmail(email, k.getString("name"), k.getString("country"), k.getString("work_place"),k.getString("phone_number"),photo,k.getString("facebook_url"),k.getString("linkedin_url"));
				UrlImageViewHelper.setUrlDrawable(context, ValuesHelper.SERVER_API_ADDRESS_MEDIA +photo,null, UrlImageViewHelper.CACHE_DURATION_INFINITE);	
				Log.i("UPDATE CONTACT", "Updating Contact"+k.getString("name"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		contactsDB.close();
	}


	public static List<JSONObject> downloadUpdatedContacts(String username, String password,
			Date lastCheck, ContactDataSource contactDB) {
		List<JSONObject> list = new LinkedList<JSONObject>();

		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_CONTACT + "?"+
				ValuesHelper.FormatJsonParam + "&"+
				ValuesHelper.UsernameParam + username+ "&"+
				ValuesHelper.PasswordParam + password+ "&"+
				ValuesHelper.LASTMODIFIED_GT_PARAM + lastCheck;

		Log.i("CONTACT UPDATE", request);


		JSONObject jData = RestClient.makeRequest(request,false,0);
		int total = 0;
		int limit= 0;
		try {
			JSONObject aux =(JSONObject) jData.get("meta");
			total = aux.getInt("total_count");
			limit = aux.getInt("limit");

			int offset = 0;
			list.add(jData);

			if(limit < total){
				offset +=limit;
				while(offset < total ){
					jData = RestClient.makeRequest(request,true,offset);
					offset+=limit;
					list.add(jData);
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		Log.i("CONTACTS", "Number requests: "+list.size());
		return list;

	}


}
