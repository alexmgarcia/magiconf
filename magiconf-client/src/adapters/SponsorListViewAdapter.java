package adapters;


import java.util.List;

import tables.Sponsor;
import utils.ValuesHelper;

import com.example.magiconf_client.R;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import dataSources.SponsorDataSource;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SponsorListViewAdapter extends BaseAdapter{
	Context MyContext;
	List<Sponsor> sponsors;
	 private LayoutInflater mInflater;
	
	 public Object getItem(int arg0) {
         return arg0;
     }

     public long getItemId(int arg0) {
         return arg0;
     }

	public SponsorListViewAdapter(Context context, List<Sponsor> sponsors ){
		 MyContext = context;
		 this.sponsors=sponsors;
		 mInflater = LayoutInflater.from(MyContext);
	}
	
	private class ViewHolder {
        ImageView image;
        
    }
	
	
	@Override
	public int getCount(){
		return sponsors.size();
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		 ViewHolder holder;
		 

         
             convertView = mInflater.inflate(R.layout.activity_sponsors_item, null);
             holder = new ViewHolder();

            // holder.name= (TextView) convertView.findViewById(R.id.sponsor_name);
             holder.image = (ImageView) convertView.findViewById(R.id.sponsor_image);
             UrlImageViewHelper.setUrlDrawable(holder.image, ValuesHelper.SERVER_API_ADDRESS_MEDIA + sponsors.get(position).getLogo(), null, UrlImageViewHelper.CACHE_DURATION_INFINITE);
             
             if(holder.image.getDrawable()==null){
     			holder.image.setImageResource(R.drawable.dummy_sponsor);
     		}
            	 
            // holder.name.setText(sponsors.get(position).getName());
        
         //convertView.setBackgroundDrawable(null);
         
         return convertView;
	}
	

}
