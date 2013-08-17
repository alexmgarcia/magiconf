package utils;

import java.sql.Date;

import org.json.JSONException;
import org.json.JSONObject;

import tables.Contact;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.magiconf_client.CheckInActivity;
import com.example.magiconf_client.DialogActivity;
import com.example.magiconf_client.MainMenu;
import com.example.magiconf_client.NotificationsActivity;
import com.example.magiconf_client.R;
import com.example.magiconf_client.StartContactExchangeActivity;
import com.example.magiconf_client.R.drawable;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import dataSources.NotificationDataSource;

public class GcmBroadcastReceiver extends BroadcastReceiver {
	static final String TAG = "GCMDemo";
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    Context ctx;
    NotificationDataSource notificationDB;
    
    private static final String CONTACT_EXCHANGE_REQUEST_REGEX = "^\\[CONTACT_EXCHANGE_REQUEST\\]Participant_name:(.+)";
    private static final String CONTACT_EXCHANGE_REQUEST_REGEX_REPLACE = "^\\[CONTACT_EXCHANGE_REQUEST\\]Participant_name:";
    private static final String CONTACT_EXCHANGE_DATA_REGEX = "^\\[CONTACT_EXCHANGE_DATA\\](.+)";
    private static final String CONTACT_EXCHANGE_DATA_REGEX_REPLACE = "^\\[CONTACT_EXCHANGE_DATA\\]";
    private static final String EVENT_UPDATE = "[EVENT_UPDATE]";
    /*
     * [NOTIFICATION], [CONTACT_EXCHANGE_REQUEST], [CONTACT_EXCHANGE_DATA], [NEW_DATA]
     * 
     * 
     * {"contact": 
							{"username" : pin_owner.username,
                           "name" : pin_owner.name,
                           "work_place" : pin_owner.work_place,
                           "photo" : pin_owner.photo,
                           "country" : pin_owner.country,
                           "phone_number" : pin_owner.phone_number,
                           "email" : pin_owner.email,
                           "facebook_url" : pin_owner.facebook_url,
                           "linkedin_url" : pin_owner.linkedin_url						   
						   } 
						}
     */
    private static final String TITLE = "Title:";
    private static final String DESCRIPTION = "Description:";
    private static final String PARTICIPANT_NAME = "Participant_name:";
    private static final String PARTICIPANT_USERNAME = "Participant_username:";
    private static final String SEPARATOR = "\\|";
    private static final String MSG = "msg";
    
    
    
    
	@Override
	public void onReceive(Context context, Intent intent) {
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
		ctx = context;
		notificationDB = new NotificationDataSource(context);
		notificationDB.open();
		String messageType = gcm.getMessageType(intent);
		if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType))
			sendNotification("ouh", "Send error: " + intent.getExtras().toString());
		else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType))
			sendNotification("ouh", "Deleted messages on server: " +
					intent.getExtras().toString());
		else {
			String msg = intent.getExtras().getString(MSG);
			Log.i("Message",msg);
			if (msg.matches(CONTACT_EXCHANGE_REQUEST_REGEX)) { // Contact exchange request
				String[] msgSplitted = msg.replaceFirst(CONTACT_EXCHANGE_REQUEST_REGEX_REPLACE, "").split(SEPARATOR);
				String participantName = msgSplitted[0].replaceFirst(PARTICIPANT_NAME, "");
				String participantUsername = msgSplitted[1].replaceFirst(PARTICIPANT_USERNAME, "");
				Intent dialogIntent = new Intent(ctx, DialogActivity.class);
				dialogIntent.putExtra(DialogActivity.TITLE, ctx.getString(R.string.contact_exchange_request_title));
				dialogIntent.putExtra(DialogActivity.TEXT, participantName + " " + ctx.getString(R.string.contact_exchange_request_text));
				dialogIntent.putExtra(DialogActivity.USERNAME, participantUsername);
				dialogIntent.putExtra(DialogActivity.TYPE, DialogActivity.YES_NO_DIALOG);
				dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				ctx.startActivity(dialogIntent);
			}
			else if (msg.matches(CONTACT_EXCHANGE_DATA_REGEX)) { // Contact exchange data
				try {
					JSONObject contact = new JSONObject(msg.replaceFirst(CONTACT_EXCHANGE_DATA_REGEX_REPLACE, ""));
					ContactExchanger contactExchanger = new ContactExchanger(ctx);
					Contact c = contactExchanger.addContactToDatabase(contact);
					Intent dialogIntent = new Intent(ctx, DialogActivity.class);
					dialogIntent.putExtra(DialogActivity.TITLE, ctx.getString(R.string.contact_added_title));
					dialogIntent.putExtra(DialogActivity.TEXT, c.getName() + " " + ctx.getString(R.string.contact_added_text));
					dialogIntent.putExtra(DialogActivity.TYPE, DialogActivity.ALERT_DIALOG);
					dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					ctx.startActivity(dialogIntent);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			else if(msg.contains(EVENT_UPDATE)){ //EVENT_UPDATE
				Log.i("message_event_update",msg);
				String barSplit[] = msg.split(SEPARATOR);
				String type = barSplit[1].split(":")[1];
				String id = barSplit[2].split(":")[1];
				Log.i("event type",type);
				Log.i("django id ",id);
				new EventUpdater(ctx).updateEvent(id, type);
			
			}
			else 	{
				String[] msgSplitted = msg.split(SEPARATOR);
				Log.i("title", msgSplitted[0]);
				Log.i("description", msgSplitted[1]);
				String title = msgSplitted[0].replaceFirst(TITLE, "");
				String description = msgSplitted[1].replaceFirst(DESCRIPTION, "");
				notificationDB.createNotification(title, description, new Date(new java.util.Date().getTime()));
				sendNotification(title, description);
				Log.i("received", intent.getExtras().getString(MSG));
			}
		}
		Log.i("ouh", "ouhh");
		setResultCode(Activity.RESULT_OK);

	}
	
	
	private void sendNotification(String title, String msg) {
		mNotificationManager = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		
		PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, new Intent(ctx, NotificationsActivity.class), 0);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx).setSmallIcon(drawable.small_logo).setContentTitle(title);
		mBuilder.setAutoCancel(true);
		mBuilder.setContentText(msg).setNumber(notificationDB.getTotalNotifications());
		mBuilder.setContentIntent(contentIntent);
		notificationDB.close();
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
		
	}
	

}
