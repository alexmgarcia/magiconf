package com.example.magiconf_client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import tables.FavPublication;
import utils.ValuesHelper;

import dataRetrieving.AuthInfo;
import dataSources.FavPublicationDataSource;
import adapters.Helper;
import android.os.Bundle;
import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FavPublicationsSearchResults extends Activity {
	
	//protected static final String PUBLICATION_ID = "publication_id";
	
	private FavPublicationDataSource favPubsDB = new FavPublicationDataSource(this);
	
	/**
	 * Updates all the views present in this activity
	 * 
	 * @param result
	 *            the object to use to populate data
	 */
	private void updateViews(String query) {
		
		setContentView(R.layout.fav_publications_search_results);
		
		favPubsDB.open();
		List<FavPublication> results = favPubsDB.search(AuthInfo.username,query);
		favPubsDB.close();
		
		if(results.size()==0){
			TextView view = (TextView) findViewById(R.id.no_results);
			view.setText(this.getString(R.string.NO_RESULTS));
		}else{
		List<FavPublication> favPubs = new ArrayList<FavPublication>(results.size());
		Iterator<FavPublication> it = results.iterator();
		while (it.hasNext())
			favPubs.add(it.next());
		
		final ListView favPubsListView = (ListView) findViewById(R.id.favorite_publications_results);
		favPubsListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			  public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
				FavPublication favPub = (FavPublication) favPubsListView.getItemAtPosition(position);
				favPubsDB.open();
				Intent intent;
				if (favPubsDB.checkifIsArticle(favPub)) {
					intent = new Intent(FavPublicationsSearchResults.this, ArticleActivity.class);
					intent.putExtra(ValuesHelper.PUBLICATION_ID, Long.valueOf(favPub.getPublicationID()));
				}
				else {
					intent = new Intent(FavPublicationsSearchResults.this, PublicationDetailsActivity.class);
					intent.putExtra(ValuesHelper.PUBLICATION_ID, Long.valueOf(favPub.getPublicationID()));
				}
				favPubsDB.close();
				startActivity(intent);
			}
		});
		// This allows to have a list inside a scrollview
		Helper.getListViewSize(favPubsListView);
		ListAdapter arrayAdapter = new ArrayAdapter<FavPublication>(this, R.layout.favorite_publication, favPubs);
		favPubsListView.setAdapter(arrayAdapter);
        registerForContextMenu(favPubsListView);  
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
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
		default: break;
		}
			return super.onOptionsItemSelected(item);
	}
	

}
