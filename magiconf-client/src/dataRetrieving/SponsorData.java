package dataRetrieving;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.internal.co;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;


import tables.Sponsor;
import utils.RestClient;
import utils.ValuesHelper;
import android.content.Context;
import android.util.Log;
import dataSources.SponsorDataSource;

public class SponsorData {

	public static String RESOURCE_SPONSOR = "/sponsor/";
	public static String PARAM_NAME="name=";

	public static Sponsor downloadSponsorData(String name,String participantUsername,String password,SponsorDataSource sponsorDB){

		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_SPONSOR + "?"+
				ValuesHelper.FormatJsonParam + "&"+
				ValuesHelper.UsernameParam + participantUsername+ "&"+
				ValuesHelper.PasswordParam + password+"&"+
				PARAM_NAME + name;

		Log.i("SPONSORDATA",request);


		JSONObject jData = RestClient.makeRequest(request,false,0);

		try {
			JSONArray aux =(JSONArray) jData.get("objects");
			//We will always get One or none! Filter applied is the resource PK

			if(aux.length() > 0){

				JSONObject k = aux.getJSONObject(0);
				sponsorDB.open();
				Sponsor s =  sponsorDB.createSponsor(name, k.getString("logo").substring(1), k.getString("website"));
				sponsorDB.close();
				return s;

			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	public static void downloadAllSponsors(Context context,String participantUsername,String password,SponsorDataSource sponsorDB) {
		List<JSONObject> list = downloadSponsors(participantUsername,password);
		try {
			Iterator<JSONObject> it = list.iterator();
			JSONObject aux = null;
			while(it.hasNext()){
				aux =  it.next();
				JSONArray data = aux.getJSONArray("objects");

				createNewSponsor(data,sponsorDB,context);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	public static List<JSONObject> downloadSponsors(String parcitipantUsername,String password){
		List<JSONObject> list = new LinkedList<JSONObject>();

		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_SPONSOR + "?"+
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

		Log.i("Sponsors", "Number requests: "+list.size());
		return list;
	}


	public static void createNewSponsor(JSONArray data, SponsorDataSource sponsorDB,Context context) {

		Sponsor aux;
		Log.i("CREATE SPONSOR", "Start adding");
		sponsorDB.open();
		Log.i("CREATE SPONSOR", ""+data.length());
		for(int i = 0 ; i < data.length() ; i++){
			try {
				JSONObject k = data.getJSONObject(i);
				String name= k.getString("name");
				if(!sponsorDB.hasSponsor(name)){
					aux = sponsorDB.createSponsor(name, k.getString("logo").substring(1), k.getString("website"));
					UrlImageViewHelper.setUrlDrawable(context, ValuesHelper.SERVER_API_ADDRESS_MEDIA +aux.getLogo(),null, UrlImageViewHelper.CACHE_DURATION_INFINITE);
					Log.i("CREATE SPONSOR", "Creating Sponsor"+aux.toString());
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		sponsorDB.close();
	}

}



