package tables;

import java.sql.Date;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Keynote {


	private long id;
	
	private String email;
	private String work_place;
	private String country;
	private String photo;
	private String name;
	private Contact contact;

	//Foreign keys, references KeynoteSession
	//private String sessionTitle;
	//private Date sessionTime;
	//private long keynoteSessionId;
	

	public long getId() {
		return id;
	}


	public void setId(long id) {
		this.id = id;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public String getWork_place() {
		return work_place;
	}


	public void setWork_place(String work_place) {
		this.work_place = work_place;
	}


	public String getCountry() {
		return country;
	}


	public void setCountry(String country) {
		this.country = country;
	}


	public String getPhoto() {
		return photo;
	}


	public void setPhoto(String photo) {
		this.photo = photo;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}

	/*public long getKeynoteSessionId() {
		return keynoteSessionId;
	}
	
	public void setKeynoteSessionId(long newId) {
		this.keynoteSessionId = newId;
	}*/
	
	//To be used by ArrayAdapter in ListView controller
	public String toString(){
		return "Name: " + name + "   Email: " + email + "   Workplace: " + work_place + "  Country: " + country + "   Photo: "+photo ;
	}


	public void setContact(Contact contact) {
		this.contact = contact;
		
	}
	
	public Contact getContact() {
		return this.contact;
		
	}
	
	
}
