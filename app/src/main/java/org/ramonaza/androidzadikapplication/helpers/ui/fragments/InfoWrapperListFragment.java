package org.ramonaza.androidzadikapplication.helpers.ui.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.ramonaza.androidzadikapplication.R;
import org.ramonazaapi.interfaces.InfoWrapper;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class InfoWrapperListFragment extends Fragment {

    protected ProgressBar progressBar;
    protected ListView listView;
    protected GetInfoWrappers currentAsync;
    protected ArrayAdapter mAdapter;
    protected int mLayoutId;
    protected View rootView;

    public InfoWrapperListFragment() {

    }

    public abstract ArrayAdapter getAdapter();

    public abstract InfoWrapper[] generateInfo();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (this.mAdapter == null) this.mAdapter = getAdapter();
        if (mLayoutId == 0) mLayoutId = R.layout.fragment_info_wrapper_list;
        rootView = inflater.inflate(mLayoutId, container, false);
        progressBar = (ProgressBar) rootView.findViewById(R.id.cProgressBar);
        listView = (ListView) rootView.findViewById(R.id.infowrapperadapterlist);
        refreshData();
        listView.setAdapter(mAdapter);
        return rootView;
    }

    public void refreshData() {
        if (currentAsync != null) currentAsync.cancel(true);
        currentAsync = new GetInfoWrappers(getActivity(), mAdapter, progressBar);
        currentAsync.execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public void onPause() {
        if (currentAsync != null) currentAsync.cancel(true);
        super.onPause();
    }

    @Override
    public void onDetach() {
        if (currentAsync != null) currentAsync.cancel(true);
        super.onDetach();
    }

    /**
     * The class for retrieving the InfoWrappers.
     */
    protected class GetInfoWrappers extends AsyncTask<Void, Integer, InfoWrapper[]> {
        protected Context mContext;
        protected ProgressBar mBar;
        protected ArrayAdapter mAdapter;

        /**
         * Constructs the activity.
         *
         * @param context     the context to use
         * @param adapter     adapter to populate
         * @param progressBar the bar to report progress to
         */
        public GetInfoWrappers(Context context, ArrayAdapter adapter, ProgressBar progressBar) {
            this.mContext = context;
            this.mBar = progressBar;
            this.mAdapter = adapter;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mBar.setVisibility(View.VISIBLE);
            mAdapter.clear();
        }

        @Override
        protected InfoWrapper[] doInBackground(Void... params) {
            return generateInfo();
        }

        @Override
        protected void onPostExecute(InfoWrapper[] infoWrappers) {
            super.onPostExecute(infoWrappers);
            if (!isAdded() || isDetached()) {
                return; //In case the calling activity is no longer attached
            }
            mAdapter.addAll(infoWrappers);
            mBar.setVisibility(View.GONE);
        }
    }

}
