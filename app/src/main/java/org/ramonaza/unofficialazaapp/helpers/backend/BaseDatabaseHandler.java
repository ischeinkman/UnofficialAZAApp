package org.ramonaza.unofficialazaapp.helpers.backend;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.ramonaza.unofficialazaapp.database.AppDatabaseHelper;

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

    protected Cursor query(String queryString, String[] selectionArgs) {
        try {
            return db.rawQuery(queryString, selectionArgs);
        } catch (Exception e) {
            Log.e("Database", e.getMessage());
            return null;
        }
    }


    protected Cursor query(String queryString) {
        return query(queryString, null);
    }

}
