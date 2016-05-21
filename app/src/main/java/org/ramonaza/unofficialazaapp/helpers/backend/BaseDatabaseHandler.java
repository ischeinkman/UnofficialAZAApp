package org.ramonaza.unofficialazaapp.helpers.backend;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.ramonaza.unofficialazaapp.database.AppDatabaseHelper;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by ilan on 4/12/16.
 */
public abstract class BaseDatabaseHandler<T> {

    protected SQLiteDatabase db;

    public BaseDatabaseHandler(SQLiteDatabase db) {
        this.db = db;
    }

    public BaseDatabaseHandler(Context context) {
        this(new AppDatabaseHelper(context).getWritableDatabase());
    }

    public BaseDatabaseHandler(BaseDatabaseHandler other) {
        this(other.db);
    }

    protected Observable<Cursor> query(final String queryString, final String[] selectionArgs) {
        return Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> subscriber) {
                try {
                    subscriber.onNext(db.rawQuery(queryString, selectionArgs));
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                    subscriber.onCompleted();
                }
            }
        });

    }


    protected Observable<Cursor> query(String queryString) {
        return query(queryString, null);
    }

}
