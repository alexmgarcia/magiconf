package com.example.magiconf_client;

import org.json.JSONObject;

import utils.APIResponses;
import utils.ContactExchanger;
import utils.GCMHelper;
import utils.ValuesHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SendEmailExchangeRequestActivity extends Activity {
	
	private static final String EMAIL_REGEX = "^(((\\w)+(\\.)*)+@(((\\w)+(\\.)([a-zA-Z])+))+)$";
	ProgressDialog progDialog;
	ContactExchanger contactExchanger = new ContactExchanger(SendEmailExchangeRequestActivity.this);
	
	/**
	 * Shows an alert dialog
	 * @param title title of the dialog
	 * @param msg message of the dialog
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
					SendEmailExchangeRequestActivity.this.finish();
			}
		});
		alertDialog.show();
	}
	
	/**
	 * Sends an email contact exchange request asynchronously
	 * @param email email of the contact
	 */
	private void sendAsyncEmailExchangeRequest(final String email) {
		progDialog.show();
		new AsyncTask<Void, Void, JSONObject>() {

			@Override
			protected JSONObject doInBackground(Void... arg0) {
				return contactExchanger.sendEmailExchangeRequest(email);
			}
			
			@Override
			protected void onPostExecute(JSONObject result) {
				String title = getString(R.string.error_title);
				String msg = getString(R.string.error_ocurred);
				if (result != null) {
					if (APIResponses.getKey(result).equals(APIResponses.KEY_ERROR)) {
						title = getString(R.string.request_error_title);
						msg = APIResponses.getErrorMsg(SendEmailExchangeRequestActivity.this, result);
					}
					else if (APIResponses.getKey(result).equals(APIResponses.KEY_REQUEST)) {
						title = getString(R.string.request_sent_title);
						msg = getString(R.string.request_email_sent_text);
					}
				}
				progDialog.dismiss();
				showAlertDialog(title, msg, false);
			}
			
		}.execute(null, null, null);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
		setContentView(R.layout.activity_send_email_exchange_request);
		if (ValuesHelper.isNetworkAvailable(this) && GCMHelper.registered) {
			progDialog = new ProgressDialog(this);
			progDialog.setMessage(getString(R.string.sending_exchange_request));
			progDialog.setIndeterminate(true);
			Button b = (Button) findViewById(R.id.ok_button);
			final EditText emailText = (EditText) findViewById(R.id.email_edit);
			b.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					final String email = emailText.getText().toString();
					if (ValuesHelper.isNetworkAvailable(SendEmailExchangeRequestActivity.this)) {
						if (email.matches(EMAIL_REGEX))
							sendAsyncEmailExchangeRequest(email);
						else
							showAlertDialog(getString(R.string.request_error_title), getString(R.string.email_invalid), false);
					}
					else
						showAlertDialog(getString(R.string.no_internet_title), getString(R.string.no_internet_text), false);
					
				}
			});
		}
		else if (!GCMHelper.registered)
			showAlertDialog(getString(R.string.device_not_registered_title), getString(R.string.device_not_registered_text), true);
		else
			showAlertDialog(getString(R.string.no_internet_title), getString(R.string.no_internet_text), true);
		setupActionBar();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.send_email_exchange_request, menu);
		return true;
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
