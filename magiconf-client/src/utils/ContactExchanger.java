package utils;

import java.util.concurrent.Exchanger;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.magiconf_client.AuthorActivity;
import com.example.magiconf_client.DialogActivity;
import com.example.magiconf_client.MainMenu;
import com.example.magiconf_client.R;
import com.example.magiconf_client.StartContactExchangeActivity;

import dataRetrieving.AuthInfo;
import dataSources.ContactDataSource;
import dataSources.ParticipantContactDataSource;

import tables.Author;
import tables.Contact;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * @author Alexandre
 *
 * This class represents a contact exchanger
 */
public class ContactExchanger {

	public static final String USERNAME_KEY = "user_name";
	public static final String PASSWORD_KEY = "password";
	public static final String PIN_KEY = "pin";
	public static final String USERNAME_ADDING_KEY = "username_adding";
	public static final String CONTACT_KEY = "contact";
	public static final String NAME_KEY = "name";
	public static final String WORK_PLACE_KEY = "work_place";
	public static final String PHOTO_KEY = "photo";
	public static final String COUNTRY_KEY = "country";
	public static final String PHONE_NUMBER_KEY = "phone_number";
	public static final String EMAIL_KEY = "email";
	public static final String FACEBOOK_KEY = "facebook_url";
	public static final String LINKEDIN_KEY = "linkedin_url";
	public static final String REQUEST="request";

	//Keynote/Author contact Exchange
	public static final String TYPE_KEY = "type";
	public static final String TYPE_AUTHOR = "author";
	public static final String TYPE_KEYNOTE= "keynote";

	// This static variable will be used to known how much contacts have been exchanged in a contact exchange session
	public static int totalContactsExchanged = 0;
	
	public static boolean contactExchangeStarted = false;

	private ContactDataSource contactDB;
	private ParticipantContactDataSource participantContactsDB;
	private Context context;

	public ContactExchanger(Context context) {
		this.context = context;
		contactDB = new ContactDataSource(context);
		participantContactsDB = new ParticipantContactDataSource(context);
	}

