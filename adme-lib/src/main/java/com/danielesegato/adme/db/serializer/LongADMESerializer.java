package com.danielesegato.adme.db.serializer;

import android.content.ContentValues;
import android.database.Cursor;

import com.danielesegato.adme.config.ADMEFieldConfig;
import com.danielesegato.adme.config.SQLiteType;

/**
 * Persist a primitive long in SQLite database.
 */
public class LongADMESerializer extends BaseADMESerializer {
    private static LongADMESerializer singleton = new LongADMESerializer();

    public static LongADMESerializer getSingleton() {
        return singleton;
    }

    @Override
    public SQLiteType getSQLiteType() {
        return SQLiteType.INTEGER;
    }

    @Override
    public Object sqlToJava(Cursor cursor, int columnPos, ADMEFieldConfig fieldConfig) {
        return cursor.getLong(columnPos);
    }

    @Override
    public String stringToSqlRaw(String val, ADMEFieldConfig fieldConfig) {
        return Long.toString(Long.parseLong(val));
    }

    @Override
    public void storeInContentValues(String key, ContentValues values, Object fieldValue, ADMEFieldConfig fieldConfig) throws IllegalArgumentException {
        if (fieldValue == null || !(fieldValue instanceof Long)) {
            throw new IllegalArgumentException(String.format(
                    String.format("Field value for entity %s field %s can't be considered a primitive long for key %s: %s",
                            fieldConfig.getADMEEntityConfig().getEntityName(),
                            fieldConfig.getColumnName(),
                            key,
                            fieldValue)
            ));
        }
        values.put(key, (Long) fieldValue);
    }
}
