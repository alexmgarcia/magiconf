package com.example.magiconf_client;

import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tables.Author;
import tables.Contact;
import tables.Keynote;
import tables.Participant;
import utils.Triple;
import utils.ValuesHelper;

import dataRetrieving.AuthInfo;
import dataRetrieving.CacheVars;
import dataRetrieving.ContactData;
import dataRetrieving.ParticipantData;
import dataSources.ContactDataSource;
import dataSources.ParticipantDataSource;
import dataSources.ParticipantAuthorDataSource;
import dataSources.ParticipantKeynoteDataSource;

import adapters.AuthorListViewAdapter;
import adapters.ContactListViewAdapter;
import adapters.KeynoteListViewAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

/*
 * @author José
 * List of contacts activity
 */
public class ContactsActivity extends Activity{

	private ContactDataSource contactDB = new ContactDataSource(this);
	private ParticipantDataSource participantDB = new ParticipantDataSource(this);
	private ParticipantAuthorDataSource participantAuthorDB = new ParticipantAuthorDataSource(this);
	private ParticipantKeynoteDataSource participantKeynoteDB = new ParticipantKeynoteDataSource(this);

	private static final int CONTACT_TAG = 0;
	private static final int AUTHOR_TAG = 1;
	private static final int KEYNOTE_TAG = 2;
	
	private TabHost th; //tabs
	private static final String AUTHORS = "Autores";
	private static final String KEYNOTES = "Keynotes";
	private static final String PARTICIPANTS = "Partic.";

	public final static String CONTACT_NAME="contactName";
	
	private List<Author> authors;
	private List<Keynote> keynotes;
	private List<Contact> participants;
	
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
		alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok_button), new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (finish)
					ContactsActivity.this.finish();
			}
		});
		alertDialog.show();
	}


	private void updateViews() {

		setContentView(R.layout.activity_contacts);
		final ListView participantsList = (ListView) findViewById(R.id.participants_list);
		participantsList.setTag(CONTACT_TAG);
		final ListView authorsList = (ListView) findViewById(R.id.authors_list);
		authorsList.setTag(AUTHOR_TAG);
		final ListView keynotesList = (ListView) findViewById(R.id.keynotes_list);
		keynotesList.setTag(KEYNOTE_TAG);
		participantsList.setAdapter(new ContactListViewAdapter(this, android.R.layout.simple_list_item_1, participants));

		participantsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {

				Contact contact = (Contact) participantsList.getItemAtPosition(position);

				Intent intent = new Intent(ContactsActivity.this, ContactActivity.class);
				intent.putExtra(ValuesHelper.CONTACT_ID, contact.getId()); 

				startActivity(intent);


			}


		});
		authorsList.setAdapter(new AuthorListViewAdapter(this, android.R.layout.simple_list_item_1, authors));

		authorsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {

				Author contact = (Author) authorsList.getItemAtPosition(position);

				Intent intent = new Intent(ContactsActivity.this, AuthorActivity.class);
				intent.putExtra(ValuesHelper.AUTHOR_ID, contact.getId()); 

				startActivity(intent);

			}

		});

		keynotesList.setAdapter(new KeynoteListViewAdapter(this, android.R.layout.simple_list_item_1, keynotes));

		keynotesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {

				Keynote contact = (Keynote) keynotesList.getItemAtPosition(position);

				Intent intent = new Intent(ContactsActivity.this, KeynoteSpeakerActivity.class);
				intent.putExtra(ValuesHelper.KEYNOTE_ID, contact.getId()); 

				startActivity(intent);


			}
		});

		

		// This allows to have a list inside a scrollview
