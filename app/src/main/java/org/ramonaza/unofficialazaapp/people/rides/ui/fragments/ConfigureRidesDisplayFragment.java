package org.ramonaza.unofficialazaapp.people.rides.ui.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.helpers.backend.PreferenceHelper;
import org.ramonaza.unofficialazaapp.people.rides.ui.activities.DisplayRidesActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConfigureRidesDisplayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConfigureRidesDisplayFragment extends Fragment {

    public ConfigureRidesDisplayFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ConfigureRidesDisplayFragment.
     */
    public static ConfigureRidesDisplayFragment newInstance() {
        ConfigureRidesDisplayFragment fragment = new ConfigureRidesDisplayFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_rides_configure, container, false);
        final Spinner alSpinner = (Spinner) rootView.findViewById(R.id.AlgorithmSelection);
        final Spinner clustSpinner = (Spinner) rootView.findViewById(R.id.ClusterSelection);
        final CheckBox optomizeBox = (CheckBox) rootView.findViewById(R.id.OptimizeRidesCheckbox);
        final CheckBox retainBox = (CheckBox) rootView.findViewById(R.id.RetainRides);
        final TextView alTitle = (TextView) rootView.findViewById(R.id.AlgorithmTitle);
        final TextView clustTitle = (TextView) rootView.findViewById(R.id.ClusterTitle);

        if (!PreferenceHelper.getPreferences(getActivity()).isDebugMode()) {
            alTitle.setVisibility(View.GONE);
            clustTitle.setVisibility(View.GONE);
            alSpinner.setVisibility(View.GONE);
            clustSpinner.setVisibility(View.GONE);
        }

        Button submitButton = (Button) rootView.findViewById(R.id.SubmitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int algorithm = (alSpinner.getVisibility() != View.GONE)
                        ? alSpinner.getSelectedItemPosition()
                        : -1;
                int clusterIndex = (clustSpinner.getVisibility() != View.GONE)
                        ? clustSpinner.getSelectedItemPosition()
                        : -1;
                boolean optimize = optomizeBox.isChecked();
                boolean retain = retainBox.isChecked();
                Intent displayIntent = new Intent(getActivity(), DisplayRidesActivity.class);
                displayIntent.putExtra(DisplayRidesActivity.EXTRA_ALGORITHM, algorithm);
                displayIntent.putExtra(DisplayRidesActivity.EXTRA_OPTIMIZE, optimize);
                displayIntent.putExtra(DisplayRidesActivity.EXTRA_RETAIN_RIDES, retain);
                displayIntent.putExtra(DisplayRidesActivity.EXTRA_CLUSTER_TYPE, clusterIndex);
                startActivity(displayIntent);
            }
        });
        return rootView;
    }

}
