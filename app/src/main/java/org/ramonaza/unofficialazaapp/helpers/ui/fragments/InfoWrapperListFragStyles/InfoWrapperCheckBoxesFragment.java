package org.ramonaza.unofficialazaapp.helpers.ui.fragments.InfoWrapperListFragStyles;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import org.ramonaza.unofficialazaapp.R;
import org.ramonaza.unofficialazaapp.helpers.backend.InfoWrapper;
import org.ramonaza.unofficialazaapp.helpers.ui.fragments.InfoWrapperListFragment;
import org.ramonaza.unofficialazaapp.helpers.ui.other.InfoWrapperCheckboxListAdapter;

/**
 * Parent class for fragments with a checkbox list and a submit button.
 * WARNING: Parent activity should include an action_submit actionbar button.
 */
public abstract class InfoWrapperCheckBoxesFragment extends InfoWrapperListFragment {


    protected InfoWrapperCheckboxListAdapter mAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public ArrayAdapter getAdapter() {
        this.mAdapter = new InfoWrapperCheckboxListAdapter(getActivity());
        return mAdapter;
    }


    public abstract void onSubmitButton(InfoWrapper[] checked, InfoWrapper[] unchecked);

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_submit:
                onSubmitButton(mAdapter.getBoth()[0], mAdapter.getBoth()[1]);
                getActivity().finish();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_checkbox_fragment, menu);
    }


}


