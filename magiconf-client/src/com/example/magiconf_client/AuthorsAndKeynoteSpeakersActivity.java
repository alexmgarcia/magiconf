package com.example.magiconf_client;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;



import tables.Author;
import tables.Keynote;
import tables.KeynoteSession;
import utils.RestClient;
import utils.ValuesHelper;
import adapters.Helper;
import adapters.KeynoteListViewAdapter;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import dataRetrieving.AuthInfo;
import dataRetrieving.AuthorData;
import dataRetrieving.KeynoteData;
import dataSources.AuthorDataSource;
import dataSources.KeynoteDataSource;
import fragments.AuthorsFragment;
import fragments.AuthorsFragment.OnAuthorSelectedListener;
import fragments.KeynoteSpeakersFragment;
import fragments.KeynoteSpeakersFragment.OnKeynoteSelectedListener;

public class AuthorsAndKeynoteSpeakersActivity extends FragmentActivity implements OnKeynoteSelectedListener, OnAuthorSelectedListener{

	private KeynoteDataSource keynoteSpeakerDB = new KeynoteDataSource(this);
	private AuthorDataSource authorDB = new AuthorDataSource(this);

	private AuthorsFragment authorsFragment=new AuthorsFragment();
	private KeynoteSpeakersFragment keynoteSpeakersFragment= new KeynoteSpeakersFragment(); 


	private static final int NUM_ITEMS = 2;

	private static final String AUTHOR= "a";
	private static final String KEYNOTE_SPEAKER = "k";

	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;



	/**
	 * Updates all the views present in this activity
	 * @param result the object to use to populate data
	 */
	/*private void updateViews(final List<Keynote> ks, final List<Author> as) {

		//setContentView(R.layout.activity_keynote_session);

		TextView textView = (TextView) this.findViewById(R.id.keynote_title);
		Log.i("KeynoteActivity", ""+textView);
		textView.setText(result.getTitle());
		textView = (TextView) this.findViewById(R.id.time);
		String time = new SimpleDateFormat(ValuesHelper.HOUR_FORMAT, Locale.ENGLISH).format(result.getTime());
		textView.setText(time);
		textView = (TextView) this.findViewById(R.id.place);
		textView.setText(result.getPlace());

		textView = (TextView) this.findViewById(R.id.description);
		textView.setText(result.getDescription());

		final ListView keynoteSpeakersList = (ListView)findViewById(R.id.keynote_speakers_list);
		final ArrayList<Keynote> list = new ArrayList<Keynote>();

		// Initialize the keynote speakers list
		Iterator<Keynote> it = result.getKeynoteSpeakers().iterator();
		while (it.hasNext()) 
			list.add(it.next());

		keynoteSpeakersList.setAdapter(new KeynoteListViewAdapter(this, android.R.layout.simple_list_item_1, list));
		keynoteSpeakersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {

				Keynote keynoteSpeaker = (Keynote) keynoteSpeakersList.getItemAtPosition(position);
				Intent intent = new Intent(KeynoteSessionActivity.this, KeynoteSpeakerActivity.class);
				intent.putExtra(KEYNOTE_ID, keynoteSpeaker.getId()); // TODO this is not finished. It will be the event id of a selected item in agenda
				startActivity(intent);

				// TODO Open the keynote speaker activity. To be done when story #49091965 is done
			}
		});

		// This allows to have a list inside a scrollview
		Helper.getListViewSize(keynoteSpeakersList);

		CheckBox inAgenda = (CheckBox)this.findViewById(R.id.in_agenda_checkbox);
		inAgenda.setChecked(result.getInAgenda());
		Log.i("Result", ""+result.getLastModified());
		inAgenda.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				result.setInAgenda(((CheckBox)v).isChecked());
				keynoteSessionDB.open();
				keynoteSessionDB.update(result.getId(), result.getTitle(), result.getTime(), result.getDuration(), result.getPlace(), result.getInAgenda(), result.getLastModified());
				keynoteSessionDB.close();
			}
		});
	}*/

	/**
	 * Inserts a new keynote session into the database
	 * @param result object to use to populate data
	 * @return the new keynote session
	 */

	public List<Keynote> getKeynoteSpeakers() {
		keynoteSpeakerDB.open();
		List<Keynote> ks = keynoteSpeakerDB.getAllKeynotes(true);
		keynoteSpeakerDB.close();
		return ks;
	}

