package dataRetrieving;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import tables.Member;
import utils.RestClient;
import utils.ValuesHelper;
import android.content.Context;
import android.util.Log;
import dataSources.MemberDataSource;
import dataSources.SponsorDataSource;

public class MemberData {

	public static String RESOURCE_MEMBER = "/organizationmember/";
	public static String PARAM_EMAIL = "email=";

	public static Member downloadMemberData(String email,String parcitipantUsername,String password,
			MemberDataSource memberDB){

		//TODO: http://10.22.145.102:8000/api/v1/article/?format=json&user_name=joseandrade&password=662eaa47199461d01a623884080934ab&title=ACode:%20Web-based%20System%20for%20Automatic%20Evaluation%20of%20Java%20Code
		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_MEMBER + "?"+
				ValuesHelper.FormatJsonParam + "&"+
				ValuesHelper.UsernameParam + parcitipantUsername+ "&"+
				ValuesHelper.PasswordParam + password +"&"+
				PARAM_EMAIL + email;

		Log.i("MEMBERDATA",request);


		JSONObject jData = RestClient.makeRequest(request,false,0);
		try {
			JSONArray aux =(JSONArray) jData.get("objects");
			//We will always get One or none! Filter applied is the resource PK
			if(aux.length() > 0){
				JSONObject object = aux.getJSONObject(0);
				memberDB.open();
				Member member = memberDB.createMember(object.getString("email"),object.getString("name"),
						object.getString("work_place"), object.getString("country"), object.getString("photo").substring(1),
						object.getString("role"));
				memberDB.close();
				return member;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}



	public static void downloadAllMembers(Context context,String participantUsername,String password,
			MemberDataSource memberDB) {
		List<JSONObject> list = downloadMembers(participantUsername,password);
		try {
			Iterator<JSONObject> it = list.iterator();
			JSONObject aux = null;
			while(it.hasNext()){
				aux =  it.next();
				JSONArray data = aux.getJSONArray("objects");

				createMembers(data,memberDB,context);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public static List<JSONObject> downloadMembers(String parcitipantUsername,String password){
		List<JSONObject> list = new LinkedList<JSONObject>();

		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_MEMBER + "?"+
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

		Log.i("MEMBERS", "Number requests: "+list.size());
		return list;
	}


	public static void createMembers(JSONArray data, MemberDataSource memberDB,Context context) {
		Member aux;
		Log.i("CREATE MEMBERS", "Start adding");
		memberDB.open();
		Log.i("CREATE MEMBERS", ""+data.length());
		for(int i = 0 ; i < data.length() ; i++){
			try {
				JSONObject k = data.getJSONObject(i);

				Member m = memberDB.getMemberByEmail(k.getString("email"));
				if(m == null){
					aux = memberDB.createMember(k.getString("email"),k.getString("name"),
							k.getString("work_place"), k.getString("country"), k.getString("photo").substring(1),
							k.getString("role"));
					UrlImageViewHelper.setUrlDrawable(context, ValuesHelper.SERVER_API_ADDRESS_MEDIA +aux.getPhoto(),null, UrlImageViewHelper.CACHE_DURATION_INFINITE);

					Log.i("CREATE MEMBERS", "Creating Member"+aux.toString());
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		memberDB.close();
	}



}
