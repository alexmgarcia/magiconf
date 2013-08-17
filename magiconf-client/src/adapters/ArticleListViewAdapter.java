package adapters;

import java.util.List;

import com.example.magiconf_client.R;

import tables.Article;
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
public class ArticleListViewAdapter extends ArrayAdapter<Article> {
    Context context;
    
    public ArticleListViewAdapter(Context context, int resourceId,
            List<Article> items) {
        super(context, resourceId, items);
        this.context = context;
    }
    
    private class ViewHolder {
        TextView articleTitle;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        Article rowItem = getItem(position);
 
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.articles_list, null);
            holder = new ViewHolder();
            holder.articleTitle = (TextView) convertView.findViewById(R.id.article_title);
           
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();
 
        holder.articleTitle.setText(rowItem.getTitle());
        
 
        return convertView;
    }
    
    
}
