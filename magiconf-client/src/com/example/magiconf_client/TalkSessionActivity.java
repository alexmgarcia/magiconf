package com.example.magiconf_client;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import dataRetrieving.AuthInfo;
import dataRetrieving.TalkSessionData;
import dataSources.ParticipantEventDataSource;
import dataSources.TalkDataSource;

import tables.Article;
import tables.Talk;
import utils.ValuesHelper;

import adapters.ArticleListViewAdapter;
import adapters.Helper;
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

public class TalkSessionActivity extends Activity {
	
//	public final static String ARTICLE_ID="articleID";
	public final static String ARTICLE_NAME="articleName";
	
	private TalkDataSource talkDB = new TalkDataSource(this);
	private ParticipantEventDataSource participantEventDB = new ParticipantEventDataSource(this);

	private void updateViews(final Talk result) {
		Log.i("TalkActivity", ""+result);
		setContentView(R.layout.activity_talk);
		TextView textView = (TextView) TalkSessionActivity.this.findViewById(R.id.title);
		Log.i("TalkActivity", ""+textView);
		textView.setText(result.getTitle());
		textView = (TextView) TalkSessionActivity.this.findViewById(R.id.time);
		String time = new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(result.getTime());
		textView.setText(time);
		textView = (TextView) TalkSessionActivity.this.findViewById(R.id.place);
		textView.setText(result.getPlace());
		     
		final ListView articlesList = (ListView) findViewById(R.id.articles_list);
		final ArrayList<Article> list = new ArrayList<Article>();
		
		// Initialize the articles list
		Iterator<Article> it = result.getArticles().iterator();
		while (it.hasNext())
			list.add(it.next());
	
		
		articlesList.setAdapter(new ArticleListViewAdapter(this, android.R.layout.simple_list_item_1, list));
		
		articlesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				
				Article article = (Article) articlesList.getItemAtPosition(position);
				Intent intent = new Intent(TalkSessionActivity.this, ArticleActivity.class);
				intent.putExtra(ValuesHelper.PUBLICATION_ID, article.getId()); // TODO this is not finished. It will be the article id of a selected item in the list
				
				startActivity(intent);
				
				// TODO Open the article activity.
			}
		});
		
		// This allows to have a list inside a scrollview
		Helper.getListViewSize(articlesList);
		
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
	 * Inserts a new talk session into the database
	 * @param result object to use to populate data
	 * @return the new talk session
	 */
	private Talk createNewTalk(Talk result) {
		talkDB.open();
		Log.i("Duration", result.getDuration() + "");
		Talk newTalk = talkDB.createTalk(result.getDjangoId(),result.getTitle(), result.getTime(), result.getDuration(), result.getPlace(), result.getArticles());
		
		talkDB.close();
		return newTalk;

	}
	
	private Talk getTalk(Long id) {
		talkDB.open();
		Talk e = talkDB.getTalkEvent(id); //TODO: Alterar para id
		talkDB.close();
		return e;
	}
	
	private class RESTGetTask extends AsyncTask<String, Void, Talk> {
		protected Talk doInBackground(String... params) {
			/*RestTemplate restTemplate = new RestTemplate();
		    restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
		     
		    Talk talk = restTemplate.getForObject(params[0], Talk.class);
		    return talk;*/
			return TalkSessionData.downloadTalkSessionData(Long.parseLong(params[0]), AuthInfo.username, AuthInfo.password, talkDB,TalkSessionActivity.this,false);
			
		}
		
		protected void onPostExecute(Talk result) {
			// This is new data, so let's populate the data in views and insert it into the database
			//Talk talk = createNewTalk(result);
			updateViews(result);
		}
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
		Intent intent = getIntent();
		Long eventId = intent.getLongExtra(ValuesHelper.EVENT_ID, 3);
		
		// Get an existing talk session
		Talk e = getTalk(eventId);
		
		Log.i("TalkActivity", ""+talkDB);
		Log.i("TalkActivity", ""+Long.valueOf(1));
		
		
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
		getMenuInflater().inflate(R.menu.talk, menu);
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
