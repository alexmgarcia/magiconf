package com.example.magiconf_client;

import java.text.ParseException;
import java.util.LinkedList;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import dataRetrieving.AuthInfo;
import dataRetrieving.CityData;
import dataRetrieving.ConferenceData;
import dataRetrieving.ContactData;
import dataRetrieving.HotelData;
import dataRetrieving.KeynoteSessionData;
import dataRetrieving.MemberData;
import dataRetrieving.NotPresentedPublicationData;
import dataRetrieving.ParticipantData;
import dataRetrieving.PosterData;
import dataRetrieving.PosterSessionData;
import dataRetrieving.RestaurantData;
import dataRetrieving.SightData;
import dataRetrieving.SocialEventData;
import dataRetrieving.SponsorData;
import dataRetrieving.TalkSessionData;
import dataRetrieving.WorkshopData;
import dataSources.ConferenceDataSource;
import dataSources.ContactDataSource;
import dataSources.EventDataSource;
import dataSources.KeynoteSessionDataSource;
import dataSources.MemberDataSource;
import dataSources.NotPresentedPublicationDataSource;
import dataSources.ParticipantContactDataSource;
import dataSources.ParticipantDataSource;
import dataSources.ParticipantSessionDataSource;
import dataSources.PosterSessionDataSource;
import dataSources.SessionDataSource;
import dataSources.SightsDataSource;
import dataSources.SponsorDataSource;
import dataSources.TalkDataSource;
import dataSources.WorkshopDataSource;

