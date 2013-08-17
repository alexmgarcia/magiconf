package tables;

public class Hotel extends Sight{

	private long hotelID;
	private int stars;
	private String phone;
	
	public long getHotelID() {
		return hotelID;
	}
	public void setHotelID(long hotelID) {
		this.hotelID = hotelID;
	}
	public int getStars() {
		return stars;
	}
	public void setStars(int stars) {
		this.stars = stars;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}

	
}
