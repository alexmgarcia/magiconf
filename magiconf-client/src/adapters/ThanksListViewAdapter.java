package adapters;

import java.util.List;

import tables.Developer;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.magiconf_client.R;

public class ThanksListViewAdapter  extends BaseAdapter{
	Context MyContext;
	List<String> persons;
	 
	private LayoutInflater mInflater;
	
	 public Object getItem(int arg0) {
         return arg0;
     }

     public long getItemId(int arg0) {
         return arg0;
     }

	public ThanksListViewAdapter(Context context, List<String> persons ){
		 MyContext = context;
		 this.persons=persons;
		 mInflater = LayoutInflater.from(MyContext);
	}
	
	private class ViewHolder {
        ImageView image;
        TextView name;
        
    }
	
	
	public int getCount(){
		return persons.size();
	}
	public View getView(int position, View convertView, ViewGroup parent){
		 ViewHolder holder;
		 

         
             convertView = mInflater.inflate(R.layout.activity_thanks_item, null);
             holder = new ViewHolder();

             holder.name= (TextView) convertView.findViewById(R.id.name);
             holder.image = (ImageView) convertView.findViewById(R.id.photo);
             
             String name=persons.get(position);
             Log.i("NAME", "" + name);
             holder.name.setText(name);
             
             if(name.equalsIgnoreCase("WorldCIST13")){
            	 holder.image.setImageResource(R.drawable.logo_worldcist);
             }
             
        
         //convertView.setBackgroundDrawable(null);
         
         return convertView;
	}
	

}
