package adapters;
 
import java.util.List;
import java.util.Map;

import tables.Member;

import com.example.magiconf_client.R;
 
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
 
public class MembersListViewAdapter extends BaseExpandableListAdapter {
 
    private Activity context;
    private Map<String, List<Member>> headerCollections;
    private List<String> headers;
 
    public MembersListViewAdapter(Activity context, List<String> headers,
            Map<String, List<Member>> headerCollections) {
        this.context = context;
        this.headerCollections = headerCollections;
        this.headers = headers;
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
            convertView = inflater.inflate(R.layout.member_child, null);
        }
 
        TextView item = (TextView) convertView.findViewById(R.id.member_name);

       	Member child = (Member) getChild(groupPosition, childPosition);
       	String name = child.getName();
        item.setText(name);        	
        
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
            convertView = infalInflater.inflate(R.layout.comission_header,
                    null);
        }
        TextView item = (TextView) convertView.findViewById(R.id.comission_header);
        item.setTypeface(null, Typeface.BOLD);
        item.setText(headerName);
        return convertView;
    }
 
    public boolean hasStableIds() {
        return true;
    }
 
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}