package com.example.magiconf_client;

import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import tables.Keynote;
import tables.ParticipantAuthor;
import tables.ParticipantKeynote;
import utils.ContactExchanger;
import utils.ValuesHelper;

import dataRetrieving.ArticleData;
import dataRetrieving.AuthInfo;
import dataRetrieving.KeynoteData;
import dataSources.KeynoteDataSource;
import dataSources.ParticipantAuthorDataSource;
import dataSources.ParticipantKeynoteDataSource;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class KeynoteSpeakerActivity extends Activity {

	private KeynoteDataSource keynoteSpeakerDB = new KeynoteDataSource(this); // the
																				// database
	private ParticipantKeynoteDataSource participantKeynoteDB = new ParticipantKeynoteDataSource(this);
	
	private Keynote keynote;

	/**
	 * Updates all the views present in this activity
	 * 
	 * @param result
	 *            the object to use to populate data
	 */
	private void updateViews(final Keynote result) {
		setContentView(R.layout.activity_keynote_speaker);
		ImageView imageView = (ImageView) this
				.findViewById(R.id.keynote_speaker_photo);
		UrlImageViewHelper.setUrlDrawable(imageView, result.getPhoto(), null, UrlImageViewHelper.CACHE_DURATION_INFINITE);
		if(imageView.getDrawable()==null){
			imageView.setImageResource(R.drawable.dummy_user_photo);
		}
		TextView textView = (TextView) this.findViewById(R.id.keynote_country);
		textView.setText(result.getCountry());
		Log.i("Country", result.getCountry());
		textView = (TextView) this.findViewById(R.id.keynote_organization);
		textView.setText(result.getWork_place());
		textView = (TextView) this.findViewById(R.id.keynote_email);
		
		RelativeLayout emailLayout = (RelativeLayout) this.findViewById(R.id.email_layout);
		emailLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent emailIntent = new Intent(Intent.ACTION_VIEW);
				emailIntent.setData(Uri.parse("mailto:?to=" + result.getEmail()));
				startActivity(emailIntent);
			}
		});
		
		textView.setText(result.getEmail());
		ActionBar actionBar = getActionBar();
		actionBar.setTitle(result.getName());
		
		this.keynote = result;
	}

	/**
	 * Inserts a new keynote speaker into the database
	 * 
	 * @param result
	 *            object to use to populate data
	 * @return the new keynote speaker
	 */
	private Keynote createNewKeynoteSpeaker(Keynote result) {
		keynoteSpeakerDB.open();
		Keynote newKeynoteSpeaker = keynoteSpeakerDB.createKeynote(
				result.getEmail(), result.getName(), result.getWork_place(),
				result.getCountry(), result.getPhoto(),""+result.getContact().getId(), true);

		keynoteSpeakerDB.close();
		return newKeynoteSpeaker;
	}

	private Keynote getKeynoteSpeaker(Long id) {
		keynoteSpeakerDB.open();
		Keynote e = keynoteSpeakerDB.getKeynote(id);
		keynoteSpeakerDB.close();
		return e;
	}

	/**
	 * @author Alexandre Asynchronous REST GET request
	 */
	private class RESTGetTask extends AsyncTask<String, Void, Keynote> {
		protected Keynote doInBackground(String... params) {
			/*RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(
					new MappingJacksonHttpMessageConverter());

			Keynote keynoteSpeaker = restTemplate.getForObject(params[0],
					Keynote.class);
			return keynoteSpeaker;*/
			return KeynoteData.downloadKeynoteData(params[0], AuthInfo.username, AuthInfo.password, keynoteSpeakerDB);
		}

		protected void onPostExecute(Keynote result) {
			// This is new data, so let's populate the data in views and insert
			// it into the database
			//Keynote keynoteSpeaker = createNewKeynoteSpeaker(result);
			updateViews(result);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
		setContentView(R.layout.activity_keynote_speaker);

		Intent intent = getIntent();
		Long keynoteId = intent.getLongExtra(ValuesHelper.KEYNOTE_ID,
				2);
		// Get an existing keynote session
		Keynote keynoteSpeaker = getKeynoteSpeaker(keynoteId);

		if (keynoteSpeaker == null) // it doesn't exist into the local database
			new RESTGetTask().execute(keynoteSpeaker.getEmail());
		else
			updateViews(keynoteSpeaker);
		// Show the Up button in the action bar.
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

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.keynote_speaker, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add: //add/remove from contacts
			if(!isContact()){
				addContact();
				item.setIcon(R.drawable.remove_person);
			}else{
				removeContact();
				item.setIcon(R.drawable.add_person);
			}
			return true;
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
	
	
	private void addContact() {
		String message = "";
		//Add contact to BD
		//First check if it is on contacts. If yes, ignore but send email. If no add to bd
		
		participantKeynoteDB.open();
		ParticipantKeynote pk = participantKeynoteDB.getParticipantKeynoteDB(AuthInfo.username,this.keynote.getId());
		
		if(pk == null){
			participantKeynoteDB.createParticipantKeynote(AuthInfo.username,this.keynote.getId());
			message = keynote.getName() + " "+getString(R.string.contact_added_text);
		}
		else {
			message = getString(R.string.error_already_in_contacts);
		}
		participantKeynoteDB.close();
		
		Toast.makeText(KeynoteSpeakerActivity.this, message, Toast.LENGTH_SHORT).show();
		
		//send email anyway
		if(ValuesHelper.isNetworkAvailable(KeynoteSpeakerActivity.this))
			ContactExchanger.addAuthorOrKeynote(keynote.getName(),keynote.getEmail(), ContactExchanger.TYPE_KEYNOTE,KeynoteSpeakerActivity.this);
		else
			Toast.makeText(KeynoteSpeakerActivity.this, getString(R.string.no_internet_text), Toast.LENGTH_SHORT).show();
	}
	
	private void removeContact() {
		String message = "";
		//Remove contact to BD
		//First check if it is on contacts. If yes, ignore but send email. If no add to bd
		
		participantKeynoteDB.open();
		ParticipantKeynote pa = participantKeynoteDB.getParticipantKeynoteDB(AuthInfo.username,this.keynote.getId());
		
		if(pa != null){
			participantKeynoteDB.deleteParticipantKeynote(AuthInfo.username,this.keynote.getId());
			message = keynote.getName() + " "+getString(R.string.contact_removed_text);
		}
		else {
			message = getString(R.string.error_not_in_contacts);
		}
		participantKeynoteDB.close();
		
		Toast.makeText(KeynoteSpeakerActivity.this, message, Toast.LENGTH_SHORT).show();
		
	}
	
	private boolean isContact(){
		ParticipantKeynote pa = null;
		participantKeynoteDB.open();
		pa = participantKeynoteDB.getParticipantKeynoteDB(AuthInfo.username,this.keynote.getId());
		participantKeynoteDB.close();
		
		return pa!=null;
		
	}
	
	public boolean onPrepareOptionsMenu (Menu menu) {
		if (isContact()) {
			MenuItem menuItem = menu.findItem(R.id.action_add);
			menuItem.setIcon(R.drawable.remove_person);
		}else{
			MenuItem menuItem = menu.findItem(R.id.action_add);
			menuItem.setIcon(R.drawable.add_person);
		}
		return super.onPrepareOptionsMenu(menu);
			
	}

}
