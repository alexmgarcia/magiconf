package com.example.magiconf_client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tables.Member;
import utils.Triple;
import adapters.MembersListViewAdapter;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;

import dataRetrieving.AuthInfo;
import dataRetrieving.CityData;
import dataRetrieving.HotelData;
import dataRetrieving.MemberData;
import dataRetrieving.RestaurantData;
import dataRetrieving.SightData;
import dataSources.MemberDataSource;

/**
 * @author José
 *
 * This class represents the Sights Search Results activity
 */
public class MembersActivity extends Activity{

	private MemberDataSource membersDB = new MemberDataSource(this);
	
	public static final String MEMBER_ID = "memberID";

    ExpandableListView membersView;
    
    List<String> headers;
    List<Member> cOrg;
    List<Member> cProg;
    List<Member> cCoord;
    
	/**
	 * Updates all the views present in this activity
	 */
	private void updateViews() {

		setContentView(R.layout.activity_members);

		
		if(cOrg.size()==0 && cProg.size()==0 && cCoord.size()==0){
			TextView view = (TextView) findViewById(R.id.no_results);
			view.setText(this.getString(R.string.NO_INFO));			
		}else{
			
	        membersView = (ExpandableListView) findViewById(R.id.members_view);
	        
	        headers = new ArrayList<String>();
	            
	        Map<String, List<Member>> resultsMap = new LinkedHashMap<String, List<Member>>();
	        
	        if(cProg.size()>0){
	        	headers.add(this.getString(R.string.PROG_COM));
	        	resultsMap.put(this.getString(R.string.PROG_COM), cProg);
	        }
	        
	        if(cOrg.size()>0){
	        	headers.add(this.getString(R.string.ORG_COM));
	        	resultsMap.put(this.getString(R.string.ORG_COM), cOrg);
	        }
	        
	        if(cCoord.size()>0){
	        	headers.add(this.getString(R.string.COORD_COM));
	        	resultsMap.put(this.getString(R.string.COORD_COM), cCoord);
	        }
	        
	        final MembersListViewAdapter membersViewAdapter = new MembersListViewAdapter(this, headers, resultsMap);
	        membersView.setAdapter(membersViewAdapter);
	 
	        membersView.setOnChildClickListener(new OnChildClickListener() {
	
	        	public boolean onChildClick(ExpandableListView parent, View v,
	                    int groupPosition, int childPosition, long id) {
	            	
	                final Member m = (Member) membersViewAdapter.getChild(
	                        groupPosition, childPosition);
	                
	                final Long idElement = m.getId();

	                Intent intent = new Intent(MembersActivity.this, MemberActivity.class);
	                intent.putExtra(MEMBER_ID, idElement);
	    				 
	    			startActivity(intent);
	
	                return true;
	            }
	        });
		}
			
	}
	
	/**
	 * @author José
	 * Asynchronous REST GET request
	 */
	private class RESTGetTask extends AsyncTask<String, Void, List<JSONObject>> {
		protected List<JSONObject> doInBackground(String... params) {

			List<JSONObject> list = MemberData.downloadMembers(AuthInfo.username, AuthInfo.password);

			Log.i("ASYNC_TASK MEMBERS", "Number requests: "+list.size());

			return list;
		}

		
		protected void onPostExecute(List<JSONObject> result) {
			Iterator<JSONObject> it = result.iterator();
			JSONObject aux = null;
			while(it.hasNext()){
				Log.i("POST_EXECUTE", "On postExecute");
				aux =  it.next();
				JSONArray data;
				try {
					data = aux.getJSONArray("objects");
					MemberData.createMembers(data,membersDB,MembersActivity.this);
					
					membersDB.open();
					Triple<List<Member>,List<Member>,List<Member>> allMembers = membersDB.getAllMembersByRole();

					cCoord = allMembers.getFirst();
					cOrg = allMembers.getSecond();
					cProg = allMembers.getThird();
					
					membersDB.close();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				updateViews();
				
			}
				
		}
		
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
		membersDB.open();
		
		Triple<List<Member>,List<Member>,List<Member>> allMembers = membersDB.getAllMembersByRole();

		cCoord = allMembers.getFirst();
		cOrg = allMembers.getSecond();
		cProg = allMembers.getThird();
		
		membersDB.close();
		
		if(cCoord.size()==0 && cOrg.size()==0 && cProg.size()==0){
			new RESTGetTask().execute();
		}else updateViews();
		
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

	private void createNewMember(JSONArray data) {
		Member aux;
		Log.i("CREATE MEMBERS", "Start adding");
		membersDB.open();
		Log.i("CREATE MEMBERS", ""+data.length());
		for(int i = 0 ; i < data.length() ; i++){
			try {
				JSONObject k = data.getJSONObject(i);
				
				String email= k.getString("email");
				if(!membersDB.hasMember(email)){
					aux = membersDB.createMember(email, k.getString("name"), k.getString("work_place"), k.getString("country"), k.getString("photo"), k.getString("role"));			
					Log.i("CREATE MEMBER", "Creating Member"+aux.toString());
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		membersDB.close();
		
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
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.members, menu);
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
