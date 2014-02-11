package com.danielesegato.adme.db.serializer;

import android.content.ContentValues;
import android.database.Cursor;

import com.danielesegato.adme.config.ADMEFieldConfig;
import com.danielesegato.adme.config.SQLiteType;

/**
 * Persist a primitive boolean in the SQLite database.
 */
public class BooleanADMESerializer extends BaseADMESerializer {

    public static final String BOOLEAN_TRUE = "1";
    public static final String BOOLEAN_FALSE = "0";
    private static BooleanADMESerializer singleton = new BooleanADMESerializer();

    public static BooleanADMESerializer getSingleton() {
        return singleton;
    }

    @Override
    public SQLiteType getSQLiteType() {
        return SQLiteType.NUMERIC;
    }

    @Override
    public Object sqlToJava(Cursor cursor, int columnPos, ADMEFieldConfig fieldConfig) {
        return cursor.getInt(columnPos) == 1;
    }

    @Override
    public String stringToSqlRaw(String val, ADMEFieldConfig fieldConfig) {
        return val.equals(BOOLEAN_TRUE) ? BOOLEAN_TRUE : BOOLEAN_FALSE;
    }

    @Override
    public void storeInContentValues(String key, ContentValues values, Object fieldValue, ADMEFieldConfig fieldConfig) throws IllegalArgumentException {
        if (fieldValue == null || !(fieldValue instanceof Boolean)) {
            throw new IllegalArgumentException(String.format(
                    String.format("Field value for entity %s field %s can't be considered a primitive boolean for key %s: %s",
                            fieldConfig.getADMEEntityConfig().getEntityName(),
                            fieldConfig.getColumnName(),
                            key,
                            fieldValue)
            ));
        }
        values.put(key, (Boolean) fieldValue);
    }
}
