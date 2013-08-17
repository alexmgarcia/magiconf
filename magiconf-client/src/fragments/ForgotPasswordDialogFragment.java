package fragments;


import com.example.magiconf_client.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class ForgotPasswordDialogFragment  extends DialogFragment{
	
	public interface ForgotPasswordDialogListener {
		public void onDialogPositiveClick(DialogFragment dialog, String email);
	}

	private ForgotPasswordDialogListener mListener;
	private EditText mEmailView;
	
	// Override the Fragment.onAttach() method to instantiate the ForgotPasswordDialoListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            
            mListener = (ForgotPasswordDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement ForgotPasswordDialogListener");
        }
    }

	public ForgotPasswordDialogFragment () {
		// Empty constructor required for DialogFragment
	}


	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		builder.setTitle(R.string.login_dialog_title);
		
		
		
		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		View v = inflater.inflate(R.layout.login_forgot_password_dialog, null);
		mEmailView = (EditText) v.findViewById(R.id.dialog_username);
		builder.setView(v)
		// Add action buttons
		.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				mListener.onDialogPositiveClick(ForgotPasswordDialogFragment.this, mEmailView.getText().toString());
			}
		})
		.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				ForgotPasswordDialogFragment.this.getDialog().cancel();
			}
		});      
		return builder.create();
	}

}
