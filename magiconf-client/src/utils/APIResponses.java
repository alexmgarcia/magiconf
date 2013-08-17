package utils;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.magiconf_client.R;

import android.content.Context;

/**
 * @author Alexandre
 * 
 * API Responses
 *
 */
public class APIResponses {
	// JSON Keys
	public static final String KEY_ERROR = "error";
	public static final String KEY_CONTACT = "contact";
	public static final String KEY_REQUEST = "request";
	public static final String KEY_PIN = "pin";
	public static final String KEY_EMAIL = "email";
	public static final String KEY_EXCHANGE = "exchange";
	public static final String KEY_DATA = "data";
	public static final String KEY_REMOVE = "removed";
	
	// JSON Data
	public static final String DATA_ERROR_PIN_NOT_FOUND = "Pin not found";
	public static final String DATA_ERROR_PARTICIPANT_NOT_FOUND = "Participant not found";
	public static final String DATA_ERROR_ADDING_SELF = "Adding yourself";
	public static final String DATA_ERROR_ALREADY_IN_CONTACTS = "Already in contacts";
	public static final String DATA_ERROR_NOT_IN_CONTACTS = "Not in contacts";
	public static final String DATA_ERROR_EMAIL_NOT_FOUND = "Email not found";
	public static final String DATA_ERROR_ALREADY_SENT_REQUEST = "Already sent request";
	public static final String DATA_ERROR_SENDING_EMAIL = "Error while sending email";
	public static final String DATA_ERROR_NO_DATA_TO_EDIT = "No data to edit";
	public static final String DATA_OK_SENT = "sent";
	public static final String DATA_PIN_REMOVED = "removed";
	public static final String DATA_EXCHANGE_ALLOWED = "allowed";

	/**
	 * Returns the key of a response
	 * @param jsonObject response to get key
	 * @return key of response
	 */
	public static String getKey(JSONObject jsonObject) {
		if (jsonObject.has(KEY_ERROR))
			return KEY_ERROR;
		else if (jsonObject.has(KEY_CONTACT))
			return KEY_CONTACT;
		else if (jsonObject.has(KEY_PIN))
			return KEY_PIN;
		else if (jsonObject.has(KEY_EXCHANGE))
			return KEY_EXCHANGE;
		else if (jsonObject.has(KEY_DATA))
			return KEY_DATA;
		else if (jsonObject.has(KEY_REMOVE))
			return KEY_REMOVE;
		else return KEY_REQUEST;
	}
	
	/**
	 * Returns a contact in json
	 * @param context context
	 * @param jsonObject json to get contact from
	 * @return contact in json
	 */
	public static JSONObject getContact(Context context, JSONObject jsonObject) {
		try {
			return jsonObject.getJSONObject(KEY_CONTACT);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns a error message that was received in a json response
	 * @param context context
	 * @param jsonObject json to get error message from
	 * @return error message of a json response
	 */
	public static String getErrorMsg(Context context, JSONObject jsonObject) {
		String showMsg = "";
		String responseMsg;
		try {
			responseMsg = (String) jsonObject.get(KEY_ERROR);
			if (responseMsg.equals(DATA_ERROR_ADDING_SELF))
				showMsg = context.getString(R.string.error_adding_self);
			else if (responseMsg.equals(DATA_ERROR_ALREADY_IN_CONTACTS))
				showMsg = context.getString(R.string.error_already_in_contacts);
			else if (responseMsg.equals(DATA_ERROR_NOT_IN_CONTACTS))
				showMsg = context.getString(R.string.error_not_in_contacts);
			else if (responseMsg.equals(DATA_ERROR_PARTICIPANT_NOT_FOUND))
				showMsg = context
						.getString(R.string.error_participant_not_found);
			else if (responseMsg.equals(DATA_ERROR_PIN_NOT_FOUND))
				showMsg = context.getString(R.string.error_pin_not_found);
			//add errors from adding
			else if (responseMsg.equals(DATA_ERROR_ALREADY_SENT_REQUEST))
				showMsg = context.getString(R.string.error_already_sent_request);
			else if (responseMsg.equals(DATA_ERROR_EMAIL_NOT_FOUND))
				showMsg = context.getString(R.string.error_email_not_found);
			else if (responseMsg.equals(DATA_ERROR_SENDING_EMAIL))
				showMsg = context.getString(R.string.error_sending_email);
			else if (responseMsg.equals(DATA_ERROR_NO_DATA_TO_EDIT))
				showMsg = context.getString(R.string.error_editing_profile);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return showMsg;
	}

}
