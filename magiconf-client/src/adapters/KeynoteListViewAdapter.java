package adapters;

import java.util.List;

import com.example.magiconf_client.R;
import com.koushikdutta.urlimageviewhelper.UrlDownloader;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import tables.Keynote;
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
 * @author Alexandre
 * List View Adapter to define its behaviour
 */
public class KeynoteListViewAdapter extends ArrayAdapter<Keynote> {
    Context context;
    
    public KeynoteListViewAdapter(Context context, int resourceId,
            List<Keynote> items) {
        super(context, resourceId, items);
        this.context = context;
    }
    
    private class ViewHolder {
        ImageView keynoteSpeakerPhoto;
        TextView keynoteSpeakerName;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        Keynote rowItem = getItem(position);
 
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.keynote_speakers_list, null);
            holder = new ViewHolder();
            holder.keynoteSpeakerName = (TextView) convertView.findViewById(R.id.keynote_speaker_name);
            holder.keynoteSpeakerPhoto = (ImageView) convertView.findViewById(R.id.keynote_speaker_photo);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();
 
        holder.keynoteSpeakerName.setText(rowItem.getName());
        UrlImageViewHelper.setUrlDrawable(holder.keynoteSpeakerPhoto,rowItem.getPhoto(), null, UrlImageViewHelper.CACHE_DURATION_INFINITE);
		if(holder.keynoteSpeakerPhoto.getDrawable()==null){
			holder.keynoteSpeakerPhoto.setImageResource(R.drawable.dummy_user_photo);
		}
        return convertView;
    }
    
    
}
