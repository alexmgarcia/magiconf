package com.example.magiconf_client;

import tables.Author;
import tables.Member;
import utils.ValuesHelper;
import android.app.ActionBar;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import dataRetrieving.AuthInfo;
import dataRetrieving.AuthorData;
import dataRetrieving.MemberData;
import dataSources.MemberDataSource;

/**
 * @author José
 *
 * This class represents the Member Activity
 */

public class MemberActivity extends Activity {
	
	private MemberDataSource membersDB = new MemberDataSource(this); // the database

	/**
	 * Updates all the views present in this activity
	 * 
	 * @param result
	 *            the object to use to populate data
	 */
	private void updateViews(final Member result) {

		setContentView(R.layout.activity_member);
		
		ImageView imageView = (ImageView) this
				.findViewById(R.id.photo);
		UrlImageViewHelper.setUrlDrawable(imageView, ValuesHelper.SERVER_API_ADDRESS_MEDIA+result.getPhoto(), null, UrlImageViewHelper.CACHE_DURATION_INFINITE);
		if(imageView.getDrawable()==null){
			imageView.setImageResource(R.drawable.dummy_user_photo);
 		}
		TextView textView = (TextView) this.findViewById(R.id.role);
		textView.setText(result.getRole());
		
		textView = (TextView) this.findViewById(R.id.country);
		textView.setText(result.getCountry());
		
		textView = (TextView) this.findViewById(R.id.organization);
		textView.setText(result.getWork_place());
		
		textView = (TextView) this.findViewById(R.id.email);
		textView.setText(result.getEmail());
				
		ActionBar actionBar = getActionBar();
		actionBar.setTitle(result.getName());
	}
	
	private Member getMember(Long id) {
		membersDB.open();
		Member m = membersDB.getMember(id); 
		membersDB.close();
		return m;
	}
	

	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
		Intent intent = getIntent();
		Long id = intent.getLongExtra(MembersActivity.MEMBER_ID, 2); 
		Member m = getMember(id);
		
		updateViews(m);

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
		getMenuInflater().inflate(R.menu.member, menu);
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
