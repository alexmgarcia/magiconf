package com.example.magiconf_client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tables.City;
import tables.Hotel;
import tables.Restaurant;
import tables.Sight;
import utils.Triple;
import utils.ValuesHelper;
import adapters.SightsListViewAdapter;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import dataRetrieving.AuthInfo;
import dataRetrieving.CityData;
import dataRetrieving.HotelData;
import dataRetrieving.RestaurantData;
import dataRetrieving.SightData;
import dataSources.ConferenceDataSource;
import dataSources.SightsDataSource;

/**
 * @author José
 *
 * This class represents the City Activity
 */
public class CityActivity extends Activity{

	private SightsDataSource sightsDB = new SightsDataSource(this);
	private ConferenceDataSource conferenceDB = new ConferenceDataSource(this);
	
	private static final String HOTELS = "Hotéis";
	private static final String RESTAURANTS = "Restaurantes";
	private static final String OTHER_SIGHTS = "Outros";
	private static final String CITY = "Cidade";
	private static final String DESCRIPTION = "Descrição da Cidade";
//	public static final String SIGHT_ID = "sightID";
	
    ExpandableListView descView;
    ExpandableListView sightsView;
    
    List<Sight> sights;
    List<String> headers;
    
	/**
	 * Updates all the views present in this activity
	 * @param sightsTriple 
	 * 
	 * @param result
	 *            the object to use to populate data
	 */
	private void updateViews() {

		setContentView(R.layout.activity_city);
				
		Log.i("UPDATE","Update View...");
		City city = getCity();
		Triple<List<Hotel>,List<Restaurant>,List<Sight>> sights = getAllSights();
		
		TextView textView = (TextView) this.findViewById(R.id.city_name);
		textView.setText(city.getName());
	
		ImageView imageView = (ImageView) this
				.findViewById(R.id.city_photo);
		UrlImageViewHelper.setUrlDrawable(imageView, ValuesHelper.SERVER_API_ADDRESS_MEDIA+city.getPhoto(), null, UrlImageViewHelper.CACHE_DURATION_INFINITE);
		if(imageView.getDrawable()==null){
			imageView.setImageResource(R.drawable.city_dummy);
		}
        sightsView = (ExpandableListView) findViewById(R.id.sights_view);
        
        headers = new ArrayList<String>();
        headers.add(DESCRIPTION);
          
        
        Map<String, List<Long>> sightsNames = new LinkedHashMap<String, List<Long>>();
        
        List<Long> ids = new ArrayList<Long>();
        
        ids.add((long) 0);
        
        sightsNames.put(DESCRIPTION, ids);
        
        ids = new ArrayList<Long>();
        for(Hotel h: sights.getFirst()){
        	ids.add(h.getHotelID());
        }
        
        if(ids.size()!=0){
        	headers.add(HOTELS);
        	sightsNames.put(HOTELS, ids);
        	ids = new ArrayList<Long>();
        }
                
        for(Restaurant r: sights.getSecond()){
        	ids.add(r.getRestaurantID());
        }
  
        if(ids.size()!=0){
        	headers.add(RESTAURANTS);
        	sightsNames.put(RESTAURANTS, ids);
        	ids = new ArrayList<Long>();
        }
        
        
        for(Sight s: sights.getThird()){
        	ids.add(s.getId());
        }

        if(ids.size()!=0){
        	 headers.add(OTHER_SIGHTS);
        	sightsNames.put(OTHER_SIGHTS, ids);
        }
        
        Log.i("#HEADERS",""+headers.size());
        
        final SightsListViewAdapter sightsListAdapter = new SightsListViewAdapter(this, headers, sightsNames, city.getDescription(), sightsDB);
        sightsView.setAdapter(sightsListAdapter);
 
        sightsView.setOnChildClickListener(new OnChildClickListener() {

        	public boolean onChildClick(ExpandableListView parent, View v,
                    int groupPosition, int childPosition, long id) {
            	
            	final String header = (String) sightsListAdapter.getGroup(groupPosition);
                final Long idSight = (Long) sightsListAdapter.getChild(
                        groupPosition, childPosition);
                
                if(groupPosition!=0){
                	
                	sightsDB.open();
                	
                	Intent intent;
                	
                	if(header.equals(HOTELS)){
                		intent = new Intent(CityActivity.this, HotelActivity.class);
                	}else if(header.equals(RESTAURANTS)){
                		intent = new Intent(CityActivity.this, RestaurantActivity.class);
                	}else intent = new Intent(CityActivity.this, SightActivity.class);
                	
    				intent.putExtra(ValuesHelper.SIGHT_ID, idSight); 
    				startActivity(intent);
                	sightsDB.close();
                }
                return true;
            }
        });

		
	}


	
	private Triple<List<Hotel>, List<Restaurant>, List<Sight>> getAllSights(){
		sightsDB.open();
		List<Hotel> hotels = sightsDB.getAllHotels();
		List<Restaurant> rests = sightsDB.getAllRestaurants();
		List<Sight> sights = sightsDB.getAllSights();
		sightsDB.close();
		return new Triple<List<Hotel>, List<Restaurant>, List<Sight>>(hotels,rests,sights);
	}

	
	/**
	 * @author José
	 * Asynchronous REST GET request
	 */
	private class RESTGetTask extends AsyncTask<String, Void, Pair<String, List<JSONObject>>> {
		protected Pair<String, List<JSONObject>> doInBackground(String... params) {
			
			List<JSONObject> list = null;
			if(params[0].equals(HOTELS)){
				list = HotelData.downloadHotels(AuthInfo.username, AuthInfo.password);
			}else if(params[0].equals(RESTAURANTS)){
				list = RestaurantData.downloadRestaurants(AuthInfo.username, AuthInfo.password);
			}else if(params[0].equals(OTHER_SIGHTS)){
				list = SightData.downloadSights(AuthInfo.username,AuthInfo.password);
			}else list = CityData.downloadCity(AuthInfo.username, AuthInfo.password);
			return new Pair<String, List<JSONObject>>(params[0], list);
		}

		
		protected void onPostExecute(Pair<String, List<JSONObject>> result) {
			// This is new data, so let's populate the data in views and insert it into the database
			try {
				Iterator<JSONObject> it = result.second.iterator();
				JSONObject aux = null;
				while(it.hasNext()){
					Log.i("POST_EXECUTE", "On postExecute");
					aux =  it.next();
					JSONArray data = aux.getJSONArray("objects");
					if(result.first.equals(HOTELS)){
						Log.i("POST_EXECUTE", "HOTEL branch");
						HotelData.createHotels(data,sightsDB,CityActivity.this);
					}else if(result.first.equals(RESTAURANTS)){
						Log.i("POST_EXECUTE", "RESTAURANT branch");
						RestaurantData.createRestaurants(data,sightsDB,CityActivity.this);
					}else if(result.first.equals(OTHER_SIGHTS)){
						Log.i("POST_EXECUTE", "OTHER SIGHT branch");
						SightData.createSights(data,sightsDB,CityActivity.this);
					}else{
						Log.i("POST_EXECUTE", "Keynote branch");
						CityData.createCity(data,conferenceDB);
						updateViews();
					}
				}
				

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
		setContentView(R.layout.activity_city);
	
		// Get an existing city
		City city = getCity();
        
		if (city == null){
			new RESTGetTask().execute(HOTELS);
			new RESTGetTask().execute(RESTAURANTS);
			new RESTGetTask().execute(OTHER_SIGHTS);
			new RESTGetTask().execute(CITY);
		}else{		
			updateViews();
		}
		
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

	/**
	 * Return the city into the database
	 * @return the city
	 */
	private City getCity() {
		conferenceDB.open();
		City c = conferenceDB.getCity();
		conferenceDB.close();
		return c;
	}
	
	private void createNewSights(JSONArray data) {
		Sight aux;
		Log.i("CREATE SIGHTS", "Start adding");
		sightsDB.open();
		Log.i("CREATE SIGHTS", ""+data.length());
		for(int i = 0 ; i < data.length() ; i++){
			try {
				JSONObject k = data.getJSONObject(i);
				
				String name= k.getString("name");
				String address = k.getString("address");
				if(!sightsDB.hasSight(name, address)){
					aux = sightsDB.createNewSight(name, address, k.getString("description"), k.getDouble("latitude"), k.getDouble("longitude"), k.getString("photo"));			
					Log.i("CREATE SIGHT", "Creating Sight"+aux.toString());
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		sightsDB.close();
		
	}

	private void createNewRestaurants(JSONArray data) {
		Restaurant aux;
		Log.i("CREATE RESTAURANTS", "Start adding");
		sightsDB.open();
		Log.i("CREATE RESTAURANTS", ""+data.length());
		for(int i = 0 ; i < data.length() ; i++){
			try {
				JSONObject k = data.getJSONObject(i);
				
				String name= k.getString("name");
				String address = k.getString("address");
				if(!sightsDB.hasSight(name, address)){
					aux = sightsDB.createRestaurant(name, address, k.getString("description"), k.getDouble("latitude"), k.getDouble("longitude"),k.getString("photo"),k.getString("phone_number"));	
					Log.i("CREATE RESTAURANT", "Creating Restaurant"+aux.toString());
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		sightsDB.close();	
	}
	

	private void createNewHotel(JSONArray data) {
		Hotel aux;
		Log.i("CREATE HOTELS", "Start adding");
		sightsDB.open();
		Log.i("CREATE HOTELS", ""+data.length());
		for(int i = 0 ; i < data.length() ; i++){
			try {
				JSONObject k = data.getJSONObject(i);
				String name= k.getString("name");
				String address = k.getString("address");
				if(!sightsDB.hasSight(name, address)){
					aux = sightsDB.createHotel(name, address, k.getString("description"), k.getDouble("latitude"), k.getDouble("longitude"), k.getString("photo"), k.getInt("stars"), k.getString("phone_number"));
					Log.i("CREATE HOTEL", "Creating Hotel"+aux.toString());
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		sightsDB.close();
		
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
		getMenuInflater().inflate(R.menu.city, menu);
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
		case R.id.action_search:
			onSearchRequested();
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
