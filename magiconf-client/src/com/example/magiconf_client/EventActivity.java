package com.example.magiconf_client;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import dataRetrieving.AuthInfo;
import dataRetrieving.EventData;
import dataSources.EventDataSource;
import dataSources.ParticipantEventDataSource;

import tables.Event;
import tables.SocialEvent;
import utils.ValuesHelper;

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
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class EventActivity extends Activity {

	private EventDataSource eventDB = new EventDataSource(this);
	private ParticipantEventDataSource participantEventDB = new ParticipantEventDataSource(this);

	/**
	 * Updates all the views present in this activity
	 * @param result the object to use to populate data
	 */
	private void updateViews(final Event result) {
		setContentView(R.layout.activity_event);
		TextView textView = (TextView) EventActivity.this.findViewById(R.id.event_name);
		textView.setText(result.getTitle());
		textView = (TextView) EventActivity.this.findViewById(R.id.time);
		String time = new SimpleDateFormat(ValuesHelper.HOUR_FORMAT, Locale.ENGLISH).format(result.getTime());
		textView.setText(time);
		textView = (TextView) EventActivity.this.findViewById(R.id.place);
		textView.setText(result.getPlace());
		Switch inAgenda = (Switch) EventActivity.this.findViewById(R.id.in_agenda_checkbox);
		participantEventDB.open();
		result.setInAgenda(participantEventDB.isInAgenda(AuthInfo.username, result.getId()));
		inAgenda.setChecked(result.getInAgenda());
		participantEventDB.close();
		inAgenda.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				result.setInAgenda(((Switch)v).isChecked());
				participantEventDB.open();
				if (result.getInAgenda())
					participantEventDB.createParticipantEvent(AuthInfo.username, result.getId());
				else
					participantEventDB.deleteParticipantEvent(AuthInfo.username, result.getId());
				participantEventDB.close();
			}
		});
	}

	/**
	 * Inserts a new social event into the database
	 * @param result object to use to populate data
	 * @return the new event
	 */
	/*private Event createNewEvent(Event result) {
		eventDB.open();
		Event e = eventDB.createEvent(result.getTitle(), result.getPlace(), result.getTime(),  result.getDuration());
		eventDB.close();
		return e;
	}*/

	private Event getEvent(Long id) {
		eventDB.open();
		Event e = eventDB.getEvent(id);
		eventDB.close();
		return e;
	}

	/**
	 * @author Alexandre
	 * Asynchronous REST GET request
	 */
	private class RESTGetTask extends AsyncTask<String, Void, SocialEvent> {
		protected SocialEvent doInBackground(String... params) {
			/*RestTemplate restTemplate = new RestTemplate();
		    restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());

		    SocialEvent event = restTemplate.getForObject(params[0], SocialEvent.class);
		    return event;*/
			return EventData.downloadEventData(Long.parseLong(params[0]), AuthInfo.username, AuthInfo.password, eventDB);
		}

		protected void onPostExecute(SocialEvent result) {
			//Event e = createNewEvent(result);
			updateViews(result);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
		Intent intent = getIntent();
		Long eventId = intent.getLongExtra(ValuesHelper.EVENT_ID, 1);
		// Get an existing social event
		Event e = getEvent(eventId);

		Log.i("EventActivity", ""+eventDB);
		Log.i("EventActivity", ""+Long.valueOf(1));

		if (e == null) // it doesn't exist into the local database
			new RESTGetTask().execute(""+eventId);
		else			
			updateViews(e);

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
