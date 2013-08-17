package utils;

import dataSources.ArticleDataSource;
import dataSources.NotPresentedPublicationDataSource;
import android.content.Context;
import tables.Publication;

/**
 * @author José
 * 
 * This class represents a publication type checker
 *
 */
public class PublicationTypeChecker {
	
	/**
	 * Returns type of a publication
	 * @param context application context
	 * @param p publication to get type
	 * @return type of the specified publication
	 */
	public static int getType(Context context, Publication p) {
		ArticleDataSource articlesDB = new ArticleDataSource(context);
//		NotPresentedPublicationDataSource pubsDB = new NotPresentedPublicationDataSource(context);
		
		articlesDB.open();

		int type = 0;
		
		if (articlesDB.isArticle(p.getTitle())) 
			type = ValuesHelper.PUB_TYPE.ARTICLE.ordinal();
		else type = ValuesHelper.PUB_TYPE.NOT_PRESENTED.ordinal();

		
		articlesDB.close();
	//	pubsDB.close();
		
		return type;
		
	}

}
