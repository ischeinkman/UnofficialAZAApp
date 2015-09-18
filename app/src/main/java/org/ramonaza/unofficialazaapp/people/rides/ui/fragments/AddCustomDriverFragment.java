package org.ramonaza.unofficialazaapp.people.rides.ui.fragments;


import android.app.Fragment;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseHandler;
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseHelper;
import org.ramonaza.unofficialazaapp.people.backend.ContactInfoWrapper;
import org.ramonaza.unofficialazaapp.people.rides.backend.DriverInfoWrapper;
import org.ramonaza.unofficialazaapp.people.rides.backend.RidesDatabaseHandler;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddCustomDriverFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddCustomDriverFragment extends Fragment {

    private static final String PRESET_CONTACT_ID="org.ramonaza.unofficialazaapp.CONTACT";

    private int presetID;
    private ContactInfoWrapper presContact;
    private Initializer initTask;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment AddCustomDriverFragment.
     */
    public static AddCustomDriverFragment newInstance(int presetID) {
        AddCustomDriverFragment fragment = new AddCustomDriverFragment();
        Bundle args = new Bundle();
        args.putInt(PRESET_CONTACT_ID, presetID);
        fragment.setArguments(args);
        return fragment;
    }

    public AddCustomDriverFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        presetID=getArguments().getInt(PRESET_CONTACT_ID);
        View rootView= inflater.inflate(R.layout.fragment_add_custom_driver, container, false);
        initTask=new Initializer(rootView, getActivity(), presetID);
        initTask.execute();
        return rootView;
    }

    public class Initializer extends AsyncTask<Void, Void, Void>{

        private View rootView;
        private Context context;
        private int contactID;

        public Initializer(View rootView, Context context, int contactID){
            this.rootView=rootView;
            this.context=context;
            this.contactID=contactID;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if(contactID<1) return null;
            presContact=new ContactInfoWrapper();
            ContactDatabaseHandler dbHandler=new ContactDatabaseHandler(context);
            presContact=dbHandler.getContact(contactID);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(presContact != null){

                EditText nameField=(EditText) rootView.findViewById(R.id.AddDriverName);
                nameField.setText(presContact.getName());

                EditText addressField=(EditText) rootView.findViewById(R.id.AddDriverAddress);
                addressField.setText(presContact.getAddress());

            }
            Button submitButton=(Button) rootView.findViewById(R.id.SubmitButton);
            submitButton.setOnClickListener(new SubmitListener(getActivity(),rootView));
            rootView.findViewById(R.id.scrollView).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.cProgressBar).setVisibility(View.INVISIBLE);
            super.onPostExecute(aVoid);
        }
    }

    public class SubmitListener extends AsyncTask<Void, Void, Void> implements View.OnClickListener{
        private Context context;
        private DriverInfoWrapper mDriver;
        private View myView;
        private String driverName;
        private String driverSpots;
        private String driverAddress;

        public SubmitListener(Context incontext, View inView){
            this.myView=inView;
            this.context=incontext;
            this.mDriver=new DriverInfoWrapper();
        }

        @Override
        public void onClick(View v) {
            EditText nameField=(EditText) myView.findViewById(R.id.AddDriverName);
            EditText spotsField=(EditText) myView.findViewById(R.id.AddDriverSpots);
            EditText addressField=(EditText) myView.findViewById(R.id.AddDriverAddress);
            String tryName=nameField.getText().toString();
            String trySpots=spotsField.getText().toString();
            String tryAddress=addressField.getText().toString();
            if (tryName.equals("") || trySpots.equals("") || tryAddress.equals("")){
                Toast.makeText(context, R.string.error_blank_responce, Toast.LENGTH_SHORT).show();
                return;
            }
            driverName=tryName;
            driverAddress=tryAddress;
            driverSpots=trySpots;
            this.execute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            SQLiteDatabase db= new ContactDatabaseHelper(context).getWritableDatabase();
            RidesDatabaseHandler handler=new RidesDatabaseHandler(db);
            mDriver.setName(driverName);
            int inpSpots=Integer.parseInt(driverSpots);
            mDriver.setAddress(driverAddress);
            if(mDriver.getAddress().equals(presContact.getAddress())){
                mDriver.setLongitude(""+presContact.getLongitude());
                mDriver.setLatitude(""+presContact.getLatitude());
            }
            if(mDriver.getName().equals(presContact.getName())) mDriver.setSpots(inpSpots + 1);
            else mDriver.setSpots(inpSpots);
            try {
                handler.addDriver(mDriver);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(presContact != null){
                ContactDatabaseHandler cHandler= new ContactDatabaseHandler(db);
                presContact.setPresent(true);
                try {
                    cHandler.updateContact(presContact);
                } catch (ContactDatabaseHandler.ContactCSVReadError contactCSVReadError) {
                    contactCSVReadError.printStackTrace();
                }
                handler.addAlephsToCar(mDriver.getId(),new ContactInfoWrapper[]{presContact});
            }
            getActivity().finish();
            return null;
        }
    }


}
