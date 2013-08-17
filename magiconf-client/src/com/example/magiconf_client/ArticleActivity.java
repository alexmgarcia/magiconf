package com.example.magiconf_client;

import java.util.ArrayList;
import java.util.Iterator;

import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import tables.Article;
import tables.Author;
import tables.FavPublication;
import utils.ValuesHelper;

import dataRetrieving.ArticleData;
import dataRetrieving.AuthInfo;
import dataSources.ArticleDataSource;
import dataSources.FavPublicationDataSource;

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
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

/**
 * @author José
 *
 * This classe represents the Article Activity
 */
public class ArticleActivity extends Activity {

	public final static String AUTHOR_ID="authorID";
	public final static String AUTHOR_EMAIL ="authorEmail";
	
	private ArticleDataSource articleDB = new ArticleDataSource(this); // the database
	private FavPublicationDataSource favPubsDB = new FavPublicationDataSource(this);
	private Article a;
	
	private void updateViews(final Article result) { //TODO: Adicionar a favoritos
		Log.i("ArticleActivity", ""+result);
		setContentView(R.layout.activity_article);
		TextView textView = (TextView) ArticleActivity.this.findViewById(R.id.article_title);
		Log.i("ArticleActivity", ""+textView);
		textView.setText(result.getTitle());
		textView = (TextView) ArticleActivity.this.findViewById(R.id.article_abstract);
		textView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TextView textView = (TextView) v;
				textView.setText(result.getAbstract());
				textView.setMaxLines(Integer.MAX_VALUE);
			}
		});
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
				Intent intent = new Intent(ArticleActivity.this, AuthorActivity.class);
				intent.putExtra(AUTHOR_EMAIL, author.getEmail()); // TODO this is not finished. It will be the author id of a selected item in the list
				startActivity(intent);
				
				// TODO Open the author activity.
			}
		});
		
		// This allows to have a list inside a scrollview
		Helper.getListViewSize(authorsList);
		
		Log.i("Result", ""+result.getLastModified());
		
	   
	}
	
	/**
	 * Inserts a new Article into the database
	 * @param result object to use to populate data
	 * @return the new Article
	 */
	private Article createNewArticle(Article result) {
		articleDB.open();
		Article newArt = articleDB.createArticle(result.getTitle(), result.getAbstract(), result.getAuthors());
		
		articleDB.close();
		return newArt;

	}
	
	private Article getArticle(Long id) {
		articleDB.open();
		Article a = articleDB.getArticle(id); 
		articleDB.close();
		return a;
	}
	
	private Article getArticleByParent(Long id){
		articleDB.open();
		Article a = articleDB.getArticleByParent(id); 
		articleDB.close();
		return a;		
	}
	
	private class RESTGetTask extends AsyncTask<String, Void, Article> {
		protected Article doInBackground(String... params) {
			/*RestTemplate restTemplate = new RestTemplate();
		    restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
		     
		    Article art = restTemplate.getForObject(params[0], Article.class);
		    return art;*/
			
			//TODO: tirar este context - ArticleActivity.this
			return ArticleData.downloadArticleData(params[0], AuthInfo.username, AuthInfo.password, articleDB,ArticleActivity.this);
		}
		
		protected void onPostExecute(Article result) {
			// This is new data, so let's populate the data in views and insert it into the database
			//Article art = createNewArticle(result);
			a = result;
			updateViews(result);
		}
		
	}

	//TODO: Correcto?
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
		Intent intent = getIntent();
		Long artId = intent.getLongExtra(ValuesHelper.PUBLICATION_ID, 2); //TODO
		//String artTitle = intent.getStringExtra(TalkSessionActivity.ARTICLE_NAME);
		Boolean isParentID = intent.getBooleanExtra(ValuesHelper.IS_PARENT_ID, false);
		
		
		// Get an existing article
		
		if(isParentID){
			a = getArticleByParent(artId);
		}else{
			a = getArticle(artId);
		}
		
		Log.i("ArticleActivity", ""+articleDB);
		Log.i("ArticleActivity", ""+Long.valueOf(1));
		
		
		if (a == null) // it doesn't exist into the local database
			new RESTGetTask().execute("" + artId);
		else			
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
		getMenuInflater().inflate(R.menu.article, menu);
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
		case R.id.action_about:
			intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		if (isInFavorites()) {
			MenuItem menuItem = menu.findItem(R.id.action_favorited);
			menuItem.setIcon(R.drawable.favorited);
		}
		return super.onPrepareOptionsMenu(menu);
			
	}
	
	/**
	 * Checks if the publication is in favorites
	 * @return true if the publication is in favorites; false otherwise
	 */
	private boolean isInFavorites() {
		if (a == null)
			return false;
		favPubsDB.open();
		FavPublication pub = favPubsDB.getFavoritePublication(a.getArticleId());
		favPubsDB.close();
		return pub != null;
	}
	
	/**
	 * Add the current publication to favorites
	 */
	private void addPublicationToFavorites() {
		favPubsDB.open();
		favPubsDB.createFavPublication(a.getArticleId(), AuthInfo.username);
		favPubsDB.close();
	}
	
	/**
	 * Removes the current publication from favorites
	 */
	private void removePublicationFromFavorites() {
		favPubsDB.open();
		FavPublication favpub = favPubsDB.getFavoritePublication(a.getArticleId());
		favPubsDB.deleteFavPublication(favpub);
		favPubsDB.close();
	}

}
