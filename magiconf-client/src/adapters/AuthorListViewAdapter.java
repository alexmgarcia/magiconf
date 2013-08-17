package adapters;

import java.util.List;

import com.example.magiconf_client.R;
import com.koushikdutta.urlimageviewhelper.UrlDownloader;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import utils.ValuesHelper;
import tables.Author;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author José
 * List View Adapter to define its behaviour
 */
public class AuthorListViewAdapter extends ArrayAdapter<Author> {
    Context context;
    
    public AuthorListViewAdapter(Context context, int resourceId,
            List<Author> items) {
        super(context, resourceId, items);
        this.context = context;
    }
    
    private class ViewHolder {
        ImageView authorPhoto;
        TextView authorName;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        Author rowItem = getItem(position);
 
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.authors_list, null);
            holder = new ViewHolder();
            holder.authorName = (TextView) convertView.findViewById(R.id.author_name);
            holder.authorPhoto = (ImageView) convertView.findViewById(R.id.author_photo);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();
 
        holder.authorName.setText(rowItem.getName());
        UrlImageViewHelper.setUrlDrawable(holder.authorPhoto, ValuesHelper.SERVER_API_ADDRESS_MEDIA + rowItem.getPhoto(), null, UrlImageViewHelper.CACHE_DURATION_INFINITE);
		if(holder.authorPhoto.getDrawable()==null){
			holder.authorPhoto.setImageResource(R.drawable.dummy_user_photo);
		}
		
        return convertView;
    }
        
    
}
