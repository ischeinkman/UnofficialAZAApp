package org.ramonaza.unofficialazaapp.settings.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.helpers.backend.ChapterPackHandlerSupport;

import java.io.File;

/**
 * Created by ilan on 11/28/15.
 */
public class ChapterPackSelectorFragment extends DialogFragment {

    File[] fileOptions;
    int currentSelection;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        currentSelection = -1;
        fileOptions = ChapterPackHandlerSupport.getOptions();
        CharSequence[] titles = new CharSequence[fileOptions.length];
        for (int i = 0; i < fileOptions.length; i++) {
            titles[i] = fileOptions[i].getName();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.chapter_pack_dialog_title);
        builder.setSingleChoiceItems(titles, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                currentSelection = i;
            }
        });
        builder.setPositiveButton(R.string.chapter_pack_dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (currentSelection >= 0 && currentSelection < fileOptions.length) {
                    ChapterPackHandlerSupport.getChapterPackHandler(getActivity(), fileOptions[currentSelection]);
                }
                currentSelection = -1;
                dismiss();
            }
        });
        builder.setNegativeButton(R.string.chapter_pack_dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                currentSelection = -1;
                dismiss();
            }
        });
        return builder.create();
    }
}
