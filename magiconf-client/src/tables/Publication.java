package tables;

import java.sql.Date;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Publication {

	private long id;
	
	private String title;
	private String pubAbstract;
	private Date lastModified;
	
	
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


	public String getAbstract() {
		return pubAbstract;
	}


	public void setAbstract(String pubAbstract) {
		this.pubAbstract = pubAbstract;
	}
	
	public Date getLastModified() {
		return lastModified;
	}
	
	public void setLastModified(Date modified) {
		this.lastModified = modified;
	}

	
}
