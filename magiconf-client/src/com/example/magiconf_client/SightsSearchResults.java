package com.example.magiconf_client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import tables.Hotel;
import tables.Restaurant;
import tables.Sight;
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

import dataSources.SightsDataSource;

/**
 * @author José
 *
 * This class represents the Sights Search Results activity
 */
public class SightsSearchResults extends Activity{

	private SightsDataSource sightsDB = new SightsDataSource(this);
	
	private static final String HOTELS = "Hotéis";
	private static final String RESTAURANTS = "Restaurantes";
	private static final String OTHERS = "Outros";
	//public static final String SIGHT_ID = CityActivity.SIGHT_ID;

	
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
		
		Triple<List<Hotel>,List<Restaurant>,List<Sight>> results = getSightsBySearch(search);
		
		List<Hotel> hotels = results.getFirst(); 
		List<Restaurant> restaurants = results.getSecond();
		List<Sight> others = results.getThird();
		
		if(hotels.size()==0 && restaurants.size()==0 && others.size()==0){
			TextView view = (TextView) findViewById(R.id.no_results);
			view.setText(this.getString(R.string.NO_RESULTS));			
		}else{
	        resultsView = (ExpandableListView) findViewById(R.id.search_results_view);
	        
	        headers = new ArrayList<String>();
	            
	        Map<String, List<Pair<String, Long>>> resultsMap = new LinkedHashMap<String, List<Pair<String,Long>>>();
	        
	        List<Pair<String,Long>> temp = new ArrayList<Pair<String,Long>>();
	        
	        if(hotels.size()>0){
		        for(Hotel h: hotels){
		        	temp.add(new Pair<String,Long>(h.getName(), h.getHotelID()));
		        }
		        headers.add(HOTELS);
		        resultsMap.put(HOTELS, temp);
		        temp = new ArrayList<Pair<String,Long>>(); 
	        }
	        
	        if(restaurants.size()>0){
		        for(Restaurant r: restaurants){
		        	temp.add(new Pair<String,Long>(r.getName(), r.getRestaurantID()));
		        }
		        headers.add(RESTAURANTS);
		        resultsMap.put(RESTAURANTS, temp); 
		        temp = new ArrayList<Pair<String,Long>>();
	        }
	        
	        if(others.size()>0){
		        for(Sight s: others){
		        	temp.add(new Pair<String,Long>(s.getName(), s.getId()));
		        }
		        headers.add(OTHERS);
		        resultsMap.put(OTHERS, temp); 
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
	                	
	                	if(header.equals(HOTELS)){
	                		intent = new Intent(SightsSearchResults.this, HotelActivity.class);
	                		intent.putExtra(ValuesHelper.SIGHT_ID, idElement);
	                	}else if(header.equals(RESTAURANTS)){
	                		Log.i("rest", "restaurant");
	                		intent = new Intent(SightsSearchResults.this, RestaurantActivity.class);
	                		intent.putExtra(ValuesHelper.SIGHT_ID, idElement);
	                	}else{
	                		intent = new Intent(SightsSearchResults.this, SightActivity.class);
	                		intent.putExtra(ValuesHelper.SIGHT_ID, idElement);	                		
	                	}
	    				 
	    				startActivity(intent);
	
	                
	                return true;
	            }
	        });
			}
			
	}
	
	Triple<List<Hotel>,List<Restaurant>,List<Sight>> getSightsBySearch(String text){
		
		sightsDB.open();
		Triple<List<Hotel>,List<Restaurant>,List<Sight>> results = sightsDB.search(text);
		sightsDB.close();
		return results;
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
		//TODO: Tratar do intent
		
	    // Get the intent, verify the action and get the query
	    Intent intent = getIntent();
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	      String query = intent.getStringExtra(SearchManager.QUERY);
	      updateViews(query);
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
		
		
		// Show the Up button in the action bar.
		setupActionBar();
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
