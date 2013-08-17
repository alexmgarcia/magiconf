package com.example.magiconf_client;

import utils.ValuesHelper;
import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v4.app.NavUtils;

public class AgendaOldActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_agenda_old);
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

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.agenda, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void showTalkInformation(View view){
		Intent intent = new Intent(this, TalkSessionActivity.class);
		intent.putExtra(ValuesHelper.EVENT_ID, Long.valueOf(3)); // TODO this is not finished. It will be the event id of a selected item in agenda
		startActivity(intent);
	}
	
	public void showPosterSessionInformation(View view){
		Intent intent = new Intent(this, PostersSessionActivity.class);
		intent.putExtra(ValuesHelper.EVENT_ID, Long.valueOf(18));
		startActivity(intent);
	}
	
	public void showEventInformation(View view) {
		Intent intent = new Intent(this, EventActivity.class);
		intent.putExtra(ValuesHelper.EVENT_ID, Long.valueOf(1)); // TODO this is not finished. It will be the event id of a selected item in agenda
		startActivity(intent);
	}
	
	public void showKeynoteSessionInformation(View view) {
		Intent intent = new Intent(this, KeynoteSessionActivity.class);
		intent.putExtra(ValuesHelper.EVENT_ID, Long.valueOf(2)); // TODO this is not finished. It will be the event id of a selected item in agenda
		startActivity(intent);
	}
	
	public void showWorkshop(View view) {
		Intent intent = new Intent(this, WorkshopActivity.class);
		intent.putExtra(ValuesHelper.EVENT_ID, Long.valueOf(26)); // TODO this is not finished. It will be the event id of a selected item in agenda
		startActivity(intent);
	}
	
	public void showNotPresentedPublicationInformation(View view) {
		Intent intent = new Intent(this, PublicationDetailsActivity.class);
		intent.putExtra(ValuesHelper.PUBLICATION_ID, Long.valueOf(2)); // TODO this is not finished. It will be the event id of a selected item in agenda
		startActivity(intent);
	}

}
