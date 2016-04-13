package org.ramonaza.unofficialazaapp.people.backend;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import org.ramonaza.unofficialazaapp.database.AppDatabaseContract;
import org.ramonaza.unofficialazaapp.helpers.backend.BaseDatabaseHandler;
import org.ramonazaapi.events.EventInfoWrapper;

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

    public static EventInfoWrapper[] getEventsFromCursor(Cursor queryResults) {
        if (queryResults.getCount() == 0) {
            return new EventInfoWrapper[0];
        }
        EventInfoWrapper[] events = new EventInfoWrapper[queryResults.getCount()];
        int i = 0;
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
            events[i] = temp;
            i++;
        } while (queryResults.moveToNext());
        return events;
    }

    public void close() {
        db.close();
    }

    public void addEvent(EventInfoWrapper toAdd) throws EventCSVReadError {
        ContentValues value = new ContentValues();
        value.put(AppDatabaseContract.EventListTable.COLUMN_NAME, toAdd.getName());
        value.put(AppDatabaseContract.EventListTable.COLUMN_DESC, toAdd.getDesc());
        value.put(AppDatabaseContract.EventListTable.COLUMN_PLANNER, toAdd.getPlanner());
        value.put(AppDatabaseContract.EventListTable.COLUMN_MEET, toAdd.getMeet());
        value.put(AppDatabaseContract.EventListTable.COLUMN_LOCATION, toAdd.getMapsLocation());
        value.put(AppDatabaseContract.EventListTable.COLUMN_BRING, toAdd.getBring());
        value.put(AppDatabaseContract.EventListTable.COLUMN_DATE, toAdd.getDate());

        long rowId = db.insert(AppDatabaseContract.EventListTable.TABLE_NAME, null, value);
        if (rowId == -1l) throw new EventCSVReadError("Null Event Read", toAdd);
        else toAdd.setId((int) rowId);
    }

    public void updateEvent(EventInfoWrapper toUpdate) throws EventCSVReadError {
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
        if (rowId == -1l) throw new EventCSVReadError("Null Event Read", toUpdate);
    }

    public void deleteEvent(int toDelete) {
        db.delete(AppDatabaseContract.EventListTable.TABLE_NAME, "?=?", new String[]{
                AppDatabaseContract.EventListTable._ID,
                "" + toDelete
        });
    }


    public void deleteEvents(@Nullable String whereClauses, @Nullable String[] whereArgs) {
        db.delete(AppDatabaseContract.EventListTable.TABLE_NAME, whereClauses, whereArgs);
    }

    public EventInfoWrapper getEvent(int id) {
        String query = String.format("SELECT * FROM %s WHERE %s=%d LIMIT 1",
                AppDatabaseContract.EventListTable.TABLE_NAME,
                AppDatabaseContract.EventListTable._ID,
                id
        );
        Cursor cursor = db.rawQuery(query, null);
        EventInfoWrapper[] EventArray = getEventsFromCursor(cursor);
        return EventArray[0];
    }

    public void updateEventField(String field, String value, @Nullable EventInfoWrapper[] toUpdate) {
        String query;
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
            for (EventInfoWrapper event : toUpdate) query += event.getId() + ", ";
            query = query.substring(0, query.length() - 2);
            query += ")";
        } else {
            return;
        }
        db.execSQL(query, new String[0]);
    }

    public void updateEventFieldByIDs(String field, String value, @Nullable int[] toUpdate) {
        String query;
        if (toUpdate == null) {
            query = String.format("UPDATE %s SET %s=%s ",
                    AppDatabaseContract.EventListTable.TABLE_NAME,
                    field);
        } else if (toUpdate.length > 0) {
            query = String.format("UPDATE %s SET %s=%s WHERE %s IN (",
                    AppDatabaseContract.EventListTable.TABLE_NAME,
                    field,
                    value,
                    AppDatabaseContract.EventListTable._ID);
        } else {
            return;
        }
        for (int event : toUpdate) query += event + ", ";
        query = query.substring(0, query.length() - 2);
        query += ")";
        db.execSQL(query, new String[0]);
    }

    public EventInfoWrapper[] getEvents(@Nullable String[] whereclauses, @Nullable String orderBy) {
        String query = String.format("SELECT * FROM %s ", AppDatabaseContract.EventListTable.TABLE_NAME);
        if (whereclauses != null && whereclauses.length > 0) {
            query += "WHERE ";
            for (String wc : whereclauses) query += " " + wc + " AND";
            query = query.substring(0, query.length() - 3);
        }
        if (orderBy != null) query += "ORDER BY " + orderBy;
        Cursor queryResults = db.rawQuery(query, null);
        queryResults.moveToFirst();
        return getEventsFromCursor(queryResults);
    }


    public class EventCSVReadError extends Exception {
        public EventCSVReadError(String errorMessage, EventInfoWrapper erroredEvent) {
            super(String.format("%s ON %s", errorMessage, erroredEvent));

        }
    }
}
