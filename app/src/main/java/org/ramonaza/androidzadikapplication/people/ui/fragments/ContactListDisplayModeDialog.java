package org.ramonaza.androidzadikapplication.people.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import org.ramonaza.androidzadikapplication.R;
import org.ramonaza.androidzadikapplication.people.backend.ContactListConstants;

/**
 * Created by ilan on 12/2/15.
 */
public class ContactListDisplayModeDialog extends DialogFragment {

    private static final int ALEPHS = 0;
    private static final int ADVISORS = 1;
    private static final int ALUMNI = 2;
    private ContactListCallbacks callbackFrag;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            callbackFrag = (ContactListCallbacks) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Target fragment must implement ContactListCallbacks");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(R.array.DisplayMode, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                switch (i) {
                    case ALEPHS:
                        callbackFrag.setSortingQuery(ContactListConstants.ALEPHS_QUERY, ContactListConstants.NAME_SORT, "Alephs");
                        break;
                    case ADVISORS:
                        callbackFrag.setSortingQuery(ContactListConstants.ADVISORS_QUERY, ContactListConstants.NAME_SORT, "Advisors");
                        break;
                    case ALUMNI:
                        callbackFrag.setSortingQuery(ContactListConstants.ALUMNI_QUERY, ContactListConstants.YEAR_SORT, "Alumni");
                        break;
                }
            }
        });
        return builder.create();
    }

    public interface ContactListCallbacks {
        public void setSortingQuery(String[] query, String sortBy, String name);
    }
}
