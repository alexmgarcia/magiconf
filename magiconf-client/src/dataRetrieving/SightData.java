package dataRetrieving;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import tables.Sight;
import utils.RestClient;
import utils.ValuesHelper;
import android.content.Context;
import android.util.Log;
import dataSources.SightsDataSource;

public class SightData {
	

	public static String RESOURCE_SIGHT = "/sight/";

	public static Sight downloadSightData(Long id,String parcitipantUsername,String password,
			SightsDataSource sightsDB){

		//TODO: http://10.130.124.251:8000/api/v1/event/1/?format=json&user_name=joseandrade&password=662eaa47199461d01a623884080934ab
		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_SIGHT  + id + "/" + "?"+
				ValuesHelper.FormatJsonParam + "&"+
				ValuesHelper.UsernameParam + parcitipantUsername+ "&"+
				ValuesHelper.PasswordParam + password;

		Log.i("SIGHTDATA",request);


		JSONObject jData = RestClient.makeRequest(request,false,0);
		Sight aux=null;
		try {
			//JSONArray aux =(JSONArray) jData.get("objects");
			//We will always get One or none! Filter applied is the resource PK
			if(jData.length() > 0){
				
				JSONObject k = jData;
				
				Log.i("SIGHT","JSON: "+k.toString());
				
				String name= k.getString("name");
				String address = k.getString("address");
				if(!sightsDB.hasSight(name, address)){
					aux = sightsDB.createNewSight(name, address, k.getString("description"), k.getDouble("latitude"), k.getDouble("longitude"), k.getString("photo").substring(1));			
					Log.i("CREATE SIGHT", "Creating Sight"+aux.toString());
				}
				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		return aux;
	}	
	
	
	public static List<JSONObject> downloadSights(String parcitipantUsername,String password){
		List<JSONObject> list = new LinkedList<JSONObject>();
		
		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_SIGHT + "?"+
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
		
		Log.i("SIGHTS", "Number requests: "+list.size());
		return list;
	}

	
	public static void createSights(JSONArray data, SightsDataSource sightDB,Context context) {
		Sight aux;
		Log.i("CREATE SIGHTS", "Start adding");
		sightDB.open();
		Log.i("CREATE SIGHTS", ""+data.length());
		for(int i = 0 ; i < data.length() ; i++){
			try {
				JSONObject k = data.getJSONObject(i);
				String name= k.getString("name");
				String address = k.getString("address");
				if(!sightDB.hasSight(name, address)){
					aux = sightDB.createNewSight(name, address, k.getString("description"), k.getDouble("latitude"), k.getDouble("longitude"), k.getString("photo").substring(1));
					UrlImageViewHelper.setUrlDrawable(context, ValuesHelper.SERVER_API_ADDRESS_MEDIA +aux.getPhoto(),null, UrlImageViewHelper.CACHE_DURATION_INFINITE);

					Log.i("CREATE SIGHT", "Creating Sight"+aux.toString());
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		sightDB.close();
	}
	
	public static void downloadAllSights(Context context,String parcitipantUsername,String password,
			SightsDataSource sightsDB){
		List<JSONObject> list = getAllSights(parcitipantUsername, password, sightsDB);
		Iterator<JSONObject> it = list.iterator();
		try {
			sightsDB.open();
			while(it.hasNext()){
				JSONObject obj = it.next();
				JSONArray data;

				data = obj.getJSONArray("objects");

				createSights(data,sightsDB,context);

			}	
			sightsDB.close();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	
	private static List<JSONObject> getAllSights(String parcitipantUsername,String password,
			SightsDataSource sightsDB){
		List<JSONObject> list = new LinkedList<JSONObject>();

		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_SIGHT  + "?"+
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

		Log.i("SIGHT", "Number requests: "+list.size());
		return list;

	}


}
