package org.ramonaza.androidzadikapplication.events.ui.fragments;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.ramonaza.androidzadikapplication.R;
import org.ramonaza.androidzadikapplication.people.backend.EventDatabaseHandler;
import org.ramonazaapi.events.EventInfoWrapper;

/**
 * Created by ilanscheinkman on 1/29/15.
 */
public class GeneralEventFragment extends Fragment {

    private static final String EVENT_DATA = "org.ramonaza.androidzadikapplication.EVENT_DATA";
    int eventID;

    public GeneralEventFragment() {
    }

    public static GeneralEventFragment newInstance(int eventID) {
        GeneralEventFragment fragment = new GeneralEventFragment();
        Bundle args = new Bundle();
        args.putInt(EVENT_DATA, eventID);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ActionBar actionBar = getActivity().getActionBar();
        View rootView = inflater.inflate(R.layout.fragment_event_data, container, false);
        TextView tView = (TextView) rootView.findViewById(R.id.EventPageTextView);
        LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.EventPageScrollLayout);
        eventID = getArguments().getInt(EVENT_DATA);
        final EventInfoWrapper myEvent = new EventDatabaseHandler(getActivity()).getEvent(eventID);
        actionBar.setTitle(myEvent.getName());
        String displayText = String.format(
                "<b><u>%s</u></b><br><br>Description: %s<br>",
                myEvent.getName(),
                myEvent.getDesc()
        );
        if (myEvent.getMeet() != null && myEvent.getMeet().replaceAll(" ", "").length() > 3)
            displayText += String.format("Meet: %s<br>", myEvent.getMeet());
        if (myEvent.getFood() != null && myEvent.getFood().replaceAll(" ", "").length() > 3)
            displayText += String.format("Food: %s<br>", myEvent.getFood());
        if (myEvent.getHouse() != null && myEvent.getHouse().replaceAll(" ", "").length() > 3)
            displayText += String.format("House: %s<br>", myEvent.getHouse());
        if (myEvent.getStaff() != null && myEvent.getStaff().replaceAll(" ", "").length() > 3)
            displayText += String.format("Staff: %s<br>", myEvent.getStaff());
        if (myEvent.getBring() != null && !myEvent.getBring().replaceAll(" ", "").equals(""))
            displayText += String.format("Bring: %s<br>", myEvent.getBring());
        if (myEvent.getPlanner() != null && !myEvent.getPlanner().replaceAll(" ", "").equals(""))
            displayText += String.format("Planned By: %s<br>", myEvent.getPlanner());

        tView.setTextSize(22);
        tView.setText(Html.fromHtml(displayText));
        if (myEvent.getMapsLocation() != null && myEvent.getMapsLocation().length() > 2) {
            Button dirButton = new Button(getActivity());
            dirButton.setText("Directions");
            dirButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String uri = String.format("google.navigation:q=%s", myEvent.getMapsLocation().replace(" ", "+"));
                        Intent navIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        startActivity(navIntent);
                    } catch (ActivityNotFoundException activityException) {
                        Log.d("Directions to:" + myEvent.getMapsLocation(), "Failed", activityException);
                    }
                }
            });
            layout.addView(dirButton);
        }
        return rootView;
    }
}
