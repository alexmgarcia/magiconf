package com.example.magiconf_client;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import dataRetrieving.AuthInfo;
import dataRetrieving.PosterSessionData;
import dataSources.ParticipantEventDataSource;
import dataSources.PosterSessionDataSource;

import tables.Poster;
import tables.PosterSession;
import utils.ValuesHelper;
import adapters.Helper;
import adapters.PosterListViewAdapter;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;



public class PostersSessionActivity extends Activity{
	
	static final String POSTER_ID = "posterID";
	private PosterSessionDataSource posterSessionDB = new PosterSessionDataSource(this);
	private ParticipantEventDataSource participantEventDB = new ParticipantEventDataSource(this);
	
	private void updateViews(final PosterSession result) {
		Log.i("PostersSessionActivity", ""+result);
		setContentView(R.layout.activity_postersession);
		TextView textView = (TextView) PostersSessionActivity.this.findViewById(R.id.title);
		Log.i("PostersSessionActivity", ""+textView);
		textView.setText(result.getTitle());
		textView = (TextView) PostersSessionActivity.this.findViewById(R.id.time);
		String time = new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(result.getTime());
		textView.setText(time);
		textView = (TextView) PostersSessionActivity.this.findViewById(R.id.place);
		textView.setText(result.getPlace());
		     
		final ListView postersList = (ListView) findViewById(R.id.posters_list);
		final ArrayList<Poster> list = new ArrayList<Poster>();
		
		// Initialize the posters list
		Iterator<Poster> it = result.getPosters().iterator();
		while (it.hasNext())
			list.add(it.next());
	
		
		postersList.setAdapter(new PosterListViewAdapter(this, android.R.layout.simple_list_item_1, list));
		
		postersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				
				Poster poster = (Poster) postersList.getItemAtPosition(position);
				Intent intent = new Intent(PostersSessionActivity.this, PosterActivity.class);
				intent.putExtra(POSTER_ID, poster.getId()); // TODO this is not finished. It will be the article id of a selected item in the list
				startActivity(intent);
				
				// TODO Open the article activity.
			}
		});
		
		// This allows to have a list inside a scrollview
		Helper.getListViewSize(postersList);
		
		Switch inAgenda = (Switch)this.findViewById(R.id.in_agenda_checkbox);
		participantEventDB.open();
		result.setInAgenda(participantEventDB.isInAgenda(AuthInfo.username, result.getEventId())); // TODO alterar
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
	


	/*private PosterSession createNewPosterSession(PosterSession result) {
		posterSessionDB.open();
		Log.i("Duration", result.getDuration() + "");
		PosterSession newPosterSession = posterSessionDB.createPostersSession(result.getTitle(), result.getTime(), result.getDuration(), result.getPlace(), result.getPosters());
		
		posterSessionDB.close();
		return newPosterSession;
	}*/
	
	private PosterSession getPosterSession(Long id) {
		posterSessionDB.open();
		PosterSession e = posterSessionDB.getPosterSessionEvent(id);
		posterSessionDB.close();
		return e;
	}
	
	
	
	private class RESTGetTask extends AsyncTask<String, Void, PosterSession> {
		protected PosterSession doInBackground(String... params) {
			/*RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
		     
		    PosterSession event = restTemplate.getForObject(params[0], PosterSession.class);
		    return event;*/
			return PosterSessionData.downloadPosterSessionData(Long.parseLong(params[0]), AuthInfo.username, AuthInfo.password,  posterSessionDB,PostersSessionActivity.this,false);
		}
		
		protected void onPostExecute(PosterSession result) {
			// This is new data, so let's populate the data in views and insert it into the database
			//PosterSession posterSession = createNewPosterSession(result);
			updateViews(result);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
		Intent intent = getIntent();
		Long eventId = intent.getLongExtra(ValuesHelper.EVENT_ID, 18);
		// Get an existing poster session
		PosterSession e = getPosterSession(eventId);
		
		Log.i("PosterSessionActivity", ""+posterSessionDB);
		Log.i("PosterSessionActivity", ""+Long.valueOf(1));
		
		if (e == null) // it doesn't exist into the local database
			new RESTGetTask().execute(""+eventId );
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