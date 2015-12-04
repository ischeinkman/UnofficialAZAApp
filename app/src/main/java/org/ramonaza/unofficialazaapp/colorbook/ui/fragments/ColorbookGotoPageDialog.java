package org.ramonaza.unofficialazaapp.colorbook.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.colorbook.backend.ColorbookConstants;

/**
 * Created by ilan on 12/2/15.
 */
public class ColorbookGotoPageDialog extends DialogFragment {

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
        View promptView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_colorbook_gotopage, null);
        final EditText pageInp = (EditText) promptView.findViewById(R.id.PageNum);
        builder.setView(promptView);
        builder.setPositiveButton("Go", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String inpText = pageInp.getText().toString();
                int page;
                try {
                    page = Integer.parseInt(inpText);
                } catch (NumberFormatException e) {
                    Toast.makeText(getActivity(), "Please Enter An Integer", Toast.LENGTH_SHORT);
                    return;
                }
                int displacement = ColorbookConstants.INIT_DISPLACEMENT;
                callbackFrag.setPage(page + displacement);
            }
        });
        return builder.create();
    }

    public interface BookCallbacks {
        public void setPage(int page);
    }
}
