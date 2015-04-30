/**
 *     JapaneseDictionary - an JMDict browser for Android
 Copyright (C) 2013 Jaroslav Klech

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cz.muni.fi.japanesedictionary.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import cz.muni.fi.japanesedictionary.R;

/**
 * Fragment alert dialog for displaying error messages and promt to user.
 * @author Jaroslav Klech
 *
 */
public class AnkiFragmentAlertDialog extends DialogFragment {


    /**
     * Creates new instance of MyFragmentAlertDialog
     *
     * @param title title to be set for AlertDialog
     * @param message message to be set for AlertDialog
     * @param negative determins whether dialog has only storno button
     * @return MyFragmentAlertDialog new instance of MyFragmentAlertDialog
     */
    public static AnkiFragmentAlertDialog newInstance(int title, int message,
                                                            boolean negative) {
        AnkiFragmentAlertDialog frag = new AnkiFragmentAlertDialog();
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
                            }
                        }).create();
        if (!onlyNegative) {
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                    getString(R.string.download),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("market://details?id=com.ichi2.anki"));
                            startActivity(intent);
                        }
                    });
        }

        return alertDialog;

    }
}