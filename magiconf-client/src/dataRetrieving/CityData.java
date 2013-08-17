package dataRetrieving;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import dataSources.ConferenceDataSource;

import tables.City;
import utils.RestClient;
import utils.ValuesHelper;
import android.content.Context;
import android.util.Log;

public class CityData {

	public static String RESOURCE_CITY = "/city/";

	public static City downloadCityData(Context context,String parcitipantUsername,String password,ConferenceDataSource confDB){

		//TODO: http://10.22.145.102:8000/api/v1/article/?format=json&user_name=joseandrade&password=662eaa47199461d01a623884080934ab&title=ACode:%20Web-based%20System%20for%20Automatic%20Evaluation%20of%20Java%20Code
		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_CITY + "?"+
				ValuesHelper.FormatJsonParam + "&"+
				ValuesHelper.UsernameParam + parcitipantUsername+ "&"+
				ValuesHelper.PasswordParam + password;

		Log.i("CONFDATA",request);


		JSONObject jData = RestClient.makeRequest(request,false,0);
		try {
			JSONArray aux =(JSONArray) jData.get("objects");
			//We will always get One or none! Filter applied is the resource PK
			
			if(aux.length() > 0){
				JSONObject k = aux.getJSONObject(0);
				confDB.open();
				City city = confDB.getCity();
				if(city == null){
					city = confDB.createCity(k.getString("name"),k.getString("description"), k.getString("photo").substring(1));
					UrlImageViewHelper.setUrlDrawable(context, ValuesHelper.SERVER_API_ADDRESS_MEDIA +city.getPhoto(),null, UrlImageViewHelper.CACHE_DURATION_INFINITE);
				}
				
				confDB.close();
				
				return city;
				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			return null;
			
	}

	
	
	public static List<JSONObject> downloadCity(String parcitipantUsername,String password){
		List<JSONObject> list = new LinkedList<JSONObject>();
		
		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_CITY + "?"+
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
		
		Log.i("CITIES", "Number requests: "+list.size());
		return list;
	}

	
	public static void createCity(JSONArray data, ConferenceDataSource conferenceDB) {
		City city;
		Log.i("CREATE CITIES", "Start adding");
		conferenceDB.open();
		Log.i("CREATE CITIES", ""+data.length());

		try {
			JSONObject k = data.getJSONObject(0);
			city = conferenceDB.getCity();
			if(city == null)
				city = conferenceDB.createCity(k.getString("name"),k.getString("description"), k.getString("photo").substring(1)); //TODO: Substring??
			Log.i("CREATE CONFERENCES", "Creating Conference"+city.toString());
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		conferenceDB.close();
	}



}
