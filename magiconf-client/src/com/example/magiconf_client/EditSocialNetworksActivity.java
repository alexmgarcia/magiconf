package com.example.magiconf_client;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import dataRetrieving.AuthInfo;
import dataSources.ParticipantDataSource;

import tables.Participant;
import utils.APIResponses;
import utils.ContactExchanger;
import utils.RestClient;
import utils.ValuesHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditSocialNetworksActivity extends Activity {
	
	private static final String LINKEDIN_REGEX = "^(http://)?(www.)?linkedin.com/.+$";
	private static final String FACEBOOK_REGEX = "^(http://)?(www.)?facebook.com/.+$";
	private ParticipantDataSource participantsDB = new ParticipantDataSource(this);
	private String linkedinUrl;
	private String facebookUrl;
	ProgressDialog progDialog;
	
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
					EditSocialNetworksActivity.this.finish();
			}
		});
		alertDialog.show();
	}
	
	/**
	 * Sends an edit profile request asynchronously
	 */
	private void sendAsyncEditProfileRequest() {
		progDialog.show();
		new AsyncTask<Void, Void, JSONObject>() {

			@Override
			protected JSONObject doInBackground(Void... arg0) {
				String url = ValuesHelper.SERVER_API_ADDRESS + "/participant_full/edit_profile/";

				RestClient client = new RestClient();
				client.addPostParam(new BasicNameValuePair(ContactExchanger.USERNAME_KEY, AuthInfo.username));
				client.addPostParam(new BasicNameValuePair(ContactExchanger.PASSWORD_KEY, AuthInfo.password));
				client.addPostParam(new BasicNameValuePair(ContactExchanger.LINKEDIN_KEY, linkedinUrl));
				client.addPostParam(new BasicNameValuePair(ContactExchanger.FACEBOOK_KEY, facebookUrl));
				return client.makePostRequest(url);
			}
			
			@Override
			protected void onPostExecute(JSONObject result) {
				String title = getString(R.string.error_title);
				String msg = getString(R.string.error_ocurred);
				if (result != null) {
					if (APIResponses.getKey(result).equals(APIResponses.KEY_ERROR)) {
						Log.i("error","error");
						try {
							Log.i("response", result.get("error").toString());
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						msg = APIResponses.getErrorMsg(EditSocialNetworksActivity.this, result);
					}
					else if (APIResponses.getKey(result).equals(APIResponses.KEY_DATA)) {
						Log.i("here", "here");
						participantsDB.open();
						participantsDB.updateSocialNetworksByUsername(AuthInfo.username, linkedinUrl, facebookUrl);
						participantsDB.close();
						title = getString(R.string.change_done_title);
						msg = getString(R.string.change_done_text);
					}
				}
				else
					Log.i("null", "is null");
				Log.i("ouhhh","ouhhh");
				progDialog.dismiss();
				showAlertDialog(title, msg, false);
			}
			
		}.execute(null, null, null);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
		setContentView(R.layout.activity_edit_social_networks);
		participantsDB.open();
		Participant participant = participantsDB.getParticipantByUsername(AuthInfo.username);
		participantsDB.close();
		
		final EditText linkedinEdit = (EditText) findViewById(R.id.linkedin_edit);
		final EditText facebookEdit = (EditText) findViewById(R.id.facebook_edit);
		Log.i("linkedin", participant.getLinkedinUrl());
		linkedinEdit.setText(participant.getLinkedinUrl());
		facebookEdit.setText(participant.getFacebookUrl());
		Button b = (Button) findViewById(R.id.button_ok);
		b.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (ValuesHelper.isNetworkAvailable(EditSocialNetworksActivity.this)) {
					linkedinUrl = linkedinEdit.getText().toString();
					facebookUrl = facebookEdit.getText().toString();
					Log.i("match", linkedinUrl.matches(LINKEDIN_REGEX)+"");
					Log.i("match", facebookUrl.matches(FACEBOOK_REGEX)+"");
					if ((linkedinUrl.length() == 0 || linkedinUrl.matches(LINKEDIN_REGEX)) && 
							(facebookUrl.length() == 0 || facebookUrl.matches(FACEBOOK_REGEX))) {
						progDialog = new ProgressDialog(EditSocialNetworksActivity.this);
						progDialog.setMessage(getString(R.string.editing_profile));
						progDialog.setIndeterminate(true);
						sendAsyncEditProfileRequest();
					}
					else
						showAlertDialog(getString(R.string.format_not_valid_title), 
								getString(R.string.format_not_valid_text), false);
				}
				else
					showAlertDialog(getString(R.string.no_internet_title), getString(R.string.no_internet_text), false);
				
			}
		});
		setupActionBar();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_social_networks, menu);
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
