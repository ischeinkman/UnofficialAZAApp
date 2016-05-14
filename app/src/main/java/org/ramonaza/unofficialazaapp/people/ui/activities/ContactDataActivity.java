package org.ramonaza.unofficialazaapp.people.ui.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.frontpage.ui.activities.FrontalActivity;
import org.ramonaza.unofficialazaapp.helpers.backend.ChapterPackHandlerSupport;
import org.ramonaza.unofficialazaapp.helpers.ui.activities.BaseActivity;
import org.ramonaza.unofficialazaapp.people.ui.fragments.GeneralContactFragment;
import org.ramonazaapi.contacts.ContactInfoWrapper;

import rx.Subscription;
import rx.functions.Action1;

public class ContactDataActivity extends BaseActivity {

    public static final String EXTRA_CONTRUCTION_INFO = "org.ramonaza.unofficialazaapp.CONTACT_ID";
    public static final String EXTRA_LAYER = "org.ramonaza.unofficialazaapp.LAYER_NAME";
    private int inputId;
    private Subscription refreshSub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        Intent intent = getIntent();
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle("Blank Contact Data");
        inputId = intent.getIntExtra(EXTRA_CONTRUCTION_INFO, 0);
        refreshFrag();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                Intent bacIntent = NavUtils.getParentActivityIntent(this);
                bacIntent.putExtra(FrontalActivity.EXTRA_OPENEDPAGE, FrontalActivity.CONTACTS_PAGE_INDEX);
                startActivity(bacIntent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void refreshFrag() {
        if (!(refreshSub == null) && !refreshSub.isUnsubscribed()) {
            refreshSub.unsubscribe();
        }
        refreshSub = ChapterPackHandlerSupport.getContactHandler(this).getContacts(inputId).subscribe(new Action1<ContactInfoWrapper>() {
            @Override
            public void call(ContactInfoWrapper contactInfoWrapper) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction().replace(R.id.container, GeneralContactFragment.newInstance(1, contactInfoWrapper));
                transaction.commit();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                showText(throwable.getMessage());
            }
        });
    }
}
