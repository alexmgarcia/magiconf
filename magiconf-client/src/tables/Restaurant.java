package tables;

public class Restaurant extends Sight{

	private long restaurantID;
	private String phone;
	
	public long getRestaurantID() {
		return restaurantID;
	}
	public void setRestaurantID(long restaurantID) {
		this.restaurantID = restaurantID;
	}

	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
}
