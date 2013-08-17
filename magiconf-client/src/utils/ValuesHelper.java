
package utils;

import java.util.Locale;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.TypedValue;

/**
 * @author Alexandre
 * 
 * This class represents common values between the activities
 */
public class ValuesHelper {
	
	//OUR VM IP: 193.136.122.135
	
	public final static String SERVER_GCM_ADDRESS = "http://192.168.1.77:8000/gcm/v1";

	public final static String SERVER_API_ADDRESS="http://192.168.1.77:8000/api/v1";

	public final static String SERVER_API_ADDRESS_MEDIA="http://192.168.1.77:8000/";

	public final static String SERVER_VALIDATE_USER="validate_user";
	public final static String HOUR_FORMAT = "HH:mm";
	public final static String DATE_FORMAT = "dd-MM-yyyy";
	public final static String DATE_FORMAT_SERVER = "yyyy-MM-dd";
	public final static String DATE_FORMAT_AGENDA = "d MMM";

	public final static Locale PT_LOCALE = new Locale("pt", "PT");

	public static final String UsernameParam = "user_name=";

	public static final String PasswordParam = "password=";

	public static final String FormatJsonParam = "format=json";
	
	public static final String LASTMODIFIED_GT_PARAM = "last_modified_date__gt=";
	
	public static final String AUTHOR_ID = "authorID";
	public static final String KEYNOTE_ID = "keynoteID";
	public static final String CONTACT_ID = "contactID";
	public static final String EVENT_ID = "eventID";
	public static final String PUBLICATION_ID = "publicationID";
	public static final String SIGHT_ID = "sightID";
	
	public static final String FORGOT_PASSWORD_RESOURCE = "forgot_password/";

	public static final String IS_PARENT_ID = "isParentID";

	

	public static enum EVENT_TYPE {
		SOCIAL_EVENT, KEYNOTE_SESSION, POSTER_SESSION, TALK_SESSION, WORKSHOP
	};
	
	public static enum PUB_TYPE {
		ARTICLE, NOT_PRESENTED
	};
	
	public static enum SIGHT_TYPE {
		HOTEL, RESTAURANT, OTHER_SIGHT
	};
	
	public static enum CONTACTS_TYPE {
		AUTHOR_CONTACT, KEYNOTE_CONTACT, PARTICIPANT_CONTACT
	}

	public static boolean debugDownload = true;

	/**
	 * Converts DP to PX
	 * 
	 * @param context
	 *            application context
	 * @param dp
	 *            dp value to convert
	 * @return inserted dp value in px
	 */
	public static int getPxFromDp(Context context, long dp) {
		Resources r = context.getResources();
		int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				dp, r.getDisplayMetrics());
		return px;
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
	}

	/**
	 * Default Values for Layouts Lists
	 * */

}
