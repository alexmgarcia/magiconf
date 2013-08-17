package com.example.magiconf_client;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dataRetrieving.AuthInfo;
import dataSources.ConferenceDataSource;
import dataSources.EventDataSource;
import dataSources.ParticipantEventDataSource;

import tables.Conference;
import tables.Event;
import utils.ValuesHelper;
import views.CustomViewPager;

import fragments.ConferenceAgendaFragment;
import fragments.ParticipantAgendaFragment;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;

/**
 * @author Alexandre
 * 
 * This class represents the Agenda Activity
 *
 */
public class AgendaActivity extends FragmentActivity {
	
	public final static String ONLY_IN_AGENDA = "in_agenda";

	private static final String SAVED_INSTANCE_TAB_POS = "tabPos";

	private ConferenceAgendaFragment conferenceAgendaFragment;
	private ParticipantAgendaFragment participantAgendaFragment;
	private EventDataSource eventDB = new EventDataSource(this);
	private ParticipantEventDataSource participantAgendaDB = new ParticipantEventDataSource(this);
	private ConferenceDataSource conferenceDB = new ConferenceDataSource(this);

	private long eventIDToChangeInAgenda;
	
	// Tags to be used to identify the fragments
	private static final String AGENDA_TAG= "a";
	private static final String MY_AGENDA_TAG = "p";
	
	AgendaPageAdapter pageAdapter;
	CustomViewPager pager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
	    if (savedInstanceState != null) {
	        conferenceAgendaFragment = (ConferenceAgendaFragment) getSupportFragmentManager().getFragment(savedInstanceState, AGENDA_TAG);
	        participantAgendaFragment = (ParticipantAgendaFragment) getSupportFragmentManager().getFragment(savedInstanceState, MY_AGENDA_TAG);
	    } else {
	    	conferenceAgendaFragment = new ConferenceAgendaFragment();
	    	participantAgendaFragment = new ParticipantAgendaFragment();
	    }
		setContentView(R.layout.activity_agenda);
		pageAdapter = new AgendaPageAdapter(getSupportFragmentManager());
		pager = (CustomViewPager) findViewById(R.id.agenda_pager);
		pager.setAdapter(pageAdapter);
		
		// Create the tabs bar
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		Tab tab = actionBar.newTab()
				.setText(R.string.conference_agenda_tab)
				.setTabListener(new ActionBar.TabListener() {
					public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
						pager.setCurrentItem(tab.getPosition());
					}

					@Override
					public void onTabReselected(Tab tab, FragmentTransaction ft) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onTabUnselected(Tab tab, FragmentTransaction ft) {
						// TODO Auto-generated method stub
						
					}
				});

		actionBar.addTab(tab);

		tab = actionBar.newTab()
				.setText(R.string.participant_agenda_tab)
				.setTabListener(new ActionBar.TabListener() {

					@Override
					public void onTabReselected(Tab tab,
							android.app.FragmentTransaction ft) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onTabSelected(Tab tab,
							android.app.FragmentTransaction ft) {
						pager.setCurrentItem(tab.getPosition());
						
					}

					@Override
					public void onTabUnselected(Tab tab,
							android.app.FragmentTransaction ft) {
						// TODO Auto-generated method stub
						
					}

				});

		actionBar.addTab(tab);
		
		
		if (savedInstanceState != null) {
			pager.setCurrentItem(savedInstanceState.getInt(SAVED_INSTANCE_TAB_POS));
			actionBar.getTabAt(pager.getCurrentItem()).select();
		}
		
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
	protected void onRestart() {
		conferenceAgendaFragment.refresh(false);
		participantAgendaFragment.refresh(true);
		super.onRestart();
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
		getMenuInflater().inflate(R.menu.agenda, menu);
		return true;
	}
	
	/**
	 * Returns all events from a date
	 * @param d date to get events
	 * @return events from the specified date
	 * @throws ParseException
	 */
	public List<Event> getEventsFromDate(Date d) throws ParseException {
		eventDB.open();
		List<Event> eventList = eventDB.getEventsFromDate(d);
		eventDB.close();
		return eventList;
	}
	
	/**
	 * Returns all events from a date that are on agenda of a participant
	 * @param d date to get events
	 * @param username username of participant
	 * @return events from the specified date
	 * @throws ParseException
	 */
	public List<Event> getEventsInAgendaFromDate(Date d, String username) throws ParseException {
		participantAgendaDB.open();
		List<Event> eventList = participantAgendaDB.getEventsInAgenda(d, username);
		participantAgendaDB.close();
		return eventList;
	}
	
	/**
	 * Returns the first day of conference
	 * @return first day of conference
	 * @throws ParseException 
	 */
	public Date getConferenceFirstDay() throws ParseException {
		conferenceDB.open();
		Conference c = conferenceDB.getConference();
		conferenceDB.close();
		return c.getBeginningDate();
	}

	/**
	 * Returns the last day of conference
	 * @return last day of conference
	 * @throws ParseException 
	 */
	public Date getConferenceLastDay() throws ParseException {
		conferenceDB.open();
		Conference c = conferenceDB.getConference();
		conferenceDB.close();
		return c.getEndingDate();
	}

	
	/**
	 * @author Alexandre
	 * 
	 * Custom page adapter to the agenda
	 *
	 */
	class AgendaPageAdapter extends FragmentPagerAdapter {

		public AgendaPageAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int pos) {
			switch (pos) {
			case 0:
				return conferenceAgendaFragment;
			case 1:
				return participantAgendaFragment;
			default:
				break;
			}
			return null;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = ValuesHelper.PT_LOCALE;
			switch (position) {
			case 0:
				return getString(R.string.conference_agenda_tab).toUpperCase(l);
			case 1:
				return getString(R.string.participant_agenda_tab)
						.toUpperCase(l);
			}
			return null;
		}

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, MainMenu.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		case R.id.action_search: // search button clicked
			Bundle b = new Bundle();
			
			if(pager.getCurrentItem()==0) //TODO: Correcto?
				b.putBoolean(ONLY_IN_AGENDA, false);
			else b.putBoolean(ONLY_IN_AGENDA, true);
			startSearch(null, false, b, false);
			return true;
		case R.id.action_about:
			intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
	    outState.putInt(SAVED_INSTANCE_TAB_POS, pager.getCurrentItem());
	    getSupportFragmentManager().putFragment(outState, AGENDA_TAG, conferenceAgendaFragment);
	    getSupportFragmentManager().putFragment(outState, MY_AGENDA_TAG, participantAgendaFragment);
	    super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		eventIDToChangeInAgenda = (Long)v.getTag(R.id.tag_event_id);
		if (!(Boolean)v.getTag(R.id.tag_in_agenda))
			menu.add(0, R.id.menu_add_agenda, 0, getString(R.string.add_to_agenda));
		else
			menu.add(0, R.id.menu_remove_agenda, 0, getString(R.string.remove_from_agenda));

	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add_agenda:
			participantAgendaDB.open();
			participantAgendaDB.createParticipantEvent(AuthInfo.username, eventIDToChangeInAgenda); // TODO alterar
			conferenceAgendaFragment.refresh(true);
			participantAgendaFragment.refresh(false);
			participantAgendaDB.close();
			return true;
		case R.id.menu_remove_agenda:
			participantAgendaDB.open();
			participantAgendaDB.deleteParticipantEvent(AuthInfo.username, eventIDToChangeInAgenda); // TODO alterar
			participantAgendaFragment.refresh(true);
			conferenceAgendaFragment.refresh(true);
			participantAgendaDB.close();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
}
