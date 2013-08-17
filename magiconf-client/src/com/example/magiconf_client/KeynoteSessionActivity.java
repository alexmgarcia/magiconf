package com.example.magiconf_client;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import tables.Keynote;
import tables.KeynoteSession;
import utils.ValuesHelper;
import dataRetrieving.AuthInfo;
import dataRetrieving.KeynoteSessionData;
import dataSources.KeynoteSessionDataSource;
import dataSources.ParticipantEventDataSource;
import adapters.Helper;
import adapters.KeynoteListViewAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

/**
 * @author Alexandre
 *
 * This classe represents the KeynoteSession Activity
 */
public class KeynoteSessionActivity extends Activity {
	
	//public final static String KEYNOTE_ID="keynoteID";

	public static final String KEYNOTE_EMAIL = "keynoteEmail";
	
	private KeynoteSessionDataSource keynoteSessionDB = new KeynoteSessionDataSource(this); // the database
	private ParticipantEventDataSource participantEventDB = new ParticipantEventDataSource(this);
	
	/**
	 * Updates all the views present in this activity
	 * @param result the object to use to populate data
	 */
	private void updateViews(final KeynoteSession result) {
		Log.i("KeynoteActivity", ""+result);
		setContentView(R.layout.activity_keynote_session);
		TextView textView = (TextView) this.findViewById(R.id.keynote_title);
		Log.i("KeynoteActivity", ""+textView);
		textView.setText(result.getTitle());
		textView = (TextView) this.findViewById(R.id.time);
		String time = new SimpleDateFormat(ValuesHelper.HOUR_FORMAT, Locale.ENGLISH).format(result.getTime());
		textView.setText(time);
		textView = (TextView) this.findViewById(R.id.place);
		textView.setText(result.getPlace());
		
		textView = (TextView) this.findViewById(R.id.description);
		textView.setText(result.getDescription());
		
		final ListView keynoteSpeakersList = (ListView)findViewById(R.id.keynote_speakers_list);
		final ArrayList<Keynote> list = new ArrayList<Keynote>();
		
		// Initialize the keynote speakers list
		Iterator<Keynote> it = result.getKeynoteSpeakers().iterator();
		while (it.hasNext()) 
			list.add(it.next());
		
		keynoteSpeakersList.setAdapter(new KeynoteListViewAdapter(this, android.R.layout.simple_list_item_1, list));
		keynoteSpeakersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				
				Keynote keynoteSpeaker = (Keynote) keynoteSpeakersList.getItemAtPosition(position);
				Intent intent = new Intent(KeynoteSessionActivity.this, KeynoteSpeakerActivity.class);
				intent.putExtra(ValuesHelper.KEYNOTE_ID, keynoteSpeaker.getId()); // TODO this is not finished. It will be the event id of a selected item in agenda
				startActivity(intent);
				
				// TODO Open the keynote speaker activity. To be done when story #49091965 is done
			}
		});
		
		// This allows to have a list inside a scrollview
		Helper.getListViewSize(keynoteSpeakersList);
		
		Switch inAgenda = (Switch)this.findViewById(R.id.in_agenda_checkbox);
		participantEventDB.open();
		result.setInAgenda(participantEventDB.isInAgenda(AuthInfo.username, result.getEventId())); 
		inAgenda.setChecked(result.getInAgenda());
		participantEventDB.close();
		inAgenda.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				result.setInAgenda(((Switch)v).isChecked());
				participantEventDB.open();
				if (result.getInAgenda())
					participantEventDB.createParticipantEvent(AuthInfo.username, result.getEventId());
				else
					participantEventDB.deleteParticipantEvent(AuthInfo.username, result.getEventId());
				participantEventDB.close();
			}
		});
	}
	
	/**
	 * Inserts a new keynote session into the database
	 * @param result object to use to populate data
	 * @return the new keynote session
	 */
	/*private KeynoteSession createNewKeynoteSession(KeynoteSession result) {
		keynoteSessionDB.open();
		Log.i("Duration", result.getDuration() + "");
		KeynoteSession newKeynoteSession = keynoteSessionDB.createKeynoteSession(result.getTitle(), result.getTime(), result.getDuration(), result.getPlace(), result.getDescription(), result.getKeynoteSpeakers());
		
		keynoteSessionDB.close();
		return newKeynoteSession;
	}*/
	
	private KeynoteSession getKeynoteSession(Long id) {
		keynoteSessionDB.open();
		KeynoteSession e = keynoteSessionDB.getKeynoteSessionEvent(id); // TODO alterar para id
		keynoteSessionDB.close();
		return e;
	}
	
	
	/**
	 * @author Alexandre
	 * Asynchronous REST GET request
	 */
	private class RESTGetTask extends AsyncTask<String, Void, KeynoteSession> {
		protected KeynoteSession doInBackground(String... params) {
			/*RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
		     
		    KeynoteSession event = restTemplate.getForObject(params[0], KeynoteSession.class);
		    return event;*/
			return KeynoteSessionData.downloadKeynoteSessionData(Long.parseLong(params[0]), AuthInfo.username, AuthInfo.password,  keynoteSessionDB,KeynoteSessionActivity.this,false);
		}
		
		protected void onPostExecute(KeynoteSession result) {
			// This is new data, so let's populate the data in views and insert it into the database
			//KeynoteSession keynoteSession = createNewKeynoteSession(result);
			updateViews(result);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
		Intent intent = getIntent();
		Long eventId = intent.getLongExtra(ValuesHelper.EVENT_ID, 2);
		// Get an existing keynote session
		KeynoteSession e = getKeynoteSession(eventId);
		
		Log.i("KeynoteSessionActivity", ""+keynoteSessionDB);
		Log.i("KeynoteSessionActivity", ""+Long.valueOf(1));
		
		if (e == null) // it doesn't exist into the local database
			new RESTGetTask().execute(""+eventId);
		else			
			updateViews(e);

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
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.event, menu);
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
