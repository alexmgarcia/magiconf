package utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class AppPreferences {

	public static final String KEY_PREFS_FIRST_RUN = "first_run";
	private static final String APP_SHARED_PREFS = AppPreferences.class.getSimpleName(); //  Name of the file -.xml
	private SharedPreferences _sharedPrefs;
	private Editor _prefsEditor;

	public AppPreferences(Context context) {
		this._sharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
		this._prefsEditor = _sharedPrefs.edit();
	}

	public String getFirstRun() {
		return _sharedPrefs.getString(KEY_PREFS_FIRST_RUN, "");
	}

	public void saveFirstRun(String text) {
		_prefsEditor.putString(KEY_PREFS_FIRST_RUN, text);
		_prefsEditor.commit();
	}


}
