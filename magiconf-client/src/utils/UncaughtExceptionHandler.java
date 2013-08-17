package utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.example.magiconf_client.LoginActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Process;

/**
 * @author Alexandre
 *
 * This class represents an exception handler to avoid the application from crashing when there's an unhandled exception
 */
public class UncaughtExceptionHandler implements
		java.lang.Thread.UncaughtExceptionHandler {
	
	private Context context;
	
	public UncaughtExceptionHandler(Context context) {
		this.context = context;
	}

	@Override
	public void uncaughtException(Thread thread, Throwable exception) {
		StringWriter stackTrace = new StringWriter();
		exception.printStackTrace(new PrintWriter(stackTrace));
        System.err.println(stackTrace);
        
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(LoginActivity.UNCAUGHT_EXCEPTION, true);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		context.startActivity(intent);
		
        Process.killProcess(Process.myPid());
        System.exit(10);

	}

}
