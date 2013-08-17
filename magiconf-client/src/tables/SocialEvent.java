package tables;

import java.sql.Date;

public class SocialEvent extends Event {
	
	private long id;
	private String title;
	private Date time;
	private long DjangoId;
	
	
	public long getId(){
		return id;
	}
	
	public String getTitle(){
		return title;
	}
	
	public Date getTime(){
		return time;
	}
	
	public void setId(long id){
		this.id = id;
	}
	
	public void setTitle(String title){
		this.title=title;
	}
	
	public void setTime(Date time){
		this.time=time;
	}
}
