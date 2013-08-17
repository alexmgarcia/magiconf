package adapters;

import java.util.List;

import tables.Developer;
import tables.Sponsor;
import utils.ValuesHelper;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.magiconf_client.R;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class DevelopersListViewAdapter 	extends BaseAdapter{
		Context MyContext;
		List<Developer> developers;
		 
		private LayoutInflater mInflater;
		
		 public Object getItem(int arg0) {
	         return arg0;
	     }

	     public long getItemId(int arg0) {
	         return arg0;
	     }

		public DevelopersListViewAdapter(Context context, List<Developer> developers ){
			 MyContext = context;
			 this.developers=developers;
			 mInflater = LayoutInflater.from(MyContext);
		}
		
		private class ViewHolder {
	        ImageView image;
	        TextView name;
	        TextView number;
	    }
		
		
		@Override
		public int getCount(){
			return developers.size();
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			 ViewHolder holder;
			 

	         
	             convertView = mInflater.inflate(R.layout.activity_developer, null);
	             holder = new ViewHolder();

	             holder.name= (TextView) convertView.findViewById(R.id.name);
	             holder.number= (TextView) convertView.findViewById(R.id.number);
	             holder.image = (ImageView) convertView.findViewById(R.id.photo);
	             
	             String name=developers.get(position).getName();
	             Log.i("test", "" + name);
	             holder.name.setText(name);
	             
	             if(name.equals("Alexandre Garcia")){
	            	 holder.image.setImageResource(R.drawable.alexandre);
	             }
	             else if(name.equals("David Semedo")){
	            	 holder.image.setImageResource(R.drawable.david);
	             }
	             else if(name.equals("Farah Mussa")){
	            	 holder.image.setImageResource(R.drawable.farah);
	             }
	             else if(name.equals("José Arjona")){
	            	 holder.image.setImageResource(R.drawable.jose);
	             }
	             holder.number.setText("Nº" + developers.get(position).getNumber());
	        
	         //convertView.setBackgroundDrawable(null);
	         
	         return convertView;
		}
		

}
