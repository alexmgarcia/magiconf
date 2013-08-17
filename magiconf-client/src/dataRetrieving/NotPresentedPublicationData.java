package dataRetrieving;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tables.Article;
import tables.Author;
import tables.Hotel;
import tables.NotPresentedPublication;
import tables.Sight;
import utils.RestClient;
import utils.ValuesHelper;
import android.util.Log;
import dataSources.ArticleDataSource;
import dataSources.NotPresentedPublicationDataSource;
import dataSources.PublicationDataSource;
import dataSources.SightsDataSource;

public class NotPresentedPublicationData {
	

	public static String RESOURCE_NOT_PRESENTED_PUB = "/notpresentedpublication/";
	public static String PARAM_TITLE="title=";
	
	public static NotPresentedPublication downloadNotPresentedPubData(String title,String parcitipantUsername,String password,
			NotPresentedPublicationDataSource pubDB){

		String encodedTitle = title.replace(" ", "%20");
	//TODO: http://10.22.145.102:8000/api/v1/notpresentedpublication/?format=json&user_name=joseandrade&password=662eaa47199461d01a623884080934ab&title=Toward%20the%20design%20quality%20evaluation%20of%20object-oriented%20software%20systems
	String request= ValuesHelper.SERVER_API_ADDRESS+
			RESOURCE_NOT_PRESENTED_PUB + "?"+
			ValuesHelper.FormatJsonParam + "&"+
			ValuesHelper.UsernameParam + parcitipantUsername+ "&"+
			ValuesHelper.PasswordParam + password + "&"+
			PARAM_TITLE + encodedTitle;

	Log.i("ARTICLEDATA",request);


	JSONObject jData = RestClient.makeRequest(request,false,0);
	try {
		JSONArray aux =(JSONArray) jData.get("objects");
		//We will always get One or none! Filter applied is the resource PK
		if(aux.length() > 0){
			JSONObject object = aux.getJSONObject(0);
			NotPresentedPublication pub = createNewNotPresentedPublication(object,pubDB);
			return pub;
		}
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	return null;
}
	
	
	public static List<JSONObject> downloadNotPresentedPublications(String parcitipantUsername,String password){
		List<JSONObject> list = new LinkedList<JSONObject>();
		
		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_NOT_PRESENTED_PUB + "?"+
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
		
		Log.i("NOT PRESENTED PUBS", "Number requests: "+list.size());
		return list;
	}

	
	public static void createNotPresentedPublications(JSONArray data, NotPresentedPublicationDataSource pubsDB) {
		NotPresentedPublication aux;
		Log.i("CREATE NOT PRESENTED PUBS", "Start adding");
		pubsDB.open();
		Log.i("CREATE NOT PRESENTED PUBS", ""+data.length());
		for(int i = 0 ; i < data.length() ; i++){
			try {
				JSONObject k = data.getJSONObject(i);
				String name= k.getString("title");
				if(!pubsDB.hasPublication(name)){
					aux = pubsDB.createNotPresentedPublication(name, k.getString("abstract"), k.getString("authors"));
					Log.i("CREATE NOT PRESENTED PUB", "Creating pub"+aux.toString());
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		pubsDB.close();
	}
	
	/**
	 * 
	 * @param object
	 * @param pubDB
	 * @return
	 */
	private static NotPresentedPublication createNewNotPresentedPublication(JSONObject object ,
			NotPresentedPublicationDataSource pubDB) {
		pubDB.open();
		Log.i("NEWARTICLE","Creating entry");
		NotPresentedPublication newPub = null;
		try {		
			Log.i("NEWARTICLE","Title: "+object.getString("title"));
			String name = object.getString("title");
			newPub = pubDB.createNotPresentedPublication(name, object.getString("abstract"), object.getString("authors"));
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		pubDB.close();
		return newPub;
	}
	
	public static void downloadAllNotPresentedPublications(String parcitipantUsername,String password,
			NotPresentedPublicationDataSource notPresentedPubsDB){
		List<JSONObject> list = getAllNotPresentedPublications(parcitipantUsername, password, notPresentedPubsDB);
		Iterator<JSONObject> it = list.iterator();
		try {
			notPresentedPubsDB.open();
			while(it.hasNext()){
				JSONObject obj = it.next();
				JSONArray data;

				data = obj.getJSONArray("objects");

				createNotPresentedPublications(data,notPresentedPubsDB);

			}	
			notPresentedPubsDB.close();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	
	
	private static List<JSONObject> getAllNotPresentedPublications(String parcitipantUsername,String password,
			NotPresentedPublicationDataSource notPresentedPubsDB){
		List<JSONObject> list = new LinkedList<JSONObject>();

		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_NOT_PRESENTED_PUB  + "?"+
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

		Log.i("NOT PRESENTED PUB", "Number requests: "+list.size());
		return list;

	}


}
