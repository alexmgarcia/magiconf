package tables;

import java.sql.Date;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {

	private long id;
	private long djangoId;
	
	private String title;
	private String place;
	private Date time;
	private Date lastModified;
	private int duration;
	private boolean inAgenda;
	
	public long getId(){
		return id;
	}
	
	public void setId(long id){
		this.id = id;
	}
	
	public int getDuration() {
		return duration;
	}
	
	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	public String getTitle(){
		return title;
	}
	
	public void setTitle(String title){
		this.title = title;
	}
	
	public String getPlace(){
		return place;
	}
	
	public void setPlace(String place) {
		this.place = place;
	}
	
	public Date getTime(){
		return time;
	}
	
	public void setTime(Date time){
		this.time = time;
	}
	
	public Date getLastModified() {
		return lastModified;
	}
	
	public void setLastModified(Date modified) {
		this.lastModified = modified;
	}
	
	public boolean getInAgenda(){
		return inAgenda;
	}
	
	public void setInAgenda(boolean inAgenda){
		this.inAgenda = inAgenda;
	}
	
	//To be used by ArrayAdapter in ListView controller
	public String toString(){
		return "Title: " + title + "   Place: " + place + "   Time: " + time;
	}
	
	public void setDjangoId(long djangoId) {
		this.djangoId = djangoId;
	}

	public long getDjangoId() {
		return this.djangoId;
	}

}
