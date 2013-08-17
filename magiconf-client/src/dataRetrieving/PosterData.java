package dataRetrieving;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import tables.Author;
import tables.Contact;
import tables.Poster;
import utils.RestClient;
import utils.ValuesHelper;
import android.content.Context;
import android.util.Log;
import dataSources.AuthorDataSource;
import dataSources.PosterDataSource;
import dataSources.PosterSessionDataSource;
import dataSources.TalkDataSource;

public class PosterData {
	public static String RESOURCE_POSTER = "/poster/";
	public static String PARAM_TITLE="title=";


	public static Poster downloadPosterData(String title,String parcitipantUsername,String password,
			PosterDataSource posterDB){

		//TODO: http://10.22.145.102:8000/api/v1/poster/?format=json&user_name=joseandrade&password=662eaa47199461d01a623884080934ab&title=ACode:%20Web-based%20System%20for%20Automatic%20Evaluation%20of%20Java%20Code
		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_POSTER + "?"+
				ValuesHelper.FormatJsonParam + "&"+
				ValuesHelper.UsernameParam + parcitipantUsername+ 
				ValuesHelper.PasswordParam + password + "&"+
				PARAM_TITLE + title;

		Log.i("POSTERDATA",request);


		JSONObject jData = RestClient.makeRequest(request,false,0);
		try {
			JSONArray aux =(JSONArray) jData.get("objects");
			//We will always get One or none! Filter applied is the resource PK
			if(aux.length() > 0){
				JSONObject object = aux.getJSONObject(0);
				Poster poster = createNewPoster(object,posterDB);
				return poster;
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
      "abstract": "Most current approaches for automatic evaluation of source code use input/output testing to validate student-submitted solutions. However, very few use software engineering metrics to analyze source code. With the limitations of related work in mind, we present, in this paper, our 4-stage approach for auto-matic evaluation of source code: i) the code is first compiled and checked for any errors; ii) the compiled code is then tested against a set of JUnit tests pro-vided by the teacher; iii) a set of software engineering metrics is used to com-pare the student's solution against the teacher's solution; iv) and finally, based on the previous stages, feedback is provided to the students so they can self-evaluate and identify the areas in which they need further study.", 
      "authors": [
        {
          "contact": null, 
          "country": "Portugal", 
          "email": "alopes@iscte.pt", 
          "name": "António Lopes", 
          "participant": null, 
          "photo": "/photos/authors/antonio_luis_lopes.jpg", 
          "work_place": "ISCTE-IUL"
        }, 
        {
          "contact": null, 
          "country": "Portugal", 
          "email": "marcospinto@iscte.pt", 
          "name": "Marcos Pinto", 
          "participant": null, 
          "photo": "/photos/authors/marcos_andre_pinto.jpg", 
          "work_place": "ISCTE-IUL"
        }
      ], 
      "title": "ACode: Web-based System for Automatic Evaluation of Java Code"
    }
  ]
	 */

	/**
	 * 
	 * @param object
	 * @param posterDB
	 * @return
	 */
	private static Poster createNewPoster(JSONObject object ,
			PosterDataSource posterDB) {
		posterDB.open();
		Log.i("NEWPOSTER","Creating entry");
		Poster newPoster = null;
		try {
			JSONArray authors = object.getJSONArray("authors");
			List<Author> listAuthors= buildAuthorsList(authors);

			Log.i("NEWPOSTER","Title: "+object.getString("title"));

			newPoster = posterDB.createPoster(object.getString("title"), listAuthors);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		posterDB.close();
		return newPoster;
	}

	public static List<Author> buildAuthorsList(JSONArray data) {
		Author aux;
		Log.i("CREATE AUTHORS", "Start adding");
		List<Author> list = new LinkedList<Author>();
		Log.i("CREATE AUTHORS", ""+data.length());
		for(int i = 0 ; i < data.length() ; i++){
			try {
				JSONObject k = data.getJSONObject(i);
				String contact = k.getString("contact");
				aux = new Author();

				if(!contact.equals("null")){
					Contact c = new Contact();
					c.setId(Integer.parseInt(contact));
					aux.setContact(c);
					Log.i("CREATE AUTHORS", "Contact is null");
				}

				aux.setCountry( k.getString("country"));
				aux.setEmail(k.getString("email"));
				aux.setName(k.getString("name"));
				aux.setPhoto(k.getString("photo").substring(1));
				aux.setWork_place(k.getString("work_place"));

				list.add(aux);
				Log.i("CREATE AUTHORS", "Created Author"+aux.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
				String contact = k.getString("contact");
				if(contact != null)
					Log.i("CREATE AUTHORS", "Contact is null");

				aux = authorDB.createAuthor(k.getString("email"),k.getString("name"), k.getString("work_place"), k.getString("country"), k.getString("photo"),k.getString("contact"),false);
				Log.i("CREATE AUTHORS", "Created Author"+aux.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		authorDB.close();
	}



}


