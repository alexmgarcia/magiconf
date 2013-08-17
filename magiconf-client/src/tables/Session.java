

package tables;

import java.sql.Date;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)

public class Session {
	private long id;

	private String password;
	private Date lastModified;

	public long getId(){
		return id;
	}


	public String getPassword(){
		return password;
	}


	public Date getLastModified(){
		return lastModified;
	}

	public void setId(long id){
		this.id = id;
	}

	public void setPassword(String password){
		this.password=password;
	}

	public void setLastModified(Date lastModified){
		this.lastModified= lastModified;
	}

}