/*		Helper.getListViewSize(authorsList);
		Helper.getListViewSize(keynotesList);
		Helper.getListViewSize(participantsList);*/
		
		//Separa-o por tabs

		th = (TabHost) findViewById(R.id.tabhost);
		th.setup();
	
		TabSpec specs = th.newTabSpec("tag1");
		specs.setContent(R.id.tab_participants);
		specs.setIndicator(PARTICIPANTS);
		th.addTab(specs);	
		specs = th.newTabSpec("tag2");
		specs.setContent(R.id.tab_authors);
		specs.setIndicator(AUTHORS);
		th.addTab(specs);
		specs = th.newTabSpec("tag3");
		specs.setContent(R.id.tab_keynotes);
		specs.setIndicator(KEYNOTES);
		th.addTab(specs);

		if(authors.size()==0){
			TextView t = (TextView) findViewById(R.id.authors_no_results);
			t.setText(this.getString(R.string.no_author_contacts));
		}
		
		if(keynotes.size()==0){
			TextView t = (TextView) findViewById(R.id.keynotes_no_results);
			t.setText(this.getString(R.string.no_keynote_contacts));
		}
		
		if(participants.size()==0){
			TextView t = (TextView) findViewById(R.id.participants_no_results);
			t.setText(this.getString(R.string.no_participant_contacts));
		}
 
		registerForContextMenu(keynotesList);  
		registerForContextMenu(authorsList);  
		registerForContextMenu(participantsList);  
	}


	private class RESTGetTask extends AsyncTask<String, Void, List<JSONObject>> {
		protected List<JSONObject> doInBackground(String... params) {
			
			
			//Get lastCheck
			participantDB.open();
			Participant p = participantDB.getParticipantByUsername(AuthInfo.username);
			//Send request to server with lastcheck
			ContactData.checkForContactUpdates(AuthInfo.username, AuthInfo.password,p.getLastCheck(),contactDB,ContactsActivity.this);
			//update contacts
			
			//set new lastcheck
			
			//List<JSONObject> list = ContactData.downloadNewestContacts(AuthInfo.username, AuthInfo.password,params[0]);
					//ContactData.downloadContacts(AuthInfo.username, AuthInfo.password);
			participantDB.close();
			return null;
		}

		
		protected void onPostExecute(List<JSONObject> result) {
			// This is new data, so let's populate the data in views and insert it into the database
			/*try {
				Iterator<JSONObject> it = result.iterator();
				JSONObject aux = null;
				while(it.hasNext()){
					Log.i("POST_EXECUTE", "On postExecute");
					aux =  it.next();
					JSONArray data = aux.getJSONArray("objects");
					ContactData.createContacts(data,contactDB);
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			contactDB.open();
			Triple<List<Author>,List<Keynote>,List<Contact>> contacts=contactDB.getAllContactsSortedByGroup();
			contactDB.close();
			authors = contacts.getFirst();
			keynotes = contacts.getSecond();
			participants = contacts.getThird();
			*/
			updateViews();
			
		}


	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
		contactDB.open();
		Triple<List<Author>,List<Keynote>,List<Contact>> contacts=contactDB.getAllContactsSortedByGroup();
		contactDB.close();
		
		authors = contacts.getFirst();
		keynotes = contacts.getSecond();
		participants = contacts.getThird();
		
		boolean updateContacts = false;
		
		//Pre-Condition: Internet Connectivity must be present
		if(ValuesHelper.isNetworkAvailable(ContactsActivity.this)){
			
			long currentTime = System.currentTimeMillis();
			if(CacheVars.CONTACTS_TIMESTAMP < 0 || (currentTime - CacheVars.CONTACTS_TIMESTAMP) > CacheVars.INTERVAL_IN_MS ){
				updateContacts = true;
				CacheVars.CONTACTS_TIMESTAMP = currentTime;
				Log.i("UPDATE", "TIME TO UPDATE");
			}
		}
		
		if(updateContacts)
			new RESTGetTask().execute();
		else
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
		getMenuInflater().inflate(R.menu.contacts, menu);
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
		case R.id.action_contact_exchange: // contact exchange button clicked
			intent = new Intent(this, ContactExchangeActivity.class);
			startActivity(intent);
			return true;
		case R.id.action_search:
			onSearchRequested();
			return true;
		case R.id.action_about:
			intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		Integer tag = (Integer) v.getTag();
		MenuInflater inflater = getMenuInflater();
		
		switch(tag){
		case AUTHOR_TAG:
			inflater.inflate(R.menu.contact_authors_context_menu, menu);
			break;
		case KEYNOTE_TAG:
			inflater.inflate(R.menu.contact_keynote_context_menu, menu);
			break;
		case CONTACT_TAG:
			inflater.inflate(R.menu.contact_participant_context_menu, menu);
			break;
		}
		
		ListView participantsList = (ListView) findViewById(R.id.participants_list);

		ListView authorsList = (ListView) findViewById(R.id.authors_list);

		ListView keynotesList = (ListView) findViewById(R.id.keynotes_list);

		
		ContactListViewAdapter adapterContacts = (ContactListViewAdapter) participantsList.getAdapter();
		AuthorListViewAdapter adapterAuthors = (AuthorListViewAdapter) authorsList.getAdapter();
		KeynoteListViewAdapter adapterKeynotes = (KeynoteListViewAdapter) keynotesList.getAdapter();
		adapterContacts.notifyDataSetChanged();
		adapterAuthors.notifyDataSetChanged();
		adapterKeynotes.notifyDataSetChanged();

	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.remove_author:
			ListView authorsList = (ListView) findViewById(R.id.authors_list);
			Author a = (Author) authorsList.getItemAtPosition(info.position);
			removeAuthorFromContacts(a);
			AuthorListViewAdapter adapterAuthors = (AuthorListViewAdapter) authorsList.getAdapter();
			adapterAuthors.remove(a);
			adapterAuthors.notifyDataSetChanged();
			break;
		case R.id.remove_keynote:
			ListView keynotesList = (ListView) findViewById(R.id.keynotes_list);
			Keynote k = (Keynote) keynotesList.getItemAtPosition(info.position);
			removeKeynoteFromContacts(k);
			KeynoteListViewAdapter adapterKeynotes = (KeynoteListViewAdapter) keynotesList.getAdapter();
			adapterKeynotes.remove(k);
			adapterKeynotes.notifyDataSetChanged();
			break;		
		case R.id.remove_participant:
			if (ValuesHelper.isNetworkAvailable(ContactsActivity.this)) {
				ListView participantsList = (ListView) findViewById(R.id.participants_list);
				Contact c = (Contact) participantsList.getItemAtPosition(info.position);
				removeParticipantContact(c);
				ContactListViewAdapter adapterContacts = (ContactListViewAdapter) participantsList.getAdapter();
				adapterContacts.remove(c);
				adapterContacts.notifyDataSetChanged();
			}
			else
				showAlertDialog(ContactsActivity.this.getString(R.string.no_internet_title), 
						ContactsActivity.this.getString(R.string.no_internet_title), false);
			break;	
		default:
			return super.onContextItemSelected(item);
		}
		
		return true;
	}

	@Override
	public void onRestart() {
		super.onRestart();
		Log.i("RESTART","fez restart");
		
		contactDB.open();
		Triple<List<Author>,List<Keynote>,List<Contact>> contacts=contactDB.getAllContactsSortedByGroup();
		contactDB.close();
		
		authors = contacts.getFirst();
		keynotes = contacts.getSecond();
		participants = contacts.getThird();
		updateViews();
	}


	private void removeAuthorFromContacts(Author a) {
		participantAuthorDB.open();
		participantAuthorDB.deleteParticipantAuthor(AuthInfo.username,a.getId());
		participantAuthorDB.close();
	}




	private void removeKeynoteFromContacts(Keynote k) {
		participantKeynoteDB.open();
		participantKeynoteDB.deleteParticipantKeynote(AuthInfo.username,k.getId());
		participantKeynoteDB.close();
	}




	private void removeParticipantContact(Contact c) {
		ParticipantData.deleteContact(ContactsActivity.this, c);		
	}
}
