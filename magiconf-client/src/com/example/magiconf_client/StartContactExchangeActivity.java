package com.example.magiconf_client;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import utils.APIResponses;
import utils.ContactExchanger;
import utils.GCMHelper;
import utils.RestClient;
import utils.ValuesHelper;

import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class StartContactExchangeActivity extends Activity {
	
	private ContactExchanger contactExchanger = new ContactExchanger(StartContactExchangeActivity.this);
	
	TextView pinText;
	ProgressDialog progDialog;
	
	/**
	 * Shows an alert dialog
	 * @param title title of the dialog
	 * @param msg text of the dialog
	 * @param finish if clicking in OK button will finish the activity
	 */
	private void showAlertDialog(String title, String msg, final boolean finish) {
		final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(msg);
		alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok_button), new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (finish)
					StartContactExchangeActivity.this.finish();
			}
		});
		alertDialog.show();
	}
	
	/**
	 * Send an asynchronous finish request to server and displays an alert dialog about it
	 */
	private void finishExchangeAsync() {
		new AsyncTask<Void, Void, JSONObject>() {

			@Override
			protected JSONObject doInBackground(Void... arg0) {
				return contactExchanger.finishExchange();
			}
			
			@Override
			protected void onPostExecute(JSONObject result) {
				try {
					if (result != null) {
						if (APIResponses.getKey(result).equals(APIResponses.KEY_PIN))
								if (result.get(APIResponses.KEY_PIN).equals(APIResponses.DATA_PIN_REMOVED))
									if (ContactExchanger.totalContactsExchanged != 1) { // More or less than one contact exchanged
										showAlertDialog(StartContactExchangeActivity.this.getString(R.string.exchange_finished_title), 
												StartContactExchangeActivity.this.getString(R.string.exchange_finished_text) + " " + 
										ContactExchanger.totalContactsExchanged + " " + StartContactExchangeActivity.this.getString(R.string.exchange_finished_text_2), true);
									}
									else { // One contact exchanged
										showAlertDialog(StartContactExchangeActivity.this.getString(R.string.exchange_finished_title), 
												StartContactExchangeActivity.this.getString(R.string.exchange_finished_text_single) + " " + 
										ContactExchanger.totalContactsExchanged + " " + StartContactExchangeActivity.this.getString(R.string.exchange_finished_text_single_2), true);
									}
									else
									showAlertDialog(StartContactExchangeActivity.this.getString(R.string.exchange_finished_error_title), 
											StartContactExchangeActivity.this.getString(R.string.exchange_finished_error_text), true);
		
							else if (APIResponses.getKey(result).equals(APIResponses.KEY_ERROR)) {
							if (result.get(APIResponses.KEY_ERROR).equals(APIResponses.DATA_ERROR_PIN_NOT_FOUND))
								showAlertDialog(StartContactExchangeActivity.this.getString(R.string.exchange_finished_error_title), 
										StartContactExchangeActivity.this.getString(R.string.exchange_finished_error_pin_not_found_text), true);
						}
							else {
								showAlertDialog(StartContactExchangeActivity.this.getString(R.string.error_title), 
										StartContactExchangeActivity.this.getString(R.string.error_ocurred), true);
							}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				finally {
					ContactExchanger.totalContactsExchanged = 0;
					ContactExchanger.contactExchangeStarted = false;
					progDialog.dismiss();
				}
			}
		}.execute(null, null, null);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
		setContentView(R.layout.activity_start_contact_exchange);
		if (ValuesHelper.isNetworkAvailable(this) && GCMHelper.registered) {
		    progDialog = new ProgressDialog(this);
		    progDialog.setMessage(getString(R.string.getting_pin));
		    progDialog.setIndeterminate(true);
		    progDialog.show();
			pinText = (TextView) findViewById(R.id.pin);
		    getAsyncPinRequest();
		}
		else if (!GCMHelper.registered)
			showAlertDialog(getString(R.string.device_not_registered_title), getString(R.string.device_not_registered_text), true);
		else
			showAlertDialog(getString(R.string.no_internet_title), getString(R.string.no_internet_text), true);
		
		Button finish = (Button)findViewById(R.id.finish_button);
		finish.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (ValuesHelper.isNetworkAvailable(StartContactExchangeActivity.this)) {
					progDialog = new ProgressDialog(StartContactExchangeActivity.this);
				    progDialog.setMessage(getString(R.string.finishing_contact_exchange));
				    progDialog.setIndeterminate(true);
				    progDialog.show();
				    
				    finishExchangeAsync();
				}
			    else
			    	showAlertDialog(getString(R.string.no_internet_title), getString(R.string.no_internet_text), false);
			}
		});
		
		setupActionBar();
		
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
	
	/**
	 * Issues a message to server to generate a new pin asynchronously and shows it
	 */
	private void getAsyncPinRequest() {
		new AsyncTask<Void, Void, JSONObject>() {

			@Override
			protected JSONObject doInBackground(Void... arg0) {
				return contactExchanger.getPin();
			}
			
			@Override
			protected void onPostExecute(JSONObject result) {
				try {
					if (result != null) {
						pinText.setText((CharSequence) result.get(APIResponses.KEY_PIN));
						ContactExchanger.contactExchangeStarted = true;
					}
					else
						showAlertDialog(StartContactExchangeActivity.this.getString(R.string.error_title), 
								StartContactExchangeActivity.this.getString(R.string.error_ocurred), false);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				finally {
					progDialog.dismiss();
				}
			}
			
		}.execute(null, null, null);
	}
	
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(true);

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, MainMenu.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		case R.id.action_about:
			intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	
}
