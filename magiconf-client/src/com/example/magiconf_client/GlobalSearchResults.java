package com.example.magiconf_client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import tables.Author;
import tables.Contact;
import tables.Event;
import tables.Hotel;
import tables.Keynote;
import tables.Publication;
import tables.Restaurant;
import tables.Sight;
import utils.EventTypeChecker;
import utils.PublicationTypeChecker;
import utils.SightTypeChecker;
import utils.Triple;
import utils.ValuesHelper;
import adapters.GlobalSearchResultsViewAdapter;
import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;

import dataSources.AuthorDataSource;
import dataSources.ContactDataSource;
import dataSources.EventDataSource;
import dataSources.KeynoteDataSource;
import dataSources.PublicationDataSource;
import dataSources.SightsDataSource;

/**
 * @author José
 *
 * This class represents the Global Search Results activity
 */
public class GlobalSearchResults extends Activity{

	private ContactDataSource contactsDB = new ContactDataSource(this);
	private AuthorDataSource authorsDB = new AuthorDataSource(this);
	private KeynoteDataSource keynotesDB = new KeynoteDataSource(this);
	private EventDataSource eventsDB = new EventDataSource(this);
	private PublicationDataSource pubsDB = new PublicationDataSource(this);
	private SightsDataSource sightsDB = new SightsDataSource(this);
	
	private List<Event> events;
	private List<Author> authors;
	private List<Keynote> keynotes;
	private List<Contact> contacts;
	private List<Author> authorContacts;
	private List<Keynote> keynoteContacts;
	private List<Publication> publications;
	private List<Sight> sights;
	private List<Hotel> hotels;
	private List<Restaurant> restaurants;

	private static final String AUTHORS = "Autores";
	private static final String KEYNOTES = "Keynotes";
	private static final String CONTACTS = "Contactos";
	private static final String EVENTS = "Eventos";
	private static final String PUBLICATIONS = "Publicações";
	private static final String SIGHTS = "Pontos de Interesse";
		
    ExpandableListView resultsView;
    
    List<String> headers;
    