	/**
	 * Parses a json and adds a contact to database
	 * @param contact contact in json
	 * @return parsed contact
	 */
	public Contact addContactToDatabase(JSONObject contact) {
		try {
			Log.i("COntact received", contact.toString());
			
			contactDB.open();
			participantContactsDB.open();
			JSONObject contactToAdd = APIResponses.getContact(context, contact);
			String name;
			name = contactToAdd.getString(NAME_KEY);

			String email = contactToAdd.getString(EMAIL_KEY);
			String workPlace = contactToAdd.getString(WORK_PLACE_KEY);
			String photo = contactToAdd.getString(PHOTO_KEY);
			if(photo.charAt(0) == '/')
				photo = photo.substring(1);
			Log.i("contact photo", photo);
			String phoneNumber = contactToAdd.getString(PHONE_NUMBER_KEY);
			String country = contactToAdd.getString(COUNTRY_KEY);
			String facebookUrl = contactToAdd.getString(FACEBOOK_KEY);
			String linkedinUrl = contactToAdd.getString(LINKEDIN_KEY);
			Contact c;
			if ((c = contactDB.getContact(email)) == null)
				c = contactDB.createContact(email, name, country, workPlace, phoneNumber, photo, facebookUrl, linkedinUrl);
			if (!participantContactsDB.hasParticipantContact(AuthInfo.username, c.getId())) {
				totalContactsExchanged++;
				participantContactsDB.createParticipantContact(AuthInfo.username, c.getId());
			}

			contactDB.close();
			participantContactsDB.close();
			Log.i("contact added", c.getName());
			return c;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Sends a contact exchange request to the specified email
	 * @param email of the other participant
	 * @return server response in json
	 */
	public JSONObject sendEmailExchangeRequest(String email) {
		String url = ValuesHelper.SERVER_API_ADDRESS + "/contact/add_email/";

		RestClient client = new RestClient();
		client.addPostParam(new BasicNameValuePair(USERNAME_KEY, AuthInfo.username));
		client.addPostParam(new BasicNameValuePair(PASSWORD_KEY, AuthInfo.password));
		client.addPostParam(new BasicNameValuePair(EMAIL_KEY, email));
		return client.makePostRequest(url);
	}
	
	/**
	 * Sends a contact exchange finish request
	 * @return response in json from server
	 */
	public JSONObject finishExchange() {
		String url = ValuesHelper.SERVER_API_ADDRESS + "/pin/remove/";
		RestClient client = new RestClient();
		client.addPostParam(new BasicNameValuePair(USERNAME_KEY, AuthInfo.username));
		client.addPostParam(new BasicNameValuePair(PASSWORD_KEY, AuthInfo.password));
		JSONObject response = client.makePostRequest(url);
		return response;
	}

	/**
	 * Sends a contact exchange request to the contact exchange session with the specified pin
	 * @param pin pin of the exchange session
	 * @return server response in json
	 */
	public JSONObject sendExchangeRequest(String pin) {
		String url = ValuesHelper.SERVER_API_ADDRESS + "/contact/add/";

		RestClient client = new RestClient();
		client.addPostParam(new BasicNameValuePair(USERNAME_KEY, AuthInfo.username));
		client.addPostParam(new BasicNameValuePair(PASSWORD_KEY, AuthInfo.password));
		client.addPostParam(new BasicNameValuePair(PIN_KEY, pin));
		return client.makePostRequest(url);
	}

	/**
	 * Allows a contact exchange
	 * @param usernameAdding username that is adding the contact
	 */
	public void allowExchange(final String usernameAdding) {
		new AsyncTask<Void, Void, JSONObject>() {

			@Override
			protected JSONObject doInBackground(Void... arg0) {
				String url = ValuesHelper.SERVER_API_ADDRESS + "/contact/allow_exchange/";

				RestClient client = new RestClient();
				client.addPostParam(new BasicNameValuePair(USERNAME_KEY, AuthInfo.username));
				client.addPostParam(new BasicNameValuePair(PASSWORD_KEY, AuthInfo.password));
				client.addPostParam(new BasicNameValuePair(USERNAME_ADDING_KEY, usernameAdding));
				JSONObject contact = client.makePostRequest(url);
				return contact;
			}

			@Override
			protected void onPostExecute(JSONObject result) {
				Intent intent = new Intent(context, DialogActivity.class);
				
				if (APIResponses.getKey(result).equals(APIResponses.KEY_EXCHANGE)) {
					intent.putExtra(DialogActivity.TITLE, context.getString(R.string.exchange_allowed_title));
					intent.putExtra(DialogActivity.TEXT, context.getString(R.string.exchange_allowed_text));
					intent.putExtra(DialogActivity.TYPE, DialogActivity.ALERT_DIALOG);
				}
				else {	
					Contact c;
					if ((c = addContactToDatabase(result)) != null) {
						intent.putExtra(DialogActivity.TITLE, context.getString(R.string.contact_added_title));
						intent.putExtra(DialogActivity.TEXT, c.getName() + " " + context.getString(R.string.contact_added_text));
						intent.putExtra(DialogActivity.TYPE, DialogActivity.ALERT_DIALOG);
					}
					else {
						intent.putExtra(DialogActivity.TITLE, context.getString(R.string.exchange_error_title));
						intent.putExtra(DialogActivity.TEXT, context.getString(R.string.exchange_error_text));
						intent.putExtra(DialogActivity.TYPE, DialogActivity.ALERT_DIALOG);
					}
				}
				context.startActivity(intent);
			}

		}.execute(null, null, null);

	}

	/**
	 * Retrieves a pin from the server
	 * @return pin in json
	 */
	public JSONObject getPin() {
		String url = ValuesHelper.SERVER_API_ADDRESS + "/pin/generate/";

		RestClient client = new RestClient();
		client.addPostParam(new BasicNameValuePair(USERNAME_KEY, AuthInfo.username));
		client.addPostParam(new BasicNameValuePair(PASSWORD_KEY, AuthInfo.password));
		JSONObject obj = client.makePostRequest(url);
		return obj;
	}
	
	
	/**
	 * Adds an author or keynote. If he is a participant send request
	 * @param email
	 * @param type
	 */
	public static void addAuthorOrKeynote(final String name,final String email,final String type,final Context context) {
		new AsyncTask<Void, Void, Triple<JSONObject,Context,String>>() {

			@Override
			protected Triple<JSONObject,Context,String> doInBackground(Void... arg0) {
				String url = ValuesHelper.SERVER_API_ADDRESS + "/contact/add_author_keynote/";

				RestClient client = new RestClient();
				client.addPostParam(new BasicNameValuePair(ContactExchanger.USERNAME_KEY, AuthInfo.username));
				client.addPostParam(new BasicNameValuePair(ContactExchanger.PASSWORD_KEY, AuthInfo.password));
				client.addPostParam(new BasicNameValuePair(ContactExchanger.EMAIL_KEY,email));
				client.addPostParam(new BasicNameValuePair(ContactExchanger.TYPE_KEY, type));
				JSONObject response = client.makePostRequest(url);
				
				return new Triple<JSONObject, Context, String>(response, context,name );
			}

			@Override
			protected void onPostExecute(Triple<JSONObject,Context,String> result) {

				String toShow = "";
				JSONObject obj = result.getFirst();
				Log.i("AUTH_REQUEST",obj.toString());
				try {
					if(obj.has("error")){
						String error;

						error = (String) obj.get("error");

						if(error.equals(APIResponses.DATA_ERROR_ALREADY_IN_CONTACTS))
							toShow = context.getString(R.string.error_already_in_contacts);
						else if(error.equals(APIResponses.DATA_ERROR_PARTICIPANT_NOT_FOUND))
							toShow = context.getString(R.string.error_participant_not_found);
						else if(error.equals(APIResponses.DATA_ERROR_ADDING_SELF))
							toShow = context.getString(R.string.error_adding_self);
						else if(error.equals(APIResponses.DATA_ERROR_SENDING_EMAIL))
							toShow = context.getString(R.string.error_sending_email);
					}
					else{
						if(obj.has(REQUEST)){
							String r = (String) obj.getString(REQUEST);
							if(r.endsWith(APIResponses.DATA_OK_SENT))
								toShow = result.getThird() + " "+context.getString(R.string.contact_added_text);
						}
						else
							toShow = context.getString(R.string.author_keynote_add_error);
					}

					//Toast.makeText(context, toShow, Toast.LENGTH_SHORT).show();

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}.execute(null, null, null);

	}


	

}
