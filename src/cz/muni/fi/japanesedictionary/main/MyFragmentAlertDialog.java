package cz.muni.fi.japanesedictionary.main;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockDialogFragment;

import cz.muni.fi.japanesedictionary.R;

public class MyFragmentAlertDialog extends SherlockDialogFragment {

		public static MyFragmentAlertDialog newInstance(int title, int message,
				boolean negative) {
			MyFragmentAlertDialog frag = new MyFragmentAlertDialog();
			Bundle args = new Bundle();
			args.putInt("title", title);
			args.putInt("message", message);
			args.putBoolean("onlyNegative", negative);
			frag.setArguments(args);
			return frag;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			int title = getArguments().getInt("title");
			int message = getArguments().getInt("message");
			boolean onlyNegative = getArguments().getBoolean("onlyNegative",
					false);

			AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
					.setIcon(R.drawable.alert_dialog_icon)
					.setTitle(title)
					.setMessage(message)
					.setNegativeButton(R.string.storno,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									((MainActivity) getActivity())
											.doNegativeClick();
								}
							}).create();
			if (!onlyNegative) {
				alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
						getString(R.string.download),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								((MainActivity) getActivity())
										.doPositiveClick();
							}
						});
			}

			return alertDialog;

		}
	}