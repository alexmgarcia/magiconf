package adapters;
 
import java.util.List;
import java.util.Map;

import tables.Sight;

import com.example.magiconf_client.R;

import dataSources.SightsDataSource;
 
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
 
public class SightsListViewAdapter extends BaseExpandableListAdapter {
 
    private Activity context;
    private Map<String, List<Long>> headerCollections;
    private List<String> headers;
    private String description;
    private SightsDataSource sightsDB;
 
    public SightsListViewAdapter(Activity context, List<String> headers,
            Map<String, List<Long>> headerCollections, String description, SightsDataSource sightsDB) {
        this.context = context;
        this.headerCollections = headerCollections;
        this.headers = headers;
        this.description = description;
        this.sightsDB=sightsDB;
    }
 
    public Object getChild(int groupPosition, int childPosition) {
        return headerCollections.get(headers.get(groupPosition)).get(childPosition);
    }
 
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
 
    public View getChildView(final int groupPosition, final int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
        
        LayoutInflater inflater = context.getLayoutInflater();
 
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.sight_child, null);
        }
 
        TextView item = (TextView) convertView.findViewById(R.id.sight_name);

        if(groupPosition==0){
        	item.setText(description);
        }else{
        	Long id = (Long) getChild(groupPosition, childPosition);
        	sightsDB.open();
        	String name = sightsDB.getSight(id).getName();
        	sightsDB.close();
        	item.setText(name);        	
        }

        return convertView;
    }
 
    public int getChildrenCount(int groupPosition) {
        return headerCollections.get(headers.get(groupPosition)).size();
    }
 
    public Object getGroup(int groupPosition) {
        return headers.get(groupPosition);
    }
 
    public int getGroupCount() {
        return headers.size();
    }
 
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
 
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
        String headerName = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.sight_header,
                    null);
        }
        TextView item = (TextView) convertView.findViewById(R.id.sight_header);
        item.setTypeface(null, Typeface.BOLD);
        item.setText(headerName);
        return convertView;
    }
 
    public boolean hasStableIds() {
        return true;
    }
 
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return groupPosition!=0;
    }
}