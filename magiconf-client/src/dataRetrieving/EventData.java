package dataRetrieving;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tables.Author;
import tables.Event;
import tables.SocialEvent;
import tables.Workshop;
import utils.RestClient;
import utils.ValuesHelper;
import android.text.format.DateFormat;
import android.util.Log;
import dataSources.AuthorDataSource;
import dataSources.EventDataSource;
import dataSources.WorkshopDataSource;

public class EventData {
	

	public static String RESOURCE_EVENT = "/event/";

	public static SocialEvent downloadEventData(Long id,String parcitipantUsername,String password,
			EventDataSource eventDB){

		//TODO: http://10.130.124.251:8000/api/v1/event/1/?format=json&user_name=joseandrade&password=662eaa47199461d01a623884080934ab
		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_EVENT  + id + "/" + "?"+
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
				eventDB.open();
				Event event = eventDB.createEvent(id,object.getString("title"), object.getString("place"), d,Integer.parseInt(object.getString("duration")));
				eventDB.close();
				
				SocialEvent se = new SocialEvent();
				se.setDuration(event.getDuration());
				se.setId(event.getId());
				se.setInAgenda(event.getInAgenda());
				se.setLastModified(event.getLastModified());
				se.setPlace(event.getPlace());
				se.setTime(event.getTime());
				se.setTitle(event.getTitle());
				se.setDjangoId(event.getDjangoId());
				return se;
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
	
	
	

}
