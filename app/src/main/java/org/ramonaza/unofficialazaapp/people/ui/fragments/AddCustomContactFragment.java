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
import org.ramonaza.unofficialazaapp.people.backend.LocationSupport;
import org.ramonazaapi.contacts.ContactInfoWrapper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddCustomContactFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddCustomContactFragment extends Fragment {

    //The email to send new contact information to
    private static final String[] UPDATE_EMAIL = {"ramonazadev@gmail.com"};

    private Subscription addContactSubscription;

    private CheckBox globalUpdate;

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
        globalUpdate = (CheckBox) rootView.findViewById(R.id.NewContactReqUpdate);
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
        View myView;

        public SubmitListener(Context incontext, View inView) {
            this.myView = inView;
            this.context = incontext;
        }

        @Override
        public void onClick(View v) {
            final ContactDatabaseHandler handler = ChapterPackHandlerSupport.getContactHandler(context);
            if (addContactSubscription != null && !addContactSubscription.isUnsubscribed()) {
                Toast.makeText(getActivity(), "Warning: already submitting contact", Toast.LENGTH_LONG);
                return;
            }
            addContactSubscription = Observable.just(new ContactInfoWrapper()).map(new Func1<ContactInfoWrapper, ContactInfoWrapper>() {
                @Override
                public ContactInfoWrapper call(ContactInfoWrapper mContact) {
                    EditText nameField = (EditText) myView.findViewById(R.id.NewContactName);
                    EditText addressField = (EditText) myView.findViewById(R.id.NewContactAddress);
                    EditText phoneField = (EditText) myView.findViewById(R.id.NewContactPhone);
                    EditText schoolField = (EditText) myView.findViewById(R.id.NewContactSchool);
                    EditText emailField = (EditText) myView.findViewById(R.id.NewContactEmail);
                    EditText gradeField = (EditText) myView.findViewById(R.id.NewContactGrade);

                    final String nameVal = nameField.getText().toString();
                    final String addressVal = addressField.getText().toString();
                    final String phoneVal = phoneField.getText().toString();
                    final String schoolVal = schoolField.getText().toString();
                    final String emailVal = emailField.getText().toString();
                    final String gradeVal = gradeField.getText().toString();
                    Set<String> valArray = new HashSet<String>(Arrays.asList(nameVal, addressVal, phoneVal, schoolVal, emailVal, gradeVal));
                    if (valArray.contains(null) || valArray.contains("")) {
                        throw new RuntimeException(getString(R.string.error_blank_responce));
                    }
                    mContact.setName(nameVal);
                    mContact.setAddress(addressVal);
                    mContact.setPhoneNumber(phoneVal);
                    mContact.setSchool(schoolVal);
                    mContact.setEmail(emailVal);
                    int grade = Integer.parseInt(gradeVal);
                    mContact.setGrade(grade);
                    mContact.setPresent(true);
                    return mContact;
                }
            }).map(new Func1<ContactInfoWrapper, ContactInfoWrapper>() {
                @Override
                public ContactInfoWrapper call(ContactInfoWrapper mContact) {
                    double[] coords = LocationSupport.getCoordsFromAddress(mContact.getAddress(), context);
                    if (coords != null) {
                        mContact.setLatitude(coords[0]);
                        mContact.setLongitude(coords[1]);
                    }
                    return mContact;
                }
            }).flatMap(new Func1<ContactInfoWrapper, Observable<ContactInfoWrapper>>() {
                @Override
                public Observable<ContactInfoWrapper> call(ContactInfoWrapper contactInfoWrapper) {
                    return handler.addContacts(contactInfoWrapper);
                }
            }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<ContactInfoWrapper>() {
                        @Override
                        public void call(ContactInfoWrapper mContact) {
                            if (globalUpdate.isChecked()) {
                                Intent updateEmailIntent = new Intent(Intent.ACTION_SEND);
                                updateEmailIntent.setType("text/html");
                                updateEmailIntent.putExtra(Intent.EXTRA_EMAIL, UPDATE_EMAIL);
                                updateEmailIntent.putExtra(Intent.EXTRA_SUBJECT, "NEW CONTACT:" + mContact.getName());
                                String message = String.format("Name: %s\nSchool: %s\nGraduation year: %s\nAddress: %s\nEmail: %s\n Phone: %s\n", mContact.getName(), mContact.getSchool(), mContact.getGradYear(), mContact.getAddress(), mContact.getEmail(), mContact.getPhoneNumber());
                                updateEmailIntent.putExtra(Intent.EXTRA_TEXT, message);
                                startActivity(Intent.createChooser(updateEmailIntent, "Request update using..."));
                            }
                            if (getActivity() != null) getActivity().finish();
                        }
                    });
        }
    }


}
