package tables;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Workshop extends Event{

	private long eventId;
	private long djangoId;
	private String description;

	public void setEventId(long eventId) {
		this.eventId = eventId;
	}

	public long getEventId() {
		return eventId;
	}


	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDjangoId(long djangoId) {
		this.djangoId = djangoId;
	}

	public long getDjangoId() {
		return this.djangoId;
	}

}
