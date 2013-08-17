package tables;

import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)

public class PosterSession extends Event {

	private long eventId;
	private ArrayList<Poster> posters;
	private long djangoId;
	
	public ArrayList<Poster> getPosters() {
		return posters;
	}

	public void setPosters(ArrayList<Poster> posters) {
		this.posters = posters;
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
