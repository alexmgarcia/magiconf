package adapters;


import com.example.magiconf_client.R;
 
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
 
//Utilizar para o caso dos abstracts!
public class CityDescriptionViewAdapter extends BaseExpandableListAdapter {
	private Activity context;
    private String description;
 
	private static final String DESCRIPTION = "Descrição da Cidade";
	
    public CityDescriptionViewAdapter(Activity context, String description) {
        this.context = context;
        this.description = description;
    }
 
    public View getChildView(int arg0, int arg1, boolean arg2, View convertView, ViewGroup arg4) { //Shows the description
           

        LayoutInflater inflater = context.getLayoutInflater();
 
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.city_description, null);
        }
 
        TextView item = (TextView) convertView.findViewById(R.id.city_description);

        item.setText(description);
        return convertView;
    }
 

    public View getGroupView(int arg0, boolean arg1, View convertView, ViewGroup arg3) { //Shows the header

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.city_description_header,
                    null);
        }
        TextView item = (TextView) convertView.findViewById(R.id.city_description_header);
        item.setTypeface(null, Typeface.BOLD);
        item.setText(DESCRIPTION);
        return convertView;
    }

    public Object getChild(int groupPosition, int childPosition) {
        return description;
    }
 
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }


	@Override
	public int getChildrenCount(int arg0) {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public Object getGroup(int arg0) {
		// TODO Auto-generated method stub
		return description;
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public long getGroupId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChildSelectable(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return false;
	}
 

}