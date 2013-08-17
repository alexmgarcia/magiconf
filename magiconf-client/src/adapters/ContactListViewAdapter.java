package adapters;

import java.util.List;

import tables.Contact;
import tables.Publication;
import utils.ValuesHelper;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.magiconf_client.R;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;


/**
 * @author José
 * List View Adapter to define its behaviour
 */

	public class  ContactListViewAdapter extends ArrayAdapter<Contact> {
	    Context context;
	    
	    public ContactListViewAdapter(Context context, int resourceId,List<Contact> items) {
	        super(context, resourceId, items);
	        this.context = context;
	    }
	    
	    private class ViewHolder {
	        TextView contactName;
	        ImageView contactPhoto;
	    }
	    
	    public View getView(int position, View convertView, ViewGroup parent) {
	        ViewHolder holder = null;
	        Contact c = getItem(position);
	 
	        LayoutInflater mInflater = (LayoutInflater) context
	                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	        if (convertView == null) {
	            convertView = mInflater.inflate(R.layout.contact_item, null);
	            holder = new ViewHolder();
	            holder.contactName = (TextView) convertView.findViewById(R.id.contact_name);
	            holder.contactPhoto = (ImageView) convertView.findViewById(R.id.contact_photo);
	            convertView.setTag(holder);
	           
	        } else
	            holder = (ViewHolder) convertView.getTag();       
	        	        
	        holder.contactName.setText(c.getName());
	        UrlImageViewHelper.setUrlDrawable(holder.contactPhoto, ValuesHelper.SERVER_API_ADDRESS_MEDIA + c.getPhoto(), null, UrlImageViewHelper.CACHE_DURATION_INFINITE);
			if(holder.contactPhoto.getDrawable()==null){
				holder.contactPhoto.setImageResource(R.drawable.dummy_user_photo);
			}
	        return convertView;
	    }
	    
	    
	}
