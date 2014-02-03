package com.danielesegato.adme.db.serializer;

import android.content.ContentValues;
import android.database.Cursor;

import com.danielesegato.adme.config.ADMEFieldConfig;
import com.danielesegato.adme.config.SQLiteType;

import java.util.Date;

/**
 * Persist a {@link java.util.Date} as the timestamp (milliseconds) in the SQLite database.
 */
public class DateAsTimestampADMESerializer extends BaseADMESerializer {
    private static DateAsTimestampADMESerializer singleton = new DateAsTimestampADMESerializer();

    public static DateAsTimestampADMESerializer getSingleton() {
        return singleton;
    }

    @Override
    public SQLiteType getSQLiteType() {
        return SQLiteType.INTEGER;
    }

    @Override
    public Date sqlToJava(Cursor cursor, int columnPos, ADMEFieldConfig fieldConfig) {
        return cursor.isNull(columnPos) ? null : new Date(cursor.getLong(columnPos));
    }

    @Override
    public String stringToSqlRaw(String val, ADMEFieldConfig fieldConfig) {
        return val != null ? Long.toString(Long.parseLong(val)) : NULL_RAW;
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
            values.put(key, ((Date) fieldValue).getTime());
        } else {
            values.putNull(key);
        }
    }
}