	public List<Author> getAuthors() {
		authorDB.open();
		List<Author> as = authorDB.getAllAuthors(true);
		authorDB.close();
		return as;
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
		getMenuInflater().inflate(R.menu.authors_keynote_speakers, menu);
		
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
		case R.id.action_search:
			onSearchRequested();
			return true;
		case R.id.action_about:
			intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
			return true;
		default: break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
		setContentView(R.layout.acitivity_authors_keynote_speakers);

		List<Keynote> ks = getKeynoteSpeakers();
		List<Author> as = getAuthors();

		boolean noRequest= true;

		Log.i("AuthorsAndKeynoteSpeakersActivity", ""+keynoteSpeakerDB);
		Log.i("AuthorsAndKeynoteSpeakersActivity", ""+authorDB);
		Log.i("AuthorsAndKeynoteSpeakersActivity", "Number of Entries on BD: "+ ks.size());
		Log.i("AuthorsAndKeynoteSpeakersActivity", "Number of Entries on BD: "+ as.size());

		if (ks.size() == 0){ //Carefull!. In fact, this can be 0!(0 keynotes)
			noRequest = false;
			new RESTGetTask().execute(KEYNOTE_SPEAKER);
			ks=getKeynoteSpeakers();
		}	
		if (as.size() == 0){//Carefull!. In fact, this can be 0! (0 authors)
			noRequest = false;
			new RESTGetTask().execute(AUTHOR);
			as = getAuthors();
		}

		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		mViewPager.setOnPageChangeListener(
				new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						// When swiping between pages, select the
						// corresponding tab.
						getActionBar().setSelectedNavigationItem(position);
					}
				});

		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		Tab tab = actionBar.newTab()
				.setText(R.string.authors_tab)
				.setTabListener(new ActionBar.TabListener() {
					public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
						mViewPager.setCurrentItem(tab.getPosition());
					}

					public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
					}

					public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
					}
				});

		actionBar.addTab(tab);

		tab = actionBar.newTab()
				.setText(R.string.keynote_speaker_tab)
				.setTabListener(new ActionBar.TabListener() {
					public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
						mViewPager.setCurrentItem(tab.getPosition());
					}

					public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
					}

					public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
					}
				});

		actionBar.addTab(tab);
		
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
	 *
	 * Asynchronous REST GET request
	 */
	private class RESTGetTask extends AsyncTask<String, Void, Pair<String, List<JSONObject>>> {
		protected Pair<String, List<JSONObject>> doInBackground(String... params) {

			/*List<JSONObject> list = new LinkedList<JSONObject>();
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
			
			Log.i("ASYNC_TASK", "Number requests: "+list.size());*/

			List<JSONObject> list = null;
			if(params[0].equals(AUTHOR)){
				list = AuthorData.downloadAuthors(AuthInfo.username, AuthInfo.password);
			}
			else {
				list = KeynoteData.downloadKeynotes(AuthInfo.username, AuthInfo.password);
			}
			return new Pair<String, List<JSONObject>>(params[0], list);
		}

		protected void onPostExecute(Pair<String,List<JSONObject>> result) {
			// This is new data, so let's populate the data in views and insert it into the database
			try {
				Iterator<JSONObject> it = result.second.iterator();
				JSONObject aux = null;
				while(it.hasNext()){
					Log.i("POST_EXECUTE", "On postExecute");
					aux =  it.next();
					JSONArray data = aux.getJSONArray("objects");
					if(result.first.equals(AUTHOR)){
						Log.i("POST_EXECUTE", "AUTHOR branch");
						AuthorData.createAuthors(data,authorDB);
						keynoteSpeakersFragment.selfUpdateList();
					}
					else {
						Log.i("POST_EXECUTE", "Keynote branch");
						KeynoteData.createKeynotes(data,keynoteSpeakerDB);
						authorsFragment.selfUpdateList();
						
					}
				}
				//updateViews(keynoteSession);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


	}

	private void createKeynotes(JSONArray data) {
		Keynote aux;
		Log.i("CREATE KEYNOTES", "Start adding");
		keynoteSpeakerDB.open();
		Log.i("CREATE KEYNOTES", ""+data.length());
		for(int i = 0 ; i < data.length() ; i++){
			try {
				JSONObject k = data.getJSONObject(i);
				aux = keynoteSpeakerDB.createKeynote(k.getString("email"),k.getString("name"), k.getString("work_place"), k.getString("country"), k.getString("photo"),k.getString("contact"),false);
				Log.i("CREATE KEYNOTES", "Creating Keynote"+aux.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		keynoteSpeakerDB.close();

	}

	private void createAuthors(JSONArray data) {
		Author aux;
		Log.i("CREATE AUTHORS", "Start adding");
		authorDB.open();
		Log.i("CREATE AUTHORS", ""+data.length());
		for(int i = 0 ; i < data.length() ; i++){
			try {
				JSONObject k = data.getJSONObject(i);
				aux = authorDB.createAuthor(k.getString("email"),k.getString("name"), k.getString("work_place"), k.getString("country"), k.getString("photo"),k.getString("contact"), false);
				Log.i("CREATE AUTHORS", "Creating Author"+aux.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		authorDB.close();
	}


	/** 
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to 
	 * one of the sections/tabs/pages. 
	 */  
	public class SectionsPagerAdapter extends FragmentPagerAdapter {  

		public SectionsPagerAdapter(FragmentManager fm) {  
			super(fm);  
		}  

		@Override  
		public Fragment getItem(int position) {  
			// getItem is called to instantiate the fragment for the given page.  
			Fragment f = null;
			switch (position) {
			case 0:  
				return f = authorsFragment;
			case 1:  
				return f = keynoteSpeakersFragment;  
			default:  
				break;
			}

			return f;
		}  

		@Override  
		public int getCount() {  
			return 2;  
		}  

		@Override  
		public CharSequence getPageTitle(int position) {  
			Locale l = Locale.getDefault();  
			switch (position) {  
			case 0:  
				return getString(R.string.authors_tab).toUpperCase(l);  
			case 1:  
				return getString(R.string.keynote_speaker_tab).toUpperCase(l);  
			}  
			return null;  
		}  
	}


	@Override
	public void onKeynoteSelected(long keynoteID) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(AuthorsAndKeynoteSpeakersActivity.this, KeynoteSpeakerActivity.class);
		intent.putExtra(ValuesHelper.KEYNOTE_ID, keynoteID);
		startActivity(intent);

	}

	@Override
	public void onAuthorSelected(long authorID) {
		Intent intent = new Intent(AuthorsAndKeynoteSpeakersActivity.this, AuthorActivity.class);
		intent.putExtra(ValuesHelper.AUTHOR_ID, authorID);
		startActivity(intent);
		

	}  
	
	@Override
	public void onRestart(){
		super.onRestart();
		authorsFragment.selfUpdateList();
		keynoteSpeakersFragment.selfUpdateList();
	}




}
