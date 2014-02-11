package com.danielesegato.adme.db.serializer;

import android.content.ContentValues;
import android.database.Cursor;

import com.danielesegato.adme.config.ADMEFieldConfig;
import com.danielesegato.adme.config.SQLiteType;

/**
 * Persist a {@link java.lang.Boolean} object in the SQLite database (can be null).
 */
public class BooleanObjectADMESerializer extends BaseADMESerializer {

    private static BooleanObjectADMESerializer singleton = new BooleanObjectADMESerializer();

    public static BooleanObjectADMESerializer getSingleton() {
        return singleton;
    }

    @Override
    public SQLiteType getSQLiteType() {
        return SQLiteType.NUMERIC;
    }

    @Override
    public Object sqlToJava(Cursor cursor, int columnPos, ADMEFieldConfig fieldConfig) {
        return cursor.isNull(columnPos) ? null : cursor.getInt(columnPos) == 1;
    }

    @Override
    public String stringToSqlRaw(String val, ADMEFieldConfig fieldConfig) {
        return val != null ? (val.equals(BooleanADMESerializer.BOOLEAN_TRUE) ? BooleanADMESerializer.BOOLEAN_TRUE : BooleanADMESerializer.BOOLEAN_FALSE) : NULL_RAW;
    }

    @Override
    public void storeInContentValues(String key, ContentValues values, Object fieldValue, ADMEFieldConfig fieldConfig) throws IllegalArgumentException {
        if (fieldValue != null && !(fieldValue instanceof Boolean)) {
            throw new IllegalArgumentException(String.format(
                    String.format("Field value for entity %s field %s can't be considered a Boolean for key %s: %s",
                            fieldConfig.getADMEEntityConfig().getEntityName(),
                            fieldConfig.getColumnName(),
                            key,
                            fieldValue)
            ));
        }
        if (fieldValue != null) {
            values.put(key, (Boolean) fieldValue);
        } else {
            values.putNull(key);
        }
    }
}
