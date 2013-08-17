package adapters;

import java.util.List;

import com.example.magiconf_client.R;
import com.koushikdutta.urlimageviewhelper.UrlDownloader;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import dataRetrieving.AuthInfo;
import dataSources.ParticipantKeynoteDataSource;

import tables.Keynote;
import tables.ParticipantKeynote;
import utils.ValuesHelper;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * List View Adapter to define its behaviour
 */
public class KeynoteListViewAdapterContact extends ArrayAdapter<Keynote> {
    
	Context context;
    
	private ParticipantKeynoteDataSource participantKeynoteDB;
    
    public KeynoteListViewAdapterContact(Context context, int resourceId,
            List<Keynote> items) {
        super(context, R.layout.keynote_speakers_list_contact, items);
        this.context = context;
    	participantKeynoteDB = new ParticipantKeynoteDataSource(context);
    }
    
    private class ViewHolder {
        ImageView keynoteSpeakerPhoto;
        TextView keynoteSpeakerName;
        ImageView addContact;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        Keynote rowItem = getItem(position);
 
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.keynote_speakers_list_contact, null);
            holder = new ViewHolder();
            holder.keynoteSpeakerName = (TextView) convertView.findViewById(R.id.keynote_speaker_name);
            holder.keynoteSpeakerPhoto = (ImageView) convertView.findViewById(R.id.keynote_speaker_photo);
            holder.addContact =  (ImageView) convertView.findViewById(R.id.keynote_speaker_add_contact);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();
 
        holder.keynoteSpeakerName.setText(rowItem.getName());
        if(isContact(rowItem)){
        	holder.addContact.setBackgroundResource(R.drawable.ic_action_user);
        }else{
        	holder.addContact.setBackgroundResource(0);
        }
        UrlImageViewHelper.setUrlDrawable(holder.keynoteSpeakerPhoto,rowItem.getPhoto(), null, UrlImageViewHelper.CACHE_DURATION_INFINITE);
		if(holder.keynoteSpeakerPhoto.getDrawable()==null){
			holder.keynoteSpeakerPhoto.setImageResource(R.drawable.dummy_user_photo);
		}

        return convertView;
    }
    
	private boolean isContact(Keynote k){
		ParticipantKeynote pa = null;
		participantKeynoteDB.open();
		pa = participantKeynoteDB.getParticipantKeynoteDB(AuthInfo.username,k.getId());
		participantKeynoteDB.close();
		
		return pa!=null;
		
	}
    
    
}
