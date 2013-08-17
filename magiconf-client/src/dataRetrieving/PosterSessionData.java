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

import tables.Article;
import tables.Author;
import tables.Contact;
import tables.Keynote;
import tables.KeynoteSession;
import tables.Poster;
import tables.PosterSession;
import tables.Talk;
import tables.Workshop;
import utils.RestClient;
import utils.ValuesHelper;
import android.content.Context;
import android.util.Log;
import dataSources.ArticleDataSource;
import dataSources.AuthorDataSource;
import dataSources.KeynoteDataSource;
import dataSources.KeynoteSessionDataSource;
import dataSources.PosterSessionDataSource;
import dataSources.TalkDataSource;
import dataSources.WorkshopDataSource;

public class PosterSessionData {
	
	public static String RESOURCE_POSTER_SESSION = "/postersession/";

	public static PosterSession downloadPosterSessionData(Long id,String parcitipantUsername,String password,
			PosterSessionDataSource postersessionDB,Context context,boolean update){

		//TODO: http://10.130.124.251:8000/api/v1/workshop/26/?format=json&user_name=joseandrade&password=662eaa47199461d01a623884080934ab
		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_POSTER_SESSION  + id + "/" + "?"+
				ValuesHelper.FormatJsonParam + "&"+
				ValuesHelper.UsernameParam + parcitipantUsername+ "&"+
				ValuesHelper.PasswordParam + password;

		Log.i("POSTER_SESSIONDATA",request);


		JSONObject jData = RestClient.makeRequest(request,false,0);
		try {
			//We will always get One or none! Filter applied is the resource PK
			if(jData.length() > 0){
				
				JSONObject object = jData;
				Log.i("WORKSHOP","JSON: "+object.toString());
				
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				Date d = null;
				try {
					d = new java.sql.Date(format.parse(object.getString("time")).getTime());
				} catch (ParseException e) {
					
					e.printStackTrace();
					return null;
				}
				
				postersessionDB.open();
				
				id = Long.parseLong(object.getString("id"));
				PosterSession ps = postersessionDB.getPosterSessionEvent(id);
				if(ps  == null){
					List<Poster> posters = buildPosterList(object.getJSONArray("posters"),context);
					ps = postersessionDB.createPostersSession(Long.parseLong(object.getString("id")),object.getString("title"), d,
							Integer.parseInt(object.getString("duration")),object.getString("place"), posters);
				}
				else if(update){
					List<Poster> posters = buildPosterList(object.getJSONArray("posters"),context);
					postersessionDB.updatePostersSession(ps.getId(),Long.parseLong(object.getString("id")),object.getString("title"), d,
							Integer.parseInt(object.getString("duration")),object.getString("place"), posters);
				}
				
				
				postersessionDB.close();
				return ps;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	/**
	 * 
	 * @param data
	 * @return
	 */
	public static List<Poster> buildPosterList(JSONArray data,Context context) {
		Poster aux;
		Log.i("CREATE posters", "Start adding");
		List<Poster> list = new LinkedList<Poster>();
		Log.i("CREATE POSTERS", ""+data.length());
		for(int i = 0 ; i < data.length() ; i++){
			try {
				JSONObject k = data.getJSONObject(i);
				aux = new Poster();
				List<Author> authors = ArticleData.buildAuthorsList(k.getJSONArray("authors"),context);
				
				aux.setAuthors(authors);
				aux.setTitle(k.getString("title"));
				
				list.add(aux);
				Log.i("CREATE Posters", "Created Poster" + aux.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return list;
	}
	
	public static void downloadAllPosterSessions(String parcitipantUsername,String password,
			PosterSessionDataSource postersessionDB,Context context){
		List<JSONObject> list = getAllPosterSessions(parcitipantUsername, password, postersessionDB);
		Iterator<JSONObject> it = list.iterator();
		try {
			postersessionDB.open();
			while(it.hasNext()){
				JSONObject obj = it.next();
				JSONArray data;

				data = obj.getJSONArray("objects");

				createPosterSessions(data,postersessionDB,context);

			}	
			postersessionDB.close();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	private static void createPosterSessions(JSONArray data, PosterSessionDataSource postersessionDB,Context context) {
		Log.i("CREATE POSTER_SESSION", "Start adding");
		Long id;
		Log.i("CREATE POSTER_SESSION", ""+data.length());
		for(int i = 0 ; i < data.length() ; i++){
			try {
				JSONObject k = data.getJSONObject(i);
				Log.i("POSTER_SESSION ","JSON: "+k.toString());
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				Date d = null;
				try {
					d = new java.sql.Date(format.parse(k.getString("time")).getTime());
				} catch (ParseException e) {

					e.printStackTrace();
					return;
				}

				id = Long.parseLong(k.getString("id"));
				PosterSession ps = postersessionDB.getPosterSessionEvent(id);
				if(ps  == null){
					List<Poster> posters = buildPosterList(k.getJSONArray("posters"),context);
					PosterSession ks = postersessionDB.createPostersSession(Long.parseLong(k.getString("id")),k.getString("title"), d,
							Integer.parseInt(k.getString("duration")),k.getString("place"), posters);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}	
	}


	private static List<JSONObject> getAllPosterSessions(String parcitipantUsername,String password,
			PosterSessionDataSource postersessionDB){
		List<JSONObject> list = new LinkedList<JSONObject>();

		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_POSTER_SESSION + "?"+
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

		Log.i("POSTER_SESSION", "Number requests: "+list.size());
		return list;



	}


}
