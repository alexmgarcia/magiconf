package com.example.magiconf_client;

import java.util.List;
import tables.Event;
import utils.EventTypeChecker;
import utils.Pair;
import utils.ValuesHelper;
import adapters.Helper;
import adapters.SessionsListViewAdapter;
import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import dataSources.EventDataSource;

/**
 * @author José
 *
 * This class represents the Agenda Search Results activity
 */
public class AgendaSearchResults extends Activity{

	private EventDataSource eventsDB = new EventDataSource(this);
	
	//public static final String EVENT_ID = ValuesHelper.EVENT_ID;

	private List<Event> results;
	private List<Event> resultsInAgenda;
	
	boolean onlyInAgenda;

	/**
	 * Updates all the views present in this activity
	 * 
	 */
	private void updateViews() {
		
		TextView view = (TextView) findViewById(R.id.no_results);
		view.setText("");
		final ListView sessionListView = (ListView) findViewById(R.id.sessions_list);
		sessionListView.setAdapter(null);
		
		if(onlyInAgenda){
			
			if(resultsInAgenda.size()==0){
				view.setText(this.getString(R.string.NO_RESULTS));
				
			}else{
								
				sessionListView.setAdapter(new SessionsListViewAdapter(this, android.R.layout.simple_list_item_1, resultsInAgenda));
				sessionListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int position,long arg3) {
						
						Event event = (Event) sessionListView.getItemAtPosition(position);
						Intent intent;
						if (EventTypeChecker.getType(AgendaSearchResults.this, event) == ValuesHelper.EVENT_TYPE.TALK_SESSION.ordinal()) {
							intent = new Intent(AgendaSearchResults.this, TalkSessionActivity.class);
							intent.putExtra(ValuesHelper.EVENT_ID, event.getDjangoId());
							startActivity(intent);
						}else if(EventTypeChecker.getType(AgendaSearchResults.this, event) == ValuesHelper.EVENT_TYPE.POSTER_SESSION.ordinal()) {
							intent = new Intent(AgendaSearchResults.this, PostersSessionActivity.class);
							intent.putExtra(ValuesHelper.EVENT_ID, event.getDjangoId());
							startActivity(intent);
						}else if(EventTypeChecker.getType(AgendaSearchResults.this, event) == ValuesHelper.EVENT_TYPE.KEYNOTE_SESSION.ordinal()) {
							intent = new Intent(AgendaSearchResults.this, KeynoteSessionActivity.class);
							intent.putExtra(ValuesHelper.EVENT_ID, event.getDjangoId());
							startActivity(intent);
						}else if(EventTypeChecker.getType(AgendaSearchResults.this, event) == ValuesHelper.EVENT_TYPE.SOCIAL_EVENT.ordinal()) {
							intent = new Intent(AgendaSearchResults.this, EventActivity.class);
							intent.putExtra(ValuesHelper.EVENT_ID, event.getDjangoId());
							startActivity(intent);
						}else if(EventTypeChecker.getType(AgendaSearchResults.this, event) == ValuesHelper.EVENT_TYPE.WORKSHOP.ordinal()) {
							intent = new Intent(AgendaSearchResults.this, WorkshopActivity.class);
							intent.putExtra(ValuesHelper.EVENT_ID, event.getDjangoId());
							startActivity(intent);
						}
					}
				
				});
				
				// This allows to have a list inside a scrollview
				Helper.getListViewSize(sessionListView);
			}
			
		}else{ 
			
			if(results.size()==0){
				view.setText(this.getString(R.string.NO_RESULTS));
			}else{
								
				sessionListView.setAdapter(new SessionsListViewAdapter(this, android.R.layout.simple_list_item_1, results));
				sessionListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int position,long arg3) {
						
						Event event = (Event) sessionListView.getItemAtPosition(position);
						Intent intent;
						if (EventTypeChecker.getType(AgendaSearchResults.this, event) == ValuesHelper.EVENT_TYPE.TALK_SESSION.ordinal()) {
							intent = new Intent(AgendaSearchResults.this, TalkSessionActivity.class);
							intent.putExtra(ValuesHelper.IS_PARENT_ID, true);
							intent.putExtra(ValuesHelper.EVENT_ID, event.getDjangoId());
							startActivity(intent);
						}else if(EventTypeChecker.getType(AgendaSearchResults.this, event) == ValuesHelper.EVENT_TYPE.POSTER_SESSION.ordinal()) {
							intent = new Intent(AgendaSearchResults.this, PostersSessionActivity.class);
							intent.putExtra(ValuesHelper.IS_PARENT_ID, true);
							intent.putExtra(ValuesHelper.EVENT_ID, event.getDjangoId());
							startActivity(intent);
						}else if(EventTypeChecker.getType(AgendaSearchResults.this, event) == ValuesHelper.EVENT_TYPE.KEYNOTE_SESSION.ordinal()) {
							intent = new Intent(AgendaSearchResults.this, KeynoteSessionActivity.class);
							intent.putExtra(ValuesHelper.IS_PARENT_ID, true);
							intent.putExtra(ValuesHelper.EVENT_ID, event.getDjangoId());
							startActivity(intent);
						}else if(EventTypeChecker.getType(AgendaSearchResults.this, event) == ValuesHelper.EVENT_TYPE.SOCIAL_EVENT.ordinal()) {
							intent = new Intent(AgendaSearchResults.this, EventActivity.class);
							intent.putExtra(ValuesHelper.IS_PARENT_ID, true);
							intent.putExtra(ValuesHelper.EVENT_ID, event.getDjangoId());
							startActivity(intent);
						}else if(EventTypeChecker.getType(AgendaSearchResults.this, event) == ValuesHelper.EVENT_TYPE.WORKSHOP.ordinal()) {
							intent = new Intent(AgendaSearchResults.this, WorkshopActivity.class);
							intent.putExtra(ValuesHelper.IS_PARENT_ID, true);
							intent.putExtra(ValuesHelper.EVENT_ID, event.getDjangoId());
							startActivity(intent);
						}
					}
				
				});
				
				// This allows to have a list inside a scrollview
				Helper.getListViewSize(sessionListView);
				
			}
	
		}
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));

	    // Get the intent, verify the action and get the query
	    Intent intent = getIntent();

	    onlyInAgenda = intent.getBundleExtra(SearchManager.APP_DATA).getBoolean(AgendaActivity.ONLY_IN_AGENDA);
	    
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	      String query = intent.getStringExtra(SearchManager.QUERY);
	      eventsDB.open();
	      Pair<List<Event>,List<Event>> allResults = eventsDB.search(query);
	      results = allResults.getFirst(); 
	      resultsInAgenda = allResults.getSecond();
	      eventsDB.close();
	      setContentView(R.layout.agenda_search);
	      
	      Switch check = (Switch) findViewById(R.id.in_agenda_checkbox);
	      
	      if(onlyInAgenda){
	    	  check.setChecked(true);
	      }else check.setChecked(false);
	      
		  check.setOnCheckedChangeListener(new OnCheckedChangeListener() {

		          @Override
		          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		              onlyInAgenda = isChecked;
		              updateViews();
		          }
		          
		  });
		  
	      updateViews();
		
		// Show the Up button in the action bar.
		setupActionBar();
	    }
	    
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
		getMenuInflater().inflate(R.menu.search_results, menu);
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
