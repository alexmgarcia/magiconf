package tables;

public class ParticipantKeynote {
	
	private long keynoteID;
	private String username;

	public long getKeynoteId(){
		return keynoteID;
	}
	
	public String getUsername(){
		return username;
	}

	public void setUsername(String username){
		this.username=username;
	}
	
	public long setKeynoteId(long keynoteID){
		return this.keynoteID = keynoteID;
	}

}
