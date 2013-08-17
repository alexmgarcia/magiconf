package fragments;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import tables.Event;
import utils.EventTypeChecker;
import utils.ValuesHelper;
import views.AutoResizeTextView;

import com.example.magiconf_client.AgendaActivity;
import com.example.magiconf_client.EventActivity;
import com.example.magiconf_client.KeynoteSessionActivity;
import com.example.magiconf_client.PostersSessionActivity;
import com.example.magiconf_client.R;
import com.example.magiconf_client.TalkSessionActivity;
import com.example.magiconf_client.WorkshopActivity;

import dataRetrieving.AuthInfo;

import android.R.color;
import android.R.style;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ParticipantAgendaFragment extends Fragment {
	
	private static final String HOUR_SEPARATOR = "-";
	private static final int MULTIPLY_FACTOR = 3;
	private static final int TRACK_WIDTH_DP = 140;
	private static final int FIRST_HOUR_CONFERENCE = 8;
	private static final int FIRST_MINUTE_CONFERENCE = 0;
	private static final int FIRST_SECOND_CONFERENCE = 0;
	private static final int FIRST_MILLISESCOND_CONFERENCE = 0;
	private static final int FIRST_HOUR_CONFERENCE_PERIOD = Calendar.AM;
	
	private static final int LAST_HOUR_CONFERENCE = 24;
	
	private static final int MILLISECOND_TO_SECOND = 1000;
	private static final int SECOND_TO_MINUTE = 60;
	private static final int MINUTE_TO_HOUR = 60;
	private static final int HOUR_TO_DAY = 24;
	
	private static final int EVENT_TIME_ALIGN = Gravity.CENTER;
	private static final int EVENT_TITLE_ALIGN = Gravity.CENTER;
	private static final int EVENT_TITLE_TYPEFACE = Typeface.BOLD;
	private static final int EVENT_TIME_TEXT_SIZE = style.TextAppearance_Small;
	private static final int EVENT_TITLE_DISTANCE_TOP_DP = 14;
	
	private static final int TOTAL_HOURS = LAST_HOUR_CONFERENCE-FIRST_HOUR_CONFERENCE;
	private static final int QUARTER_HOUR = 4;
	private static final int SESSION_MINUTES_INTERVAL = 15;
	
	private static final int MAX_TRACKS_DEFAULT = 6;

	private static final int PADDING_SESSION_BOTTOM_DP = 2;
	private static final int PADDING_SESSION_RIGHT_DP = 2;
	
	private static final String SAVED_INSTANCE_DATE_KEY = "date";
	
	private static final int DAY_BUTTON_TOP_PADDING_DP = 1;
	private static final int DAY_BUTTON_LEFT_PADDING_DP = 1;
	
	private int eventsInAgenda ;
	private int nTracks ;
	private int currentTrack;
	private List<Boolean>[] timetable;
	Vector<Integer> nEventsInTrack;
	private List<Button> daysButtons = new LinkedList<Button>();
	private Map<String, Date> buttonDate = new HashMap<String, Date>();
	private final Handler handler = new Handler();
	private Date d;
	private ProgressDialog loadingdialog;
	
	Date beginningDate;
	Date endingDate;

	/**
	 * Initializes the data structure that represents the timetable
	 */
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
	
	/**
	 * Returns the relative position from agenda first hour to the specified hour
	 * @param date hour to get relative position to top
	 * @return relative position from agenda first hour to the specified hour
	 */
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
		
		return (int)pos;
		
	}
	
	/**
	 * Inserts an event into timetable structure
	 * @param e event to insert
	 */
	private void addEventToAgenda(Event e) {
		int duration = e.getDuration();
		int firstPos = getRelativePositionFromStart(e.getTime());
		int nPos = duration/SESSION_MINUTES_INTERVAL;
		for (int i = firstPos;i<firstPos+nPos;i++)
			timetable[i].set(currentTrack, true);
	}
	
	/**
	 * Creates a new track layout
	 * @return created track layout
	 */
	private RelativeLayout createNewTrackLayout() {
		RelativeLayout trackLayout = new RelativeLayout(getActivity());
		trackLayout.setId(nTracks++);
		if (nTracks+1 > timetable[0].size()) {
			for (int i = 0;i<TOTAL_HOURS*QUARTER_HOUR; i++) {
				for (int j = 0;j<MAX_TRACKS_DEFAULT;j++) {
					timetable[i].add(false);
				}
			}
		}
		return trackLayout;
	}
	
	/**
	 * Draws an event in agenda
	 * @param e event to draw
	 */
	private void drawEventInAgenda(final Event e) {
		
		// Agenda layout
		LinearLayout agendaLayout = (LinearLayout) getView().findViewById(R.id.agenda);
		
		RelativeLayout trackLayout = null;
		if (eventsInAgenda == 0) {
			trackLayout = createNewTrackLayout();
			nEventsInTrack.add(0);
		}
		else {
			int pos = getRelativePositionFromStart(e.getTime());
			int duration = e.getDuration() / SESSION_MINUTES_INTERVAL;
			boolean intersect = false;
			int count = 0;
			
			// Will try to find an empty position to draw an event
			int i;
			for (i = 0;i<nTracks;i++) {
				for (int j = 0;j<duration;j++) {
					if (!timetable[pos+j].get(i))
						count++;
				}
				if (count == duration)
					break;
				else
					count = 0;
			}
			if (count == duration) { // it has an empty position which the current event fits on
				trackLayout = (RelativeLayout) getView().findViewById(i);
				nEventsInTrack.set(i, nEventsInTrack.get(i)+1);
				currentTrack = i;
			}
			else
				intersect = true;
			
			if (intersect) { // there's no empty position so let's create a new track
				trackLayout = createNewTrackLayout();
				nEventsInTrack.add(0);
				currentTrack = nTracks-1;
			}
		}
				
		// Session layout
		RelativeLayout sessionLayout = new RelativeLayout(getActivity());
		
		LinearLayout.LayoutParams trackParams = new LinearLayout.LayoutParams(ValuesHelper.getPxFromDp(getActivity(), TRACK_WIDTH_DP), LayoutParams.MATCH_PARENT);
		trackLayout.setLayoutParams(trackParams);
		
		// Helper object to get milliseconds from a date
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
		
		int eventDurationPx = ValuesHelper.getPxFromDp(getActivity(), e.getDuration()*MULTIPLY_FACTOR);
		
		RelativeLayout.LayoutParams eventParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, eventDurationPx);
		
		int distanceFromSessionToTheTop = ValuesHelper.getPxFromDp(getActivity(), diff*MULTIPLY_FACTOR);
		eventParams.setMargins(0, distanceFromSessionToTheTop, ValuesHelper.getPxFromDp(getActivity(), PADDING_SESSION_RIGHT_DP), 0);

		if (e.getInAgenda())
			sessionLayout.setBackgroundColor(getResources().getColor(R.color.session_in_agenda));
		else
			sessionLayout.setBackgroundColor(getResources().getColor(R.color.session_not_agenda));
		
		sessionLayout.setLayoutParams(eventParams);
		
		TextView eventTime = new TextView(getActivity());
		eventTime.setTextAppearance(getActivity(), EVENT_TIME_TEXT_SIZE);
		eventTime.setTypeface(null, EVENT_TITLE_TYPEFACE);
		
		diff = e.getTime().getTime() + e.getDuration()*SECOND_TO_MINUTE*MILLISECOND_TO_SECOND;
		Date eventEndDate = new Date(diff);
		SimpleDateFormat dateFormat = new SimpleDateFormat(ValuesHelper.HOUR_FORMAT, Locale.US);
		String startHour = dateFormat.format(e.getTime());
		String endHour = dateFormat.format(eventEndDate);
		eventTime.setText(startHour + HOUR_SEPARATOR + endHour);
		eventTime.setId(e.getTitle().hashCode());
		RelativeLayout.LayoutParams timeParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		eventTime.setGravity(EVENT_TIME_ALIGN);
		eventTime.setLayoutParams(timeParams);
		
		AutoResizeTextView eventTitle = new AutoResizeTextView(getActivity());

		eventTitle.setHeight(sessionLayout.getHeight()-eventTime.getHeight());
		
		eventTitle.setText(e.getTitle());
		eventTitle.resizeText(ValuesHelper.getPxFromDp(getActivity(), TRACK_WIDTH_DP), ValuesHelper.getPxFromDp(getActivity(), (e.getDuration()*MULTIPLY_FACTOR)-EVENT_TITLE_DISTANCE_TOP_DP));

		RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		titleParams.addRule(RelativeLayout.BELOW, e.getTitle().hashCode());

		eventTitle.setLayoutParams(titleParams);
		eventTitle.setGravity(EVENT_TITLE_ALIGN);

		sessionLayout.addView(eventTime);
		sessionLayout.addView(eventTitle);
		
		sessionLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent;
				if (EventTypeChecker.getType(getActivity(), e) == ValuesHelper.EVENT_TYPE.KEYNOTE_SESSION.ordinal()) {
					intent = new Intent(getActivity(), KeynoteSessionActivity.class);
					intent.putExtra(ValuesHelper.EVENT_ID, e.getDjangoId());
					startActivity(intent);
				}
				else if (EventTypeChecker.getType(getActivity(), e) == ValuesHelper.EVENT_TYPE.POSTER_SESSION.ordinal()) {
					intent = new Intent(getActivity(), PostersSessionActivity.class);
					intent.putExtra(ValuesHelper.EVENT_ID, e.getDjangoId());
					startActivity(intent);
				}
				else if (EventTypeChecker.getType(getActivity(), e) == ValuesHelper.EVENT_TYPE.TALK_SESSION.ordinal()) {
					intent = new Intent(getActivity(), TalkSessionActivity.class);
					intent.putExtra(ValuesHelper.EVENT_ID, e.getDjangoId());
					Log.i("djangoid", e.getDjangoId()+"");
					startActivity(intent);
				}
				else if (EventTypeChecker.getType(getActivity(), e) == ValuesHelper.EVENT_TYPE.WORKSHOP.ordinal()) {
					intent = new Intent(getActivity(), WorkshopActivity.class);
					intent.putExtra(ValuesHelper.EVENT_ID, e.getDjangoId());
					startActivity(intent);
				}		
				else if (EventTypeChecker.getType(getActivity(), e) == ValuesHelper.EVENT_TYPE.SOCIAL_EVENT.ordinal()) {
					intent = new Intent(getActivity(), EventActivity.class);
					intent.putExtra(ValuesHelper.EVENT_ID, e.getDjangoId());
					startActivity(intent);
				}
			}
		});
		
		// Create context menu for the session
		registerForContextMenu(sessionLayout);
		
		// Save some information about the event in session layout to be used by context menu
		sessionLayout.setTag(R.id.tag_event_id, e.getId());
		sessionLayout.setTag(R.id.tag_in_agenda, e.getInAgenda());
		
		trackLayout.addView(sessionLayout);
		
		View line = new View(getActivity());
		RelativeLayout.LayoutParams lineParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, ValuesHelper.getPxFromDp(getActivity(), PADDING_SESSION_BOTTOM_DP));
		line.setBackgroundColor(getResources().getColor(color.black));
		lineParams.setMargins(0, distanceFromSessionToTheTop, 0, 0);
		line.setLayoutParams(lineParams);
		
		trackLayout.addView(line); // event separator
		if (nEventsInTrack.get(currentTrack) == 0) { // it is an empty track, so let's add it to agendaLayout
			agendaLayout.addView(trackLayout);
			nEventsInTrack.set(currentTrack, 1);
		}
		else
			nEventsInTrack.set(currentTrack, nEventsInTrack.get(currentTrack)+1);
        eventsInAgenda++;        	
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.tab_participant_agenda, container, false);
	}
	
	/**
	 * Draws the agenda for a specified day
	 * @param day day to draw agenda
	 */
	private void drawAgenda(Date day) {
        try {
    		eventsInAgenda = 0;
    		nTracks = 0;
    		currentTrack = 0;
    		nEventsInTrack = new Vector<Integer>();
    		
    		LinearLayout agendaLayout = (LinearLayout) getView().findViewById(R.id.agenda);
    		agendaLayout.removeAllViews();
    		
    		initTimetable();
    		
			List<Event> eventList = ((AgendaActivity)getActivity()).getEventsInAgendaFromDate(day, AuthInfo.username);
			Iterator<Event> it = eventList.iterator();
			Event next;
			while (it.hasNext()) {
				next = it.next();
				drawEventInAgenda(next);
				addEventToAgenda(next);
			}
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Draws the days buttons and the agenda
	 */
	private void drawAgendaAndButtons() {
		final Calendar calEnd = new GregorianCalendar();
		calEnd.setTime(beginningDate);
		
		long diff = endingDate.getTime() - beginningDate.getTime();
		int diffDays = (int)diff / (MILLISECOND_TO_SECOND * SECOND_TO_MINUTE * MINUTE_TO_HOUR * HOUR_TO_DAY);
		LinearLayout linearLayout = (LinearLayout) getView().findViewById(R.id.conference_days);
		SimpleDateFormat df = new SimpleDateFormat(ValuesHelper.DATE_FORMAT_AGENDA, ValuesHelper.PT_LOCALE); 
		Button b;

		loadingdialog = ProgressDialog.show(getActivity(),
                "",getString(R.string.loading,true));
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(ValuesHelper.getPxFromDp(getActivity(), DAY_BUTTON_LEFT_PADDING_DP), ValuesHelper.getPxFromDp(getActivity(), DAY_BUTTON_TOP_PADDING_DP), 0, 0);
		
		// Creates buttons for all days
		for (int i = 0;i<=diffDays;i++) {
			b = new Button(getActivity());
			b.setText(df.format(calEnd.getTime()));
			buttonDate.put((String) b.getText(), calEnd.getTime());
			if (calEnd.getTime().getTime() == d.getTime())
				b.setBackgroundResource(R.drawable.button_day_active);
			else
				b.setBackgroundResource(R.drawable.button_day_enabled);
			
			daysButtons.add(b);
			
			// Open event details when an event was clicked
			b.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					loadingdialog = ProgressDialog.show(getActivity(),
			                "",getString(R.string.loading),true);
					((Button)v).setActivated(false);
					((Button)v).setBackgroundResource(R.drawable.button_day_active);
					Iterator<Button> it = daysButtons.iterator();
					Button next;
					
					// Deactivate day buttons
					while (it.hasNext()) {
						next = it.next();
						next.setActivated(false);
						if (next != v)
							next.setBackgroundResource(R.drawable.button_day_enabled);
					}
					
					d = buttonDate.get(((Button)v).getText());
					
					// Needs to run in another thread because drawing agenda is an heavy task
					new Thread(new Runnable() {
						@Override
						public void run() {
							drawAgendaTask();
						}
					}).start();
				}
			});
			calEnd.add(Calendar.DATE, 1);
			linearLayout.addView(b, params);
		}
		
		// Draw agenda for default selected day
		new Thread(new Runnable() {
			@Override
			public void run() {
				drawAgendaTask();
			}
		}).start();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
	    outState.putLong(SAVED_INSTANCE_DATE_KEY, d.getTime());
	    super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		try {
			beginningDate = ((AgendaActivity)getActivity()).getConferenceFirstDay();
			endingDate = ((AgendaActivity)getActivity()).getConferenceLastDay();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (savedInstanceState == null) // there was no previous check of agenda so default selected day is the conference's first day
			d = beginningDate;
		else
			d = new Date(savedInstanceState.getLong(SAVED_INSTANCE_DATE_KEY));
		
		drawAgendaAndButtons();
	}
	
	/**
	 * Refreshes the agenda
	 * @param showLoading if will be shown the loading progress dialog
	 */
	public void refresh(boolean showLoading) {
		if (showLoading)
			loadingdialog = ProgressDialog.show(getActivity(),
					"",getString(R.string.loading),true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				drawAgendaTask();
			}
		}).start();
	}
	
	/**
	 * Calls the drawAgendaRunnable with a delay of 100ms to allow the 
	 * loading dialog to show before running the task
	 */
	private void drawAgendaTask() {
		handler.postDelayed(drawAgendaRunnable, 100);
	}
	
	/**
	 * Runnable to draw the agenda in another thread
	 * It allows to modify layouts created by a thread in another thread
	 */
	final Runnable drawAgendaRunnable = new Runnable() {
		public void run() {
			drawAgenda(d);
			
			// Activate day buttons
			Iterator<Button> it = daysButtons.iterator();
			while (it.hasNext())
				it.next().setActivated(true);
			loadingdialog.dismiss();
		}
		
	};
}
