package com.example.magiconf_client;

import java.util.ArrayList;
import java.util.List;

import tables.Developer;
import tables.Member;
import tables.Sponsor;
import utils.ValuesHelper;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;


import adapters.DevelopersListViewAdapter;
import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class DeveloperActivity  extends Activity{
	List<Developer> developers= new ArrayList<Developer>();
	private static final String DEVELOPER_ID = "id";
	private static final String DEVELOPER_1="Alexandre Garcia";
	private static final int DEVELOPER1_NUMBER=34625;
	private static final String DEVELOPER_2="David Semedo";
	private static final int DEVELOPER2_NUMBER=35133;
	private static final String DEVELOPER_3="Farah Mussa";
	private static final int  DEVELOPER3_NUMBER=34973;
	private static final String DEVELOPER_4="José Arjona";
	private static final int DEVELOPER4_NUMBER=34250;
	

	/**
	 * Updates all the views present in this activity
	 * 
	 * @param result
	 *            the object to use to populate data
	 */
	private void updateViews() {
		setContentView(R.layout.activity_developers);
		ListView myListView= (ListView) findViewById(R.id.developers);
		
		myListView.setAdapter(new DevelopersListViewAdapter(this, developers));
		
	}
	
	public List<Developer> getDevelopers() {
		return developers;
		
		
	}
	

	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		createDevelopers();
			
				
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
	
	
	
	private void createDevelopers(){
		
		
		Developer developer = new Developer(DEVELOPER_1, DEVELOPER1_NUMBER);
		developers.add(developer);
		Developer developer1 = new Developer(DEVELOPER_2, DEVELOPER2_NUMBER);
		developers.add(developer1);
		developer =new Developer(DEVELOPER_3, DEVELOPER3_NUMBER);
		developers.add(developer);
		developer =new Developer(DEVELOPER_4, DEVELOPER4_NUMBER);
		developers.add(developer);
		Log.i("size",""+ developers.size() );
		
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
		getMenuInflater().inflate(R.menu.developer, menu);
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
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
