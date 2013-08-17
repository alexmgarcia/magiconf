package com.example.magiconf_client;




import tables.Contact;
import tables.ParticipantKeynote;

import utils.ValuesHelper;


import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import dataRetrieving.AuthInfo;
import dataRetrieving.ParticipantData;
import dataSources.ContactDataSource;
import dataSources.ParticipantContactDataSource;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/*
 * Single contact activity
 */
public class ContactActivity extends Activity {
	
	private ContactDataSource contactDB = new ContactDataSource(this); // the database
	private ParticipantContactDataSource participantDB = new ParticipantContactDataSource(this);
	private Contact c;
	
	/**
	 * Shows an alert dialog
	 * @param title title of the dialog
	 * @param msg message of the dialog
	 * @param finish if clicking in OK button will finish the activity
	 */
	private void showAlertDialog(String title, String msg, final boolean finish) {
		final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(msg);
		alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok_button), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (finish)
					ContactActivity.this.finish();
			}
		});
		alertDialog.show();
	}
	
	/**
	 * Updates all the views present in this activity
	 * 
	 * @param result the object to use to populate data
	 */
	private void updateViews(final Contact result) {
		c = result;
		setContentView(R.layout.activity_contact);
		ImageView imageView = (ImageView) this.findViewById(R.id.contact_photo);
		Log.i("Contact Photo",result.getPhoto());
		UrlImageViewHelper.setUrlDrawable(imageView, ValuesHelper.SERVER_API_ADDRESS_MEDIA+result.getPhoto(), null, UrlImageViewHelper.CACHE_DURATION_INFINITE);
		if(imageView.getDrawable()==null){
			imageView.setImageResource(R.drawable.dummy_user_photo);
		}
		
		TextView textView = (TextView) this.findViewById(R.id.contact_workplace);
		textView.setText(result.getWorkPlace());
		textView = (TextView) this.findViewById(R.id.contact_country);
		textView.setText(result.getCountry());
		Log.i("Country", result.getCountry());
		textView = (TextView) this.findViewById(R.id.phone);
		textView.setText(result.getPhoneNumber());
		RelativeLayout phoneLayout = (RelativeLayout) this.findViewById(R.id.phone_layout);
		phoneLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent callIntent = new Intent(Intent.ACTION_CALL);
				callIntent.setData(Uri.parse("tel:"+result.getPhoneNumber()));
				startActivity(callIntent);
			}
		});
		textView = (TextView) this.findViewById(R.id.contact_email);
		textView.setText(result.getEmail());
		RelativeLayout emailLayout = (RelativeLayout) this.findViewById(R.id.email_layout);
		emailLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent emailIntent = new Intent(Intent.ACTION_VIEW);
				emailIntent.setData(Uri.parse("mailto:?to=" + result.getEmail()));
				startActivity(emailIntent);
			}
		});
		ImageView facebookImage = (ImageView) this.findViewById(R.id.world3);
		if (result.getFacebookUrl().equals(""))
			facebookImage.setVisibility(View.INVISIBLE);
		else {
			facebookImage.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String url = result.getFacebookUrl();
					if(!url.equals("")){
						Intent i = new Intent(Intent.ACTION_VIEW);
						i.setData(Uri.parse(url));
						startActivity(i);
					}
				}
			});
		}
		ImageView linkedinImage = (ImageView) this.findViewById(R.id.world);
		if (result.getLinkedinUrl().equals(""))
			linkedinImage.setVisibility(View.INVISIBLE);
		else {
			linkedinImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String url = result.getLinkedinUrl();
				if(!url.equals("")){
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(url));
					startActivity(i);
					}
				}
			});
		}
	
		ActionBar actionBar = getActionBar();
		actionBar.setTitle(result.getName());
	}


	private Contact getContact(Long id) {
		contactDB.open();
		Contact e = contactDB.getContact(id);
		contactDB.close();
		return e;
	}

	/*
	private class RESTGetTask extends AsyncTask<String, Void, Contact> {
		protected Contact doInBackground(String... params) {
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(
					new MappingJacksonHttpMessageConverter());

			Contact contact = restTemplate.getForObject(params[0],
					Contact.class);
			return contact;
		}

		protected void onPostExecute(Contact result) {
			// This is new data, so let's populate the data in views and insert
			// it into the database
			Contact contact = createNewContact(String email, String name, String country, String role, String phone, String photo, long id, boolean bool);
			updateViews(contact);
		}
	}*/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
		setContentView(R.layout.activity_contact);

		Intent intent = getIntent();
		Long contactId = intent.getLongExtra(ValuesHelper.CONTACT_ID, 2);
		 //Get an existing contact
		Contact contact = getContact(contactId);
		updateViews(contact);
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

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contact, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, MainMenu.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		case R.id.action_delete:
			if (ValuesHelper.isNetworkAvailable(this))
				removeContact();
			else
				showAlertDialog(getString(R.string.no_internet_title), getString(R.string.no_internet_text), false);
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
	
	private void removeContact(){
		
		AlertDialog alertDialog = new AlertDialog.Builder(ContactActivity.this).create(); //Read Update

        alertDialog.setMessage("Remover "+c.getName()+" dos seus contactos?");

        alertDialog.setButton(Dialog.BUTTON_POSITIVE,"Confirmar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
         	   confirmRemoveContact();
            }
         });
        
        alertDialog.setButton(Dialog.BUTTON_NEGATIVE,"Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	//do nothing
            }
         });

        alertDialog.show();
		
	}

	protected void confirmRemoveContact() {
		
		ParticipantData.deleteContact(ContactActivity.this, c);	

	}

}
