package tables;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Article extends Publication{

	private long articleId;
	private List<Author> authors;
	
	public long getArticleId() {
		return articleId;
	}


	public void setArticleId(long id) {
		this.articleId = id;
	}
	
	public List<Author> getAuthors(){
		return authors;
	}
	
	public void setAuthors(List<Author> authors){
		this.authors = authors;
	}



	
}
