package dataRetrieving;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.example.magiconf_client.ContactActivity;
import com.example.magiconf_client.ContactsActivity;
import com.example.magiconf_client.DialogActivity;
import com.example.magiconf_client.EditSocialNetworksActivity;
import com.example.magiconf_client.R;
import com.google.android.gms.internal.em;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import dataSources.ContactDataSource;
import dataSources.ParticipantContactDataSource;
import dataSources.ParticipantDataSource;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.util.Log;

import tables.Contact;
import tables.Participant;
import utils.APIResponses;
import utils.RestClient;
import utils.ValuesHelper;

public class ParticipantData {
	
	public static String RESOURCE_PARTICIPANT = "/participant_full/";
	public static String PARAM_NAME = "name=";
	public static final String EMAIL_REMOVE = "email_remove";
	public static final String USERNAME_KEY = "user_name";
	public static final String PASSWORD_KEY = "password";
	
	
	public static Participant downloadParticipantData(Context context,String participantUsername,String password,
			ParticipantDataSource participantsDB, ContactDataSource contactsDB,ParticipantContactDataSource participantContactDB,boolean contacts){
		
		/*RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(
				new MappingJacksonHttpMessageConverter());*/
		
		//TODO: http://192.168.1.33:8000/api/v1/participant_full/?format=json&user_name=joseandrade&password=662eaa47199461d01a623884080934ab
		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_PARTICIPANT + "?"+
				ValuesHelper.FormatJsonParam + "&"+
				ValuesHelper.UsernameParam + participantUsername+ "&"+
				ValuesHelper.PasswordParam + password;
		
		Log.i("PARTICIPANTDATA",request);
		
		
		JSONObject jData = RestClient.makeRequest(request,false,0);
		try {
			JSONArray aux =(JSONArray) jData.get("objects");
			JSONObject object = aux.getJSONObject(0);
			Participant participant = createNewParticipant(object,participantsDB,context);
			
			//download contacts
			if(contacts){
				addContacts(object,contactsDB,participantContactDB,participantUsername,password,context);
			}
			
			return participant;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*Participant participant = restTemplate.getForObject(request, Participant.class);
		
		participant = createNewParticipant(participant,participantsDB);*/
		
		return null;
	}
	
	private static void addContacts(JSONObject object,
			ContactDataSource contactsDB,ParticipantContactDataSource participantContactDB,
			String participantUsername,String password,Context c) {
		List<JSONObject> list = ContactData.downloadContacts(participantUsername, password);
		try {
			Iterator<JSONObject> it = list.iterator();
			JSONObject aux = null;
			while(it.hasNext()){
				Log.i("POST_EXECUTE", "On postExecute");
				aux =  it.next();
				JSONArray data = aux.getJSONArray("objects");
				createContacts(participantUsername,data,contactsDB,participantContactDB,c);
				
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void createContacts(String participantUsername,JSONArray data, 
			ContactDataSource contactsDB,ParticipantContactDataSource participantContactDB, Context context) {
		Contact aux;
		Log.i("CREATE CONTACT", "Start adding");
		contactsDB.open();
		participantContactDB.open();
		Log.i("CREATE CONTACT", ""+data.length());
		for(int i = 0 ; i < data.length() ; i++){
			try {
				JSONObject k = data.getJSONObject(i);
				String email= k.getString("email");
				String username = k.getString("username");
				if(!username.equals(participantUsername)){//Ignore participant who makes the request
					Contact c = contactsDB.getContact(email);
					if(c == null){
						String photo = k.getString("photo").substring(1);
						aux = contactsDB.createContact(email, k.getString("name"), k.getString("country"), k.getString("work_place"),k.getString("phone_number"),photo,k.getString("facebook_url"),k.getString("linkedin_url"));			
						Log.i("CREATE CONTACT", "Creating Contact"+aux.toString());
						UrlImageViewHelper.setUrlDrawable(context, ValuesHelper.SERVER_API_ADDRESS_MEDIA +photo,null, UrlImageViewHelper.CACHE_DURATION_INFINITE);
						participantContactDB.createParticipantContact(participantUsername, aux.getId());
					}
					else {
						participantContactDB.createParticipantContact(participantUsername, c.getId());
					}
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		contactsDB.close();
		participantContactDB.close();
	}

	private static Participant createNewParticipant(Participant result,ParticipantDataSource participantsDB) {
		participantsDB.open();
		Log.i("CHECK-IN","Creating entry");
		Log.i("CHECK-IN","QRCODE Result: "+result.getQrcode().substring(1));
		Participant newParticipant = participantsDB.createParticipant(result.getCountry(),
				result.getUsername(), result.getName(), result.getPhone_number(),
				result.getEmail(), result.getPhoto().substring(1), result.getWork_place(), result.getQrcode().substring(1), result.getLinkedinUrl(), result.getFacebookUrl());
		
		participantsDB.close();
		return newParticipant;
	}
	
	/**
	 * "objects": [
    {
      "contact": {
        "id": 1
      }, 
      "contacts": [], 
      "country": "Portugal", 
      "email": "joseandrade@emails.com", 
      "name": "José Andrade", 
      "phone_number": 123456789, 
      "photo": "/photos/user_photo/15094629-buddhist-monk-cartoon--vector.jpg", 
      "qrcode": "/photos/qrcodes/QR_joseandrade.jpg", 
      "username": "joseandrade", 
      "work_place": "FCT"
    }
  ]
	 */
	/**
	 * 
	 * @param object
	 * @param participantsDB
	 * @return
	 */
	private static Participant createNewParticipant(JSONObject object ,ParticipantDataSource participantsDB,
			Context c) {
		participantsDB.open();
		Log.i("CHECK-IN","Creating entry");
		Participant newParticipant = null;
		try {
			Log.i("CHECK-IN","QRCODE Result: "+object.getString("qrcode").substring(1));
			
			newParticipant = participantsDB.createParticipant(object.getString("country"),object.getString("username"),object.getString("name"),
					object.getString("phone_number"),object.getString("email"),object.getString("photo").substring(1),object.getString("work_place"),
					object.getString("qrcode").substring(1),object.getString("linkedin_url"),object.getString("facebook_url"));
			UrlImageViewHelper.setUrlDrawable(c, ValuesHelper.SERVER_API_ADDRESS_MEDIA +newParticipant.getPhoto(),null, UrlImageViewHelper.CACHE_DURATION_INFINITE);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				

		participantsDB.close();
		return newParticipant;
	}


	
	public static void deleteContact(final Context context, final Contact contact){

		
		new AsyncTask<Void, Void, JSONObject>() {

			@Override
			protected JSONObject doInBackground(Void... arg0) {
				String url = ValuesHelper.SERVER_API_ADDRESS + "/contact/remove/";
				
				RestClient client = new RestClient();
				client.addPostParam(new BasicNameValuePair(USERNAME_KEY, AuthInfo.username));
				client.addPostParam(new BasicNameValuePair(PASSWORD_KEY, AuthInfo.password));
				client.addPostParam(new BasicNameValuePair(EMAIL_REMOVE, contact.getEmail()));
				JSONObject res = client.makePostRequest(url);
				return res;
			}

			@Override
			protected void onPostExecute(JSONObject result) {
				final Intent intent = new Intent(context, ContactsActivity.class);
				
				if (APIResponses.getKey(result).equals(APIResponses.KEY_REMOVE)) {
					Log.i("REMOVIDO","Removido");
					ParticipantContactDataSource participantDB = new ParticipantContactDataSource(context);
					participantDB.open();
					participantDB.deleteParticipantContact(AuthInfo.username,contact.getId());		
					participantDB.close();
					final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
					alertDialog.setTitle(context.getString(R.string.remove_contact_title));
					alertDialog.setMessage(context.getString(R.string.remove_contact_text));
					alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, context.getString(R.string.ok_button), new OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							context.startActivity(intent);
						}
					});
					alertDialog.show();
					
					
				}
				else {	
				
					final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
					alertDialog.setTitle(context.getString(R.string.remove_error_title));
					alertDialog.setMessage(context.getString(R.string.remove_error_text));
					alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, context.getString(R.string.ok_button), new OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							context.startActivity(intent);
							
							
						}
					});
					alertDialog.show();
					
					
					
				}

			}

		}.execute(null, null, null);
		
	
	}

}
