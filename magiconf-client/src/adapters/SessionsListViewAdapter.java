package adapters;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import com.example.magiconf_client.R;

import tables.Event;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * @author José
 * List View Adapter to define its behaviour
 */
public class SessionsListViewAdapter extends ArrayAdapter<Event> {
	private static String DATE_FORMAT = "dd-MM-yyyy";
    Context context;
    
    public SessionsListViewAdapter(Context context, int resourceId,
            List<Event> items) {
        super(context, resourceId, items);
        this.context = context;
    }
    
    private class ViewHolder {
        TextView eventDate;
        TextView eventTitle;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        Event rowItem = getItem(position);
 
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.sessions_list, null);
            holder = new ViewHolder();
            holder.eventDate = (TextView) convertView.findViewById(R.id.session_date);
            holder.eventTitle = (TextView) convertView.findViewById(R.id.session_title);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();
        SimpleDateFormat ft =  new SimpleDateFormat (DATE_FORMAT, Locale.US);
        holder.eventDate.setText(ft.format(rowItem.getTime()).toString());
        holder.eventTitle.setText(rowItem.getTitle());
 
        return convertView;
    }
}
