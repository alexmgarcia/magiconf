package tables;

public class ParticipantSession {

	private long sessionId;
	private String username;

	public long getSessionId(){
		return sessionId;
	}
	
	public String getUsername(){
		return username;
	}

	public void setUsername(String username){
		this.username=username;
	}
	
	public long setSessionId(long sessionId){
		return this.sessionId = sessionId;
	}

	
}
