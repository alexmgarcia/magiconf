package adapters;

import java.util.List;

import com.example.magiconf_client.DbHelper;
import com.example.magiconf_client.R;

import dataRetrieving.AuthInfo;
import dataSources.AuthorDataSource;
import dataSources.FavPublicationDataSource;
import tables.FavPublication;
import tables.Publication;
import utils.Pair;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


//TODO
/**
 * @author José
 * Publication List View Adapter to define its behaviour
 */
public class PublicationListViewAdapter extends ArrayAdapter<Publication> {
    Context context;
    DbHelper dbHelper;
    SQLiteDatabase database;
    
    FavPublicationDataSource favDB;
    
    private static final int UNFAV_CODE = 0;
    private static final int FAV_CODE = 1;
    
    public PublicationListViewAdapter(Context context, int resourceId,
            List<Publication> items) {
        super(context, resourceId, items);
        this.context = context;
        dbHelper = new DbHelper(context);
        favDB = new FavPublicationDataSource(context);
		database = dbHelper.getWritableDatabase();
    }
    
    private class ViewHolder {
        TextView pubTitle;
        ImageView addFav;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        Publication rowItem = getItem(position);
 
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.publications_list, null);
            holder = new ViewHolder();
            holder.pubTitle = (TextView) convertView.findViewById(R.id.publication_title);
            holder.addFav =  (ImageView) convertView.findViewById(R.id.pub_add_fav);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();
 
        holder.pubTitle.setText(rowItem.getTitle());

           
       final long id = getPubID(rowItem.getTitle());
       
        if(!isFavourited(id)){
        holder.addFav.setBackgroundResource(R.drawable.unfavorited);
        holder.addFav.setTag(UNFAV_CODE);
        }else{
            holder.addFav.setBackgroundResource(R.drawable.favorited);
            holder.addFav.setTag(FAV_CODE);	
        }
        holder.addFav.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	
            	favDB.open();
        		
            	
            	if(view.getTag().equals(UNFAV_CODE)){
            		view.setTag(FAV_CODE);
            		view.setBackgroundResource(R.drawable.favorited);
            		favDB.createFavPublication(id, AuthInfo.username);
            		
            	}else{
            		view.setTag(UNFAV_CODE);
            		view.setBackgroundResource(R.drawable.unfavorited);
            		favDB.deleteFavPublicationByPubID(id);
            	}
            	favDB.close();
            }
        });
        return convertView;
    }
    
	

	public void close(){
		dbHelper.close();
	}

	public  Long getPubID(String pubTitle) {

		String whereClause = DbHelper.PUB_TITLE + " = '" +pubTitle+"'" ;
		
		Cursor cursor = database.query(DbHelper.TABLE_PUBLICATIONS, null, whereClause, null, null, null, null);
		cursor.moveToFirst();
		long pubId= cursor.getLong(0);
		cursor.close();
		return pubId;
	}
	
	public boolean isFavourited(long pubId){
				
		String whereClause = DbHelper.FAV_PUBLICATION_PUB_ID + " =" +pubId ;
		
		
		Cursor cursor = database.query(DbHelper.TABLE_FAVPUBLICATIONS, null, whereClause, null, null, null, null);
		boolean isFavourited = cursor.moveToFirst();
		cursor.close();
		return isFavourited;
		
	}
    
    
}
