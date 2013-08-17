package utils;

import dataSources.SightsDataSource;
import android.content.Context;
import tables.Sight;

/**
 * @author José
 * 
 * This class represents a publication type checker
 *
 */
public class SightTypeChecker {
	
	/**
	 * Returns type of a sight
	 * @param context application context
	 * @param s sight to get type
	 * @return type of the specified sight
	 */
	public static int getType(Context context, Sight p) {
		SightsDataSource sightsDB = new SightsDataSource(context);

		sightsDB.open();

		int type = 0;
		
		if (sightsDB.getHotel(p.getId())!=null) 
			type = ValuesHelper.SIGHT_TYPE.HOTEL.ordinal();
		else if(sightsDB.getRestaurant(p.getId())!=null){
			type = ValuesHelper.SIGHT_TYPE.RESTAURANT.ordinal();
		} else type = ValuesHelper.SIGHT_TYPE.OTHER_SIGHT.ordinal();

		
		sightsDB.close();
	//	pubsDB.close();
		
		return type;
		
	}

}
