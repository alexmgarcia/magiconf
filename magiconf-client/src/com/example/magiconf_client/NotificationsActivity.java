package com.example.magiconf_client;

import java.util.ArrayList;
import java.util.Iterator;

import dataSources.NotificationDataSource;

import tables.Notification;
import adapters.NotificationListViewAdapter;
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
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * @author Alexandre
 *
 * This class represents the Notification activity
 */
public class NotificationsActivity extends Activity {
	
	public static final String NOTIFICATION_ID = "notificationID";
	
	private NotificationDataSource notificationDB = new NotificationDataSource(this);
	
	private void updateViews() {
		notificationDB.open();
		final ListView notificationsList = (ListView)findViewById(R.id.notifications);
		final ArrayList<Notification> list = new ArrayList<Notification>();
		
		// Initialize the notifications list
		Iterator<Notification> it = notificationDB.getNotifications().iterator();
		while (it.hasNext()) 
			list.add(it.next());
		notificationDB.close();
		notificationsList.setAdapter(new NotificationListViewAdapter(this, android.R.layout.simple_list_item_1, list));
		notificationsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				
				Notification notification = (Notification) notificationsList.getItemAtPosition(position);
				Intent intent = new Intent(NotificationsActivity.this, NotificationActivity.class);
				intent.putExtra(NOTIFICATION_ID, notification.getId());
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
		setContentView(R.layout.activity_notifications);
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
	
	@Override
	public void onRestart() {
		super.onRestart();
		setContentView(R.layout.activity_notifications);
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
		getMenuInflater().inflate(R.menu.notifications, menu);
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
