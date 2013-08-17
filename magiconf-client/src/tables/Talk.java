package tables;

import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Talk extends Event{

	private long eventId;
	private long djangoId;
	private ArrayList<Article> articles;
	
	public ArrayList<Article> getArticles() {
		return articles;
	}

	public void setArticles(ArrayList<Article> articles) {
		this.articles = articles;
	}
	
	public void setEventId(long eventId) {
		this.eventId = eventId;
	}
	
	public long getEventId() {
		return eventId;
	}
	
	public void setDjangoId(long djangoId) {
		this.djangoId = djangoId;
	}

	public long getDjangoId() {
		return this.djangoId;
	}

	
}
