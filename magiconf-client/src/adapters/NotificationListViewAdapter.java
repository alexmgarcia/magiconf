package adapters;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import com.example.magiconf_client.R;
import com.koushikdutta.urlimageviewhelper.UrlDownloader;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import tables.Keynote;
import tables.Notification;
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
public class NotificationListViewAdapter extends ArrayAdapter<Notification> {
    Context context;
    
    public NotificationListViewAdapter(Context context, int resourceId,
            List<Notification> items) {
        super(context, resourceId, items);
        this.context = context;
    }
    
    private class ViewHolder {
        TextView notificationDate;
        TextView notificationTitle;
        TextView notificationDescription;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        Notification rowItem = getItem(position);
 
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.notification_list, null);
            holder = new ViewHolder();
            holder.notificationDate = (TextView) convertView.findViewById(R.id.notification_date);
            holder.notificationTitle = (TextView) convertView.findViewById(R.id.notification_title);
            holder.notificationDescription = (TextView) convertView.findViewById(R.id.notification_description);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();
 
        SimpleDateFormat ft =  new SimpleDateFormat (ValuesHelper.DATE_FORMAT + " " + ValuesHelper.HOUR_FORMAT, Locale.US);
        holder.notificationDate.setText(ft.format(rowItem.getTime()).toString());
        holder.notificationTitle.setText(rowItem.getTitle());
        holder.notificationDescription.setText(rowItem.getDescription());
        return convertView;
    }
    
    
}
