package utils;

import java.util.Comparator;

import tables.Event;

/**
 * @author Alexandre
 *
 * This comparator sorts a collection of events by date
 */
public class EventComparator implements Comparator<Event> {

	@Override
	public int compare(Event e1, Event e2) {
		if (e1.getTime().compareTo(e2.getTime()) > 0)
			return 1;
		else if (e1.getTime().compareTo(e2.getTime()) < 0)
			return -1;
		return 0;
	}

}
