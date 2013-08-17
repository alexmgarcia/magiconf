package com.example.magiconf_client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import tables.Author;
import tables.Contact;
import tables.Keynote;
import utils.Pair;
import utils.Triple;
import utils.ValuesHelper;
import adapters.SearchResultsViewAdapter;
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
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;

import dataSources.ContactDataSource;

/**
 * @author José
 *
 * This class represents the Contacts Search Results activity
 */
public class ContactsSearchResults extends Activity{

	private ContactDataSource contactsDB = new ContactDataSource(this);
	
	
	private static final String AUTHORS = "Autores";
	private static final String KEYNOTES = "Keynotes";
	private static final String OTHERS = "Participantes";
//	public static final String CONTACT_ID = "contact_id";
	
    ExpandableListView resultsView;
    
    List<String> headers;
    
	/**
	 * Updates all the views present in this activity
	 * @param sightsTriple 
	 * 
	 * @param result
	 *            the object to use to populate data
	 */
	private void updateViews(String query) {

		setContentView(R.layout.activity_search_results);
				
		Log.i("UPDATE","Update View...");
				
		contactsDB.open();
		Triple<List<Author>,List<Keynote>,List<Contact>> contacts = contactsDB.search(query);
		List<Author> authors = contacts.getFirst(); 
		List<Keynote> keynotes = contacts.getSecond(); 
		List<Contact> others = contacts.getThird();
		contactsDB.close();
		
		if(authors.size()==0 && keynotes.size()==0 && others.size()==0){
			TextView view = (TextView) findViewById(R.id.no_results);
			view.setText(this.getString(R.string.NO_RESULTS));			
		}else{
	        resultsView = (ExpandableListView) findViewById(R.id.search_results_view);
	        
	        headers = new ArrayList<String>();
	            
	        Map<String, List<Pair<String, Long>>> resultsMap = new LinkedHashMap<String, List<Pair<String,Long>>>();
	        
	        List<Pair<String,Long>> results = new ArrayList<Pair<String,Long>>();
	        
	        if(authors.size()>0){
		        for(Author a: authors){
		        	results.add(new Pair<String,Long>(a.getName(), a.getId()));
		        }
		        headers.add(AUTHORS);
		        resultsMap.put(AUTHORS, results);
		        results = new ArrayList<Pair<String,Long>>(); 
	        }
	        
	        if(keynotes.size()>0){
		        for(Keynote k: keynotes){
		        	results.add(new Pair<String,Long>(k.getName(), k.getId()));
		        }
		        headers.add(KEYNOTES);
		        resultsMap.put(KEYNOTES, results);   
		        results = new ArrayList<Pair<String,Long>>(); 
	        }
	        
	        if(others.size()>0){
		        for(Contact o: others){
		        	results.add(new Pair<String,Long>(o.getName(), o.getId()));
		        }
		        headers.add(OTHERS);
		        resultsMap.put(OTHERS, results);   
	        }
	
	        
	        final SearchResultsViewAdapter resultsViewAdapter = new SearchResultsViewAdapter(this, headers, resultsMap);
	        resultsView.setAdapter(resultsViewAdapter);
	        for(int i=0; i<headers.size();i++){
	        	resultsView.expandGroup(i);
	        }
	        resultsView.setOnChildClickListener(new OnChildClickListener() {
	
	        	public boolean onChildClick(ExpandableListView parent, View v,
	                    int groupPosition, int childPosition, long id) {
	            	
	                @SuppressWarnings("unchecked")
					final Pair<String,Long> element = (Pair<String,Long>) resultsViewAdapter.getChild(
	                        groupPosition, childPosition);
	
	                final Long idElement = element.getSecond();
	                
	                String header = (String) resultsViewAdapter.getGroup(groupPosition);
	                Intent intent;
	                
	                if(header.equals(AUTHORS)){
	                	intent = new Intent(ContactsSearchResults.this, AuthorActivity.class);
	                	intent.putExtra(ValuesHelper.AUTHOR_ID, idElement);
	                }else if(header.equals(KEYNOTES)){
	                	intent = new Intent(ContactsSearchResults.this, KeynoteSpeakerActivity.class);
	                	intent.putExtra(ValuesHelper.KEYNOTE_ID, idElement);	
	                }else{
	                	intent = new Intent(ContactsSearchResults.this, ContactActivity.class);
	                	intent.putExtra(ValuesHelper.CONTACT_ID, idElement);
	                }

	                startActivity(intent);
	                return true;
	            }
	        });
			}
			
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
		
	    // Get the intent, verify the action and get the query
	    Intent intent = getIntent();
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	      String query = intent.getStringExtra(SearchManager.QUERY);
	      updateViews(query);
		
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
