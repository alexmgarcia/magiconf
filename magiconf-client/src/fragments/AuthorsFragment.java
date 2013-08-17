package fragments;

import java.util.List;

import tables.Author;

import com.example.magiconf_client.AuthorsAndKeynoteSpeakersActivity;
import com.example.magiconf_client.R;
import adapters.AuthorListViewAdapter;
import adapters.AuthorListViewAdapterContact;
import android.support.v4.app.ListFragment;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

public class AuthorsFragment extends ListFragment  {

	public static final String ARG_OBJECT = "object";

	private List<Author> authors;

	OnAuthorSelectedListener mListener;
	AuthorListViewAdapterContact adapter;

	// Container Activity must implement this interface
	public interface OnAuthorSelectedListener {
		void onAuthorSelected(long authorID);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.tab_authors, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		authors = ((AuthorsAndKeynoteSpeakersActivity)getActivity()).getAuthors();
		adapter = new AuthorListViewAdapterContact	(getActivity(),
				android.R.layout.simple_list_item_1, authors);
		setListAdapter(adapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		mListener.onAuthorSelected(((Author)getListView().getItemAtPosition(position)).getId());
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnAuthorSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnAuthorSelectedListener");
		}
	}



	public void updateList(List<Author> as){
		this.authors = as;
		if(adapter != null)
			adapter.notifyDataSetChanged();
	}

	public void selfUpdateList(){
		authors = ((AuthorsAndKeynoteSpeakersActivity)getActivity()).getAuthors();
		if(adapter != null)
			adapter.notifyDataSetChanged();	
	}
	

}


