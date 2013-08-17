package dataRetrieving;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tables.Author;
import tables.Contact;
import tables.Event;
import tables.Workshop;
import utils.RestClient;
import utils.ValuesHelper;
import android.text.format.DateFormat;
import android.util.Log;
import dataSources.AuthorDataSource;
import dataSources.EventDataSource;
import dataSources.WorkshopDataSource;

public class SocialEventData {


	public static String RESOURCE_SOCIAL_EVENT = "/socialevent/";

	public static Event downloadSocialEventData(Long id,String parcitipantUsername,String password,
			EventDataSource eventDB,boolean update){

		//TODO: http://192.168.1.69:8000/api/v1/socialevent/?format=json&user_name=joseandrade&password=662eaa47199461d01a623884080934ab
		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_SOCIAL_EVENT   + id + "/" + "?"+
				ValuesHelper.FormatJsonParam + "&"+
				ValuesHelper.UsernameParam + parcitipantUsername+ "&"+
				ValuesHelper.PasswordParam + password;

		Log.i("SOCIAL_EVENT DATA",request);


		JSONObject jData = RestClient.makeRequest(request,false,0);
		try {
			//JSONArray aux =(JSONArray) jData.get("objects");
			//We will always get One or none! Filter applied is the resource PK
			if(jData.length() > 0){

				JSONObject object = jData;
				Log.i("SOCIAL_EVENT ","JSON: "+object.toString());
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				Date d = null;
				try {
					d = new java.sql.Date(format.parse(object.getString("time")).getTime());
				} catch (ParseException e) {

					e.printStackTrace();
					return null;
				}
				eventDB.open();
				
				Event e = eventDB.getEvent(Long.parseLong(object.getString("id")));
				if(e == null)
					e = eventDB.createEvent(Long.parseLong(object.getString("id")),object.getString("title"),object.getString("place"), d, Integer.parseInt(object.getString("duration")));
				else if(update){
					eventDB.update(e.getId(),Long.parseLong(object.getString("id")),object.getString("title"),object.getString("place"), d, Integer.parseInt(object.getString("duration")),new java.util.Date());
				}
				eventDB.close();
				return e;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}


	public static void downloadAllSocialEvents(String parcitipantUsername,String password,
			EventDataSource eventDB){
		List<JSONObject> list = getAllSocialEvents(parcitipantUsername, password, eventDB);
		Iterator<JSONObject> it = list.iterator();
		try {
			eventDB.open();
			while(it.hasNext()){
				JSONObject obj = it.next();
				JSONArray data;

				data = obj.getJSONArray("objects");

				createSocialEvents(data,eventDB);

			}
			eventDB.close();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	private static void createSocialEvents(JSONArray data, EventDataSource eventDB) {
		Log.i("CREATE SOCIAL_EVENT", "Start adding");
		Long id;
		Log.i("CREATE SOCIAL_EVENT", ""+data.length());
		for(int i = 0 ; i < data.length() ; i++){
			try {
				JSONObject k = data.getJSONObject(i);
				Log.i("SOCIAL_EVENT ","JSON: "+k.toString());
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				Date d = null;
				try {
					d = new java.sql.Date(format.parse(k.getString("time")).getTime());
				} catch (ParseException e) {

					e.printStackTrace();
					return;
				}
				id = Long.parseLong(k.getString("id"));
				Event e = eventDB.getEvent(Long.parseLong(k.getString("id")));
				if(e == null)
					eventDB.createEvent(id,k.getString("title"),k.getString("place"), d, Integer.parseInt(k.getString("duration")));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
	}


	private static List<JSONObject> getAllSocialEvents(String parcitipantUsername,String password,
			EventDataSource eventDB){
		List<JSONObject> list = new LinkedList<JSONObject>();

		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_SOCIAL_EVENT  + "?"+
				ValuesHelper.FormatJsonParam + "&"+
				ValuesHelper.UsernameParam + parcitipantUsername+ "&"+
				ValuesHelper.PasswordParam + password;

		JSONObject jData = RestClient.makeRequest(request,false,0);

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
					jData = RestClient.makeRequest(request,true,offset);
					offset+=limit;
					list.add(jData);
				}
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Log.i("SOCIAL_EVENT", "Number requests: "+list.size());
		return list;



	}




}
