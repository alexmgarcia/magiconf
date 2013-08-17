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

import tables.Conference;
import utils.RestClient;
import utils.ValuesHelper;
import android.content.Context;
import android.util.Log;

public class ConferenceData {

	public static String RESOURCE_CONF = "/conference/";

	public static Conference downloadConferenceData(Context c,String parcitipantUsername,String password,ConferenceDataSource confDB){

		//TODO: http://10.22.145.102:8000/api/v1/article/?format=json&user_name=joseandrade&password=662eaa47199461d01a623884080934ab&title=ACode:%20Web-based%20System%20for%20Automatic%20Evaluation%20of%20Java%20Code
		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_CONF + "?"+
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

				SimpleDateFormat dateFormat = new SimpleDateFormat(ValuesHelper.DATE_FORMAT_SERVER, Locale.US);
				Date beginDate = dateFormat.parse(k.getString("beginning_date")); 
				Date endDate = dateFormat.parse(k.getString("ending_date")); 
				Conference conf = confDB.getConference();
				if(conf == null){
					
					conf = confDB.createConference(k.getString("name"),beginDate,
							endDate, k.getString("description"),k.getInt("edition"), 
							k.getString("place"), k.getDouble("latitude"),k.getDouble("longitude"), 
							k.getString("website"),k.getString("photo").substring(1));
					UrlImageViewHelper.setUrlDrawable(c, ValuesHelper.SERVER_API_ADDRESS_MEDIA +conf.getLogo(),null, UrlImageViewHelper.CACHE_DURATION_INFINITE);

				}
				confDB.close();
				return conf;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}



	public static List<JSONObject> downloadConference(String parcitipantUsername,String password){
		List<JSONObject> list = new LinkedList<JSONObject>();

		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_CONF + "?"+
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

		Log.i("CONFERENCES", "Number requests: "+list.size());
		return list;
	}


	public static void createConference(JSONArray data, ConferenceDataSource conferenceDB) {
		Conference newConf;
		Log.i("CREATE CONFERENCES", "Start adding");
		conferenceDB.open();
		Log.i("CREATE CONFERENCES", ""+data.length());

		try {
			JSONObject k = data.getJSONObject(0);

			SimpleDateFormat dateFormat = new SimpleDateFormat(ValuesHelper.DATE_FORMAT_SERVER, Locale.US);
			Date beginDate = dateFormat.parse(k.getString("beginning_date")); 
			Date endDate = dateFormat.parse(k.getString("ending_date")); 
			newConf = conferenceDB.createConference(k.getString("name"),beginDate,
					endDate, k.getString("description"),k.getInt("edition"), 
					k.getString("place"), k.getDouble("latitude"),k.getDouble("longitude"), 
					k.getString("website"),k.getString("photo").substring(1));
			Log.i("CREATE CONFERENCES", "Creating Conference"+newConf.toString());

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		conferenceDB.close();
	}



}
