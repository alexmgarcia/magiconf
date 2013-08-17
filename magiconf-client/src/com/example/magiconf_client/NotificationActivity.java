package com.example.magiconf_client;

import java.text.SimpleDateFormat;
import java.util.Locale;

import tables.Notification;
import utils.ValuesHelper;
import dataSources.NotificationDataSource;
import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * @author Alexandre
 *
 * This class represents the Notification activity
 */
public class NotificationActivity extends Activity {

	private NotificationDataSource notificationDB = new NotificationDataSource(
			this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
		setContentView(R.layout.activity_notification);
		Intent intent = getIntent();
		notificationDB.open();
		TextView date = (TextView) findViewById(R.id.date);
		TextView description = (TextView) findViewById(R.id.description);
		Long notificationID = intent.getLongExtra(
				NotificationsActivity.NOTIFICATION_ID, 1);
		Notification notification = notificationDB
				.getNotification(notificationID);
		getActionBar().setTitle(notification.getTitle());
		SimpleDateFormat df = new SimpleDateFormat(ValuesHelper.DATE_FORMAT
				+ " " + ValuesHelper.HOUR_FORMAT, Locale.US);
		date.setText(df.format(notification.getTime()));
		description.setText(notification.getDescription());
		notificationDB.deleteNotification(notification);
		notificationDB.close();
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
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.notification, menu);
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
			return super.onOptionsItemSelected(item);
		}
	}

}
