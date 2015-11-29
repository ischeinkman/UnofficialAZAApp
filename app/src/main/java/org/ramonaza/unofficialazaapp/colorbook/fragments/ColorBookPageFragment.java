package org.ramonaza.unofficialazaapp.colorbook.fragments;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import org.ramonaza.unofficialazaapp.R;

/**
 * Created by ilan on 11/25/15.
 */
public class ColorBookPageFragment extends Fragment {


    public static final String PAGE_NUMBER = "org.ramonaza.unofficialazaapp.PAGENUM";
    public static final String PRELOADED_IMAGE = "instance";

    private int pagenum;
    private View rootView;
    private SubsamplingScaleImageView imageView;
    private ProgressBar bar;


    public ColorBookPageFragment() {

    }


    public static ColorBookPageFragment newInstance(int pagenum, Bitmap preloadedMap) {
        ColorBookPageFragment rval = new ColorBookPageFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRELOADED_IMAGE, preloadedMap);
        args.putInt(PAGE_NUMBER, pagenum);
        rval.setArguments(args);
        return rval;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            pagenum = savedInstanceState.getInt(PAGE_NUMBER);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            pagenum = savedInstanceState.getInt(PAGE_NUMBER);
        }
        rootView = inflater.inflate(R.layout.fragment_colorbook_pages, null);
        imageView = (SubsamplingScaleImageView) rootView.findViewById(R.id.PageImage);
        bar = (ProgressBar) rootView.findViewById(R.id.cProgressBar);
        Bundle args = getArguments();
        if (args.getParcelable(PRELOADED_IMAGE) != null) {
            imageView.setImage(ImageSource.bitmap((Bitmap) args.getParcelable(PRELOADED_IMAGE)));
            bar.setVisibility(View.GONE);
        }
        return rootView;
    }

    public void setImage(Bitmap image) {
        imageView.setImage(ImageSource.bitmap(image));
        bar.setVisibility(View.GONE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PAGE_NUMBER, pagenum);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindDrawables(rootView.findViewById(R.id.PageImage));
    }

    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }
}
