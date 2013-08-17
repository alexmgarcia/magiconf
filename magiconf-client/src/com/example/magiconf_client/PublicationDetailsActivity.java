package com.example.magiconf_client;

import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import tables.Article;
import tables.FavPublication;
import tables.NotPresentedPublication;
import utils.ValuesHelper;

import dataRetrieving.ArticleData;
import dataRetrieving.AuthInfo;
import dataRetrieving.NotPresentedPublicationData;
import dataSources.FavPublicationDataSource;
import dataSources.NotPresentedPublicationDataSource;
import dataSources.PublicationDataSource;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.R.menu;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class PublicationDetailsActivity extends Activity {

private NotPresentedPublicationDataSource notPresentedPublicationDB = new NotPresentedPublicationDataSource(this); // the database
private FavPublicationDataSource favPubsDB = new FavPublicationDataSource(this);
private NotPresentedPublication e;
	/**
	 * Updates all the views present in this activity
	 * @param result the object to use to populate data
	 */
	private void updateViews(final NotPresentedPublication result) {
		setContentView(R.layout.activity_publication_details);
		TextView textView = (TextView) this.findViewById(R.id.publication_title);
		Log.i("UpdateViews", result+"");
		Log.i("UpdateViews", result.getTitle());
		textView.setText(result.getTitle());
		textView = (TextView) this.findViewById(R.id.abstract_text);
		textView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TextView textView = (TextView) v;
				textView.setText(result.getAbstract());
				textView.setMaxLines(Integer.MAX_VALUE);
			}
		});
		
		textView = (TextView) this.findViewById(R.id.authors);
		textView.setText(result.getAuthors());
	}
	
	/**
	 * Inserts a new not presented publication into the database
	 * @param result object to use to populate data
	 * @return the new not presented publication session
	 */
	private NotPresentedPublication createNewNotPresentedPublication(NotPresentedPublication result) {
		notPresentedPublicationDB.open();
		NotPresentedPublication newNotPresentedPublication = notPresentedPublicationDB.createNotPresentedPublication(result.getTitle(), result.getAbstract(), result.getAuthors());
		notPresentedPublicationDB.close();
		return newNotPresentedPublication;
	}
	
	private NotPresentedPublication getNotPresentedPublication(Long id) {
		notPresentedPublicationDB.open();
		NotPresentedPublication e = notPresentedPublicationDB.getNotPresentedPublication(id); // TODO alterar para id
		notPresentedPublicationDB.close();
		return e;
	}
	
	private NotPresentedPublication getNotPresentedPublicationByParent(Long id){
		notPresentedPublicationDB.open();
		NotPresentedPublication e = notPresentedPublicationDB.getNotPresentedPublicationByParent(id); 
		notPresentedPublicationDB.close();
		return e;		
	}
	
	/**
	 * @author Alexandre
	 * Asynchronous REST GET request
	 */
	private class RESTGetTask extends AsyncTask<String, Void, NotPresentedPublication> {
		protected NotPresentedPublication doInBackground(String... params) {
			
			return NotPresentedPublicationData.downloadNotPresentedPubData(params[0], AuthInfo.username, AuthInfo.password, notPresentedPublicationDB);
		}
		
		protected void onPostExecute(NotPresentedPublication result) {
			// This is new data, so let's populate the data in views and insert it into the database
			e = result;
			updateViews(result);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
		Intent intent = getIntent();
		Long publicationID = intent.getLongExtra(ValuesHelper.PUBLICATION_ID, 5);//TODO alterar. Quando for feito o download inicial, nao são necessários mais pedidos.
		Boolean isParent = intent.getBooleanExtra(ValuesHelper.IS_PARENT_ID, false);
		
		// Get an existing not presented publication
		if(isParent){
			Log.i("É PARENT","sim");
			e = getNotPresentedPublicationByParent(publicationID);
		}else{
			Log.i("É PARENT","não");
			e = getNotPresentedPublication(publicationID);
		}
		
		if (e == null){ // it doesn't exist into the local database
			String title= "Toward the design quality evaluation of object-oriented software systems";
			e = getNotPresentedPublicationByTitle(title); //isto é só para efeitos de teste
			if(e==null){
				new RESTGetTask().execute(title);
			}else updateViews(e);
			
		}else			
			updateViews(e);
		
		// Show the Up button in the action bar.
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
	
	private NotPresentedPublication getNotPresentedPublicationByTitle(String title) {
		notPresentedPublicationDB.open();
		NotPresentedPublication e = notPresentedPublicationDB.getNotPresentedPublicationByTitle(title); // TODO alterar para id
		notPresentedPublicationDB.close();
		return e;
	}

	/**
	 * Checks if the publication is in favorites
	 * @return true if the publication is in favorites; false otherwise
	 */
	private boolean isInFavorites() {
		if (e == null)
			return false;
		favPubsDB.open();
		FavPublication pub = favPubsDB.getFavoritePublication(e.getPublicationID());
		favPubsDB.close();
		return pub != null;
	}
	
	/**
	 * Add the current publication to favorites
	 */
	private void addPublicationToFavorites() {
		favPubsDB.open();
		Log.i("NOME DA PUBLICAÇÃO",e.getTitle());
		Log.i("ID DA PUBLICAÇÃO",""+e.getPublicationID());
		favPubsDB.createFavPublication(e.getPublicationID(), AuthInfo.username);
		favPubsDB.close();
	}
	
	/**
	 * Removes the current publication from favorites
	 */
	private void removePublicationFromFavorites() {
		favPubsDB.open();
		FavPublication favpub = favPubsDB.getFavoritePublication(e.getPublicationID());
		favPubsDB.deleteFavPublication(favpub);
		favPubsDB.close();
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
		getMenuInflater().inflate(R.menu.publication_details, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		if (isInFavorites()) {
			MenuItem menuItem = menu.findItem(R.id.action_favorited);
			menuItem.setIcon(R.drawable.favorited);
		}
		return super.onPrepareOptionsMenu(menu);
			
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
		case R.id.action_favorited: // add/remove to favorites button clicked
			if (!isInFavorites()) {
				addPublicationToFavorites();
				item.setIcon(R.drawable.favorited);
			}
			else {
				removePublicationFromFavorites();
				item.setIcon(R.drawable.unfavorited);
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
