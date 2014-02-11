package com.danielesegato.adme.db.serializer;

import android.content.ContentValues;
import android.database.Cursor;

import com.danielesegato.adme.config.ADMEFieldConfig;
import com.danielesegato.adme.config.SQLiteType;

/**
 * Persist an {@link java.lang.Long} in the SQLite database, it can be null.
 */
public class LongObjectADMESerializer extends BaseADMESerializer {
    private static LongObjectADMESerializer singleton = new LongObjectADMESerializer();

    public static LongObjectADMESerializer getSingleton() {
        return singleton;
    }

    @Override
    public SQLiteType getSQLiteType() {
        return SQLiteType.INTEGER;
    }

    @Override
    public Object sqlToJava(Cursor cursor, int columnPos, ADMEFieldConfig fieldConfig) {
        return cursor.isNull(columnPos) ? null : cursor.getLong(columnPos);
    }

    @Override
    public String stringToSqlRaw(String val, ADMEFieldConfig fieldConfig) {
        return val != null ? Long.toString(Long.parseLong(val)) : NULL_RAW;
    }

    @Override
    public void storeInContentValues(String key, ContentValues values, Object fieldValue, ADMEFieldConfig fieldConfig) throws IllegalArgumentException {
        if (fieldValue != null && !(fieldValue instanceof Long)) {
            throw new IllegalArgumentException(String.format(
                    String.format("Field value for entity %s field %s can't be considered a Long for key %s: %s",
                            fieldConfig.getADMEEntityConfig().getEntityName(),
                            fieldConfig.getColumnName(),
                            key,
                            fieldValue)
            ));
        }
        if (fieldValue != null) {
            values.put(key, (Long) fieldValue);
        } else {
            values.putNull(key);
        }
    }
}
