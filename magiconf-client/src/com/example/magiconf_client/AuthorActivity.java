package com.example.magiconf_client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import tables.Article;
import tables.Author;
import tables.Event;
import tables.Keynote;
import tables.NotPresentedPublication;
import tables.ParticipantAuthor;
import tables.Publication;
import tables.FavPublication;
import utils.APIResponses;
import utils.ContactExchanger;
import utils.EventTypeChecker;
import utils.Pair;
import utils.PublicationTypeChecker;
import utils.RestClient;
import utils.Triple;
import utils.ValuesHelper;

import dataRetrieving.AuthInfo;
import dataRetrieving.AuthorData;
import dataSources.ArticleAuthorDataSource;
import dataSources.AuthorDataSource;
import dataSources.ParticipantAuthorDataSource;

import adapters.ArticleListViewAdapter;
import adapters.AuthorSessionsListViewAdapter;
import adapters.Helper;
import adapters.PublicationListViewAdapter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

/**
 * @author José
 *
 * This class represents the Author Activity (with tabs: details, sessions and publications)
 */
public class AuthorActivity extends Activity{

	private AuthorDataSource authorDB = new AuthorDataSource(this); // the database
	private ParticipantAuthorDataSource participantAuthorDB = new ParticipantAuthorDataSource(this);
	private TabHost th; //tabs
    private static final String BIO = "Bio";
    private static final String SESSIONS = "Sessões";
    private static final String PUBS = "Pubs";
    private Author author;
//	public final static String ARTICLE_ID="articleID";
//	public final static String ARTICLE_NAME="articleName";
//	public final static String ARTICLE_TITLE="articleTitle";
	


