package com.example.magiconf_client;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tables.Conference;
import utils.RestClient;
import utils.ValuesHelper;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import dataRetrieving.AuthInfo;
import dataRetrieving.AuthorData;
import dataRetrieving.ConferenceData;
import dataSources.ConferenceDataSource;

/**
 * @author José
 *
 * This class represents the Conference Activity
 */
public class ConferenceActivity extends Activity{

	private ConferenceDataSource conferenceDB = new ConferenceDataSource(this); // the database

	/**
	 * Updates all the views present in this activity
	 * 
	 * @param result
	 *            the object to use to populate data
	 */
	private void updateViews(final Conference result) {

		setContentView(R.layout.conference_activity);
				
		TextView textView = (TextView) this.findViewById(R.id.title);
		textView.setText(result.getTitle());
		
		textView = (TextView) this.findViewById(R.id.edition);
		
		textView.setText(result.getEdition()+"ª Edição");

		ImageView imageView = (ImageView) this
				.findViewById(R.id.conference_photo);
		UrlImageViewHelper.setUrlDrawable(imageView, ValuesHelper.SERVER_API_ADDRESS_MEDIA+result.getLogo(), null, UrlImageViewHelper.CACHE_DURATION_INFINITE);
		if(imageView.getDrawable()==null){
			imageView.setImageResource(R.drawable.logo_horizontal);
 		}
		String beginning = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(result.getBeginningDate());
		String ending = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(result.getEndingDate());
		
		textView = (TextView) this.findViewById(R.id.conference_beginning);
		textView.setText(beginning);
		
		textView = (TextView) this.findViewById(R.id.conference_ending);
		textView.setText(ending);	
		
		textView = (TextView) this.findViewById(R.id.conference_place);
		textView.setText(result.getPlace());
		
		textView = (TextView) this.findViewById(R.id.conference_gps);
		double lat = result.getLatitude();
		double lon = result.getLongitude();
		Log.i("LONG",""+lon);
		textView.setText(lat+", "+lon);
		textView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				double lat = result.getLatitude();
				double lon = result.getLongitude();
				String label = result.getTitle();
				String uriBegin = "geo:" + lat + "," + lon;
				String query = lat + "," + lon + "(" + label + ")";
				String encodedQuery = Uri.encode(query);
				String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
				Uri uri = Uri.parse(uriString);
				Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});
		
		ImageView im = (ImageView) this.findViewById(R.id.conference_gps_image);
		im.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				double lat = result.getLatitude();
				double lon = result.getLongitude();
				String label = result.getTitle();
				String uriBegin = "geo:" + lat + "," + lon;
				String query = lat + "," + lon + "(" + label + ")";
				String encodedQuery = Uri.encode(query);
				String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
				Uri uri = Uri.parse(uriString);
				Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});
		
		textView = (TextView) this.findViewById(R.id.conference_website);
		textView.setText(result.getWebsite());
		textView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String url = result.getWebsite();
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});
		
		im = (ImageView) this.findViewById(R.id.conference_website_image);
		im.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String url = result.getWebsite();
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});
		
		
		textView = (TextView) this.findViewById(R.id.conference_description);
		textView.setText(result.getDescription());
	
	}

	/**
	 * Inserts a new author into the database
	 * 
	 * @param data
	 *            object to use to populate data
	 * @return the new author
	 * @throws ParseException 
	 */
	private void createNewConference(JSONArray data) throws ParseException {
		Conference newConf = null;
		conferenceDB.open();

		try {
			JSONObject k = data.getJSONObject(0);

		    SimpleDateFormat dateFormat = new SimpleDateFormat(ValuesHelper.DATE_FORMAT_SERVER, Locale.US);
		    Date beginDate = dateFormat.parse(k.getString("beginning_date")); 
		    Date endDate = dateFormat.parse(k.getString("ending_date")); 
			newConf = conferenceDB.createConference(k.getString("name"),beginDate,
					endDate, k.getString("description"),k.getInt("edition"), 
					k.getString("place"), k.getDouble("latitude"),k.getDouble("longitude"), 
					k.getString("website"),k.getString("photo"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		conferenceDB.close();
		updateViews(newConf);
	}

	private Conference getConference() throws ParseException {
		conferenceDB.open();
		Conference c = conferenceDB.getConference();
		conferenceDB.close();
		return c;
	}

	

	
	/**
	 * @author José
	 * Asynchronous REST GET request
	 */
	private class RESTGetTask extends AsyncTask<String, Void, Conference> {
		protected Conference doInBackground(String... params) {
/*
			List<JSONObject> list = new LinkedList<JSONObject>();
			JSONObject jData = RestClient.makeRequest(params[0],false,0);
			int total = 0;
			int limit= 0;
			try {
				JSONObject aux =(JSONObject) jData.get("meta");
				total = aux.getInt("total_count");
				limit = aux.getInt("limit");

				int offset = 0;
				list.add(jData);
				if(limit < total){
					offset +=limit;
					while(offset < total ){
						jData = RestClient.makeRequest(params[0],true,offset);
						offset+=limit;
						list.add(jData);
					}
				}
					
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Log.i("ASYNC_TASK", "Number requests: "+list.size());


			return new ArrayList<JSONObject>(list);*/
			return ConferenceData.downloadConferenceData(ConferenceActivity.this,AuthInfo.username, AuthInfo.password, conferenceDB);
		}

		//TODO: Funciona para apenas a cidade, mas é necessário modificar para pontos de interesse...
		protected void onPostExecute(Conference result) {
			// This is new data, so let's populate the data in views and insert it into the database
			if(result!=null){
				updateViews(result);
			}else{
				Toast.makeText(getBaseContext(), "Informação indisponível", Toast.LENGTH_LONG).show();
			}

		}

	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
		// Get an existing conference
		Conference conf=null;
		try {
			conf = getConference();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (conf == null) // it doesn't exist into the local database //TODO: não entra id, mas email
			new RESTGetTask().execute();
		else updateViews(conf);
		
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
		getMenuInflater().inflate(R.menu.conference, menu);
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
