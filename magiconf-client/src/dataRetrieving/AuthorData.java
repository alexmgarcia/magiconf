package dataRetrieving;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tables.Article;
import tables.Author;
import utils.RestClient;
import utils.ValuesHelper;
import android.util.Log;
import dataSources.ArticleDataSource;
import dataSources.AuthorDataSource;

public class AuthorData {

	public static String RESOURCE_AUTHOR = "/author/";
	public static String PARAM_EMAIL = "email=";

	public static Author downloadAuthorData(String email,String parcitipantUsername,String password,
			AuthorDataSource authorDB){

		//TODO: http://10.22.145.102:8000/api/v1/article/?format=json&user_name=joseandrade&password=662eaa47199461d01a623884080934ab&title=ACode:%20Web-based%20System%20for%20Automatic%20Evaluation%20of%20Java%20Code
		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_AUTHOR + "?"+
				ValuesHelper.FormatJsonParam + "&"+
				ValuesHelper.UsernameParam + parcitipantUsername+ "&"+
				ValuesHelper.PasswordParam + password +"&"+
				PARAM_EMAIL + email;

		Log.i("AUTHORDATA",request);


		JSONObject jData = RestClient.makeRequest(request,false,0);
		try {
			JSONArray aux =(JSONArray) jData.get("objects");
			//We will always get One or none! Filter applied is the resource PK
			if(aux.length() > 0){
				JSONObject object = aux.getJSONObject(0);
				authorDB.open();
				Author author = authorDB.createAuthor(object.getString("email"),object.getString("name"),
						object.getString("work_place"), object.getString("country"), object.getString("photo").substring(1),
						object.getString("contact"), false);
				authorDB.close();
				return author;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	/**
	 * "objects": [
    {
      "contact": null, 
      "country": "França", 
      "email": "Jean-Christophe.Filliatre@Iri.fr", 
      "name": "Jean-Christophe Filliâtre", 
      "participant": null, 
      "photo": "/photos/authors/jean_christophe_filliatre.jpg", 
      "work_place": "CNRS"
    }
  ]
	 */
	
	
	public static List<JSONObject> downloadAuthors(String parcitipantUsername,String password){
		List<JSONObject> list = new LinkedList<JSONObject>();
		
		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_AUTHOR + "?"+
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
		
		Log.i("AUTHORS", "Number requests: "+list.size());
		return list;
	}

	
	public static void createAuthors(JSONArray data, AuthorDataSource authorDB) {
		Author aux;
		Log.i("CREATE AUTHORS", "Start adding");
		authorDB.open();
		Log.i("CREATE AUTHORS", ""+data.length());
		for(int i = 0 ; i < data.length() ; i++){
			try {
				JSONObject k = data.getJSONObject(i);
				aux = authorDB.createAuthor(k.getString("email"),k.getString("name"), k.getString("work_place"), k.getString("country"), k.getString("photo").substring(1),k.getString("contact"), false);
				Log.i("CREATE AUTHORS", "Creating Author"+aux.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		authorDB.close();
	}



}
