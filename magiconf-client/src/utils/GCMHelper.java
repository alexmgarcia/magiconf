package utils;

import java.io.IOException;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;

/**
 * @author Alexandre
 *
 * Helper to Google Cloud Messaging
 */
public class GCMHelper {

	public static final String PROPERTY_REG_ID = "registration_id";
	public static final String PROPERTY_APP_VERSION = "appVersion";
	public static final String PROPERTY_ON_SERVER_EXPIRATION_TIME =
	            "onServerExpirationTimeMs";
	public static final String GCMHELPER = "gcm_helper";
	public static final long REGISTRATION_EXPIRY_TIME_MS = 1000 * 3600 * 24 * 7;
	private static String SENDER_ID = "781504963235";
	
	public static boolean registered = false;
	
	private GoogleCloudMessaging gcm;
	
	public String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.length() == 0) {
			Log.v("GCM", "Registration not found");
			return "";
		}
		
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		
		if (registeredVersion != currentVersion || isRegistrationExpired(context))
			return "";
		
		return registrationId;
	}
	
	private SharedPreferences getGCMPreferences(Context context) {
		return context.getSharedPreferences(GCMHELPER, Context.MODE_PRIVATE);
	}
	
	private static int getAppVersion(Context context) {
		PackageInfo packageInfo;
		try {
			packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException("Could not get package name: " + e);
		}
	}
	
	public boolean isRegistrationExpired(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		
		long expirationTime = prefs.getLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, -1);
		
		return System.currentTimeMillis() > expirationTime;
	}
	
	public void setRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersion(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		long expirationTime = System.currentTimeMillis() + REGISTRATION_EXPIRY_TIME_MS;
		
		editor.putLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, expirationTime);
		editor.commit();
	}
}
