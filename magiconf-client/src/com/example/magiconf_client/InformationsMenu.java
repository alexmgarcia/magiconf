package com.example.magiconf_client;

import android.app.Activity;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class InformationsMenu extends Activity{

	static final String CONFERENCE_ID = "conferenceId";

	//ImageButtons
	ImageView conferenceButton;
	ImageView organizationButton;
	ImageView sponsorsButton;
	ImageView sightsButton;
	ImageView aboutButton;
	ImageView thanksButton;



	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
		setContentView(R.layout.activity_informations);
		// Show the Up button in the action bar.
		addListenerOnButton();
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


	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.informations, menu);
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







	public void addListenerOnButton() {

		conferenceButton = (ImageView) findViewById(R.id.conferenceButtonIcon);
		conferenceButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				/*	Toast.makeText(InformationsMenu.this,
						"conferenceButton is clicked!", Toast.LENGTH_SHORT).show();*/
				Intent intent = new Intent(InformationsMenu.this, ConferenceActivity.class);
				startActivity(intent);



			}
		});

		organizationButton = (ImageView) findViewById(R.id.organizationButtonIcon);
		organizationButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(InformationsMenu.this, MembersActivity.class);
				startActivity(intent);
			}
		});

		sponsorsButton = (ImageView) findViewById(R.id.sponsorsButtonIcon);
		sponsorsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				/* Toast.makeText(InformationsMenu.this,
							"sponsorsButton is clicked!", Toast.LENGTH_SHORT).show();*/
				Intent intent = new Intent(InformationsMenu.this, SponsorActivity.class);
				startActivity(intent);

			}
		});

		sightsButton = (ImageView) findViewById(R.id.sightsButtonIcon);
		sightsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(InformationsMenu.this, CityActivity.class);
				startActivity(intent);
			}});

		aboutButton = (ImageView) findViewById(R.id.aboutButtonIcon);
		aboutButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				/*Toast.makeText(InformationsMenu.this,
						"aboutButton is clicked!", Toast.LENGTH_SHORT).show();*/

				Intent intent = new Intent(InformationsMenu.this, AboutActivity.class);
				startActivity(intent);

			}
		});

		thanksButton = (ImageView) findViewById(R.id.thanksButtonIcon);
		thanksButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				/*Toast.makeText(InformationsMenu.this,
						"thanksButton is clicked!", Toast.LENGTH_SHORT).show();*/
				Intent intent = new Intent(InformationsMenu.this, ThanksActivity.class);
				startActivity(intent);
			}
		});
	}



}
