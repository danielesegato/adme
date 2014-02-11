package com.danielesegato.adme.db.serializer;

import android.content.ContentValues;
import android.database.Cursor;

import com.danielesegato.adme.config.ADMEFieldConfig;
import com.danielesegato.adme.config.SQLiteType;
import com.danielesegato.adme.utils.DateHelper;

import java.text.ParseException;
import java.util.Date;

/**
 * Persist a {@link java.util.Date} as an ISO8601 (UTC) string in the SQLite database. This storage
 * data type allow usage of %like% queries on the database and is more readable then storing the timestamp.
 *
 * @see com.danielesegato.adme.db.serializer.DateAsTimestampADMESerializer
 */
public class DateAsStringADMESerializer extends BaseADMESerializer {
    private static DateAsStringADMESerializer singleton = new DateAsStringADMESerializer();

    public static DateAsStringADMESerializer getSingleton() {
        return singleton;
    }

    @Override
    public SQLiteType getSQLiteType() {
        return SQLiteType.TEXT;
    }

    @Override
    public Object sqlToJava(Cursor cursor, int columnPos, ADMEFieldConfig fieldConfig) {
        try {
            return cursor.isNull(columnPos) ? null : DateHelper.parseUTCDateTime(cursor.getString(columnPos));
        } catch (ParseException e) {
            throw new IllegalStateException(
                    String.format("Couldn't parse date from column %s (pos=%d), value is not an ISO8601 date: %s",
                            cursor.getColumnName(columnPos), columnPos, cursor.getString(columnPos)
                    ), e);
        }
    }

    @Override
    public String stringToSqlRaw(String val, ADMEFieldConfig fieldConfig) {
        try {
            return val != null ? DateHelper.formatUTCDateTime(DateHelper.parseUTCDateTime(val)) : null;
        } catch (ParseException e) {
            throw new IllegalStateException(
                    String.format("Invalid date format, the following string is not an ISO8601 date: %s",
                            val
                    ), e);
        }
    }

    @Override
    public void storeInContentValues(String key, ContentValues values, Object fieldValue, ADMEFieldConfig fieldConfig) throws IllegalArgumentException {
        if (fieldValue != null && !(fieldValue instanceof Date)) {
            throw new IllegalArgumentException(String.format(
                    String.format("Field value for entity %s field %s can't be considered a Date for key %s: %s",
                            fieldConfig.getADMEEntityConfig().getEntityName(),
                            fieldConfig.getColumnName(),
                            key,
                            fieldValue)
            ));
        }
        if (fieldValue != null) {
            values.put(key, DateHelper.formatUTCDateTime((Date) fieldValue));
        } else {
            values.putNull(key);
        }
    }
}
