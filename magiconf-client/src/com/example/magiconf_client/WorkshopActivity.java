package com.example.magiconf_client;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import tables.Keynote;
import tables.KeynoteSession;
import tables.Workshop;
import utils.ValuesHelper;
import dataRetrieving.AuthInfo;
import dataRetrieving.WorkshopData;
import dataSources.KeynoteSessionDataSource;
import dataSources.ParticipantEventDataSource;
import dataSources.WorkshopDataSource;
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
 * This class represents the Workshop Activity
 */
public class WorkshopActivity extends Activity {
	
	private WorkshopDataSource workshopDB = new WorkshopDataSource(this); // the database
	private ParticipantEventDataSource participantEventDB = new ParticipantEventDataSource(this);
	
	/**
	 * Updates all the views present in this activity
	 * @param result the object to use to populate data
	 */
	private void updateViews(final Workshop result) {
		setContentView(R.layout.activity_workshop);
		TextView textView = (TextView) this.findViewById(R.id.workshop_title);
		textView.setText(result.getTitle());
		textView = (TextView) this.findViewById(R.id.time);
		String time = new SimpleDateFormat(ValuesHelper.HOUR_FORMAT, Locale.ENGLISH).format(result.getTime());
		textView.setText(time);
		textView = (TextView) this.findViewById(R.id.place);
		textView.setText(result.getPlace());
		
		textView = (TextView) this.findViewById(R.id.description);
		textView.setText(result.getDescription());
		
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
	 * Inserts a new workshop session into the database
	 * @param result object to use to populate data
	 * @return the new workshop session
	 */
	/*private Workshop createWorkshop(Workshop result) {
		workshopDB.open();
		Log.i("Duration", result.getDuration() + "");
		Workshop newWorkshop = workshopDB.createWorkshop(result.getTitle(), result.getTime(), result.getDuration(), result.getPlace(), result.getDescription());
		
		workshopDB.close();
		return newWorkshop;
	}*/
	
	private Workshop getWorkshop(Long id) {
		workshopDB.open();
		Workshop e = workshopDB.getWorkshopEvent(id); // TODO alterar para id
		workshopDB.close();
		return e;
	}
	
	
	/**
	 * @author Alexandre
	 * Asynchronous REST GET request
	 */
	private class RESTGetTask extends AsyncTask<String, Void, Workshop> {
		protected Workshop doInBackground(String... params) {
			/*RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
		     
			Workshop event = restTemplate.getForObject(params[0], Workshop.class);
		    return event;*/
			Log.i("WORKSHOP_ID",params[0]);
			return WorkshopData.downloadWorkshopData(Long.parseLong(params[0]), AuthInfo.username, AuthInfo.password, workshopDB,false);
		}
		
		protected void onPostExecute(Workshop result) {
			// This is new data, so let's populate the data in views and insert it into the database
			//Workshop workshop = createWorkshop(result);
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
		Workshop e = getWorkshop(eventId);
		
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
