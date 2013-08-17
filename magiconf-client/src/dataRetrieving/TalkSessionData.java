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
import tables.Talk;
import tables.Workshop;
import utils.RestClient;
import utils.ValuesHelper;
import android.content.Context;
import android.util.Log;
import dataSources.ArticleDataSource;
import dataSources.AuthorDataSource;
import dataSources.TalkDataSource;
import dataSources.WorkshopDataSource;

public class TalkSessionData {

	public static String RESOURCE_TALK_SESSION = "/talksession/";

	public static Talk downloadTalkSessionData(Long id,String parcitipantUsername,String password,
			TalkDataSource talksessionDB,Context context,boolean update){
		
		//TODO: http://10.130.124.251:8000/api/v1/workshop/26/?format=json&user_name=joseandrade&password=662eaa47199461d01a623884080934ab
		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_TALK_SESSION  + id + "/" + "?"+
				ValuesHelper.FormatJsonParam + "&"+
				ValuesHelper.UsernameParam + parcitipantUsername+ "&"+
				ValuesHelper.PasswordParam + password;

		Log.i("TALK_SESSIONDATA",request);


		JSONObject jData = RestClient.makeRequest(request,false,0);
		try {
			//We will always get One or none! Filter applied is the resource PK
			if(jData.length() > 0){

				JSONObject object = jData;
				Log.i("TALK","JSON: "+object.toString());

				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				Date d = null;
				try {
					d = new java.sql.Date(format.parse(object.getString("time")).getTime());
				} catch (ParseException e) {

					e.printStackTrace();
					return null;
				}
				
				talksessionDB.open();
				Talk talk = talksessionDB.getTalkEvent(id);
				if(talk == null){
				List<Article> articles = buildArticleList(object.getJSONArray("articles"),context);
				talk = talksessionDB.createTalk(Long.parseLong(object.getString("id")),object.getString("title"), d,
						Integer.parseInt(object.getString("duration")),object.getString("place"), articles);
				talksessionDB.close();
				}
				else if(update){
					
					//We should only update if the lastmodified_object_received > lastmodified_object_in_db
					List<Article> articles = buildArticleList(object.getJSONArray("articles"),context);
					talksessionDB.update(talk.getId(),Long.parseLong(object.getString("id")),object.getString("title"), d,
							Integer.parseInt(object.getString("duration")),object.getString("place"),  new java.util.Date(), articles);
				}
				return talk;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	/**
	 * "articles": [
    {
      "abstract": "Uma das estratégias para tirar partido dos múltiplos processadores\r\ndisponíveis nos computadores atuais passa por adaptar código\r\nlegado, inicialmente concebido para ser executado num contexto\r\nmeramente sequencial, para ser agora executado num contexto multithreading.\r\nNesse processo de adaptação é necessário proteger apropriadamente\r\nos dados que são agora partilhados e acedidos por diferentes\r\nthreads concorrentes. A proteção dos dados com locks usando uma granulosidade\r\ngrossa inibe a concorrência e opõe-se ao objetivo inicial de\r\nexplorar o paralelismo suportado por múltiplos processadores. Por outro\r\nlado, a utilização de uma granulosidade fina pode levar �  ocorrência de\r\nanomalias próprias da concorrência, como deadlocks e violações de atomicidade\r\n(high-level data races). Este artigo discute o conceito de fecho\r\nde um programa e uma metodologia que, quando aplicados em conjunto,\r\npermitem adaptar código legado para o tornar thread-safe, garantindo a\r\nausência de violações de atomicidade na versão corrente do software e\r\nantecipando algumas violações de atomicidade que poderão ocorrer em\r\nversões futuras do mesmo software.", 
      "authors": [
        {
          "contact": null, 
          "country": "Portugal", 
          "email": "dg.sousa@fct.unl.pt", 
          "name": "Diogo G. Sousa", 
          "participant": null, 
          "photo": "/photos/authors/images_4.jpg", 
          "work_place": "FCTUNL"
        }, 
        {
          "contact": null, 
          "country": "Israel", 
          "email": "farchi@il.ibm.com", 
          "name": "Eitan Farchi", 
          "participant": null, 
          "photo": "/photos/authors/eitan_farchi.jpg", 
          "work_place": "IBM"
        }, 
        {
          "contact": null, 
          "country": "Israel", 
          "email": "itais@il.ibm.com", 
          "name": "Itai Segall", 
          "participant": null, 
          "photo": "/photos/authors/itai_segall.jpg", 
          "work_place": "IBM"
        }, 
        {
          "contact": null, 
          "country": "Portugal", 
          "email": "j.lourenco@fct.unl.pt", 
          "name": "João Lourenço", 
          "participant": null, 
          "photo": "/photos/authors/jml.png", 
          "work_place": "FCTUNL"
        }
      ], 
      "title": "Aplicação do Fecho de Programas na Deteção de Anomalias de Concorrência"
    }, 
    {
      "abstract": "The European Desktop Grid Initiative (EDGI) is an Euro-\r\npean project that aims to close the gap between service push-based grids,\r\nsuch as gLite, ARC and UNICORE, and desktop, pull-based, grids, such\r\nas BOINC and XtremWeb. Given the inevitably large size of the overall\r\ninfrastructure, the EDGI team needs to benchmark its components, to\r\nidentify con\fguration problems, performance bottlenecks, and to ensure\r\nthe appropriate QoS levels.\r\nIn this paper, we describe our benchmarking e\u000bort. To take our mea-\r\nsurements, we submitted batches of jobs to demonstration facilities, and\r\nto components that we disconnected from the infrastructure. We focused\r\nour measurements on the two most important metrics for any grid re-\r\nsource: latency and throughput of jobs. Additionally, by increasing job\r\nsubmission load to the limits of the EDGI components, we identi\fed sev-\r\neral bottlenecks in the job \r\now processes. The results of our work provide\r\nimportant performance guidelines for grid developers and administrators.", 
      "authors": [
        {
          "contact": null, 
          "country": "Portugal", 
          "email": "faraujo@fct.uc.pt", 
          "name": "Filipe Araújo", 
          "participant": null, 
          "photo": "/photos/authors/images_17.jpg", 
          "work_place": "FCTUC"
        }, 
        {
          "contact": null, 
          "country": "Portugal", 
          "email": "sherhiy@fct.uc.pt", 
          "name": "Serhiy Boychenko", 
          "participant": null, 
          "photo": "/photos/authors/images_47.jpg", 
          "work_place": "FCTUC"
        }
      ], 
      "title": "Benchmarking the EDGI Infrastructure"
    }, 
    {
      "abstract": "Vários sistemas surgiram recentemente com o intuito de facilitar\r\na utilização de serviços de armazenamento nas clouds. Muitos\r\nutilizadores das clouds têm a necessidade de manter os seus dados disponíveis\r\ne privados, requisitos nem sempre atendidos pelos sistemas de\r\narmazenamento em cloud. Recentemente foi demonstrado que é possível\r\natender estes requisitos através do uso de vários provedores de cloud, ao\r\ninvés de um só, ao que foi dado o nome de cloud-of clouds. Com vista\r\na responder a estas necessidades apresentamos o C2FS, um sistema de\r\nficheiros multi-utilizador para cloud-of-clouds. Este sistema é tolerante a\r\nfalhas por parte dos provedores de clouds e mantém a privacidade dos\r\ndados e metadados armazenados desde que menos de um terço dos provedores\r\nusados sejam faltosos. O C2FS tem uma interface estilo POSIX\r\ne satisfaz um modelo de consistência flexível que permite aos seus utilizadores\r\ncontrolarem os custos (em termos monetários e de desempenho)\r\nrelacionados com o acesso a clouds e as garantias de consistência e durabilidade\r\noferecidas pelo sistema.", 
      "authors": [
        {
          "contact": null, 
          "country": "Brasil", 
          "email": "abessani@fc.ul.pt", 
          "name": "Alysson Bessani", 
          "participant": null, 
          "photo": "/photos/authors/alysson_bessani.jpg", 
          "work_place": "FCUL"
        }, 
        {
          "contact": null, 
          "country": "Brasil", 
          "email": "mpasin@fc.ul.pt", 
          "name": "Marcelo Pasin", 
          "participant": null, 
          "photo": "/photos/authors/images_46.jpg", 
          "work_place": "FCUL"
        }, 
        {
          "contact": null, 
          "country": "Portugal", 
          "email": "rmendes@fc.ul.pt", 
          "name": "Ricardo Mendes", 
          "participant": null, 
          "photo": "/photos/authors/images_43.jpg", 
          "work_place": "FCUL"
        }, 
        {
          "contact": null, 
          "country": "Portugal", 
          "email": "toliveira@fc.ul.pt", 
          "name": "Tiago Oliveira", 
          "participant": null, 
          "photo": "/photos/authors/images_44.jpg", 
          "work_place": "FCUL"
        }
      ], 
      "title": "C2FS: um Sistema de Ficheiros Seguro e Fiável para Cloud-of-clouds"
    }, 
    {
      "abstract": "In this work, we propose RATS, a middleware to enhance\r\nand extend the Terracotta framework for Java with the ability to transparently\r\nexecute multi-threaded Java applications to provide a singlesystem\r\nimage. It supports e\u000ecient scheduling of threads, according to\r\navailable resources, across several nodes in a Terracotta cluster, taking\r\nadvantage of the extra computational and memory resources available.\r\nIt also supports pro\fling to gather application characteristics such as\r\ndispersion of thread workload, thread inter-arrival time and resource usage\r\nof the application. It uses bytecode instrumentations to pro\fle and\r\nadd clustering capabilities to multi-threaded Java applications, as well\r\nas extra synchronization if needed. We developed a range of alternative\r\nscheduling heuristics and classify them based on the application and cluster\r\nbehavior. The middleware is tested with a cpu-intensive application\r\nwith varying thread characteristics to assess and classify the scheduling\r\nheuristics with respect to application speed-ups and load balancing.", 
      "authors": [
        {
          "contact": null, 
          "country": "Portugal", 
          "email": "luisveiga@ist.utl.pt", 
          "name": "Luís Veiga", 
          "participant": null, 
          "photo": "/photos/authors/luis_veiga.jpg", 
          "work_place": "IST"
        }, 
        {
          "contact": null, 
          "country": "Espanha", 
          "email": "navaneeth@ist.utl.pt", 
          "name": "Navaneeth Rameshan", 
          "participant": null, 
          "photo": "/photos/authors/navaneeth-rameshan.jpg", 
          "work_place": "IST"
        }
      ], 
      "title": "RATS - Resource Aware Thread Scheduling for JVM-level Clustering"
    }
  ], 
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
  "duration": 120, 
  "id": 3, 
  "place": "Sala 201 Edifício 4", 
  "time": "2012-09-06T11:00:00", 
  "title": "Computação Paralela, Distribuída e de Larga Escala 1"
	 */

	public static List<Article> buildArticleList(JSONArray data,Context c) {
		Article aux;
		Log.i("CREATE Articles", "Start adding");
		List<Article> list = new LinkedList<Article>();
		Log.i("CREATEArticles", ""+data.length());
		for(int i = 0 ; i < data.length() ; i++){
			try {
				JSONObject k = data.getJSONObject(i);
				aux = new Article();
				List<Author> authors = ArticleData.buildAuthorsList(k.getJSONArray("authors"),c);

				aux.setAbstract( k.getString("abstract"));
				aux.setAuthors(authors);
				aux.setTitle(k.getString("title"));

				list.add(aux);
				Log.i("CREATE Articles", "Created Author"+aux.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return list;
	}



	public static void downloadAllTalkSessions(Context c,String parcitipantUsername,String password,
			TalkDataSource talksessionDB){
		List<JSONObject> list = getAllTalkSessions(parcitipantUsername, password, talksessionDB);
		Iterator<JSONObject> it = list.iterator();
		try {
			talksessionDB.open();
			while(it.hasNext()){
				JSONObject obj = it.next();
				JSONArray data;

				data = obj.getJSONArray("objects");

				createTalkSessions(data,talksessionDB,c);

			}	
			talksessionDB.close();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	private static void createTalkSessions(JSONArray data, TalkDataSource talksessionDB,Context c) {
		Log.i("CREATE TALK_SESSION", "Start adding");
		Long id;
		Log.i("CREATE TALK_SESSION", ""+data.length());
		for(int i = 0 ; i < data.length() ; i++){
			try {
				JSONObject k = data.getJSONObject(i);
				Log.i("TALK_SESSION ","JSON: "+k.toString());
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				Date d = null;
				try {
					d = new java.sql.Date(format.parse(k.getString("time")).getTime());
				} catch (ParseException e) {

					e.printStackTrace();
					return;
				}

				id = Long.parseLong(k.getString("id"));
				Talk talk = talksessionDB.getTalkEvent(id);
				if(talk == null){
					List<Article> articles = buildArticleList(k.getJSONArray("articles"),c);
					Talk t = talksessionDB.createTalk(Long.parseLong(k.getString("id")),k.getString("title"), d,
							Integer.parseInt(k.getString("duration")),k.getString("place"), articles);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}	
	}


	private static List<JSONObject> getAllTalkSessions(String parcitipantUsername,String password,
			TalkDataSource talksessionDB){
		List<JSONObject> list = new LinkedList<JSONObject>();

		String request= ValuesHelper.SERVER_API_ADDRESS+
				RESOURCE_TALK_SESSION + "?"+
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

		Log.i("TALK_SESSION", "Number requests: "+list.size());
		return list;



	}

}
