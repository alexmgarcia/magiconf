package tables;

import java.util.ArrayList;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KeynoteSession extends Event {
	private long eventId;
	private String description;
	private ArrayList<Keynote> keynotes;
	
	public ArrayList<Keynote> getKeynoteSpeakers() {
		return keynotes;
	}

	public void setKeynotes(ArrayList<Keynote> keynoteSpeakers) {
		this.keynotes = keynoteSpeakers;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setEventId(long eventId) {
		this.eventId = eventId;
	}
	
	public long getEventId() {
		return eventId;
	}
	
}
