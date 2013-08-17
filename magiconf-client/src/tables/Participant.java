package tables;

import java.sql.Date;

public class Participant {
	 private long id;
	 
	private String country;
	private String username;
	private String password;
	private String name;
	private String phone_number;
	private String email;
	private String photo;
	private String work_place;
	private String qrcode;
	private String linkedinUrl;
	private String facebookUrl;
	private Contact contact;
	private Date lastCheck;
	
	public long getId(){
		return id;
	}
	
	public String getCountry(){
		return country;
	}
	
	public String getUsername(){
		return username;
	}
	
	public String getPassword(){
		return password;
	}
	
	public String getName(){
		return name;
	}
	
	public String getPhone_number(){
		return phone_number;
	}
	
	public String getEmail(){
		return email;
	}
	
	public String getPhoto(){
		return photo;
	}
	
	public String getWork_place(){
		return work_place;
	}
	
	public String getQrcode(){
		return qrcode;
	}
	
	/*public Contact getContact(){
		return this.contact;
	}*/
	
	public Date getLastCheck(){
		return this.lastCheck;
	}
	
	public String getLinkedinUrl() {
		return linkedinUrl;
	}
	
	public String getFacebookUrl() {
		return facebookUrl;
	}
	
	public void setId(long id){
		this.id = id;
	}
	
	public void setCountry(String country){
		this.country=country;
	}
	
	public void setUsername(String username){
		this.username=username;
	}
	
	public void setPassword(String password){
		this.password= password;
	}
	
	public void setName(String name){
		this.name= name;
	}
	
	public void setPhone_number(String phone_number){
		this.phone_number=phone_number;
	}
	
	public void setEmail(String email){
		this.email=email;
	}
	
	public void setPhoto(String photo){
		this.photo=photo;
	}

	public void setWork_place(String work_place){
		this.work_place=work_place;
	}
	
	/*public void setContact(Contact contact){
		this.contact=contact;
	}*/
	
	public void setQrcode(String qrcode){
		this.qrcode = qrcode;
	}
	
	public void setLinkedinUrl(String linkedinUrl) {
		this.linkedinUrl = linkedinUrl;
	}
	
	public void setFacebookUrl(String facebookUrl) {
		this.facebookUrl = facebookUrl;
	}

	public void setLastCheck(Date date) {
		this.lastCheck = date;
		
	}
}
