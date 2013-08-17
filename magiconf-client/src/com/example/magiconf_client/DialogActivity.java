package com.example.magiconf_client;

import utils.ContactExchanger;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;
import android.view.Menu;

/**
 * @author Alexandre
 *
 * This activity represents an activity that will be used just to display a dialog in GcmBroadcastReceiver
 */
public class DialogActivity extends Activity {
	
	public static final String TITLE="titleID";
	public static final String TEXT="textID";
	public static final String USERNAME="usernameID";
	public static final String TYPE="typeID";
	
	public static final String YES_NO_DIALOG="yesno";
	public static final String ALERT_DIALOG="alert";
	
	private Intent intent;
	
	/**
	 * Shows an alert dialog
	 * @param title title of the dialog
	 * @param text text of the dialog
	 * @param type type of the dialog
	 */
	private void showAlertDialog(String title, String text, String type) {
		final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(text);
		if (type.equals(YES_NO_DIALOG)) {
			alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes_button), new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ContactExchanger contactExchanger = new ContactExchanger(DialogActivity.this);
					contactExchanger.allowExchange(intent.getStringExtra(USERNAME));
					DialogActivity.this.finish();
				}
			});
			alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no_button), new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					DialogActivity.this.finish();
				}
			});
		}
		else if (type.equals(ALERT_DIALOG)) {
			alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok_button), new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					DialogActivity.this.finish();
				}
			});
		}
		alertDialog.show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dialog);
		intent = getIntent();
		showAlertDialog(intent.getStringExtra(TITLE), intent.getStringExtra(TEXT), intent.getStringExtra(TYPE));
		IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction("com.package.ACTION_LOGOUT");
	    registerReceiver(new BroadcastReceiver() {

	                    @Override
	                    public void onReceive(Context context, Intent intent) {
	                        Log.d("onReceive","Logout in progress");
	                        finish();
	                    }
	                }, intentFilter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.dialog, menu);
		return true;
	}

}
