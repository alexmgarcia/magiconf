package com.example.magiconf_client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import tables.Author;
import tables.Keynote;
import utils.Pair;
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

import dataSources.AuthorDataSource;
import dataSources.KeynoteDataSource;

/**
 * @author José
 *
 * This class represents the Authors And Keynotes Search Results activity
 */
public class AuthorsAndKeynotesSearchResults extends Activity{

	private AuthorDataSource authorsDB = new AuthorDataSource(this);
	private KeynoteDataSource keynotesDB = new KeynoteDataSource(this);
	
	private static final String AUTHORS = "Autores";
	private static final String KEYNOTES = "Keynotes";
	//public static final String AUTHOR_ID = ArticleActivity.AUTHOR_ID;
	public static final String AUTHOR_EMAIL = ArticleActivity.AUTHOR_EMAIL;
	//public static final String KEYNOTE_ID = KeynoteSessionActivity.KEYNOTE_ID;
	public static final String KEYNOTE_EMAIL = KeynoteSessionActivity.KEYNOTE_EMAIL;

	
    ExpandableListView resultsView;
    
    List<String> headers;
    
	/**
	 * Updates all the views present in this activity
	 * @param sightsTriple 
	 * 
	 * @param result
	 *            the object to use to populate data
	 */
	private void updateViews(String search) {

		setContentView(R.layout.activity_search_results);
				
		Log.i("UPDATE","Update View...");
				
		List<Author> authors = getAuthorsBySearch(search); 
		List<Keynote> keynotes = getKeynotesBySearch(search); 
		
		if(authors.size()==0 && keynotes.size()==0){
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
	        }
	
	        
	        final SearchResultsViewAdapter resultsViewAdapter = new SearchResultsViewAdapter(this, headers, resultsMap);
	        resultsView.setAdapter(resultsViewAdapter);
	        for(int i=0; i<headers.size();i++){
	        	resultsView.expandGroup(i);
	        }
	        resultsView.setOnChildClickListener(new OnChildClickListener() {
	
	        	public boolean onChildClick(ExpandableListView parent, View v,
	                    int groupPosition, int childPosition, long id) {
	            	
	            	final String header = (String) resultsViewAdapter.getGroup(groupPosition);
	                @SuppressWarnings("unchecked")
					final Pair<String,Long> element = (Pair<String,Long>) resultsViewAdapter.getChild(
	                        groupPosition, childPosition);
	
	                final Long idElement = element.getSecond();
	                
	                	Intent intent;
	                	
	                	if(header.equals(AUTHORS)){
	                		intent = new Intent(AuthorsAndKeynotesSearchResults.this, AuthorActivity.class);
	                		intent.putExtra(ValuesHelper.AUTHOR_ID, idElement);
	                	}else{
	                		intent = new Intent(AuthorsAndKeynotesSearchResults.this, KeynoteSpeakerActivity.class);
	                		intent.putExtra(ValuesHelper.KEYNOTE_ID, idElement);
	                	}              	
	    				 
	    				startActivity(intent);
	
	                
	                return true;
	            }
	        });
			}
			
	}


	
	private List<Author> getAuthorsBySearch(String text) {
		
		authorsDB.open();
		List<Author> authors = authorsDB.search(text);
		authorsDB.close();
		return authors;
		
	}



	private List<Keynote> getKeynotesBySearch(String text) {
		
		keynotesDB.open();
		List<Keynote> keynotes = keynotesDB.search(text);
		keynotesDB.close();
		return keynotes;
		
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
