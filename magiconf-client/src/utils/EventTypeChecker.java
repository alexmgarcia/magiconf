package utils;

import android.content.Context;
import dataSources.KeynoteSessionDataSource;
import dataSources.PosterSessionDataSource;
import dataSources.TalkDataSource;
import dataSources.WorkshopDataSource;
import tables.Event;

/**
 * @author Alexandre
 * 
 * This class represents an event type checker
 *
 */
public class EventTypeChecker {
	
	/**
	 * Returns type of an event
	 * @param context application context
	 * @param e event to get type
	 * @return type of the specified event
	 */
	public static int getType(Context context, Event e) {
		TalkDataSource talksDB = new TalkDataSource(context);
		KeynoteSessionDataSource keynoteSessionsDB = new KeynoteSessionDataSource(context);
		PosterSessionDataSource posterSessionsDB = new PosterSessionDataSource(context);
		WorkshopDataSource workshopsDB = new WorkshopDataSource(context);
		
		talksDB.open();
		keynoteSessionsDB.open();
		posterSessionsDB.open();
		workshopsDB.open();
		
		int type = 0;
		
		if (talksDB.hasTalkEvent(e.getId())) 
			type = ValuesHelper.EVENT_TYPE.TALK_SESSION.ordinal();
		else if (keynoteSessionsDB.hasKeynoteSessionEvent(e.getId())) {
			type = ValuesHelper.EVENT_TYPE.KEYNOTE_SESSION.ordinal();
		}
		else if (posterSessionsDB.hasPosterSessionEvent(e.getId()))
			type = ValuesHelper.EVENT_TYPE.POSTER_SESSION.ordinal();
		else if (workshopsDB.hasWorkshopEvent(e.getId()))
			type = ValuesHelper.EVENT_TYPE.WORKSHOP.ordinal();
		else type = ValuesHelper.EVENT_TYPE.SOCIAL_EVENT.ordinal();
		
		talksDB.close();
		keynoteSessionsDB.close();
		posterSessionsDB.close();
		workshopsDB.close();
		
		return type;
		
	}
	
	/**
	 * Returns type of an event
	 * @param context application context
	 * @param id of the event to get type
	 * @return type of the specified event
	 */
	public static int getTypeById(Context context, Long id) {
		TalkDataSource talksDB = new TalkDataSource(context);
		KeynoteSessionDataSource keynoteSessionsDB = new KeynoteSessionDataSource(context);
		PosterSessionDataSource posterSessionsDB = new PosterSessionDataSource(context);
		WorkshopDataSource workshopsDB = new WorkshopDataSource(context);
		
		talksDB.open();
		keynoteSessionsDB.open();
		posterSessionsDB.open();
		workshopsDB.open();
		
		int type = 0;
		
		if (talksDB.hasTalkEvent(id)) 
			type = ValuesHelper.EVENT_TYPE.TALK_SESSION.ordinal();
		else if (keynoteSessionsDB.hasKeynoteSessionEvent(id)) {
			type = ValuesHelper.EVENT_TYPE.KEYNOTE_SESSION.ordinal();
		}
		else if (posterSessionsDB.hasPosterSessionEvent(id))
			type = ValuesHelper.EVENT_TYPE.POSTER_SESSION.ordinal();
		else if (workshopsDB.hasWorkshopEvent(id))
			type = ValuesHelper.EVENT_TYPE.WORKSHOP.ordinal();
		else type = ValuesHelper.EVENT_TYPE.SOCIAL_EVENT.ordinal();
		
		talksDB.close();
		keynoteSessionsDB.close();
		posterSessionsDB.close();
		workshopsDB.close();
		
		return type;
		
	}


}
