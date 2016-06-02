package org.ramonaza.unofficialazaapp.helpers.backend;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.ramonaza.unofficialazaapp.database.AppDatabaseHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ilan on 4/12/16.
 */
public abstract class DatabaseHandler {

    protected SQLiteDatabase db;

    private static SQLiteDatabase dbRef;
    private static Map<String, DatabaseHandler> handlerCache;

    public static void init(Context context){
        dbRef = new AppDatabaseHelper(context).getWritableDatabase();
        handlerCache = new HashMap<>();
    }

    public static boolean isInitialized(){
        return dbRef != null && handlerCache != null;
    }

    private static void closeConnections(){
        if (dbRef != null && dbRef.isOpen()) dbRef.close();
        dbRef = null;
        handlerCache.clear();
    }

    public static DatabaseHandler getHandler(Class<? extends DatabaseHandler> handlerClass){
        if (!isInitialized()) throw new RuntimeException("Connections not yet initialized. Call init() when starting the context.");
        if (handlerCache.get(handlerClass.getName()) != null) return handlerCache.get(handlerClass.getName());
        try {
            Log.d("Databasemanagers", "Consts: "+handlerClass.getConstructors().length);
            DatabaseHandler newDbH = handlerClass.getConstructor(SQLiteDatabase.class).newInstance(dbRef);
            handlerCache.put(newDbH.getClass().getName(), newDbH);
            return newDbH;
        } catch (Exception e) {
            Log.e("DatabaseManagers", e.getMessage()); //Should never happen
            return null;
        }
    }

    protected DatabaseHandler(SQLiteDatabase db) {
        this.db = db;
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
