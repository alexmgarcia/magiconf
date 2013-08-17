package com.example.magiconf_client;

import dataRetrieving.AuthInfo;
import dataSources.ParticipantDataSource;
import dataSources.ParticipantSessionDataSource;
import dataSources.SessionDataSource;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.message.BasicNameValuePair;

import tables.ParticipantSession;
import tables.Session;
import utils.GCMHelper;
import utils.RestClient;
import utils.ValuesHelper;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings.Secure;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainMenu extends FragmentActivity  {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current dropdown position.
	 */
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	private SessionDataSource sessionDB = new SessionDataSource(this);
	private ParticipantDataSource participantDB = new ParticipantDataSource(this);
	private ParticipantSessionDataSource participantSessionDB = new ParticipantSessionDataSource(this);




	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private static final String PROPERTY_ON_SERVER_EXPIRATION_TIME =
			"onServerExpirationTimeMs";
	/**
	 * Default lifespan (7 days) of a reservation until it is considered expired.
	 */
	public static final long REGISTRATION_EXPIRY_TIME_MS = 1000 * 3600 * 24 * 7;

	/**
	 * Substitute you own sender ID here.
	 */
	String SENDER_ID = "781504963235";

	/**
	 * Tag used on log messages.
	 */
	static final String TAG = "GCMDemo";

	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	SharedPreferences prefs;
	Context context;
	GCMHelper gcmHelper = new GCMHelper();
	String regid;
	private Handler handler = new Handler();



	//ImageButtons
	ImageView agendaButton;
	ImageView checkInButton;
	ImageView contactsButton;
	ImageView pubsOfInterestButton;
	ImageView AuthorsAndKeynotesButton;
	ImageView informationsButton;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
		setContentView(R.layout.activity_main_menu);


		context = getApplicationContext();
		regid = gcmHelper.getRegistrationId(context);
		Log.i("regid", regid);

		gcm = GoogleCloudMessaging.getInstance(context);

		//if (regid.length() == 0)
		if (!GCMHelper.registered)
			registerAsyncTask();
		/*else {
	        	try {
					gcm.unregister();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	registerAsyncTask();
	        }*/
		//gcm = GoogleCloudMessaging.getInstance(context);


		// Set up the action bar
		//final ActionBar actionBar = getActionBar();
		//actionBar.setDisplayShowTitleEnabled(true);

		final ActionBar actionBar = getActionBar();
		actionBar.setCustomView(R.layout.actionbar_mainmenu);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayUseLogoEnabled(false);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setDisplayShowHomeEnabled(false);



		TextView text = (TextView) findViewById(R.id.actionbar_user_name);
		text.setText(AuthInfo.username);
		text.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				usernameClicked(v);

			}
		});

		findViewById(R.id.user_logo).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				usernameClicked(v);

			}
		});


		findViewById(R.id.actionbar_search).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				searchButtonClicked(v);
			}
		});
	}

	protected void searchButtonClicked(View v) {
			onSearchRequested();
	}

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.add(0, v.getId(), 0, R.string.menu_edit_links);
		menu.add(0, v.getId(), 0, R.string.menu_logout);
	}

	protected void usernameClicked(View v) {
		registerForContextMenu(v);
		openContextMenu(v);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getTitle().equals(getString(R.string.menu_edit_links))) {
			Intent intent = new Intent(MainMenu.this, EditSocialNetworksActivity.class);
			startActivity(intent);
		}
		else if(item.getTitle().equals(getString(R.string.menu_logout))){

			AlertDialog alertDialog = new AlertDialog.Builder(MainMenu.this).create();
			if (ValuesHelper.isNetworkAvailable(MainMenu.this)) {
				alertDialog.setTitle(getString(R.string.menu_logout));
				alertDialog.setMessage(getString(R.string.confirm_logout));
				alertDialog.setIcon(android.R.drawable.ic_dialog_info);
				alertDialog.setButton(Dialog.BUTTON_POSITIVE,getString(R.string.confirm_logout_button) , new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						logoutUser();
					}
				});
				alertDialog.setButton(Dialog.BUTTON_NEGATIVE,getString(R.string.cancel_logout_button) , new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						//Do nothing
					}
				});
			}
			else {
				alertDialog.setTitle(getString(R.string.no_internet_title));
				alertDialog.setMessage(getString(R.string.no_internet_text));
				alertDialog.setIcon(android.R.drawable.ic_dialog_info);
				alertDialog.setButton(Dialog.BUTTON_NEUTRAL, getString(R.string.ok_button), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						;
					}
				});
			}
			alertDialog.show();
		}  
		return true;
	}

	private void logoutUser() {
		participantSessionDB.open();
		sessionDB.open();
		ParticipantSession ps = participantSessionDB.getSessionByUsername(AuthInfo.username);
		if(ps != null){
			participantSessionDB.deleteParticipantSession(AuthInfo.username,ps.getSessionId() );
			sessionDB.deleteSession(ps.getSessionId());
			
			Intent intent = new Intent(MainMenu.this, LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
		participantSessionDB.close();
		sessionDB.close();
		//TODO: We need to unregister gcm device here!
		if (ValuesHelper.isNetworkAvailable(this))
			unregisterAsyncTask();

	}
	
	private Runnable registerRunnable = new Runnable() {
		public void run() {
			registerAsyncTask();
		}
	};
	
	private Runnable unregisterRunnable = new Runnable() {
		public void run() {
			unregisterAsyncTask();
		}
	};

	private void registerAsyncTask() {
		Log.i("async", "ok");
		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				Log.i("async", "ok2");
				try {
					if (gcm == null)
						gcm = GoogleCloudMessaging.getInstance(context);
					gcm.unregister();
					Log.i("here1","here");
					regid = gcm.register(SENDER_ID);
					Log.i("here2","here");
					String url = ValuesHelper.SERVER_GCM_ADDRESS + "/device/register/";

				        RestClient client = new RestClient();
				        Log.i("device", Secure.getString(context.getContentResolver(), Secure.ANDROID_ID) );
				        Log.i("device2", "ouhh");
				        
				        client.addPostParam(new BasicNameValuePair( "dev_id", Secure.getString(context.getContentResolver(), Secure.ANDROID_ID) ));
				        client.addPostParam(new BasicNameValuePair("reg_id", regid));
				        client.addPostParam(new BasicNameValuePair("user_name", AuthInfo.username));
				        client.addPostParam(new BasicNameValuePair("password", AuthInfo.password));
				        client.executePost(url);
					msg = "Device registered, registration id = " + regid;
					gcmHelper.setRegistrationId(context, regid);
				} catch (IOException e) {
					msg = "Error: " + e.getMessage();
					Log.i("error", msg);
					handler.postDelayed(registerRunnable, 5000);
				}
				Log.i("ouh", msg);
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				GCMHelper.registered = true;
				Log.i("post", msg);
			}
		}.execute(null, null, null);
	}

	private void unregisterAsyncTask() {
		Log.i("async", "ok");
		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				Log.i("async", "ok2");
				try {
					if (gcm == null)
						gcm = GoogleCloudMessaging.getInstance(context);
					gcm.unregister();
					Log.i("here","here");
					String url = ValuesHelper.SERVER_GCM_ADDRESS + "/device/unregister/";

				        RestClient client = new RestClient();
				        Log.i("device", Secure.getString(context.getContentResolver(), Secure.ANDROID_ID) );
				        Log.i("device2", "ouhh");
				        
				        client.addPostParam(new BasicNameValuePair( "dev_id", Secure.getString(context.getContentResolver(), Secure.ANDROID_ID) ));
				        client.addPostParam(new BasicNameValuePair("reg_id", regid));
				        client.addPostParam(new BasicNameValuePair("user_name", AuthInfo.username));
				        client.addPostParam(new BasicNameValuePair("password", AuthInfo.password));
				        client.executePost(url);
					msg = "Device unregistered";
					gcmHelper.setRegistrationId(context, regid);
				} catch (IOException e) {
					msg = "Error: " + e.getMessage();
					Log.i("error", msg);
					handler.postDelayed(unregisterRunnable, 5000);
				}
				Log.i("ouh", msg);
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				GCMHelper.registered = false;
				Log.i("post", msg);
			}
		}.execute(null, null, null);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current dropdown position.
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current dropdown position.
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		addListenerOnButton();
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		/*MenuItem m = menu.findItem(R.id.menu_current_user);
        m.setTitle(AuthInfo.username);*/
		return true;
	}

	@Override
	public boolean onOptionsItemSelected (MenuItem item){
		switch (item.getItemId()) {
		case R.id.action_about:
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
			return true;
		default: break;
		}
		
		return super.onOptionsItemSelected(item);

	}



	public void addListenerOnButton() {

		agendaButton = (ImageView) findViewById(R.id.agendaButtonIcon);
		agendaButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				/*	Toast.makeText(MainMenu.this,
						"AgendaButton is clicked!", Toast.LENGTH_SHORT).show();*/
				Intent intent = new Intent(MainMenu.this, AgendaActivity.class);
				startActivity(intent);
			}
		});

		checkInButton = (ImageView) findViewById(R.id.checkInButtonIcon);
		checkInButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(MainMenu.this, CheckInActivity.class);
				intent.putExtra("username", AuthInfo.username);
				startActivity(intent);
			}
		});

		contactsButton = (ImageView) findViewById(R.id.contactsButtonIcon);
		contactsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(MainMenu.this, ContactsActivity.class);
				startActivity(intent);
			}
		});

		pubsOfInterestButton = (ImageView) findViewById(R.id.pubsOfInterestButtonIcon);
		pubsOfInterestButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(MainMenu.this, FavoritePublicationsActivity.class);
				startActivity(intent);
			}});

		AuthorsAndKeynotesButton = (ImageView) findViewById(R.id.AuthorsAndKeynotesButtonIcon);
		AuthorsAndKeynotesButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				/*	Toast.makeText(MainMenu.this,
						"AgendaButton is clicked!", Toast.LENGTH_SHORT).show();*/
				Intent intent = new Intent(MainMenu.this, AuthorsAndKeynoteSpeakersActivity.class);
				startActivity(intent);
			}
		});

		informationsButton = (ImageView) findViewById(R.id.informationsButtonIcon);
		informationsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(MainMenu.this, InformationsMenu.class);
				startActivity(intent);
			}
		});
		
	}


}
