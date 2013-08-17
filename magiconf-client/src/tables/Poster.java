package tables;

import java.sql.Date;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)

public class Poster {
	private long id;
	
	private String title;
	private List<Author> authors;
	private Date lastModified;
	
	public long getId(){
		return id;
	}
	
	
	public String getTitle(){
		return title;
	}
	
	
	public Date getLastModified(){
		return lastModified;
	}
	
	public void setId(long id){
		this.id = id;
	}
	
	public List<Author> getAuthors(){
		return authors;
	}
	
	public void setAuthors(List<Author> authors){
		this.authors=authors;
	}
	
	
	public void setTitle(String title){
		this.title=title;
	}
	
		
	public void setLastModified(Date lastModified){
		this.lastModified= lastModified;
	}
}
