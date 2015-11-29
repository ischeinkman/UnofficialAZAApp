package org.ramonaza.unofficialazaapp.colorbook.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.colorbook.other.ColorBookPagerAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ColorBookFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ColorBookFragment extends Fragment {

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_color_book, container, false);
        pagesPager = (ViewPager) root.findViewById(R.id.BookPagesView);
        pagesAdapter = new ColorBookPagerAdapter(getChildFragmentManager(), getActivity());
        pagesPager.setAdapter(pagesAdapter);

        return root;
    }


}
