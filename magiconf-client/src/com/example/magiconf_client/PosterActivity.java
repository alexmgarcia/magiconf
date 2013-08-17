package com.example.magiconf_client;

import java.util.ArrayList;
import java.util.Iterator;

import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import dataRetrieving.ArticleData;
import dataRetrieving.AuthInfo;
import dataRetrieving.PosterData;
import dataSources.PosterDataSource;

import tables.Author;
import tables.Poster;
import utils.ValuesHelper;


import adapters.AuthorListViewAdapter;
import adapters.Helper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

/**
 * @author Farah
 *
 * This classe represents the Poster Activity
 */
public class PosterActivity extends Activity {

	//public final static String AUTHOR_ID="authorID";
	
	private PosterDataSource posterDB = new PosterDataSource(this); // the database
	
	private void updateViews(final Poster result) { //TODO: Adicionar a favoritos
		Log.i("PosterActivity result", ""+result);
		setContentView(R.layout.activity_poster);
		setTitle("Poster");
		TextView textView = (TextView) PosterActivity.this.findViewById(R.id.poster_title);
		Log.i("PosterActivity", ""+textView);
		textView.setText(result.getTitle());
		Log.i("Poster title",""+result.getTitle());
		final ListView authorsList = (ListView) findViewById(R.id.authors_list);
		final ArrayList<Author> list = new ArrayList<Author>();
		
		// Initialize the authors list
		Iterator<Author> it = result.getAuthors().iterator();
		while (it.hasNext())
			list.add(it.next());
	
		
		authorsList.setAdapter(new AuthorListViewAdapter(this, android.R.layout.simple_list_item_1, list));
		
		authorsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				
				Author author = (Author) authorsList.getItemAtPosition(position);
				Intent intent = new Intent(PosterActivity.this, AuthorActivity.class);
				intent.putExtra(ValuesHelper.AUTHOR_ID, author.getId()); // TODO this is not finished. It will be the author id of a selected item in the list
				startActivity(intent);
				
				// TODO Open the author activity.
			}
		});
		
		// This allows to have a list inside a scrollview
		Helper.getListViewSize(authorsList);
		
		Log.i("Result", ""+result.getLastModified());
		
	   
	}
	
	/**
	 * Inserts a new Poster into the database
	 * @param result object to use to populate data
	 * @return the new Poster
	 */
	private Poster createNewPoster(Poster result) {
		posterDB.open();
		Poster newPost = posterDB.createPoster(result.getTitle(), result.getAuthors());
		
		posterDB.close();
		return newPost;

	}
	
	private Poster getPoster(Long id) {
		posterDB.open();
		Poster a = posterDB.getPoster(id); 
		posterDB.close();
		return a;
	}
	
	private class RESTGetTask extends AsyncTask<String, Void,Poster> {
		protected Poster doInBackground(String... params) {
			/*RestTemplate restTemplate = new RestTemplate();
		    restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
		     
		    Poster post = restTemplate.getForObject(params[0], Poster.class);
		    */
		    return PosterData.downloadPosterData(params[0], AuthInfo.username, AuthInfo.password, posterDB);
		   
		}
		
		protected void onPostExecute(Poster result) {
			// This is new data, so let's populate the data in views and insert it into the database
			//Poster post = createNewPoster(result);
			updateViews(result);
		}
		
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
		Intent intent = getIntent();
		Long postId = intent.getLongExtra(PostersSessionActivity.POSTER_ID, 2); //TODO
		
		
		// Get an existing poster
		Poster a = getPoster(postId);
		
		Log.i("ENTROU NO PosterActivity", "Sim");
		
		Log.i("PosterActivity", ""+posterDB);
		Log.i("PosterActivity", ""+Long.valueOf(1));
		
		
		
		if (a == null){ // it doesn't exist into the local database
			Log.i("PosterActivity NULL", "Entrou no a=null");
			new RESTGetTask().execute("" + postId);;
		}else		
			if(a.getAuthors()==null){
				Log.i("Authors","está a NULL");
			}
			updateViews(a);

		setupActionBar();
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.package.ACTION_LOGOUT");
		registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Log.d("onReceive","Logout in progress");
				finish();
			}
		}, intentFilter);
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.poster, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, MainMenu.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		case R.id.action_about:
			intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}

