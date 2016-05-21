package org.ramonaza.unofficialazaapp.events.backend;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import org.ramonaza.unofficialazaapp.database.AppDatabaseContract;
import org.ramonaza.unofficialazaapp.helpers.backend.BaseDatabaseHandler;
import org.ramonazaapi.events.EventInfoWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Yuval Zach aka kingi2001 on 12/28/2015.
 */
public class EventDatabaseHandler extends BaseDatabaseHandler<EventInfoWrapper> {

    public EventDatabaseHandler(Context context) {
        super(context);
    }

    public EventDatabaseHandler(SQLiteDatabase db) {
        super(db);
    }

    public EventDatabaseHandler(BaseDatabaseHandler other) {
        super(other);
    }

    public static Observable<EventInfoWrapper> getEventsFromCursor(final Cursor queryResults) {
        if (queryResults.getCount() == 0) {
            return Observable.empty();
        }
        return Observable.create(new Observable.OnSubscribe<EventInfoWrapper>() {
            @Override
            public void call(Subscriber<? super EventInfoWrapper> subscriber) {
                queryResults.moveToFirst();
                do {
                    EventInfoWrapper temp = new EventInfoWrapper();
                    temp.setId(queryResults.getInt(queryResults.getColumnIndexOrThrow(AppDatabaseContract.EventListTable._ID)));
                    temp.setName(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.EventListTable.COLUMN_NAME)));
                    temp.setDesc(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.EventListTable.COLUMN_DESC)));
                    temp.setPlanner(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.EventListTable.COLUMN_PLANNER)));
                    temp.setMeet(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.EventListTable.COLUMN_MEET)));
                    temp.setMapsLocation(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.EventListTable.COLUMN_LOCATION)));
                    temp.setBring(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.EventListTable.COLUMN_BRING)));
                    temp.setDate(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.EventListTable.COLUMN_DATE)));
                    subscriber.onNext(temp);
                } while (queryResults.moveToNext());
                subscriber.onCompleted();
            }
        });
    }

    public static Observable<ContentValues> prepareEventsForCursor(EventInfoWrapper... events) {
        return Observable.from(events).map(new Func1<EventInfoWrapper, ContentValues>() {
            @Override
            public ContentValues call(EventInfoWrapper toAdd) {
                ContentValues value = new ContentValues();
                value.put(AppDatabaseContract.EventListTable.COLUMN_NAME, toAdd.getName());
                value.put(AppDatabaseContract.EventListTable.COLUMN_DESC, toAdd.getDesc());
                value.put(AppDatabaseContract.EventListTable.COLUMN_PLANNER, toAdd.getPlanner());
                value.put(AppDatabaseContract.EventListTable.COLUMN_MEET, toAdd.getMeet());
                value.put(AppDatabaseContract.EventListTable.COLUMN_LOCATION, toAdd.getMapsLocation());
                value.put(AppDatabaseContract.EventListTable.COLUMN_BRING, toAdd.getBring());
                value.put(AppDatabaseContract.EventListTable.COLUMN_DATE, toAdd.getDate());
                return value;
            }
        });
    }

    public void close() {
        db.close();
    }

    public Observable<EventInfoWrapper> addEvents(final EventInfoWrapper... toAdd) {
        final Map<String, EventInfoWrapper> eventsByName = new HashMap<>(toAdd.length);
        for (EventInfoWrapper event : toAdd) eventsByName.put(event.getName(), event);
        return prepareEventsForCursor(toAdd)
                .map(new Func1<ContentValues, EventInfoWrapper>() {
                    @Override
                    public EventInfoWrapper call(ContentValues contentValues) {
                        long rowId = db.insert(AppDatabaseContract.EventListTable.TABLE_NAME, null, contentValues);
                        if (rowId == -1l) {
                            throw new RuntimeException("Null Event Read");
                        }
                        EventInfoWrapper inserted = eventsByName.get(contentValues.getAsString(AppDatabaseContract.EventListTable.COLUMN_NAME));
                        inserted.setId((int) rowId);
                        return inserted;
                    }
                });
    }

    public Observable<EventInfoWrapper> addEvents(final Collection<? extends EventInfoWrapper> toAdd) {
        return addEvents(toAdd.toArray(new EventInfoWrapper[0]));
    }

    public Observable<Integer> updateEvent(final EventInfoWrapper toUpdate) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                ContentValues value = new ContentValues();
                value.put(AppDatabaseContract.EventListTable.COLUMN_NAME, toUpdate.getName());
                value.put(AppDatabaseContract.EventListTable.COLUMN_DESC, toUpdate.getDesc());
                value.put(AppDatabaseContract.EventListTable.COLUMN_PLANNER, toUpdate.getPlanner());
                value.put(AppDatabaseContract.EventListTable.COLUMN_MEET, toUpdate.getMeet());
                value.put(AppDatabaseContract.EventListTable.COLUMN_LOCATION, toUpdate.getMapsLocation());
                value.put(AppDatabaseContract.EventListTable.COLUMN_BRING, toUpdate.getBring());
                value.put(AppDatabaseContract.EventListTable.COLUMN_DATE, toUpdate.getDate());

                long rowId = db.update(AppDatabaseContract.EventListTable.TABLE_NAME, value,
                        AppDatabaseContract.EventListTable._ID + "=?", new String[]{"" + toUpdate.getId()});
                if (rowId == -1l) subscriber.onError(new IOException("Null Event Read"));
                subscriber.onNext((int) rowId);
                subscriber.onCompleted();
            }
        });
    }

    public Observable<Integer> deleteEvents(int... toDelete) {
        if (toDelete == null) return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(db.delete(AppDatabaseContract.EventListTable.TABLE_NAME, null, null));
                subscriber.onCompleted();
            }
        });
        Integer[] allInts = new Integer[toDelete.length];
        for (int i = 0; i < toDelete.length; i++) allInts[i] = toDelete[i];
        return Observable.from(allInts).map(new Func1<Integer, Integer>() {
            @Override
            public Integer call(Integer toDelete) {
                db.delete(AppDatabaseContract.EventListTable.TABLE_NAME, "?=?", new String[]{
                        AppDatabaseContract.EventListTable._ID,
                        "" + toDelete
                });
                return toDelete;
            }
        });
    }


    public Observable<Integer> deleteEvents(@Nullable String whereClauses, @Nullable String[] whereArgs) {
        return Observable.just(db.delete(AppDatabaseContract.EventListTable.TABLE_NAME, whereClauses, whereArgs))
                .subscribeOn(Schedulers.io());
    }

    public Observable<EventInfoWrapper> getEvents(final int... ids) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                String query;
                if (ids != null) {
                    String queryBase = String.format("SELECT * FROM %s WHERE %s IN (",
                            AppDatabaseContract.EventListTable.TABLE_NAME,
                            AppDatabaseContract.EventListTable._ID);
                    StringBuilder builder = new StringBuilder(queryBase);
                    for (int id : ids) {
                        builder.append(id + ",");
                    }
                    builder.deleteCharAt(builder.length()-1);
                    builder.append(")");
                    query = builder.toString();
                } else {
                    query = String.format("SELECT * FROM %s",
                            AppDatabaseContract.EventListTable.TABLE_NAME);
                }
                subscriber.onNext(query);
                subscriber.onCompleted();
            }
        }).flatMap(new Func1<String, Observable<Cursor>>() {
            @Override
            public Observable<Cursor> call(String s) {
                return query(s);
            }
        }).flatMap(new Func1<Cursor, Observable<EventInfoWrapper>>() {
            @Override
            public Observable<EventInfoWrapper> call(Cursor cursor) {
                return getEventsFromCursor(cursor);
            }
        });
    }

    public EventInfoWrapper[] getEventsSync(int... ids) {
        String query;
        if (ids != null) {
            String queryBase = String.format("SELECT * FROM %s WHERE %s IN (",
                    AppDatabaseContract.EventListTable.TABLE_NAME,
                    AppDatabaseContract.EventListTable._ID);
            StringBuilder builder = new StringBuilder(queryBase);
            for (int id : ids) {
                builder.append(id + ",");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(")");
            query = builder.toString();
        } else {
            query = String.format("SELECT * FROM %s",
                    AppDatabaseContract.EventListTable.TABLE_NAME);
        }
        Cursor queryResults = db.rawQuery(query, null);
        List<EventInfoWrapper> events = new ArrayList<>();
        queryResults.moveToFirst();
        do {
            EventInfoWrapper temp = new EventInfoWrapper();
            temp.setId(queryResults.getInt(queryResults.getColumnIndexOrThrow(AppDatabaseContract.EventListTable._ID)));
            temp.setName(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.EventListTable.COLUMN_NAME)));
            temp.setDesc(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.EventListTable.COLUMN_DESC)));
            temp.setPlanner(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.EventListTable.COLUMN_PLANNER)));
            temp.setMeet(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.EventListTable.COLUMN_MEET)));
            temp.setMapsLocation(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.EventListTable.COLUMN_LOCATION)));
            temp.setBring(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.EventListTable.COLUMN_BRING)));
            temp.setDate(queryResults.getString(queryResults.getColumnIndexOrThrow(AppDatabaseContract.EventListTable.COLUMN_DATE)));
            events.add(temp);
        } while (queryResults.moveToNext());
        return events.toArray(new EventInfoWrapper[0]);
    }

    public Observable<Integer> updateEventField(final String field, final String value, @Nullable final EventInfoWrapper[] toUpdate) {

        return Observable.just(toUpdate).flatMap(new Func1<EventInfoWrapper[], Observable<Integer>>() {
            @Override
            public Observable<Integer> call(EventInfoWrapper[] eventInfoWrappers) {
                int[] ids = new int[eventInfoWrappers.length];
                for (int i = 0; i < eventInfoWrappers.length; i++)
                    ids[i] = eventInfoWrappers[i].getId();
                return updateEventFieldByIDs(field, value, ids);
            }
        });
    }

    public Observable<Integer> updateEventFieldByIDs(final String field, final String value, @Nullable final int[] toUpdate) {

        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                String query = "";
                if (toUpdate == null) {
                    query = String.format("UPDATE %s SET %s=%s ",
                            AppDatabaseContract.EventListTable.TABLE_NAME,
                            field,
                            value);
                } else if (toUpdate.length > 0) {
                    query = String.format("UPDATE %s SET %s=%s WHERE %s IN (",
                            AppDatabaseContract.EventListTable.TABLE_NAME,
                            field,
                            value,
                            AppDatabaseContract.EventListTable._ID);
                    for (int event : toUpdate) query += event + ", ";
                    query = query.substring(0, query.length() - 2);
                    query += ")";
                } else {
                    subscriber.onCompleted();
                }
                try {
                    db.execSQL(query, new String[0]);
                } catch (Exception e) {
                    subscriber.onError(e);
                }
                subscriber.onNext(1);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<EventInfoWrapper> getEvents(@Nullable final String[] whereclauses, @Nullable final String orderBy) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                String query = String.format("SELECT * FROM %s ", AppDatabaseContract.EventListTable.TABLE_NAME);
                if (whereclauses != null && whereclauses.length > 0) {
                    query += "WHERE ";
                    for (String wc : whereclauses) query += " " + wc + " AND";
                    query = query.substring(0, query.length() - 3);
                }
                if (orderBy != null) query += "ORDER BY " + orderBy;
                subscriber.onNext(query);
                subscriber.onCompleted();
            }
        }).flatMap(new Func1<String, Observable<Cursor>>() {
            @Override
            public Observable<Cursor> call(String s) {
                return query(s);
            }
        }).flatMap(new Func1<Cursor, Observable<EventInfoWrapper>>() {
            @Override
            public Observable<EventInfoWrapper> call(Cursor cursor) {
                return getEventsFromCursor(cursor);
            }
        });
    }
}