	/**
	 * Updates all the views present in this activity
	 * @param sightsTriple 
	 * 
	 * @param result
	 *            the object to use to populate data
	 */
	private void updateViews(String query) {

		setContentView(R.layout.activity_search_results);
				
		Log.i("UPDATE","Update View...");
				
		searchEvents(query);
		searchAuthors(query);
		searchKeynotes(query);
		searchContacts(query);
		searchPublications(query);
		searchSights(query);
			
		if(authors.size()==0 && keynotes.size()==0 && events.size()==0 && contacts.size()==0 && publications.size()==0 && sights.size()==0 && hotels.size()==0 && restaurants.size()==0){
			TextView view = (TextView) findViewById(R.id.no_results);
			view.setText(this.getString(R.string.NO_RESULTS));			
		}else{
	        resultsView = (ExpandableListView) findViewById(R.id.search_results_view);
	        
	        headers = new ArrayList<String>();
	            
	        Map<String, List<Triple<String, Long,Integer>>> resultsMap = new LinkedHashMap<String, List<Triple<String,Long,Integer>>>();
	        
	        List<Triple<String,Long,Integer>> results = new ArrayList<Triple<String,Long,Integer>>();
		
	        if(events.size()>0){
		        for(Event e: events){
		        	results.add(new Triple<String,Long,Integer>(e.getTitle(), e.getDjangoId(),null));
		        }
		        headers.add(EVENTS);
		        resultsMap.put(EVENTS, results);   
		        results = new ArrayList<Triple<String,Long,Integer>>(); 
	        }
	        
	        if(authors.size()>0){
		        for(Author a: authors){
		        	results.add(new Triple<String,Long,Integer>(a.getName(), a.getId(),null));
		        }
		        headers.add(AUTHORS);
		        resultsMap.put(AUTHORS, results);
		        results = new ArrayList<Triple<String,Long,Integer>>(); 
	        }
	                
	        if(keynotes.size()>0){
		        for(Keynote k: keynotes){
		        	results.add(new Triple<String,Long,Integer>(k.getName(), k.getId(),null));
		        }
		        headers.add(KEYNOTES);
		        resultsMap.put(KEYNOTES, results);   
		        results = new ArrayList<Triple<String,Long,Integer>>(); 
	        }
	        
	        if(contacts.size()>0 || authorContacts.size()>0 || keynoteContacts.size()>0){
		        for(Contact o: contacts){
		        	results.add(new Triple<String,Long,Integer>(o.getName(), o.getId(),ValuesHelper.CONTACTS_TYPE.PARTICIPANT_CONTACT.ordinal()));
		        }
		        for(Author o: authorContacts){
		        	results.add(new Triple<String,Long,Integer>(o.getName(), o.getId(),ValuesHelper.CONTACTS_TYPE.AUTHOR_CONTACT.ordinal()));
		        }
		        for(Keynote o: keynoteContacts){
		        	results.add(new Triple<String,Long,Integer>(o.getName(), o.getId(),ValuesHelper.CONTACTS_TYPE.KEYNOTE_CONTACT.ordinal()));
		        }
		        headers.add(CONTACTS);
		        resultsMap.put(CONTACTS, results);   
		        results = new ArrayList<Triple<String,Long,Integer>>(); 
	        }
	        
	        if(publications.size()>0){
		        for(Publication p: publications){
		        	results.add(new Triple<String,Long,Integer>(p.getTitle(), p.getId(),null));
		        }
		        headers.add(PUBLICATIONS);
		        resultsMap.put(PUBLICATIONS, results);   
		        results = new ArrayList<Triple<String,Long,Integer>>(); 
	        }
	        
	        if(sights.size()>0 || hotels.size()>0 || restaurants.size()>0){

		        for(Hotel s: hotels){
		        	results.add(new Triple<String,Long,Integer>(s.getName(), s.getHotelID(),ValuesHelper.SIGHT_TYPE.HOTEL.ordinal()));
		        }
		        for(Restaurant s: restaurants){
		        	results.add(new Triple<String,Long,Integer>(s.getName(), s.getRestaurantID(),ValuesHelper.SIGHT_TYPE.RESTAURANT.ordinal()));
		        }
		        for(Sight s: sights){
		        	results.add(new Triple<String,Long,Integer>(s.getName(), s.getId(),ValuesHelper.SIGHT_TYPE.OTHER_SIGHT.ordinal()));
		        }
		        
		        headers.add(SIGHTS);
		        resultsMap.put(SIGHTS, results);   
	        }
	        	        
	        final GlobalSearchResultsViewAdapter resultsViewAdapter = new GlobalSearchResultsViewAdapter(this, headers, resultsMap);
	        resultsView.setAdapter(resultsViewAdapter);
/*	        for(int i=0; i<headers.size();i++){ //Para mostrar cada grupo expandido inicialmente
	        	resultsView.expandGroup(i);
	        }*/
	        resultsView.setOnChildClickListener(new OnChildClickListener() {
	
	        	public boolean onChildClick(ExpandableListView parent, View v,
	                    int groupPosition, int childPosition, long id) {
	            	
	                @SuppressWarnings("unchecked")
					final Triple<String,Long,Integer> element = (Triple<String,Long,Integer>) resultsViewAdapter.getChild(
	                        groupPosition, childPosition);
	
	                final Long idElement = element.getSecond();
	                
	                final String header = (String) resultsViewAdapter.getGroup(groupPosition);
	                
	                if(header.equals(EVENTS)){
	                	eventsDB.open();
	                	Event event = eventsDB.getEvent(idElement);
	                	eventsDB.close();
	                	Intent intent;
	                	if (EventTypeChecker.getType(GlobalSearchResults.this, event) == ValuesHelper.EVENT_TYPE.TALK_SESSION.ordinal()) {
							intent = new Intent(GlobalSearchResults.this, TalkSessionActivity.class);
							intent.putExtra(ValuesHelper.EVENT_ID, event.getDjangoId());
							startActivity(intent);
						}else if(EventTypeChecker.getType(GlobalSearchResults.this, event) == ValuesHelper.EVENT_TYPE.POSTER_SESSION.ordinal()) {
							intent = new Intent(GlobalSearchResults.this, PostersSessionActivity.class);
							intent.putExtra(ValuesHelper.EVENT_ID, event.getDjangoId());
							startActivity(intent);
						}else if(EventTypeChecker.getType(GlobalSearchResults.this, event) == ValuesHelper.EVENT_TYPE.KEYNOTE_SESSION.ordinal()) {
							intent = new Intent(GlobalSearchResults.this, KeynoteSessionActivity.class);
							intent.putExtra(ValuesHelper.EVENT_ID, event.getDjangoId());
							startActivity(intent);
						}else if(EventTypeChecker.getType(GlobalSearchResults.this, event) == ValuesHelper.EVENT_TYPE.SOCIAL_EVENT.ordinal()) {
							intent = new Intent(GlobalSearchResults.this, EventActivity.class);
							intent.putExtra(ValuesHelper.EVENT_ID, event.getDjangoId());
							startActivity(intent);
						}else if(EventTypeChecker.getType(GlobalSearchResults.this, event) == ValuesHelper.EVENT_TYPE.WORKSHOP.ordinal()) {
							intent = new Intent(GlobalSearchResults.this, WorkshopActivity.class);
							intent.putExtra(ValuesHelper.EVENT_ID, event.getDjangoId());
							startActivity(intent);
						}
	                	
	                	return true;

	                }
	                
	                if(header.equals(AUTHORS)){
	                	Intent intent = new Intent(GlobalSearchResults.this, AuthorActivity.class);
	                	intent.putExtra(ValuesHelper.AUTHOR_ID, idElement);
	    				startActivity(intent);	
	    				return true;
	                }
	                
	                if(header.equals(KEYNOTES)){
	                	Intent intent = new Intent(GlobalSearchResults.this, KeynoteSpeakerActivity.class);
	                	intent.putExtra(ValuesHelper.KEYNOTE_ID, idElement);
	    				startActivity(intent);
	    				return true;
	                }
	                
	                if(header.equals(CONTACTS)){
	                	Intent intent;
	                	int type = element.getThird();

	                	if(type == ValuesHelper.CONTACTS_TYPE.PARTICIPANT_CONTACT.ordinal()){
		                	intent = new Intent(GlobalSearchResults.this, ContactActivity.class);
		                	intent.putExtra(ValuesHelper.CONTACT_ID, idElement);
	                	}else if(type==ValuesHelper.CONTACTS_TYPE.AUTHOR_CONTACT.ordinal()){
		                	intent = new Intent(GlobalSearchResults.this, AuthorActivity.class);
		                	intent.putExtra(ValuesHelper.AUTHOR_ID, idElement);
	                	}else{
		                	intent = new Intent(GlobalSearchResults.this, KeynoteSpeakerActivity.class);
		                	intent.putExtra(ValuesHelper.KEYNOTE_ID, idElement);
	                	}

	    				startActivity(intent);
	    				return true;

	                }
	                
	                if(header.equals(PUBLICATIONS)){
	                	pubsDB.open();
	    				Publication pub = pubsDB.getPublication(idElement);
	    				pubsDB.close();
	    				Intent intent;
	    				if (PublicationTypeChecker.getType(GlobalSearchResults.this, pub) == ValuesHelper.PUB_TYPE.ARTICLE.ordinal()) {
	    					intent = new Intent(GlobalSearchResults.this, ArticleActivity.class);
	    					intent.putExtra(ValuesHelper.PUBLICATION_ID, idElement);
	    				}
	    				else {
	    					intent = new Intent(GlobalSearchResults.this, PublicationDetailsActivity.class);
	    					intent.putExtra(ValuesHelper.PUBLICATION_ID, idElement);
	    				}
	    				intent.putExtra(ValuesHelper.IS_PARENT_ID, true);
	    				startActivity(intent);
	    				return true;
	                }
	                
	                if(header.equals(SIGHTS)){

	                	Intent intent;
	                	int type = element.getThird();

	                	if(type == ValuesHelper.SIGHT_TYPE.HOTEL.ordinal()){
	    					intent = new Intent(GlobalSearchResults.this, HotelActivity.class);
	    					intent.putExtra(ValuesHelper.SIGHT_ID, idElement);
	    				}else if (type== ValuesHelper.SIGHT_TYPE.RESTAURANT.ordinal()) {
	    					intent = new Intent(GlobalSearchResults.this, RestaurantActivity.class);
	    					intent.putExtra(ValuesHelper.SIGHT_ID, idElement);
	    				}else {
	    					intent = new Intent(GlobalSearchResults.this, SightActivity.class);
	    					intent.putExtra(ValuesHelper.SIGHT_ID, idElement);
	    				}

	    				startActivity(intent);
	    				return true;
	                }

	                return false;
	            }
	        });
			}
			
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
	    // Get the intent, verify the action and get the query
	    Intent intent = getIntent();
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	      String query = intent.getStringExtra(SearchManager.QUERY);
	      updateViews(query);
		
	    }
	    
