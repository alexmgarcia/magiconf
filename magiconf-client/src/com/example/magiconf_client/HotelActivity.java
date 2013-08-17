package com.example.magiconf_client;

import tables.Hotel;
import utils.ValuesHelper;
import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import dataSources.SightsDataSource;

/**
 * @author José
 *
 * This class represents the Hotel Activity
 */

public class HotelActivity extends Activity {

	private SightsDataSource sightsDB = new SightsDataSource(this); // the database

	/**
	 * Updates all the views present in this activity
	 * 
	 * @param result
	 *            the object to use to populate data
	 */
	private void updateViews(final Hotel result) {

		setContentView(R.layout.activity_hotel);

		TextView textView = (TextView) this.findViewById(R.id.local);
		if(result==null){
			Log.i("HOTEL","null");
		}
		if(textView==null){
			Log.i("TEXTview","null");
		}
		textView.setText(result.getAddress());

		textView = (TextView) this.findViewById(R.id.contact);
		textView.setText(result.getPhone());
		RelativeLayout contactLayout = (RelativeLayout) findViewById(R.id.contact_layout);
		contactLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent callIntent = new Intent(Intent.ACTION_CALL);
				callIntent.setData(Uri.parse("tel:"+result.getPhone()));
				startActivity(callIntent);

			}
		});

		ImageView imageView = (ImageView) this
				.findViewById(R.id.photo);
		UrlImageViewHelper.setUrlDrawable(imageView, ValuesHelper.SERVER_API_ADDRESS_MEDIA+result.getPhoto(), null, UrlImageViewHelper.CACHE_DURATION_INFINITE);
		if(imageView.getDrawable()==null){
			imageView.setImageResource(R.drawable.hotel_dummy);
		}
		textView = (TextView) this.findViewById(R.id.gps);
		double lat = result.getLatitude();
		double lon = result.getLongitude();

		textView.setText(lat+", "+lon);
		RelativeLayout gpsLayout = (RelativeLayout) findViewById(R.id.gps_layout);
		gpsLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				double lat = result.getLatitude();
				double lon = result.getLongitude();
				String label = result.getName();
				Log.i("LABEL",label);
				String uriBegin = "geo:" + lat + "," + lon;
				String query = lat + "," + lon + "(" + label + ")";
				String encodedQuery = Uri.encode(query);
				String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
				Uri uri = Uri.parse(uriString);
				Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});
		
		textView = (TextView) this.findViewById(R.id.description);
		textView.setText(result.getDescription());

		ActionBar actionBar = getActionBar();
		actionBar.setTitle(result.getName()+" "+result.getStars()+"*");
	}

	private Hotel getHotel(Long id) {
		sightsDB.open();
		Hotel s = sightsDB.getHotel(id); 
		sightsDB.close();
		return s;
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
		Intent intent = getIntent();
		Long id = intent.getLongExtra(ValuesHelper.SIGHT_ID, 2); //TODO
		Hotel s = getHotel(id);

		updateViews(s);

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
		getMenuInflater().inflate(R.menu.sight, menu);
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
