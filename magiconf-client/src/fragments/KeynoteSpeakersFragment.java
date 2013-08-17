package fragments;

import java.util.List;

import tables.Author;
import tables.Keynote;

import com.example.magiconf_client.AuthorsAndKeynoteSpeakersActivity;
import com.example.magiconf_client.R;


import adapters.AuthorListViewAdapter;
import adapters.KeynoteListViewAdapter;
import adapters.KeynoteListViewAdapterContact;
import android.support.v4.app.ListFragment;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

public class KeynoteSpeakersFragment extends ListFragment  {

	public static final String ARG_OBJECT = "object";
	
	private List<Keynote> keynotes;
	
	OnKeynoteSelectedListener mListener;
	KeynoteListViewAdapterContact adapter;

	// Container Activity must implement this interface
    public interface OnKeynoteSelectedListener {
        void onKeynoteSelected(long keynoteID);
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.tab_keynote_speakers, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setFastScrollEnabled(true);
		keynotes = ((AuthorsAndKeynoteSpeakersActivity)getActivity()).getKeynoteSpeakers();
		adapter = new KeynoteListViewAdapterContact(getActivity(),
				android.R.layout.simple_list_item_1, keynotes);

		setListAdapter(adapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		mListener.onKeynoteSelected(((Keynote)getListView().getItemAtPosition(position)).getId());
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnKeynoteSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnKeynoteSelectedListener");
        }
    }



	public void updateList(List<Keynote> ks){
		this.keynotes = ks;
		if(adapter != null)
			adapter.notifyDataSetChanged();	
	}
	
	public void selfUpdateList(){
		keynotes = ((AuthorsAndKeynoteSpeakersActivity)getActivity()).getKeynoteSpeakers();
		if(adapter != null)
			adapter.notifyDataSetChanged();	
	}
	
	
	


}
