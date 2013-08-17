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

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import tables.Article;
import tables.Author;
import tables.Contact;
import tables.Keynote;
import tables.KeynoteSession;
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
import dataSources.TalkDataSource;
import dataSources.WorkshopDataSource;

public class KeynoteSessionData {

	public static String RESOURCE_KEYNOTE_SESSION = "/keynotesession/";

	public static KeynoteSession downloadKeynoteSessionData(Long id,String parcitipantUsername,String password,
			KeynoteSessionDataSource keynotesessionDB,Context context,boolean update){

		//TODO: http://10.130.124.251:8000/api/v1/workshop/26/?format=json&user_name=joseandrade&password=662eaa47199461d01a623884080934ab
		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_KEYNOTE_SESSION  + id + "/" + "?"+
				ValuesHelper.FormatJsonParam + "&"+
				ValuesHelper.UsernameParam + parcitipantUsername+ "&"+
				ValuesHelper.PasswordParam + password;

		Log.i("KEYNOTE_SESSIONDATA",request);


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

				keynotesessionDB.open();

				KeynoteSession ks = keynotesessionDB.getKeynoteSessionEvent(id);
				if(ks == null){
					List<Keynote> keynoteSpeakers = buildKeynoteList(object.getJSONArray("keynotes"),context);
					ks = keynotesessionDB.createKeynoteSession(Long.parseLong(object.getString("id")),object.getString("title"), d,
							Integer.parseInt(object.getString("duration")),object.getString("place"),object.getString("description"), keynoteSpeakers);
				}
				else if(update){
					List<Keynote> keynoteSpeakers = buildKeynoteList(object.getJSONArray("keynotes"),context);
					keynotesessionDB.update(ks.getId(),Long.parseLong(object.getString("id")),object.getString("title"), d,
							Integer.parseInt(object.getString("duration")),object.getString("place"),object.getString("description"), new java.util.Date(), keynoteSpeakers);
				}
				keynotesessionDB.close();
				return ks;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * {
  "conference": {
    "beginning_date": "2012-09-05", 
    "city": "/api/v1/city/Almada/", 
    "description": "A Informática em Portugal é uma área académica e científica consolidada, com cursos de licenciatura e pós-graduação em praticamente todas as instituições de ensino superior do país e com um número considerável de unidades de investigação reconhecidas pela FCT. É igualmente uma área na qual várias empresas nacionais apresentam resultados de I&D de grande relevância internacional.\r\n\r\nO INForum tem como objectivo ser um evento privilegiado de reunião da comunidade nacional nas diversas vertentes da informática e ambiciona ser o fórum de eleição para a divulgação, discussão e reconhecimento de trabalhos científicos. O INForum surge com particular oportunidade como palco para a estreia de jovens investigadores que buscam a divulgação, a crítica construtiva e o encorajamento ao seu trabalho.\r\n\r\nO INForum pretende ser abrangente e dinâmico no conjunto de tópicos abordados. Apresentará um conjunto de tópicos propostos e seleccionados anualmente, com sessões organizadas e realizadas de forma independente. Promove-se assim a massa crítica para o fortalecimento e evolução da informática no país criando-se, simultaneamente, espaço �  evolução do contexto da conferência e fomentando a reunião de investigadores em áreas emergentes.", 
    "edition": 4, 
    "ending_date": "2012-09-08", 
    "id": 1, 
    "latitude": "38.660951", 
    "longitude": "-9.204397", 
    "name": "INForum 2012", 
    "photo": "/photos/conference_logos/inforum_logo_2.jpg", 
    "place": "Faculdade de Ciências e Tecnologia\r\n2829-516 Caparica\r\nPortugal", 
    "website": "http://inforum.org.pt/INForum2012"
  }, 
  "description": "A edição deste ano do INFORUM associa-se �  Celebração do Centenário da vida e obra de Alan Turing e vai contar com uma sessão especial com os seguintes oradores convidados:\r\n\r\nAlan Turing and Bletchley Park, José Manuel Valença, Universidade do Minho\r\nTuring: From Virtual to Real Machines and Back, Luís Caires, Universidade Nova de Lisboa", 
  "duration": 75, 
  "id": 2, 
  "keynotes": [
    {
      "contact": null, 
      "country": "Portugal", 
      "email": "j.valenca@uminho.pt", 
      "name": "José Manuel Valença", 
      "participant": null, 
      "photo": "http://www.uminho.pt/Newsletters/HTMLExt/29/website/DSC00471-BN.jpg", 
      "work_place": "UMinho"
    }, 
    {
      "contact": null, 
      "country": "Portugal", 
      "email": "luis.caires@fct.unl.pt", 
      "name": "Luís Caires", 
      "participant": null, 
      "photo": "http://citi.di.fct.unl.pt/member/members_photos/lc.jpg", 
      "work_place": "FCTUNL"
    }
  ], 
  "place": "Sala 201 Edifício 4", 
  "time": "2012-09-06T09:45:00", 
  "title": "Abertura+Sessão Turing"
}
	 */

	/**
	 * 
	 * @param data
	 * @return
	 */
	public static List<Keynote> buildKeynoteList(JSONArray data,Context context) {
		Keynote aux;
		Log.i("CREATE Keynotes", "Start adding");
		List<Keynote> list = new LinkedList<Keynote>();
		Log.i("CREATE KEYNOTES", ""+data.length());
		for(int i = 0 ; i < data.length() ; i++){
			try {
				JSONObject k = data.getJSONObject(i);
				aux = new Keynote();
				String contact = k.getString("contact");
				if(!contact.equals("null")){
					Contact c = new Contact();
					c.setId(Integer.parseInt(k.getJSONObject("contact").getString("id")));
					aux.setContact(c);
					Log.i("CREATE KEYNOTES", "Contact is null");
				}
				aux.setCountry(k.getString("country"));
				aux.setEmail(k.getString("email"));
				aux.setName(k.getString("name"));
				aux.setPhoto(k.getString("photo").substring(1));
				UrlImageViewHelper.setUrlDrawable(context, ValuesHelper.SERVER_API_ADDRESS_MEDIA +aux.getPhoto(),null, UrlImageViewHelper.CACHE_DURATION_INFINITE);

				aux.setWork_place(k.getString("work_place"));
				list.add(aux);
				Log.i("CREATE Keynotes", "Created Keynote"+aux.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return list;
	}




	public static void downloadAllKeynoteSessions(Context c,String parcitipantUsername,String password,
			KeynoteSessionDataSource keynotesessionDB){
		List<JSONObject> list = getAllKeynoteSessions(parcitipantUsername, password, keynotesessionDB);
		Iterator<JSONObject> it = list.iterator();
		try {
			keynotesessionDB.open();
			while(it.hasNext()){
				JSONObject obj = it.next();
				JSONArray data;

				data = obj.getJSONArray("objects");

				createKeynoteSessions(data,keynotesessionDB,c);

			}
			keynotesessionDB.close();
		} catch (JSONException e) {
			e.printStackTrace();
		}


	}

	private static void createKeynoteSessions(JSONArray data, KeynoteSessionDataSource keynotesessionDB,Context c) {
		Log.i("CREATE KEYNOTE_SESSION", "Start adding");
		Long id;
		Log.i("CREATE KEYNOTE_SESSION", ""+data.length());
		for(int i = 0 ; i < data.length() ; i++){
			try {
				JSONObject k = data.getJSONObject(i);
				Log.i("KEYNOTE_SESSION ","JSON: "+k.toString());
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				Date d = null;
				try {
					d = new java.sql.Date(format.parse(k.getString("time")).getTime());
				} catch (ParseException e) {

					e.printStackTrace();
					return;
				}

				id = Long.parseLong(k.getString("id"));
				KeynoteSession ks = keynotesessionDB.getKeynoteSessionEvent(id);
				if(ks == null){
					List<Keynote> keynoteSpeakers = buildKeynoteList(k.getJSONArray("keynotes"),c);
					ks = keynotesessionDB.createKeynoteSession(Long.parseLong(k.getString("id")),k.getString("title"), d,
							Integer.parseInt(k.getString("duration")),k.getString("place"),k.getString("description"), keynoteSpeakers);
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}	
	}


	private static List<JSONObject> getAllKeynoteSessions(String parcitipantUsername,String password,
			KeynoteSessionDataSource keynotesessionDB){
		List<JSONObject> list = new LinkedList<JSONObject>();

		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_KEYNOTE_SESSION + "?"+
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

		Log.i("KEYNOTE_SESSION", "Number requests: "+list.size());
		return list;



	}

}
