package adapters;

import java.util.List;

import com.example.magiconf_client.R;

import tables.Poster;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * @author Farah
 * List View Adapter to define its behaviour
 */
public class PosterListViewAdapter extends ArrayAdapter<Poster> {
    Context context;
    
    public PosterListViewAdapter(Context context, int resourceId,
            List<Poster> items) {
        super(context, resourceId, items);
        this.context = context;
    }
    
    private class ViewHolder {
        TextView posterName;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        Poster rowItem = getItem(position);
 
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.posters_list, null);
            holder = new ViewHolder();
            holder.posterName = (TextView) convertView.findViewById(R.id.poster_title);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();
 
        holder.posterName.setText(rowItem.getTitle());
        
        return convertView;
    }
    
    
}