import tables.KeynoteSession;
import tables.Participant;
import tables.ParticipantSession;
import tables.Session;
import utils.APIResponses;
import utils.AppPreferences;
import utils.ContactExchanger;
import utils.GCMHelper;
import utils.MD5Calc;
import utils.RestClient;
import utils.Triple;
import utils.ValuesHelper;
import fragments.ForgotPasswordDialogFragment;
import fragments.ForgotPasswordDialogFragment.ForgotPasswordDialogListener;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.SQLException;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.SyncStateContract.Constants;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends FragmentActivity implements  ForgotPasswordDialogListener {

	private SessionDataSource sessionDB = new SessionDataSource(this);
	private ParticipantDataSource participantDB = new ParticipantDataSource(this);
	private ParticipantSessionDataSource participantSessionDB = new ParticipantSessionDataSource(this);
	private ParticipantContactDataSource participantContactDB = new ParticipantContactDataSource(this);
	private ContactDataSource contactsDB = new ContactDataSource(this);
	private WorkshopDataSource workshopDB = new WorkshopDataSource(this);
	private ConferenceDataSource conferenceDB = new ConferenceDataSource(this);
	private TalkDataSource talksessionDB = new TalkDataSource(this);
	private KeynoteSessionDataSource keynoteSessionDB = new KeynoteSessionDataSource(this);
	private PosterSessionDataSource postersessionDB = new PosterSessionDataSource(this);
	private SponsorDataSource sponsorDB = new SponsorDataSource(this);
	private MemberDataSource memberDB = new MemberDataSource(this);
	private SightsDataSource sightDB = new SightsDataSource(this);
	private NotPresentedPublicationDataSource notPresentedPubDB = new NotPresentedPublicationDataSource(this);
	private EventDataSource eventDB = new EventDataSource(this);
	
	/**
	 * The default username to populate the email field with.
	 */
	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";
	public static final String LOGIN_TYPE = "Login";
	public static final String DOWNLOAD_TYPE = "Download";
	public static final String UNCAUGHT_EXCEPTION = "uncaughtExceptionId";

	private AppPreferences appPrefs;

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for username and password at the time of the login attempt.
	private String mUsername;
	private String mPassword;

	// UI references.
	private EditText mUsernameView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private View mForgotPassword;
	private TextView mLoginStatusMessageView;
	private boolean noInternet = false;
	private Context context;
	
	private void checkUncaughtException() {
		Intent intent = getIntent();
		boolean uncaughtException = intent.getBooleanExtra(UNCAUGHT_EXCEPTION, false);
		if (uncaughtException) {
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle(getString(R.string.error_title));
			alertDialog.setMessage(getString(R.string.error_ocurred));
			alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok_button), new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						;
					}
				});
			alertDialog.show();
		}
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		GCMHelper.registered = false;
		checkUncaughtException();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		checkUncaughtException();
		Thread.setDefaultUncaughtExceptionHandler(new utils.UncaughtExceptionHandler(this));
		GCMHelper.registered = false;
		appPrefs = new AppPreferences(getApplicationContext());

		setContentView(R.layout.activity_login);
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		// Set up the login form.
		mUsername = getIntent().getStringExtra(EXTRA_EMAIL);
		mUsernameView = (EditText) findViewById(R.id.username);
		mUsernameView.clearFocus();
		
		String currentUsername = getCurrentUsername();
		if(currentUsername == null)
			mUsernameView.setText(mUsername);
		else
			mUsernameView.setText(currentUsername);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setTypeface(Typeface.DEFAULT);
		mPasswordView.setTransformationMethod(new PasswordTransformationMethod());
		mPasswordView.clearFocus();
		mPasswordView
		.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id,
					KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					attemptLogin();
					return true;
				}
				return false;
			}
		});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					
					
					@Override
					public void onClick(View view) {
						InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					    mgr.hideSoftInputFromWindow(mPasswordView.getWindowToken(), 0);
					    mgr.hideSoftInputFromWindow(mUsernameView.getWindowToken(), 0);
						attemptLogin();
					}
				});

		//Set up forgot password form
		mForgotPassword = findViewById(R.id.login_forgot_password);

		mForgotPassword.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				FragmentManager fm = getSupportFragmentManager();
				new ForgotPasswordDialogFragment().show(fm, "ForgotPasswordDialog");
			}
		});

		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction("com.package.ACTION_LOGOUT");
		sendBroadcast(broadcastIntent);
	}


	private String getCurrentUsername() {
		
		participantSessionDB.open();
		ParticipantSession ps = participantSessionDB.getFirstSession();
		if(ps != null)
			return ps.getUsername();
		participantSessionDB.close();
		return null;
	}


	// The dialog fragment receives a reference to this Activity through the
	// Fragment.onAttach() callback, which it uses to call the following methods
	// defined by the ForgotPasswordDialogFragment.ForgotPasswordDialogListener interface
	@Override
	public void onDialogPositiveClick(DialogFragment dialog,final String email) {



		// User touched the dialog's positive button

		// generate new password and send username on server ofcourse
		// Delete any session with this username
		/*Toast.makeText(LoginActivity.this,
				"LoginActivity is clicked!\nUsername: "+email, Toast.LENGTH_SHORT).show();*/
		Log.i("DEBUG",""+ValuesHelper.isNetworkAvailable(LoginActivity.this));
		if(!ValuesHelper.isNetworkAvailable(LoginActivity.this)){
			AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
			Log.i("DEBUG", "ALERT WILL RUN 2");
			alertDialog.setTitle(getString(R.string.error));
			alertDialog.setMessage(getString(R.string.check_internet));
			alertDialog.setIcon(R.drawable.alerts_and_states_error);
			alertDialog.setButton(Dialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					//do nothing
				}
			});
			alertDialog.show();
			return;
		}
		this.context = LoginActivity.this;

		new AsyncTask<Void, Void, JSONObject>() {

			@Override
			protected JSONObject doInBackground(Void... arg0) {


				String url = ValuesHelper.SERVER_API_ADDRESS +"/" + "participant"+ "/" + ValuesHelper.FORGOT_PASSWORD_RESOURCE;
				Log.i("FORGOT_PASSWORD",url);
				RestClient client = new RestClient();
				client.addPostParam(new BasicNameValuePair(ContactExchanger.EMAIL_KEY,email));
				JSONObject response = client.makePostRequest(url);

				return response;
			}

			@Override
			protected void onPostExecute(JSONObject response) {
				String message = LoginActivity.this.getString(R.string.error_ocurred);
				if(response!= null && response.has("request")){
					try {
						if(response.getString("request").equals(APIResponses.DATA_OK_SENT)){

							participantSessionDB.open();
							sessionDB.open();
							participantDB.open();
							Participant p = participantDB.getParticipantByEmail(email);
							if(p != null){

								ParticipantSession ps = participantSessionDB.getSessionByUsername(p.getUsername());
								if(ps != null){
									participantSessionDB.deleteParticipantSession(p.getUsername(),ps.getSessionId() );
									sessionDB.deleteSession(ps.getSessionId());
								}

							}
							participantSessionDB.close();
							sessionDB.close();
							participantDB.close();

							message = LoginActivity.this.getString(R.string.pw_reset_email_sent);

						}
						else{
							//Fallhou. Email inválido
							Log.i("FORGOT_PASSWORD","Falhou");
							message = LoginActivity.this.getString(R.string.invalid_email);
						}
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();

			}

		}.execute(null, null, null);


	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_forgot_password:
			FragmentManager fm = getSupportFragmentManager();
			new ForgotPasswordDialogFragment().show(fm, "ForgotPasswordDialog");
			return true;
		default: break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid username, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mUsernameView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mUsername = mUsernameView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid username address.
		if (TextUtils.isEmpty(mUsername)) {
			mUsernameView.setError(getString(R.string.error_field_required));
			focusView = mUsernameView;
			cancel = true;
		} else if (mUsername.contains("@")) {
			mUsernameView.setError(getString(R.string.error_invalid_username));
			focusView = mUsernameView;
			cancel = true;
		}

		if (cancel) {
			focusView.requestFocus();
		} else {
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask(this);
			mAuthTask.execute((Void) null);
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
			.alpha(show ? 1 : 0)
			.setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mLoginStatusView.setVisibility(show ? View.VISIBLE
							: View.GONE);
				}
			});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
			.alpha(show ? 0 : 1)
			.setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mLoginFormView.setVisibility(show ? View.GONE
							: View.VISIBLE);
				}
			});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Inserts a new session into the database
	 * @param result object to use to populate data
	 * @return the new session
	 */
	private Session createNewSession(Session result) {
		sessionDB.open();
		Log.i("PASSWORD", result.getPassword() + "");
		Session session;
		try {
			
			session = sessionDB.createSession(result.getPassword());
		} catch (ParseException e) {
			return null;
		}

		sessionDB.close();
		return session;
	}

	/**
	 * Inserts a new participant-session into the database
	 * @param result object to use to populate data
	 * @return the new participant-session
	 */
	private void createNewParticipantSession(Participant p, Session session) {
		participantSessionDB.open();
		Log.i("ParticipantSession",p.getUsername() + " "+session.getId());
		participantSessionDB.createParticipantSession(p.getUsername(), session.getId());

		participantSessionDB.close();
	}

	private ParticipantSession getParticipantSession(String username) {

		participantSessionDB.open();
		ParticipantSession ps = participantSessionDB.getSessionByUsername(username);

		participantSessionDB.close();
		return ps;
	}

	private Participant getParticipant(String username) {
		participantDB.open();
		Participant p = participantDB.getParticipantByUsername(username);

		participantDB.close();

		return p;
	}

	private Session getSession() {
		sessionDB.open();
		Session s = ((LinkedList<Session>) (sessionDB.getAllSessions())).getFirst();

		sessionDB.close();

		return s;
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
		private boolean retrieveContacts = false;
		private Participant newPart;
		private LoginActivity parentActivity;

		public UserLoginTask(LoginActivity loginActivity) {
			this.parentActivity = loginActivity;
		}

		@Override
		protected Boolean doInBackground(Void... params) {


			String encodedPassword= MD5Calc.md5Java(mPassword);
			//Participant p = getParticipant(mUsername);
			Log.i("LOGIN_MD5", encodedPassword);
			ParticipantSession ps = getParticipantSession(mUsername);

			Log.i("LOGIN", "Null?: "+ (ps== null));

			if(ps != null){
				//This can never be null if ps != null!!
				Session s = getSession();
				if(!s.getPassword().equals(encodedPassword))
					return false;
			}
			else {
				//Send request to SI-M to validate the entered data
				if(!ValuesHelper.isNetworkAvailable(LoginActivity.this)){
					noInternet = true;
					return false;
				}
				String request = ValuesHelper.SERVER_API_ADDRESS_MEDIA + ValuesHelper.SERVER_VALIDATE_USER;

				request += "/" + mUsername + "/" + encodedPassword;
				Log.i("LOGIN", request);

				String result = RestClient.makeRequest(request);
				Log.i("LOGIN", result);
				if(result.contains(RestClient.SUCCESS)){
					Participant p = getParticipant(mUsername);
					if (p == null){

						p = ParticipantData.downloadParticipantData(LoginActivity.this,mUsername, encodedPassword,
								participantDB,contactsDB,participantContactDB,true);

					}
					Session s = new Session();
					s.setPassword(encodedPassword);
					if(createNewSession(s) != null){

						Log.i("LOGIN", "Session created!");
						createNewParticipantSession(p, s);
					}
					else
						return false;
				}
				else	
					return false;
			}
			AuthInfo.username = mUsername;
			AuthInfo.password = encodedPassword;

			//Downlaod participant contacts

			//Login ok! Now if it's the first time app runs, Download data

			//TODO:APAGAR ESTA LINHA
			if(ValuesHelper.debugDownload)
				appPrefs.saveFirstRun("");
			
			if(isFirstRun()){
				if(!ValuesHelper.isNetworkAvailable(LoginActivity.this)){
					noInternet = true;
					return false;
				}
				
				
				
				runOnUiThread(new Runnable() {
	                public void run() {
	                	TextView aux = (TextView) parentActivity.findViewById(R.id.login_status_message);
	                    aux.setText(R.string.login_progress_downloading);
	                }
	            });
				
				retrieveData();
				appPrefs.saveFirstRun("true");
				
				//showProgress(true);
			}

			return true;
		}

		private void retrieveData() {
			// TODO Auto-generated method stub
			
			ConferenceData.downloadConferenceData(LoginActivity.this,mUsername, AuthInfo.password,conferenceDB);
			SocialEventData.downloadAllSocialEvents(mUsername, AuthInfo.password, eventDB);
			WorkshopData.downloadAllWorkshopEvents(mUsername, AuthInfo.password, workshopDB);
			TalkSessionData.downloadAllTalkSessions(LoginActivity.this,mUsername, AuthInfo.password, talksessionDB);
			KeynoteSessionData.downloadAllKeynoteSessions(LoginActivity.this,mUsername, AuthInfo.password, keynoteSessionDB);
			PosterSessionData.downloadAllPosterSessions(mUsername, AuthInfo.password, postersessionDB,LoginActivity.this);
			SponsorData.downloadAllSponsors(LoginActivity.this,mUsername, AuthInfo.password,sponsorDB);

			MemberData.downloadAllMembers(LoginActivity.this,mUsername, AuthInfo.password, memberDB);
			CityData.downloadCityData(LoginActivity.this,mUsername, AuthInfo.password, conferenceDB);
			HotelData.downloadAllHotels(LoginActivity.this,mUsername, AuthInfo.password,sightDB);
			RestaurantData.downloadAllRestaurants(LoginActivity.this,mUsername, AuthInfo.password,sightDB);
			SightData.downloadAllSights(LoginActivity.this,mUsername, AuthInfo.password,sightDB);
			NotPresentedPublicationData.downloadAllNotPresentedPublications(mUsername, AuthInfo.password,notPresentedPubDB);
		}

		private boolean isFirstRun() {
			String firstRun = appPrefs.getFirstRun();
			if(firstRun.equals(""))
				return true;
			else return false;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			if (success) {
				finish();

				Intent intent = new Intent(LoginActivity.this, MainMenu.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			} else if(noInternet){
				noInternet= false;
				Log.i("DEBUG", "ALERT WILL RUN");
				AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
				Log.i("DEBUG", "ALERT WILL RUN 2");
				alertDialog.setTitle(getString(R.string.error));
				alertDialog.setMessage(getString(R.string.check_internet));
				alertDialog.setIcon(R.drawable.alerts_and_states_error);
				alertDialog.setButton(Dialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent i = new Intent();
						i.putExtra("TYPE", LOGIN_TYPE);
						setResult(RESULT_OK, i);
						finish();
					}
				});
				alertDialog.show();

			}
			else {
				mPasswordView
				.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}



	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
		super.onActivityResult(requestCode, resultCode, data); 
		Log.i("LOGIN", "Entered on onActivityResult");
		switch(resultCode) { 
		case (Activity.RESULT_OK): {
			String type = data.getStringExtra("type");
			if(type.equals(LOGIN_TYPE)){
				//Launch download thread

			}
			else if (type.equals(DOWNLOAD_TYPE)){
				/*Log.i("LOGIN", "Finished Download");
				Intent intent = new Intent(LoginActivity.this, MainMenu.class);
				startActivity(intent);*/
			}

		}
		}
	}



}
