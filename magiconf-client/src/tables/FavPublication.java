package tables;

import android.util.Log;

public class FavPublication {

	private long id;
	private long publicationID;
	
	private String title;
	private String username;
	
	
	public long getId() {
		return id;
	}


	public void setId(long id) {
		this.id = id;
	}


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public String toString(){
		return title;
	}


	public void setPublicationID(long publicationID) {
		this.publicationID = publicationID;
	}
	
	public long getPublicationID() {
		return publicationID;
	}
	
}
