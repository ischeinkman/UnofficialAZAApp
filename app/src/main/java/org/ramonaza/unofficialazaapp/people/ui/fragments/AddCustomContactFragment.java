package org.ramonaza.unofficialazaapp.people.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.helpers.backend.ChapterPackHandlerSupport;
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseHandler;
import org.ramonaza.unofficialazaapp.people.backend.ContactInfoWrapper;
import org.ramonaza.unofficialazaapp.people.backend.LocationSupport;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddCustomContactFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddCustomContactFragment extends Fragment {

    //The email to send new contact information to
    private static final String[] UPDATE_EMAIL = {"ramonazadev@gmail.com"};


    public AddCustomContactFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AddCustomContactFragment.
     */
    public static AddCustomContactFragment newInstance() {
        AddCustomContactFragment fragment = new AddCustomContactFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_add_custom_contact, container, false);
        Button submitButton = (Button) rootView.findViewById(R.id.SubmitButton);
        submitButton.setOnClickListener(new SubmitListener(getActivity(), rootView));
        return rootView;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public class SubmitListener implements View.OnClickListener {
        Context context;
        ContactInfoWrapper mContact;
        View myView;

        public SubmitListener(Context incontext, View inView) {
            this.myView = inView;
            this.context = incontext;
            this.mContact = new ContactInfoWrapper();
        }

        @Override
        public void onClick(View v) {
            ContactDatabaseHandler handler = ChapterPackHandlerSupport.getContactHandler(context);

            EditText nameField = (EditText) myView.findViewById(R.id.NewContactName);
            EditText addressField = (EditText) myView.findViewById(R.id.NewContactAddress);
            EditText phoneField = (EditText) myView.findViewById(R.id.NewContactPhone);
            EditText schoolField = (EditText) myView.findViewById(R.id.NewContactSchool);
            EditText emailField = (EditText) myView.findViewById(R.id.NewContactEmail);
            EditText gradeField = (EditText) myView.findViewById(R.id.NewContactGrade);
            CheckBox globalUpdate = (CheckBox) myView.findViewById(R.id.NewContactReqUpdate);

            String nameVal = nameField.getText().toString();
            String addressVal = addressField.getText().toString();
            String phoneVal = phoneField.getText().toString();
            String schoolVal = schoolField.getText().toString();
            String emailVal = emailField.getText().toString();
            String gradeVal = gradeField.getText().toString();
            Set<String> valArray = new HashSet<String>(Arrays.asList(nameVal, addressVal, phoneVal, schoolVal, emailVal, gradeVal));
            if (valArray.contains(null) || valArray.contains("")) {
                Toast.makeText(context, R.string.error_blank_responce, Toast.LENGTH_SHORT).show();
                return;
            }

            mContact.setName(nameVal);
            mContact.setAddress(addressVal);
            mContact.setPhoneNumber(phoneVal);
            mContact.setSchool(schoolVal);
            mContact.setEmail(emailVal);
            int grade = Integer.parseInt(gradeVal);
            mContact.setGrade(grade);
            mContact.setPresent(true);
            double[] coords = LocationSupport.getCoordsFromAddress(mContact.getAddress(), context);
            if (coords != null) {
                mContact.setLatitude(coords[0]);
                mContact.setLongitude(coords[1]);
            }

            try {
                handler.addContact(mContact);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (globalUpdate.isChecked()) {
                Intent updateEmailIntent = new Intent(Intent.ACTION_SEND);
                updateEmailIntent.setType("text/html");
                updateEmailIntent.putExtra(Intent.EXTRA_EMAIL, UPDATE_EMAIL);
                updateEmailIntent.putExtra(Intent.EXTRA_SUBJECT, "NEW CONTACT:" + mContact.getName());
                String message = String.format("Name: %s\nSchool: %s\nGraduation year: %s\nAddress: %s\nEmail: %s\n Phone: %s\n", mContact.getName(), mContact.getSchool(), mContact.getGradYear(), mContact.getAddress(), mContact.getEmail(), mContact.getPhoneNumber());
                updateEmailIntent.putExtra(Intent.EXTRA_TEXT, message);
                startActivity(Intent.createChooser(updateEmailIntent, "Request update using..."));
            }
            getActivity().finish();
        }
    }


}
