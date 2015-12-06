package org.ramonaza.unofficialazaapp.colorbook.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.colorbook.backend.ColorbookConstants;

/**
 * Created by ilan on 12/2/15.
 */
public class ColorbookTableOfContentsDialog extends DialogFragment {

    private static final int BEGINNING = 0;
    private static final int HISTORY_AND_STRUCTURE = 1;
    private static final int PROGRAMMING = 2;
    private static final int RITUALS_AND_PROCEDURES = 3;
    private static final int OTHER = 4;
    private BookCallbacks callbackFrag;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            callbackFrag = (BookCallbacks) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Target fragment must implement BookCallbacks");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.TableOfContentsMenuButton);
        builder.setItems(R.array.TableOfContents, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case BEGINNING:
                        callbackFrag.setPage(ColorbookConstants.BEGINNING);
                        break;
                    case HISTORY_AND_STRUCTURE:
                        callbackFrag.setPage(ColorbookConstants.HISTORY_AND_STRUCTURE);
                        break;
                    case PROGRAMMING:
                        callbackFrag.setPage(ColorbookConstants.PROGRAMMING);
                        break;
                    case RITUALS_AND_PROCEDURES:
                        callbackFrag.setPage(ColorbookConstants.RITUALS_AND_PROCEDURES);
                        break;
                    case OTHER:
                        callbackFrag.setPage(ColorbookConstants.OTHER);
                        break;
                }
            }
        });
        return builder.create();
    }

    public interface BookCallbacks {
        public void setPage(int page);
    }
}
