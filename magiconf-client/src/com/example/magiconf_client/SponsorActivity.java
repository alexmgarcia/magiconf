package com.example.magiconf_client;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import tables.Article;
import tables.PosterSession;
import tables.Sponsor;
import utils.RestClient;
import utils.ValuesHelper;


import dataRetrieving.AuthInfo;
import dataRetrieving.CityData;
import dataRetrieving.EventData;
import dataRetrieving.HotelData;
import dataRetrieving.RestaurantData;
import dataRetrieving.SightData;
import dataRetrieving.SponsorData;
import dataSources.SponsorDataSource;

import adapters.SponsorListViewAdapter;
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
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SponsorActivity extends Activity {
	
	private SponsorDataSource sponsorDB= new SponsorDataSource(this);
	List<Sponsor> sponsors;
	
	private void updateViews(){
		setContentView(R.layout.activity_sponsors);
		GridView myGridView= (GridView) findViewById(R.id.GridView);
		myGridView.setAdapter(new SponsorListViewAdapter(this, sponsors));
		
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
		sponsorDB.open();
		sponsors= sponsorDB.getAllSponsors();
		sponsorDB.close();
		
		if(sponsors.size()==0){
			new RESTGetTask().execute();
		} else{
			updateViews();
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
		setupActionBar();
	}
	

	
	private class RESTGetTask extends AsyncTask<String, Void, List<JSONObject>> {
		 
		protected List<JSONObject> doInBackground(String... params) {

			return  SponsorData.downloadSponsors(AuthInfo.username, AuthInfo.password);
				
		  }
		  
		  
		  protected void onPostExecute(List<JSONObject> result) {
			  	
			  try {
					Iterator<JSONObject> it = result.iterator();
					JSONObject aux = null;
					while(it.hasNext()){
						Log.i("POST_EXECUTE", "On postExecute");
						aux =  it.next();
						JSONArray data = aux.getJSONArray("objects");
						
							SponsorData.createNewSponsor(data,sponsorDB,SponsorActivity.this);
					}
					sponsorDB.open();
					sponsors= sponsorDB.getAllSponsors();
					sponsorDB.close();
							updateViews();					

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			   
		 }

		  private void createNewSponsor(JSONArray data) {
			  Sponsor aux;
			  sponsorDB.open();
			  for(int i = 0 ; i < data.length() ; i++){
			   try {
			    JSONObject k = data.getJSONObject(i);
			    
			    String name= k.getString("name");
			    if(!sponsorDB.hasSponsor(name)){
			     aux = sponsorDB.createSponsor(name, k.getString("logo"), k.getString("website")); 
			     sponsors.add(aux);
		
			    }
			   } catch (JSONException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			   }
			  }
			  
			  sponsorDB.close();
	  
	 }



	
			/**
			 * Set up the {@link android.app.ActionBar}.
			 */
			private void setupActionBar() {
				getActionBar().setHomeButtonEnabled(true);
				getActionBar().setDisplayShowHomeEnabled(true);
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
					return super.onOptionsItemSelected(item);
				}
			}

    

	}

	
