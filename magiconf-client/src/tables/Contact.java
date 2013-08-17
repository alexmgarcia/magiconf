package tables;

public class Contact {

	private String facebookUrl;
	private String linkedinUrl;
	private long id;
	private String name;
	private String email;
	private String country;
	private String workPlace;
	private String phoneNumber;
	private String photo;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getCountry() {
		return country;
	}
	
	public void setCountry(String country) {
		this.country = country;
	}
	
	public String getWorkPlace() {
		return workPlace;
	}
	
	public void setWorkPlace(String workPlace) {
		this.workPlace = workPlace;
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	public String getPhoto() {
		return photo;
	}
	
	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id=id;
	}
	
	public String getFacebookUrl() {
		return facebookUrl;
	}
	
	
	public void setFacebookUrl(String facebookUrl) {
		this.facebookUrl = facebookUrl;
	}
	
	public String getLinkedinUrl() {
		return linkedinUrl;
	}
	
	public void setLinkedinUrl(String linkedinUrl) {
		this.linkedinUrl = linkedinUrl;
	}
}
