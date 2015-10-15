package org.ramonaza.unofficialazaapp.colorbook.fragments;

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
import org.ramonaza.unofficialazaapp.colorbook.other.ColorBookViewAdapter;

import java.io.IOException;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * interface.
 */
public class ColorBookPagesFragment extends ListFragment {

    PdfRenderer mPdfRenderer;
    private ParcelFileDescriptor mFileDescriptor;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ColorBookPagesFragment() {
    }

    public static ColorBookPagesFragment newInstance() {
        ColorBookPagesFragment fragment = new ColorBookPagesFragment();
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
            setListAdapter(new ColorBookViewAdapter(getActivity(), R.layout.bluebook_adapter_layout, mPdfRenderer, new int[]{size.x, size.y}));
        } catch (IOException e) {
            Toast.makeText(getActivity(), "ERROR: " + e.getMessage(), Toast.LENGTH_LONG);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void openRenderer(Context context) throws IOException {
        mFileDescriptor = context.getAssets().openFd("ColorBook.pdf").getParcelFileDescriptor();
        mPdfRenderer = new PdfRenderer(mFileDescriptor);
    }
}
