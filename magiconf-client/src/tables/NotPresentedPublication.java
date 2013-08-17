package tables;

import org.codehaus.jackson.map.annotate.JsonDeserialize;

import serializers_deserializers.NotPresentedPublicationDeserializer;

@JsonDeserialize(using = NotPresentedPublicationDeserializer.class)
public class NotPresentedPublication extends Publication {
	
	private String authors;
	private long publicationID;
	
	public NotPresentedPublication(String title, String pubAbstract, String authors) {
		super.setTitle(title);
		super.setAbstract(pubAbstract);
		this.authors = authors;
	}
	
	public NotPresentedPublication() {}

	public String getAuthors() {
		return authors;
	}
	
	public void setAuthors(String authors) {
		this.authors = authors;
	}
	
	public long getPublicationID() {
		return publicationID;
	}
	
	public void setPublicationID(long publicationID) {
		this.publicationID = publicationID;
	}

}
