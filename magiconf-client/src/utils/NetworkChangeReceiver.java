package utils;

import com.example.magiconf_client.DialogActivity;
import com.example.magiconf_client.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetworkChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!ValuesHelper.isNetworkAvailable(context)) {
			if (ContactExchanger.contactExchangeStarted) {
				Intent dialogIntent = new Intent(context, DialogActivity.class);
				dialogIntent.putExtra(DialogActivity.TITLE, context.getString(R.string.no_internet_title));
				dialogIntent.putExtra(DialogActivity.TEXT, context.getString(R.string.no_internet_text_contact_exchange));
				dialogIntent.putExtra(DialogActivity.TYPE, DialogActivity.ALERT_DIALOG);
				dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(dialogIntent);
			}
		}
		
	}

}