	/**
	 * Updates all the views present in this activity
	 * 
	 * @param result
	 *            the object to use to populate data
	 */
	private void updateViews(final Author result) {
		setContentView(R.layout.activity_author);
		ImageView imageView = (ImageView) this
				.findViewById(R.id.author_photo);
		UrlImageViewHelper.setUrlDrawable(imageView, ValuesHelper.SERVER_API_ADDRESS_MEDIA+result.getPhoto(), null, UrlImageViewHelper.CACHE_DURATION_INFINITE);
		if(imageView.getDrawable()==null){
			imageView.setImageResource(R.drawable.dummy_user_photo);
		}
		
		TextView textView = (TextView) this.findViewById(R.id.author_country);
		textView.setText(result.getCountry());
		Log.i("Country", result.getCountry());
		textView = (TextView) this.findViewById(R.id.author_organization);
		textView.setText(result.getWork_place());
		textView = (TextView) this.findViewById(R.id.author_email);
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
		
		
		
		ActionBar actionBar = getActionBar();
		actionBar.setTitle(result.getName());
		
		final ListView pubsList = (ListView) findViewById(R.id.publications_list);
		//List<Publication> list = new ArrayList<Publication>(); //TODO: Article tem que passar a Publication
		
		// Initialize the articles list
		authorDB.open();

		List<Publication> list = authorDB.getPublicationsFromAuthor(result.getId(), result.getName());
		
		//TODO: Alterar adapter de modo a ser publicação, com estrela ao lado para definir se é de interesse ou não
		pubsList.setAdapter(new PublicationListViewAdapter(this, android.R.layout.simple_list_item_1, list)); 
		
		pubsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				
				 Publication pub = (Publication) pubsList.getItemAtPosition(position);
                 Intent intent;
                 Log.i("PUBNAME",""+pub.getTitle());
                 Log.i("PUBID",""+pub.getId());
                 if (PublicationTypeChecker.getType(AuthorActivity.this, pub) == ValuesHelper.PUB_TYPE.ARTICLE.ordinal()) {
                         intent = new Intent(AuthorActivity.this, ArticleActivity.class);
                         intent.putExtra(ValuesHelper.IS_PARENT_ID, true);
                         intent.putExtra(ValuesHelper.PUBLICATION_ID, pub.getId());
                         startActivity(intent);
                 }
                 else if (PublicationTypeChecker.getType(AuthorActivity.this, pub) == ValuesHelper.PUB_TYPE.NOT_PRESENTED.ordinal()) {
                         intent = new Intent(AuthorActivity.this, PublicationDetailsActivity.class);
                         intent.putExtra(ValuesHelper.IS_PARENT_ID, true);
                         intent.putExtra(ValuesHelper.PUBLICATION_ID, pub.getId());
                         startActivity(intent);
                 }
                 
                 // TODO Open the article activity.
			}
		});
		
		// This allows to have a list inside a scrollview
		Helper.getListViewSize(pubsList);
		
		Set<Event> events = authorDB.getAuthorEvents(result.getId());
		
		final ListView sessionListView = (ListView) findViewById(R.id.sessions_list);
		List<Event> eventList = new ArrayList<Event>(events);
		sessionListView.setAdapter(new AuthorSessionsListViewAdapter(this, android.R.layout.simple_list_item_1, eventList));
		sessionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				
				Event event = (Event) sessionListView.getItemAtPosition(position);
				Intent intent;
				if (EventTypeChecker.getType(AuthorActivity.this, event) == ValuesHelper.EVENT_TYPE.TALK_SESSION.ordinal()) {
					intent = new Intent(AuthorActivity.this, TalkSessionActivity.class);
					intent.putExtra(ValuesHelper.EVENT_ID, event.getDjangoId());
					startActivity(intent);
				}
				else if (EventTypeChecker.getType(AuthorActivity.this, event) == ValuesHelper.EVENT_TYPE.POSTER_SESSION.ordinal()) {
					intent = new Intent(AuthorActivity.this, PostersSessionActivity.class);
					intent.putExtra(ValuesHelper.EVENT_ID, event.getDjangoId());
					startActivity(intent);
				}
			}
		});
		
		authorDB.close();
		
		this.author = result;
	}

	/**
	 * Inserts a new author into the database
	 * 
	 * @param result
	 *            object to use to populate data
	 * @return the new author
	 */
	private Author createNewAuthor(Author result) {
		authorDB.open();
		Author newAuthor = authorDB.createAuthor(
				result.getEmail(), result.getName(), result.getWork_place(),
				result.getCountry(), result.getPhoto(), ""+result.getContact().getId(), true);

		authorDB.close();
		return newAuthor;
	}

	private Author getAuthor(String authorEmail, long authorId) {
		authorDB.open();
		Author e = authorDB.getAuthor(authorEmail,authorId);
		authorDB.close();
		return e;
	}

	/**
	 * @author José Asynchronous REST GET request
	 */
	private class RESTGetTask extends AsyncTask<String, Void, Author> {
		protected Author doInBackground(String... params) {
			/*RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(
					new MappingJacksonHttpMessageConverter());

			Author author = restTemplate.getForObject(params[0],
					Author.class);
			return author;*/
			return AuthorData.downloadAuthorData(params[0], AuthInfo.username, AuthInfo.password, authorDB);
		}

		protected void onPostExecute(Author result) {
			// This is new data, so let's populate the data in views and insert
			// it into the database
			//Author author = createNewAuthor(result);
			updateViews(result);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
		setContentView(R.layout.activity_author);

		Intent intent = getIntent();
		String authorEmail = intent.getStringExtra(ArticleActivity.AUTHOR_EMAIL);
		long authorId = intent.getLongExtra(ValuesHelper.AUTHOR_ID, 2);
			
		// Get an existing author
		Author author = getAuthor(authorEmail,authorId);
		

        
		if (author == null) // it doesn't exist into the local database //TODO: não entra id, mas email
			new RESTGetTask().execute(authorEmail);
		else
			updateViews(author);
		// Show the Up button in the action bar.
		setupActionBar();
		
		
		//Separação por tabs
		
		th = (TabHost) findViewById(R.id.tabhost);
		th.setup();
		
		TabSpec specs = th.newTabSpec("tag1");
		specs.setContent(R.id.bio_tab);
		specs.setIndicator(BIO);
		th.addTab(specs);
		specs = th.newTabSpec("tag2");
		specs.setContent(R.id.sessions_tab);
		specs.setIndicator(SESSIONS);
		th.addTab(specs);
		specs = th.newTabSpec("tag3");
		specs.setContent(R.id.pubs_tab);
		specs.setIndicator(PUBS);
		th.addTab(specs);
		
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
		getMenuInflater().inflate(R.menu.author, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add:
			if(!isContact()){
				addContact();
				item.setIcon(R.drawable.remove_person);
			}else{
				removeContact();
				item.setIcon(R.drawable.add_person);
			}
			return true;
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

	private void addContact() {
		String message = "";
		//Add contact to BD
		//First check if it is on contacts. If yes, ignore but send email. If no add to bd
		
		participantAuthorDB.open();
		ParticipantAuthor pa = participantAuthorDB.getParticipantAuthorDB(AuthInfo.username,this.author.getId());
		
		if(pa == null){
			participantAuthorDB.createParticipantAuthor(AuthInfo.username,this.author.getId());
			message = author.getName() + " "+getString(R.string.contact_added_text);
		}
		else {
			message = getString(R.string.error_already_in_contacts);
		}
		participantAuthorDB.close();
		
		Toast.makeText(AuthorActivity.this, message, Toast.LENGTH_SHORT).show();
		
		//send email anyway
		if(ValuesHelper.isNetworkAvailable(AuthorActivity.this))
			ContactExchanger.addAuthorOrKeynote(author.getName(),author.getEmail(), ContactExchanger.TYPE_AUTHOR,AuthorActivity.this);
		else
			Toast.makeText(AuthorActivity.this, getString(R.string.no_internet_text), Toast.LENGTH_SHORT).show();
	}
	
	private void removeContact() {
		String message = "";
		//Remove contact to BD
		//First check if it is on contacts. If yes, ignore but send email. If no add to bd
		
		participantAuthorDB.open();
		ParticipantAuthor pa = participantAuthorDB.getParticipantAuthorDB(AuthInfo.username,this.author.getId());
		
		if(pa != null){
			participantAuthorDB.deleteParticipantAuthor(AuthInfo.username,this.author.getId());
			message = author.getName() + " "+getString(R.string.contact_removed_text);
		}
		else {
			message = getString(R.string.error_not_in_contacts);
		}
		participantAuthorDB.close();
		
		Toast.makeText(AuthorActivity.this, message, Toast.LENGTH_SHORT).show();
		
	}
	
	private boolean isContact(){
		ParticipantAuthor pa = null;
		participantAuthorDB.open();
		pa = participantAuthorDB.getParticipantAuthorDB(AuthInfo.username,this.author.getId());
		participantAuthorDB.close();
		
		return pa!=null;
		
	}
	
	public boolean onPrepareOptionsMenu (Menu menu) {
		if (isContact()) {
			MenuItem menuItem = menu.findItem(R.id.action_add);
			menuItem.setIcon(R.drawable.remove_person);
		}else{
			MenuItem menuItem = menu.findItem(R.id.action_add);
			menuItem.setIcon(R.drawable.add_person);
		}
		return super.onPrepareOptionsMenu(menu);
			
	}
	
	
	
}
