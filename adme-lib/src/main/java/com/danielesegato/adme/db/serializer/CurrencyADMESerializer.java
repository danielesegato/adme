package com.danielesegato.adme.db.serializer;

import android.content.ContentValues;
import android.database.Cursor;

import com.danielesegato.adme.config.ADMEFieldConfig;

import java.util.Currency;

/**
 * Serialize and deserialize data to/from the sqlite database for a {@link java.util.Currency}
 */
public class CurrencyADMESerializer extends StringADMESerializer {
    @Override
    public Object sqlToJava(Cursor cursor, int columnPos, ADMEFieldConfig fieldConfig) {
        String currency = (String) super.sqlToJava(cursor, columnPos, fieldConfig);
        try {
            return currency != null ? Currency.getInstance(currency) : null;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format(
                    "Field %s in table %s for cursor column %d (%s) with value: %s can't be converted to a Currency, not an ISO 4217 code",
                    fieldConfig.getJavaField().getName(), fieldConfig.getADMEEntityConfig().getEntityName(), columnPos, cursor.getColumnName(columnPos), currency
            ), e);
        }
    }

    @Override
    public String stringToSqlRaw(String val, ADMEFieldConfig fieldConfig) {
        try {
            Currency.getInstance(val);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format(
                    "The value %s for field %s in table %s can't be converted to a Currency, not an ISO 4217 code",
                    val, fieldConfig.getJavaField().getName(), fieldConfig.getADMEEntityConfig().getEntityName()
            ), e);
        }
        return super.stringToSqlRaw(val, fieldConfig);
    }

    @Override
    public void storeInContentValues(String key, ContentValues values, Object fieldValue, ADMEFieldConfig fieldConfig) throws IllegalArgumentException {
        if (fieldValue != null && !(fieldValue instanceof Currency)) {
            throw new IllegalArgumentException(String.format(
                    String.format("Field value for entity %s field %s can't be considered a Currency, not an ISO 4217 code for key %s: %s",
                            fieldConfig.getADMEEntityConfig().getEntityName(),
                            fieldConfig.getColumnName(),
                            key,
                            fieldValue)
            ));
        }
        if (fieldValue != null) {
            values.put(key, ((Currency) fieldValue).getCurrencyCode());
        } else {
            values.putNull(key);
        }
    }
}
