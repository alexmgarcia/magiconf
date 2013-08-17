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

import tables.Author;
import tables.Event;
import tables.Workshop;
import utils.RestClient;
import utils.ValuesHelper;
import android.text.format.DateFormat;
import android.util.Log;
import dataSources.AuthorDataSource;
import dataSources.EventDataSource;
import dataSources.WorkshopDataSource;

public class WorkshopData {
	

	public static String RESOURCE_WORKSHOP = "/workshop/";

	public static Workshop downloadWorkshopData(Long id,String parcitipantUsername,String password,
			WorkshopDataSource workshopDB,boolean update){

		//TODO: http://10.130.124.251:8000/api/v1/workshop/26/?format=json&user_name=joseandrade&password=662eaa47199461d01a623884080934ab
		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_WORKSHOP  + id + "/" + "?"+
				ValuesHelper.FormatJsonParam + "&"+
				ValuesHelper.UsernameParam + parcitipantUsername+ "&"+
				ValuesHelper.PasswordParam + password;

		Log.i("WORKSHOPDATA",request);


		JSONObject jData = RestClient.makeRequest(request,false,0);
		try {
			//JSONArray aux =(JSONArray) jData.get("objects");
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
				workshopDB.open();
				
				id = Long.parseLong(object.getString("id"));
				Workshop ws = workshopDB.getWorkshopEvent(id);
				if(ws == null)
					ws = workshopDB.createWorkshop(Long.parseLong(object.getString("id")),object.getString("title"),d,Integer.parseInt(object.getString("duration")),object.getString("place"),object.getString("description"));
				else if(update){
					workshopDB.update(ws.getId(),Long.parseLong(object.getString("id")),object.getString("title"),d,Integer.parseInt(object.getString("duration")),object.getString("place"),object.getString("description"),new java.util.Date());
				}
				workshopDB.close();
				return ws;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	/**
	 * "conference": {
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
  "description": "In research in computing we are producing artefacts, mainly software, which are then tested and evaluated. Some of those research artefacts are even used by actual users. Few will even make it to the market. How should we build our artefacts? When should we stop developing? What is good enough? Who are our clients? From the perspective of the computing sub-discipline of Human-Computer Interaction I am looking at different stages of maturity of development projects to illustrate different maturity characteristics of systems.", 
  "duration": 120, 
  "id": 26, 
  "place": "Sala 201 Edificio 4", 
  "time": "2012-09-07T15:00:00", 
  "title": "Ideas, Inventions, Innovations - Maturity of Research Systems"
	 */
	
	
	public static void downloadAllWorkshopEvents(String parcitipantUsername,String password,
			WorkshopDataSource workshopDB){
		List<JSONObject> list = getAllWorkshopEvents(parcitipantUsername, password, workshopDB);
		Iterator<JSONObject> it = list.iterator();
		try {
			workshopDB.open();
			while(it.hasNext()){
				JSONObject obj = it.next();
				JSONArray data;

				data = obj.getJSONArray("objects");

				createWorkshopEvents(data,workshopDB);

			}	
			workshopDB.close();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	private static void createWorkshopEvents(JSONArray data, WorkshopDataSource workshopDB) {
		Log.i("CREATE WORKSHOP", "Start adding");
		Long id;
		Log.i("CREATE WORKSHOP", ""+data.length());
		for(int i = 0 ; i < data.length() ; i++){
			try {
				JSONObject k = data.getJSONObject(i);
				Log.i("WORKSHOP ","JSON: "+k.toString());
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				Date d = null;
				try {
					d = new java.sql.Date(format.parse(k.getString("time")).getTime());
				} catch (ParseException e) {

					e.printStackTrace();
					return;
				}
				id = Long.parseLong(k.getString("id"));
				Workshop ws = workshopDB.getWorkshopEvent(id);
				if(ws == null)
					workshopDB.createWorkshop(Long.parseLong(k.getString("id")),k.getString("title"),d,Integer.parseInt(k.getString("duration")),k.getString("place"),k.getString("description"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}	
	}


	private static List<JSONObject> getAllWorkshopEvents(String parcitipantUsername,String password,
			WorkshopDataSource workshopDB){
		List<JSONObject> list = new LinkedList<JSONObject>();

		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_WORKSHOP + "?"+
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

		Log.i("WORKSHOP", "Number requests: "+list.size());
		return list;



	}

	
	

}
