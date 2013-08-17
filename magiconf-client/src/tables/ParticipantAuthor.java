package tables;

public class ParticipantAuthor {
	
	private long authorID;
	private String username;

	public long getAuthorId(){
		return authorID;
	}
	
	public String getUsername(){
		return username;
	}

	public void setUsername(String username){
		this.username=username;
	}
	
	public long setAuthorId(long authorID){
		return this.authorID = authorID;
	}

}
