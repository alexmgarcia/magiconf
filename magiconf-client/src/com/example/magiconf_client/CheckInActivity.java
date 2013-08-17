 package com.example.magiconf_client;

import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import tables.Keynote;
import tables.Participant;
import utils.ValuesHelper;
import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import dataRetrieving.AuthInfo;
import dataRetrieving.ParticipantData;
import dataSources.ContactDataSource;
import dataSources.KeynoteDataSource;
import dataSources.ParticipantContactDataSource;
import dataSources.ParticipantDataSource;

public class CheckInActivity extends Activity{


	private ParticipantDataSource participantsDB = new ParticipantDataSource(this);
	private ParticipantContactDataSource participantContactDB = new ParticipantContactDataSource(this);
	private ContactDataSource contactsDB = new ContactDataSource(this);
	
//	public final static String PARTICIPANT_ID="participantID";

	/**
	 * Updates all the views present in this activity
	 * 
	 * @param result
	 *            the object to use to populate data
	 */
	private void updateViews(final Participant result) {
		Log.i("CHECK-IN", "updating view");
		setContentView(R.layout.activity_check_in);
		ImageView imageView = (ImageView) this
				.findViewById(R.id.check_in_participant_photo);
		Log.i("CHECK-IN", "Photo: "+result.getPhoto());
		UrlImageViewHelper.setUrlDrawable(imageView,ValuesHelper.SERVER_API_ADDRESS_MEDIA + result.getPhoto(), null, UrlImageViewHelper.CACHE_DURATION_INFINITE);
		if(imageView.getDrawable()==null){
			imageView.setImageResource(R.drawable.dummy_user_photo);
 		}
		
		TextView textView = (TextView) this.findViewById(R.id.check_in_participant_name);
		
		textView.setText(result.getName());
		Log.i("Name", result.getName());
		
		textView = (TextView) this.findViewById(R.id.check_in_participant_organization);
		textView.setText(result.getWork_place());
		
		ImageView imageView2 = (ImageView) this
				.findViewById(R.id.check_in_qrcode);
		Log.i("CHECK-IN", "QRCode Url: "+ValuesHelper.SERVER_API_ADDRESS_MEDIA +result.getQrcode());
		UrlImageViewHelper.setUrlDrawable(imageView2, ValuesHelper.SERVER_API_ADDRESS_MEDIA +result.getQrcode(), null, UrlImageViewHelper.CACHE_DURATION_INFINITE);
		if(imageView2.getDrawable()==null){
			imageView2.setImageResource(R.drawable.small_logo);
 		}
		Log.i("CHECK-IN", "QRcode: "+result.getQrcode());
		
	}

	
	private Participant getParticipant(String participantUsername) {
		participantsDB.open();
		Participant p = participantsDB.getParticipantByUsername(participantUsername);
		participantsDB.close();
		return p;
	}

	
	private class RESTGetTask extends AsyncTask<String, Void, Participant> {
		protected Participant doInBackground(String... params) {
			Participant p = ParticipantData.downloadParticipantData(CheckInActivity.this,AuthInfo.username, AuthInfo.password, 
					participantsDB,contactsDB,participantContactDB,true);
			return p;
			/*RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(
					new MappingJacksonHttpMessageConverter());

			Participant participant = restTemplate.getForObject(params[0],
					Participant.class);
			return participant;*/
		}

		protected void onPostExecute(Participant result) {
			//Participant participant = createNewParticipant(result);
			updateViews(result);
		}

		
	}
	
	private Participant createNewParticipant(Participant result) {
		participantsDB.open();
		Log.i("CHECK-IN","Creating entry");
		Log.i("CHECK-IN","QRCODE Result: "+result.getQrcode());
		Participant newParticipant = participantsDB.createParticipant(result.getCountry(),
				result.getUsername(), result.getName(), result.getPhone_number(),
				result.getEmail(), result.getPhoto(), result.getWork_place(), result.getQrcode(), result.getLinkedinUrl(), result.getFacebookUrl());

		participantsDB.close();
		return newParticipant;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
		setContentView(R.layout.activity_check_in);

		Intent intent = getIntent();
		/*Long parcitipantUsername = intent.getLongExtra(CheckInActivity.PARTICIPANT_ID,
				2);*/
		String parcitipantUsername = intent.getStringExtra("username");
		Log.i("CHECK-IN","Username: "+parcitipantUsername);
		Participant parcitipant = getParticipant(parcitipantUsername);
		
		Log.i("CHECK-IN","Null?: "+ (parcitipant == null));
		if (parcitipant == null) // it doesn't exist into the local database
			new RESTGetTask().execute(/*ValuesHelper.SERVER_API_ADDRESS +"/participant/" + 
					parcitipantUsername + "/"
					+"?"+
				ValuesHelper.FormatJsonParam + "&"+
				ValuesHelper.UsernameParam + AuthInfo.username+ "&"+
				ValuesHelper.PasswordParam + AuthInfo.password*/);
		else			
			updateViews(parcitipant);

		//updateViews(parcitipant);
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
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(true);
		getActionBar().setTitle(getString(R.string.check_in_action_bar_title));

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.check_in, menu);
		return true;
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
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
