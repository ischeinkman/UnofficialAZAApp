package org.ramonaza.unofficialazaapp.helpers.ui.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding.widget.RxTextView;

import org.ramonaza.unofficialazaapp.R;
import org.ramonazaapi.interfaces.InfoWrapper;

import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

`

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class InfoWrapperListFragment extends Fragment {

    //The background data retrieving task
    protected Subscription subscription;

    //Views we must manipulate
    protected ProgressBar progressBar;
    protected ListView listView;
    protected ArrayAdapter mAdapter;
    protected int mLayoutId;
    protected View rootView;

    //A subject to display toasts from multiple threads
    protected Subject<String, String> toastStorage;

    //Layouts and values related to filtering
    protected CharSequence filter;
    protected LinearLayout searchLayout;
    protected TextView searchBox;
    protected ImageView searchButton;
    protected Subscription filterSubscription;

    public InfoWrapperListFragment() {

    }

    public abstract ArrayAdapter getAdapter();

    public abstract Observable<? extends InfoWrapper> generateInfo();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setUpToastSubject();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_infowrapper_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void hideSearchView(){
        if (filterSubscription != null) {
            filterSubscription.unsubscribe();
            filterSubscription = null;
        }
        searchLayout.setVisibility(View.GONE);
        filter = null;
        refreshData();
    }

    public void showSearchView(){
        searchLayout.setVisibility(View.VISIBLE);
        filterSubscription = RxTextView.textChanges(searchBox)
                .subscribe(new Action1<CharSequence>() {
                    @Override
                    public void call(CharSequence charSequence) {
                        filter = charSequence;
                        refreshData();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        showText(throwable.getMessage());
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.openSearch) {
            if (searchLayout.getVisibility() == View.GONE) {
                showSearchView();
            }
            else{
                hideSearchView();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (this.mAdapter == null) this.mAdapter = getAdapter();
        if (mLayoutId == 0) mLayoutId = R.layout.fragment_info_wrapper_list;
        rootView = inflater.inflate(mLayoutId, container, false);
        progressBar = (ProgressBar) rootView.findViewById(R.id.cProgressBar);
        listView = (ListView) rootView.findViewById(R.id.infowrapperadapterlist);
        searchLayout = (LinearLayout) rootView.findViewById(R.id.cListLinearListSearch);
        searchBox = (TextView) rootView.findViewById(R.id.searchNames);
        /*searchButton = (ImageView) rootView.findViewById(R.id.searchIcon);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSearchView();
            }
        });*/
        refreshData();
        listView.setAdapter(mAdapter);
        return rootView;
    }

    private void setUpToastSubject(){
        toastStorage = PublishSubject.create();
        toastStorage.observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<String>() {
            @Override
            public void call(String message) {
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void showText(String message) {
        toastStorage.onNext(message);
    }

    public void refreshData() {
        Log.v(this.getClass().getName(), "Refreshing Data");
        progressBar.setVisibility(View.VISIBLE);
        listView.setVisibility(View.INVISIBLE);
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        subscription = generateInfo()
                .filter(new Func1<InfoWrapper, Boolean>() {
                    @Override
                    public Boolean call(InfoWrapper infoWrapper) {
                        return filter == null || filter.length() == 0
                                || infoWrapper.getName().toLowerCase().contains(filter.toString().toLowerCase());
                    }
                })
                .toList()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<? extends InfoWrapper>>() {
                    @Override
                    public void call(List<? extends InfoWrapper> contactInfoWrappers) {
                        Log.v(this.getClass().getName(), "Sub.next");
                        mAdapter.clear();
                        mAdapter.addAll(contactInfoWrappers);
                        mAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.INVISIBLE);
                        listView.setVisibility(View.VISIBLE);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.v(this.getClass().getName(), "Sub.err: " + throwable.getMessage());
                        for (StackTraceElement elm : throwable.getStackTrace()){
                            Log.v(this.getClass().getName(),"  ST: "+elm.getClassName()+"."+elm.getMethodName()+" @ "+elm.getLineNumber());
                        }
                        showText(throwable.getMessage());
                        progressBar.setVisibility(View.GONE);
                        listView.setVisibility(View.VISIBLE);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        Log.v(this.getClass().getName(), "Sub.fin");
                        progressBar.setVisibility(View.GONE);
                        listView.setVisibility(View.VISIBLE);
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
