/**
 * 
 */
package utils;

import java.util.List;

import org.json.JSONObject;

import tables.Participant;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.magiconf_client.ContactsActivity;

import dataRetrieving.AuthInfo;
import dataRetrieving.ContactData;
import dataRetrieving.KeynoteSessionData;
import dataRetrieving.PosterData;
import dataRetrieving.PosterSessionData;
import dataRetrieving.SocialEventData;
import dataRetrieving.TalkSessionData;
import dataRetrieving.WorkshopData;
import dataSources.ConferenceDataSource;
import dataSources.EventDataSource;
import dataSources.KeynoteSessionDataSource;
import dataSources.PosterSessionDataSource;
import dataSources.SessionDataSource;
import dataSources.TalkDataSource;
import dataSources.WorkshopDataSource;

/**
 * @author David
 *
 */
public class EventUpdater {

	private static final String EVENT_TALKSESSION="talksession";
	private static final String EVENT_POSTERSESSION="postersession";
	private static final String EVENT_KEYNOTESESSION="keynotesession";
	private static final String EVENT_WORKSHOPSESSION="workshopsession";
	private static final String EVENT_SOCIALEVENT="socialevent";
	
	private Context ctx;
	
	private WorkshopDataSource workshopDB; 
	private TalkDataSource talksessionDB;
	private KeynoteSessionDataSource keynoteSessionDB;
	private PosterSessionDataSource postersessionDB;
	private EventDataSource eventDB ;

	
	
	
	public EventUpdater(Context ctx) {
		super();
		this.ctx = ctx;
	}


	private class RESTGetTask extends AsyncTask<String, Void, List<JSONObject>> {
		protected List<JSONObject> doInBackground(String... params) {
			String type = params[1];
			long id = Long.parseLong(params[0]);

			if(type.equals(EVENT_TALKSESSION)){
				talksessionDB = new TalkDataSource(ctx);
				talksessionDB.open();
				TalkSessionData.downloadTalkSessionData(id, AuthInfo.username, AuthInfo.password, talksessionDB, ctx,true);
				talksessionDB.close();
			}
			else if(type.equals(EVENT_POSTERSESSION)){
				postersessionDB = new PosterSessionDataSource(ctx);
				postersessionDB.open();
				PosterSessionData.downloadPosterSessionData(id, AuthInfo.username, AuthInfo.password, postersessionDB, ctx,true);
				postersessionDB.close();
			}
			else if(type.equals(EVENT_WORKSHOPSESSION)){
				workshopDB = new WorkshopDataSource(ctx);
				workshopDB.open();
				WorkshopData.downloadWorkshopData(id, AuthInfo.username, AuthInfo.password, workshopDB,true);
				workshopDB.close();
			}
			else if(type.equals(EVENT_KEYNOTESESSION)){
				keynoteSessionDB = new KeynoteSessionDataSource(ctx);
				keynoteSessionDB.open();
				KeynoteSessionData.downloadKeynoteSessionData(id, AuthInfo.username, AuthInfo.password, keynoteSessionDB, ctx,true);
				keynoteSessionDB.close();
			}
			else if(type.equals(EVENT_SOCIALEVENT)){
				eventDB  =  new EventDataSource(ctx);
				eventDB.open();
				SocialEventData.downloadSocialEventData(id, AuthInfo.username, AuthInfo.password, eventDB,true);
				eventDB.close();
			}
			return null;
		}


		protected void onPostExecute(List<JSONObject> result) {
			Log.i("EventUpdater","EVENTS UPDATED");
		}
	}


	public void updateEvent(String id, String type){
		new RESTGetTask().execute(id,type);
	}

}
