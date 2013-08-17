package tables;

import java.util.Date;

public class Conference {
	
	private long id;

	private String title;
	private Date beginningDate;
	private Date endingDate;
	private String description;
	private int edition;
	private String place;
	private double latitude;
	private double longitude;
	private String website;
	private String logo;
	
	
	public long getId(){
		return id;
	}
	
	public String getTitle(){
		return title;
	}
	
	public Date getBeginningDate(){
		return beginningDate;
	}
	
	public Date	getEndingDate(){
		return endingDate;
	}
	
	public String getDescription(){
		return description;
	}
	
	public int getEdition(){
		return edition;
	}
	
	public String getPlace(){
		return place;
	}
	
	public String getWebsite(){
		return website;
	}
	
	public double getLatitude(){
		return latitude;
	}
	
	public double getLongitude(){
		return longitude;
	}
	
	public String getLogo(){
		return logo;
	}
	
	public void setId(long id){
		this.id = id;
	}
	
	public void setTitle(String title){
		this.title= title;
	}
	
	public void setBeginningDate(Date date){
		this.beginningDate=date;
	}
	
	public void setEndingDate(Date date) {
		this.endingDate=date;
	}
	
	public void setDescription(String description){
		this.description=description;
	}
	
	public void setEdition(int edition){
		this.edition=edition;
	}
	
	public void setPlace(String place){
		this.place=place;
	}

	public void setWebsite(String website){
		this.website=website;
	}

	public void setLatitude(double latitude){
		this.latitude=latitude;
	}
	public void setLongitude(double longitude){
		this.longitude=longitude;
	}
	
	public void setLogo(String logo){
		this.logo=logo;
	}
}
