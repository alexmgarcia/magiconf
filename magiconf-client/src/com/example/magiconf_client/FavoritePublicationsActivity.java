package com.example.magiconf_client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import tables.FavPublication;
import utils.ValuesHelper;

import dataRetrieving.AuthInfo;
import dataSources.FavPublicationDataSource;
import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class FavoritePublicationsActivity extends Activity {
	
	
	private FavPublicationDataSource favPubsDB = new FavPublicationDataSource(this);
	private ListAdapter arrayAdapter;
	
	/**
	 * Updates all the views present in this activity
	 * 
	 * @param result
	 *            the object to use to populate data
	 */
	private void updateViews() {
		favPubsDB.open();
		List<FavPublication> favPubsList = favPubsDB.getAllFavPublications(AuthInfo.username);
		favPubsDB.close();
		List<FavPublication> favPubs = new ArrayList<FavPublication>(favPubsList.size());
		Iterator<FavPublication> it = favPubsList.iterator();
		while (it.hasNext())
			favPubs.add(it.next());
		
		setContentView(R.layout.activity_favorite_publications);
		final ListView favPubsListView = (ListView) findViewById(R.id.favorite_publications);
		favPubsListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			  public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
				FavPublication favPub = (FavPublication) favPubsListView.getItemAtPosition(position);
				favPubsDB.open();
				Intent intent;
				if (favPubsDB.checkifIsArticle(favPub)) {
					
					//TODO: Colocar se é parentID
					intent = new Intent(FavoritePublicationsActivity.this, ArticleActivity.class);
					intent.putExtra(ValuesHelper.IS_PARENT_ID, true);
					intent.putExtra(ValuesHelper.PUBLICATION_ID, Long.valueOf(favPub.getPublicationID()));
				}
				else {
					intent = new Intent(FavoritePublicationsActivity.this, PublicationDetailsActivity.class);
					intent.putExtra(ValuesHelper.IS_PARENT_ID, true);
					intent.putExtra(ValuesHelper.PUBLICATION_ID, Long.valueOf(favPub.getPublicationID()));
				}
				favPubsDB.close();
				startActivity(intent);
			}
		});
		arrayAdapter = new ArrayAdapter<FavPublication>(this, R.layout.favorite_publication, favPubs);
		favPubsListView.setAdapter(arrayAdapter);
        registerForContextMenu(favPubsListView);  
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
		setContentView(R.layout.activity_favorite_publications);
		updateViews();
		
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
	
	@Override
	public void onRestart() {
		super.onRestart();
		setContentView(R.layout.activity_favorite_publications);
		updateViews();
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
		getMenuInflater().inflate(R.menu.favorite_publications, menu);
		return true;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.favorite_publications_context_menu, menu);
		ListView favPubsListView = (ListView) findViewById(R.id.favorite_publications);
		@SuppressWarnings("unchecked")
		ArrayAdapter<FavPublication> adapter = (ArrayAdapter<FavPublication>) favPubsListView.getAdapter();
		adapter.notifyDataSetChanged();
	}
	
	/**
	 * Removes the specified favorite publication from favorites
	 * @param favPub favorite publication to remove from favorites
	 */
	private void removeFromFavorites(FavPublication favPub) {
		favPubsDB.open();
		favPubsDB.deleteFavPublication(favPub);
		favPubsDB.close();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, MainMenu.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		case R.id.action_search:
			onSearchRequested();
			return true;
		case R.id.action_about:
			intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
			return true;
		default: break;
		}
			return super.onOptionsItemSelected(item);
	}
	
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.remove_from_favorites:
			ListView favPubsListView = (ListView) findViewById(R.id.favorite_publications);
			FavPublication favpub = (FavPublication) favPubsListView.getItemAtPosition(info.position);
			removeFromFavorites(favpub);
			@SuppressWarnings("unchecked")
			ArrayAdapter<FavPublication> adapter = (ArrayAdapter<FavPublication>) favPubsListView.getAdapter();
			adapter.remove(favpub);
			adapter.notifyDataSetChanged();
		default:
			return super.onContextItemSelected(item);
		}
	}

}
