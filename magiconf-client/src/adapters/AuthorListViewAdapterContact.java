package adapters;

import java.util.List;

import com.example.magiconf_client.R;
import com.koushikdutta.urlimageviewhelper.UrlDownloader;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import dataRetrieving.AuthInfo;
import dataSources.ParticipantAuthorDataSource;

import tables.Author;
import tables.ParticipantAuthor;
import utils.ValuesHelper;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * List View Adapter to define its behaviour
 */
public class AuthorListViewAdapterContact extends ArrayAdapter<Author> {
    Context context;
    
    private ParticipantAuthorDataSource participantAuthorDB;
    
    public AuthorListViewAdapterContact(Context context, int resourceId,
            List<Author> items) {
        super(context, resourceId, items);
        this.context = context;
        participantAuthorDB = new ParticipantAuthorDataSource(context);
    }
    
    private class ViewHolder {
        ImageView authorPhoto;
        TextView authorName;
        ImageView addContact;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        Author rowItem = getItem(position);
         
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.author_list_contact, null);
            holder = new ViewHolder();
            holder.authorName = (TextView) convertView.findViewById(R.id.author_name);
            holder.authorPhoto = (ImageView) convertView.findViewById(R.id.author_photo);
            holder.addContact =  (ImageView) convertView.findViewById(R.id.author_add_contact);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();
 
        holder.authorName.setText(rowItem.getName());
        if(isContact(rowItem)){
        	holder.addContact.setBackgroundResource(R.drawable.ic_action_user);
        }else{
        	holder.addContact.setBackgroundResource(0);
        }
        UrlImageViewHelper.setUrlDrawable(holder.authorPhoto, ValuesHelper.SERVER_API_ADDRESS_MEDIA +rowItem.getPhoto(), null, UrlImageViewHelper.CACHE_DURATION_INFINITE);
		if(holder.authorPhoto.getDrawable()==null){
			holder.authorPhoto.setImageResource(R.drawable.dummy_user_photo);
		}


 
        return convertView;
    }
    
	private boolean isContact(Author a){
		ParticipantAuthor pa = null;
		Log.i("AUTHOR NAME",""+a.getName());
		Log.i("AUTHOR ID",""+a.getId());
		participantAuthorDB.open();
		pa = participantAuthorDB.getParticipantAuthorDB(AuthInfo.username,a.getId());
		participantAuthorDB.close();
		
		return pa!=null;
		
	}
    
    
}
