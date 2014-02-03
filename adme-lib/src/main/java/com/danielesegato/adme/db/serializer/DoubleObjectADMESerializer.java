package com.danielesegato.adme.db.serializer;

import android.content.ContentValues;
import android.database.Cursor;

import com.danielesegato.adme.config.ADMEFieldConfig;
import com.danielesegato.adme.config.SQLiteType;

/**
 * Persist a {@link java.lang.Double} object in the SQLite database, it can be null.
 */
public class DoubleObjectADMESerializer extends BaseADMESerializer {
    private static DoubleObjectADMESerializer singleton = new DoubleObjectADMESerializer();

    public static DoubleObjectADMESerializer getSingleton() {
        return singleton;
    }

    @Override
    public SQLiteType getSQLiteType() {
        return SQLiteType.REAL;
    }

    @Override
    public Double sqlToJava(Cursor cursor, int columnPos, ADMEFieldConfig fieldConfig) {
        return cursor.isNull(columnPos) ? null : cursor.getDouble(columnPos);
    }

    @Override
    public String stringToSqlRaw(String val, ADMEFieldConfig fieldConfig) {
        return val != null ? Double.toString(Double.parseDouble(val)) : NULL_RAW;
    }

    @Override
    public void storeInContentValues(String key, ContentValues values, Object fieldValue, ADMEFieldConfig fieldConfig) throws IllegalArgumentException {
        if (fieldValue != null && !(fieldValue instanceof Double)) {
            throw new IllegalArgumentException(String.format(
                    String.format("Field value for entity %s field %s can't be considered a Double for key %s: %s",
                            fieldConfig.getADMEEntityConfig().getEntityName(),
                            fieldConfig.getColumnName(),
                            key,
                            fieldValue)
            ));
        }
        if (fieldValue != null) {
            values.put(key, (Double) fieldValue);
        } else {
            values.putNull(key);
        }
    }
}
