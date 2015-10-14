package org.ramonaza.unofficialazaapp.bluebook.fragments;

import android.annotation.TargetApi;
import android.app.ListFragment;
import android.content.Context;
import android.graphics.Point;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.bluebook.other.BlueBookViewAdapter;

import java.io.IOException;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * interface.
 */
public class BlueBookPagesFragment extends ListFragment {

    PdfRenderer mPdfRenderer;
    private ParcelFileDescriptor mFileDescriptor;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BlueBookPagesFragment() {
    }

    public static BlueBookPagesFragment newInstance() {
        BlueBookPagesFragment fragment = new BlueBookPagesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);
        try {
            openRenderer(getActivity());
            setListAdapter(new BlueBookViewAdapter(getActivity(), R.layout.bluebook_adapter_layout, mPdfRenderer, new int[]{size.x, size.y}));
        } catch (IOException e) {
            Toast.makeText(getActivity(), "ERROR: " + e.getMessage(), Toast.LENGTH_LONG);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void openRenderer(Context context) throws IOException {
        mFileDescriptor = context.getAssets().openFd("BlueBook.pdf").getParcelFileDescriptor();
        mPdfRenderer = new PdfRenderer(mFileDescriptor);
    }
}