	    IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction("com.package.ACTION_LOGOUT");
	    registerReceiver(new BroadcastReceiver() {

	        @Override
	        public void onReceive(Context context, Intent intent) {
	            Log.d("onReceive","Logout in progress");
	            finish();
	        }
	    }, intentFilter);
	    
		// Show the Up button in the action bar.
		setupActionBar();
	    
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
		getMenuInflater().inflate(R.menu.search_results, menu);
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
	
	private void searchEvents(String query){
		eventsDB.open();
	    events = eventsDB.search(query).getFirst();
	    eventsDB.close();
	}
	
	private void searchAuthors(String query){
		authorsDB.open();
	    authors =authorsDB.search(query);
	    authorsDB.close();
	}

	private void searchKeynotes(String query){
		keynotesDB.open();
	    keynotes =keynotesDB.search(query);
	    keynotesDB.close();
	}
	
	private void searchContacts(String query){
		contactsDB.open();
		Triple<List<Author>,List<Keynote>,List<Contact>> results = contactsDB.search(query);
		authorContacts = results.getFirst();
		keynoteContacts = results.getSecond();
		contacts = results.getThird();
		contactsDB.close();
	}
	
	private void searchPublications(String query){
		pubsDB.open();
		publications = pubsDB.search(query);
		pubsDB.close();
	}
	
	private void searchSights(String query) {
		sightsDB.open();
		Triple<List<Hotel>,List<Restaurant>,List<Sight>> res = sightsDB.search(query);
		
		hotels = res.getFirst();
		restaurants = res.getSecond();
		sights = res.getThird();
		sightsDB.close();
	}
}
