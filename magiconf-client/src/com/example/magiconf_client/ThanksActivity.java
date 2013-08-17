package com.example.magiconf_client;

import java.util.ArrayList;
import java.util.List;

import tables.Developer;
import adapters.DevelopersListViewAdapter;
import adapters.ThanksListViewAdapter;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.ListView;

public class ThanksActivity extends Activity{
	private List<String> persons=new ArrayList<String>();

	/**
	 * Updates all the views present in this activity
	 * 
	 * @param result
	 *            the object to use to populate data
	 */
	private void updateViews() {
		setContentView(R.layout.activity_thanks);
		ListView myListView= (ListView) findViewById(R.id.persons);
		
		myListView.setAdapter( new ThanksListViewAdapter(this, persons));
		
	}
	
	public List<String> getPersons() {
		return persons;
		
		
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
		
		createPersons();
			
				
		updateViews();
		
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
	
	
	
	private void createPersons(){
		
		
		String p1 = getString(R.string.p1);
		persons.add(p1);
	    p1 = getString(R.string.p2);
	    persons.add(p1);
	    p1 = getString(R.string.p3);
	    persons.add(p1);
	    p1 = getString(R.string.p4);
	    persons.add(p1);
	    p1 = getString(R.string.worldcist);
	    persons.add(p1);
	    p1 = getString(R.string.p5);
	    persons.add(p1);
	    p1 = getString(R.string.p6);
	    persons.add(p1);
	    p1 = getString(R.string.p7);
	    persons.add(p1);
	    p1 = getString(R.string.p8);
	    persons.add(p1);
	    p1 = getString(R.string.p9);
	    persons.add(p1);
	    p1 = getString(R.string.p10);
	    persons.add(p1);
	    p1 = getString(R.string.p11);
	    persons.add(p1);
	    p1 = getString(R.string.p12);
	    persons.add(p1);
	    
		Log.i("size",""+ persons.size() );
		
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
		getMenuInflater().inflate(R.menu.thanks, menu);
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
