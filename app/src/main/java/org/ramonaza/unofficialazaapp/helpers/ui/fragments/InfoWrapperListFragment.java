package org.ramonaza.unofficialazaapp.helpers.ui.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.ramonaza.unofficialazaapp.R;
import org.ramonazaapi.interfaces.InfoWrapper;

import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class InfoWrapperListFragment extends Fragment {

    protected ProgressBar progressBar;
    protected ListView listView;
    protected Subscription subscription;
    protected ArrayAdapter mAdapter;
    protected int mLayoutId;
    protected View rootView;

    public InfoWrapperListFragment() {

    }

    public abstract ArrayAdapter getAdapter();

    public abstract Observable<? extends InfoWrapper> generateInfo();


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

    public void showText(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG);
    }

    public void refreshData() {
        progressBar.setVisibility(View.VISIBLE);
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        subscription = generateInfo()
                .toList()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<? extends InfoWrapper>>() {
                    @Override
                    public void call(List<? extends InfoWrapper> contactInfoWrappers) {
                        progressBar.setVisibility(View.GONE);
                        mAdapter.clear();
                        mAdapter.addAll(contactInfoWrappers);
                        mAdapter.notifyDataSetChanged();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        showText(throwable.getMessage());
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        subscription.unsubscribe();
                    }
                });

    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public void onPause() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }
        super.onPause();
    }

    @Override
    public void onDetach() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }
        super.onDetach();
    }



}
