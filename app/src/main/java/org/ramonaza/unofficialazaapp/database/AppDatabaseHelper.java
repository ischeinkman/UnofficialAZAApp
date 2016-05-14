package org.ramonaza.unofficialazaapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.ramonaza.unofficialazaapp.helpers.backend.ChapterPackHandlerSupport;
import org.ramonaza.unofficialazaapp.people.backend.ContactDatabaseHandler;
import org.ramonazaapi.chapterpacks.ChapterPackHandler;
import org.ramonazaapi.contacts.ContactInfoWrapper;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * A simple database helper for accessing the contact/rides database.
 * All database interactions should be handled by a separate handler.
 * Created by ilanscheinkman on 3/12/15.
 */
public class AppDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "ContactDriverDatabase";
    public static final int DATABASE_VERSION = 5;
    private Context context;

    public AppDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(AppDatabaseContract.DriverListTable.CREATE_TABLE);
        db.execSQL(AppDatabaseContract.ContactListTable.CREATE_TABLE);
        db.execSQL(AppDatabaseContract.RidesListTable.CREATE_TABLE);
        db.execSQL(AppDatabaseContract.EventListTable.CREATE_TABLE);
        genDatabaseFromCSV(db)
                .publish()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        Log.d("UnnofficialAZAApp", "Database recreated successfully: " + aBoolean.toString());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d("UnnofficialAZAApp", "Database recreated with error: " + throwable.getMessage());
                    }
                });
    }

    public void onDelete(SQLiteDatabase db) {
        db.execSQL(AppDatabaseContract.DriverListTable.DELETE_TABLE);
        db.execSQL(AppDatabaseContract.ContactListTable.DELETE_TABLE);
        db.execSQL(AppDatabaseContract.RidesListTable.DELETE_TABLE);
        db.execSQL(AppDatabaseContract.EventListTable.DELETE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 4 && newVersion >= 5) {
            String addTableStatement = "ALTER TABLE " + AppDatabaseContract.DriverListTable.TABLE_NAME +
                    " ADD " + AppDatabaseContract.DriverListTable.COLUMN_CONTACT_INFO + " " + AppDatabaseContract.VTYPE_INT;
            db.execSQL(addTableStatement);
            String setDefault = "UPDATE " + AppDatabaseContract.DriverListTable.TABLE_NAME +
                    " SET " + AppDatabaseContract.DriverListTable.COLUMN_CONTACT_INFO + " =-1 ";
            db.execSQL(setDefault);
        }
    }

    public Observable<Boolean> genDatabaseFromCSV(SQLiteDatabase db) {
        final ContactDatabaseHandler dbHandler = new ContactDatabaseHandler(db);
        return dbHandler
                .deleteContacts(null)
                .toList()
                .flatMap(new Func1<List<Integer>, Observable<ChapterPackHandler>>() {
                    @Override
                    public Observable<ChapterPackHandler> call(List<Integer> integers) {
                        return ChapterPackHandlerSupport.getChapterPackHandler(context);
                    }
                })
                .flatMap(new Func1<ChapterPackHandler, Observable<ContactInfoWrapper>>() {
                    @Override
                    public Observable<ContactInfoWrapper> call(ChapterPackHandler chapterPackHandler) {
                        return chapterPackHandler.getCsvHandler().getCtactInfoListFromCSV();
                    }
                })
                .toList().flatMap(new Func1<List<ContactInfoWrapper>, Observable<ContactInfoWrapper>>() {
                    @Override
                    public Observable<ContactInfoWrapper> call(List<ContactInfoWrapper> contactInfoWrappers) {
                        return dbHandler.addContacts(contactInfoWrappers);
                    }
                })
                .toList().map(new Func1<List<ContactInfoWrapper>, Boolean>() {
                    @Override
                    public Boolean call(List<ContactInfoWrapper> contactInfoWrappers) {
                        return contactInfoWrappers != null && contactInfoWrappers.size() > 0;
                    }
                })
                .defaultIfEmpty(false);
    }
}
