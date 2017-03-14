package org.ramonaza.androidzadikapplication.settings.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import org.ramonaza.androidzadikapplication.R;
import org.ramonaza.androidzadikapplication.helpers.backend.ChapterPackHandlerSupport;
import org.ramonaza.androidzadikapplication.settings.ui.activities.SettingsActivity;

import java.io.File;
import android.Manifest;

/**
 * Created by ilan on 11/28/15.
 */
public class ChapterPackSelectorFragment extends DialogFragment {

    File[] fileOptions;
    int currentSelection;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        currentSelection = -1;
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, SettingsActivity.FILE_READ);
            setShowsDialog(false);
            dismiss();
            return null;
        }
        fileOptions = ChapterPackHandlerSupport.getOptions();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (fileOptions == null) {

            builder.setTitle(R.string.chapter_pack_not_found_title);
        } else {
            CharSequence[] titles = new CharSequence[fileOptions.length];
            for (int i = 0; i < fileOptions.length; i++) {
                titles[i] = fileOptions[i].getName();
            }
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
        }

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
