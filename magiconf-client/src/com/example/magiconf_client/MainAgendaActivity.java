package com.example.magiconf_client;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import tables.Event;
import utils.ValuesHelper;
import views.AutoResizeTextView;

import dataSources.EventDataSource;
import android.os.Bundle;
import android.R.anim;
import android.R.style;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainAgendaActivity extends Activity {
	
	private static final int TRACK_WIDTH_DP = 140;
	private static final int FIRST_HOUR_CONFERENCE = 8;
	private static final int FIRST_MINUTE_CONFERENCE = 0;
	private static final int FIRST_SECOND_CONFERENCE = 0;
	private static final int FIRST_MILLISESCOND_CONFERENCE = 0;
	private static final int FIRST_HOUR_CONFERENCE_PERIOD = Calendar.AM;
	
	private static final int LAST_HOUR_CONFERENCE = 24;
	
	private static final int MILLISECOND_TO_SECOND = 1000;
	private static final int SECOND_TO_MINUTE = 60;
	
	private static final int EVENT_TIME_ALIGN = Gravity.CENTER;
	private static final int EVENT_TITLE_ALIGN = Gravity.CENTER;
	private static final int EVENT_TITLE_TYPEFACE = Typeface.BOLD;
	private static final int EVENT_TIME_TEXT_SIZE = style.TextAppearance_Small;
	
	private static final int TOTAL_HOURS = LAST_HOUR_CONFERENCE-FIRST_HOUR_CONFERENCE;
	private static final int QUARTER_HOUR = 4;
	private static final int SESSION_MINUTES_INTERVAL = 15;
	
	private static final int MAX_TRACKS_DEFAULT = 6;

	private static final int PADDING_SESSION_BOTTOM_DP = 2;
	private static final int PADDING_SESSION_RIGHT_DP = 2;
	
	private EventDataSource eventDB = new EventDataSource(this);
	private int eventsInAgenda = 0;
	private int nTracks = 0;
	private int currentTrack = 0;
	private List<Boolean>[] timetable;
	Vector<Integer> nEventsInTrack = new Vector<Integer>();

	@SuppressWarnings("unchecked")
	private void initTimetable() {
		timetable = new List[TOTAL_HOURS*QUARTER_HOUR];
		for (int i = 0;i<TOTAL_HOURS*QUARTER_HOUR; i++) {
			timetable[i] = new ArrayList<Boolean>(MAX_TRACKS_DEFAULT);
			for (int j = 0;j<MAX_TRACKS_DEFAULT;j++) {
				timetable[i].add(false);
			}
		}
			
	}
	
	private int getRelativePositionFromStart(Date date) {
		Calendar c = new GregorianCalendar();
		c.setTime(date);
		c.set(Calendar.AM_PM, FIRST_HOUR_CONFERENCE_PERIOD);
		c.set(Calendar.HOUR, FIRST_HOUR_CONFERENCE);
		c.set(Calendar.MINUTE, FIRST_MINUTE_CONFERENCE);
		c.set(Calendar.SECOND, FIRST_SECOND_CONFERENCE);
		c.set(Calendar.MILLISECOND, FIRST_MILLISESCOND_CONFERENCE);
		
		long diff = date.getTime() - c.getTime().getTime();
		
		long minutes = diff / (MILLISECOND_TO_SECOND*SECOND_TO_MINUTE);
		
		long pos = minutes/SESSION_MINUTES_INTERVAL;
		
		Log.i("Pos", (int)pos+"");
		
		return (int)pos;
		
	}
	
	private void addEventToAgenda(Event e) {
		Log.i("Adding ", e.getTitle());
		int duration = e.getDuration();
		int firstPos = getRelativePositionFromStart(e.getTime());
		int nPos = duration/SESSION_MINUTES_INTERVAL;
		for (int i = firstPos;i<firstPos+nPos;i++) {
			timetable[i].set(currentTrack, true);
			//Log.i("Timetable", "true in " + i);
		}
	}
	
	private int getPxFromDp(long dp) {
		Resources r = this.getResources();
		int px = (int) TypedValue.applyDimension(
		        TypedValue.COMPLEX_UNIT_DIP,
		        dp, 
		        r.getDisplayMetrics()
		);
		return px;
	}
	
	private RelativeLayout createNewTrackLayout() {
		RelativeLayout trackLayout = new RelativeLayout(this);
		trackLayout.setId(nTracks++);
		return trackLayout;
	}
	
	private void drawEventInAgenda(Event e) {
		// Agenda layout
		LinearLayout agendaLayout = (LinearLayout) findViewById(R.id.agenda);
		
		
		RelativeLayout trackLayout = null;
		//int currentTrack = 0;
		if (eventsInAgenda == 0) {
			trackLayout = createNewTrackLayout();
			nEventsInTrack.add(0);
		}
		else {
			Log.i("Events", timetable[getRelativePositionFromStart(e.getTime())].size()+"");
			Log.i("EventsInTrack", nEventsInTrack.size()+"");
			boolean found = false;
			int pos = getRelativePositionFromStart(e.getTime());
			int duration = e.getDuration() / QUARTER_HOUR;
			boolean intersect = false;
			int count = 0;
			for (int i = 0;i<nTracks;i++) {
				for (int j = 0;j<duration;j++) {
					if (!timetable[pos+j].get(i))
						count++;
				}
				if (count == duration) { // tem espaço
					trackLayout = (RelativeLayout) findViewById(i);
					nEventsInTrack.set(i, nEventsInTrack.get(i)+1);
					currentTrack = i;
				}
				else
					intersect = true;
				break;
			}
			
			if (intersect) {
				trackLayout = createNewTrackLayout();
				nEventsInTrack.add(0);
				currentTrack = nTracks-1;
			}
		}
			/*if (timetable[pos].size() == 0) {
				for (int i = 0;i<duration && !intersect;i++) {
					if (timetable[pos+i].size() > 0)
						intersect = true;
				}
				if (intersect) {
					trackLayout = createNewTrackLayout();
					nEventsInTrack.add(0);
					found = true;
					currentTrack = nTracks-1;
				}
				else {
					Log.i("Not intersect", "ok");
					if (nEventsInTrack.size() > 0) {
					for (int i = 0;i<nTracks && !found;i++) {
						if (nEventsInTrack.get(i) > 0) {
							trackLayout = (RelativeLayout) findViewById(i);
							found = true;
							currentTrack = i;
						}
						else {
							trackLayout = createNewTrackLayout();
							nEventsInTrack.add(0);
							found = true;
							currentTrack = i;
						}
					}
				}
				}
			}
			else {
				int size = timetable[pos].size();
				for (int i = 0;i<duration && !intersect;i++) {
					if (timetable[pos+i].size() > 0)
						intersect = true;
				}
				if (intersect || nTracks == size) {
					Log.i("New", e.getTitle());
					trackLayout = createNewTrackLayout();
					nEventsInTrack.add(0);
					currentTrack = size;
				}
				/*else if (nTracks == size) {
					trackLayout = createNewTrackLayout();
					nEventsInTrack.add(0);
					currentTrack = size;
				}
				else {
					trackLayout = (RelativeLayout) findViewById(size);
					currentTrack = size;
				}
			}
		}*/
		// Track layout
	//	RelativeLayout trackLayout = new RelativeLayout(this);
	//	trackLayout.setId(nTracks);
		
		// Session layout
		RelativeLayout sessionLayout = new RelativeLayout(this);
		
		LinearLayout.LayoutParams trackParams = new LinearLayout.LayoutParams(getPxFromDp(TRACK_WIDTH_DP), LayoutParams.MATCH_PARENT);
		trackLayout.setLayoutParams(trackParams);
		
		Calendar c = new GregorianCalendar();
		c.setTime(e.getTime());
		c.set(Calendar.AM_PM, FIRST_HOUR_CONFERENCE_PERIOD);
		c.set(Calendar.HOUR, FIRST_HOUR_CONFERENCE);
		c.set(Calendar.MINUTE, FIRST_MINUTE_CONFERENCE);
		c.set(Calendar.SECOND, FIRST_SECOND_CONFERENCE);
		c.set(Calendar.MILLISECOND, FIRST_MILLISESCOND_CONFERENCE);
		
		// Difference between the conference first hour with events and the event time
		long diff = e.getTime().getTime() - c.getTime().getTime();
		diff /= MILLISECOND_TO_SECOND*SECOND_TO_MINUTE;
		
		int eventDurationPx = getPxFromDp(e.getDuration());
		
		RelativeLayout.LayoutParams eventParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, eventDurationPx);
		
		int distanceFromSessionToTheTop = getPxFromDp(diff);
		eventParams.setMargins(0, distanceFromSessionToTheTop, getPxFromDp(PADDING_SESSION_RIGHT_DP), getPxFromDp(PADDING_SESSION_BOTTOM_DP));
		// TODO alterar cor
		if (e.getInAgenda())
			sessionLayout.setBackgroundColor(Color.GREEN);
		else
			sessionLayout.setBackgroundColor(Color.RED);
		sessionLayout.setLayoutParams(eventParams);
		
		
		TextView eventTime = new TextView(this);
		eventTime.setTextAppearance(this, EVENT_TIME_TEXT_SIZE);
		eventTime.setTypeface(null, EVENT_TITLE_TYPEFACE);
		
		diff = e.getTime().getTime() + e.getDuration()*SECOND_TO_MINUTE*MILLISECOND_TO_SECOND;
		Date eventEndDate = new Date(diff);
		SimpleDateFormat dateFormat = new SimpleDateFormat(ValuesHelper.HOUR_FORMAT, Locale.US);
		String startHour = dateFormat.format(e.getTime());
		String endHour = dateFormat.format(eventEndDate);
		eventTime.setText(startHour + "-" + endHour);
		eventTime.setId(e.getTitle().hashCode());
		RelativeLayout.LayoutParams timeParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		eventTime.setGravity(EVENT_TIME_ALIGN);
		eventTime.setLayoutParams(timeParams);
		
		
		AutoResizeTextView eventTitle = new AutoResizeTextView(this);

		eventTitle.setHeight(sessionLayout.getHeight()-eventTime.getHeight());
		
		eventTitle.setText(e.getTitle());
		eventTitle.resizeText(getPxFromDp(TRACK_WIDTH_DP), getPxFromDp(e.getDuration()-14));
		Log.i(e.getTitle(), eventTitle.getHeight()+"");
		Log.i(e.getTitle(), sessionLayout.getHeight()+"");

		Log.i("Settings text", eventTitle.getText().toString());
		RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		titleParams.addRule(RelativeLayout.BELOW, e.getTitle().hashCode());
		

		eventTitle.setLayoutParams(titleParams);
		eventTitle.setGravity(EVENT_TITLE_ALIGN);
		//b.setLayoutParams(params);
		sessionLayout.addView(eventTime);
		sessionLayout.addView(eventTitle);
		//relativeLayout.addView(v);
		trackLayout.addView(sessionLayout);
		Log.i("AddedLayout", eventTitle.getText().toString());
		if (nEventsInTrack.get(currentTrack) == 0) {// TODO
			Log.i("If", "enter");
			Log.i("TrackLayout", trackLayout.getId()+"");
			agendaLayout.addView(trackLayout);
			nEventsInTrack.set(currentTrack, 1);
		}
		else {
			Log.i("Else", "enter");
			Log.i("There was", nEventsInTrack.get(currentTrack)+"");
			Log.i("TrackLayout", trackLayout.getId()+"");
			//agendaLayout.addView(trackLayout);
			nEventsInTrack.set(currentTrack, nEventsInTrack.get(currentTrack)+1);
		}
        eventsInAgenda++;        	
        getRelativePositionFromStart(e.getTime());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_agenda);
		Calendar calEnd = new GregorianCalendar();
		calEnd.setTime(new Date());
		calEnd.set(Calendar.YEAR, 2012);
		calEnd.set(Calendar.MONTH, Calendar.SEPTEMBER);
		calEnd.set(Calendar.DAY_OF_MONTH, 6);
		calEnd.set(Calendar.HOUR_OF_DAY, 0);
		calEnd.set(Calendar.MINUTE, 0);
		calEnd.set(Calendar.SECOND, 0);
		calEnd.set(Calendar.MILLISECOND, 0);
		
		initTimetable();
        eventDB.open();
        Date d = calEnd.getTime();
        try {
			List<Event> eventList = eventDB.getEventsFromDate(d);
			Log.i("EventList", eventList.size()+"");
			Iterator<Event> it = eventList.iterator();
			Event next;
			while (it.hasNext()) {
				next = it.next();
				drawEventInAgenda(next);
				addEventToAgenda(next);
			}
			eventDB.close();
			
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_agenda, menu);
		return true;
	}

}
