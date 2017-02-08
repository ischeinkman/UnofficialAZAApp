package org.ramonaza.androidzadikapplication.colorbook.ui.fragments;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import org.ramonaza.androidzadikapplication.R;

/**
 * Created by ilan on 11/25/15.
 */
public class ColorBookPageFragment extends Fragment {

    public static final String PAGE_NUMBER = "org.ramonaza.androidzadikapplication.PAGENUM";
    public static final String PRELOADED_IMAGE = "instance";
    private int pagenum;
    private View rootView;
    private SubsamplingScaleImageView imageView;
    private ProgressBar bar;
    private boolean hasBeenTouched;
    private BookCallbacks caller;
    public ColorBookPageFragment() {

    }

    public static ColorBookPageFragment newInstance(int pagenum, Bitmap preloadedMap, BookCallbacks caller) {
        ColorBookPageFragment rval = new ColorBookPageFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRELOADED_IMAGE, preloadedMap);
        args.putInt(PAGE_NUMBER, pagenum);
        rval.setArguments(args);
        rval.setCaller(caller);
        return rval;
    }

    public void setCaller(BookCallbacks caller) {
        this.caller = caller;
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
        hasBeenTouched = false;
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
        final GestureDetector detector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (velocityY > 0) {
                    float xVelMag = (velocityX >= 0) ? velocityX : -1 * velocityX;
                    float yVelMag = (velocityY >= 0) ? velocityY : -1 * velocityY;
                    if (yVelMag > 2 * xVelMag) {
                        caller.onDownSwipe();
                        return true;
                    }
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return detector.onTouchEvent(motionEvent);
            }
        });
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

    public interface BookCallbacks {
        public void onDownSwipe();
    }


}
