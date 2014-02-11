package com.danielesegato.adme.db.serializer;

import android.content.ContentValues;
import android.database.Cursor;

import com.danielesegato.adme.config.ADMEFieldConfig;

import java.math.BigDecimal;

/**
 * Serialize and deserialize data to/from the sqlite database for a {@link java.math.BigDecimal}
 */
public class BigDecimalADMESerializer extends StringADMESerializer {
    @Override
    public Object sqlToJava(Cursor cursor, int columnPos, ADMEFieldConfig fieldConfig) {
        String bDecStr = (String) super.sqlToJava(cursor, columnPos, fieldConfig);
        try {
            return bDecStr != null ? new BigDecimal(bDecStr) : null;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format(
                    "Field %s in table %s for cursor column %d (%s) with value: %s can't be converted to a BigDecimal",
                    fieldConfig.getJavaField().getName(), fieldConfig.getADMEEntityConfig().getEntityName(), columnPos, cursor.getColumnName(columnPos), bDecStr
            ), e);
        }
    }

    @Override
    public String stringToSqlRaw(String val, ADMEFieldConfig fieldConfig) {
        try {
            new BigDecimal(val);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format(
                    "The value %s for field %s in table %s can't be converted to a BigDecimal",
                    val, fieldConfig.getJavaField().getName(), fieldConfig.getADMEEntityConfig().getEntityName()
            ), e);
        }
        return super.stringToSqlRaw(val, fieldConfig);
    }

    @Override
    public void storeInContentValues(String key, ContentValues values, Object fieldValue, ADMEFieldConfig fieldConfig) throws IllegalArgumentException {
        if (fieldValue != null && !(fieldValue instanceof BigDecimal)) {
            throw new IllegalArgumentException(String.format(
                    String.format("Field value for entity %s field %s can't be considered a BigDecimal for key %s: %s",
                            fieldConfig.getADMEEntityConfig().getEntityName(),
                            fieldConfig.getColumnName(),
                            key,
                            fieldValue)
            ));
        }
        if (fieldValue != null) {
            values.put(key, ((BigDecimal) fieldValue).toPlainString());
        } else {
            values.putNull(key);
        }
    }
}
