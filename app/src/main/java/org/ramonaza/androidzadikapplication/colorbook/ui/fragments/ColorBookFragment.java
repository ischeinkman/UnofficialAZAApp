package org.ramonaza.androidzadikapplication.colorbook.ui.fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.ramonaza.androidzadikapplication.R;
import org.ramonaza.androidzadikapplication.colorbook.ui.other.ColorBookPagerAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ColorBookFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ColorBookFragment extends Fragment
        implements ColorBookPageFragment.BookCallbacks,
        ColorbookGotoPageDialog.BookCallbacks,
        ColorbookTableOfContentsDialog.BookCallbacks {

    private static final int SHOWBAR_DEFAULT_DELAY = 3000;
    private Handler actionbarHiderHandler;
    private boolean barChangeDisabled;
    private ColorBookCallbacks callbacks;
    private ColorBookPagerAdapter pagesAdapter;
    private ViewPager pagesPager;

    public ColorBookFragment() {
        // Required empty public constructor
    }

    public static ColorBookFragment newInstance() {
        ColorBookFragment fragment = new ColorBookFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        barChangeDisabled = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_color_book, container, false);
        pagesPager = (ViewPager) root.findViewById(R.id.BookPagesView);
        pagesAdapter = new ColorBookPagerAdapter(getChildFragmentManager(), getActivity(), this);
        pagesPager.setAdapter(pagesAdapter);
        callbacks.onUiHide();
        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_colorbook, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_display_table_of_contents:
                DialogFragment contentsTable = new ColorbookTableOfContentsDialog();
                contentsTable.setTargetFragment(this, 0);
                contentsTable.show(getFragmentManager(), "Title");
                break;
            case R.id.action_display_goto_page:
                DialogFragment gotoPage = new ColorbookGotoPageDialog();
                gotoPage.setTargetFragment(this, 0);
                gotoPage.show(getFragmentManager(), "Title");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setPage(int number) {
        pagesPager.setCurrentItem(number);
    }

    public void onDownSwipe() {
        if (barChangeDisabled) return;
        callbacks.onUiShow();
        if (actionbarHiderHandler == null) actionbarHiderHandler = new Handler();
        actionbarHiderHandler.removeCallbacksAndMessages(null);
        actionbarHiderHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (callbacks != null) callbacks.onUiHide();
            }
        }, SHOWBAR_DEFAULT_DELAY);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callbacks = (ColorBookCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement ColorBookCallbacks");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (actionbarHiderHandler != null) {
            actionbarHiderHandler.removeCallbacksAndMessages(null);
            actionbarHiderHandler = null;
        }
    }

    public interface ColorBookCallbacks {
        public void onUiShow();

        public void onUiHide();
    }
}
