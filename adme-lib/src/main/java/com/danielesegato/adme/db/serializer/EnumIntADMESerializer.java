package com.danielesegato.adme.db.serializer;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.danielesegato.adme.InternalADMEConsts;
import com.danielesegato.adme.config.ADMEFieldConfig;
import com.danielesegato.adme.config.SQLiteType;

import java.lang.reflect.Field;

/**
 * Persist an {@link java.lang.Enum} in the database, as integer. This storage data type is performance
 * efficient but you need to pay attention to the order you define your enums if you want to upgrade
 * your code. Another minor draw back of using this {@link com.danielesegato.adme.db.ADMESerializer}
 * is that your data is less readable then a string.
 *
 * By default this is not used but can be registered as serializer for Enum classes.
 *
 * @see com.danielesegato.adme.db.serializer.EnumStringADMESerializer
 * @see com.danielesegato.adme.ADME#registerADMESerializer(Class, com.danielesegato.adme.db.ADMESerializer)
 */
public class EnumIntADMESerializer extends BaseADMESerializer {
    private static final String LOGTAG = InternalADMEConsts.LOGTAG;
    private static EnumIntADMESerializer singleton = new EnumIntADMESerializer();

    public static EnumIntADMESerializer getSingleton() {
        return singleton;
    }

    @Override
    public SQLiteType getSQLiteType() {
        return SQLiteType.INTEGER;
    }

    @Override
    public Object sqlToJava(Cursor cursor, int columnPos, ADMEFieldConfig fieldConfig) {
        Field field = getEnumFieldOrThrow(fieldConfig);
        if (cursor.isNull(columnPos)) {
            return null;
        }
        int ordinal = cursor.getInt(columnPos);
        return getEnumByOrdinal(field, ordinal, fieldConfig);
    }

    @Override
    public String stringToSqlRaw(String val, ADMEFieldConfig fieldConfig) {
        Field field = getEnumFieldOrThrow(fieldConfig);
        int ordinal = Integer.parseInt(val);
        // check
        Enum<?> enumVal = getEnumByOrdinal(field, ordinal, fieldConfig);
        return Integer.toString(enumVal.ordinal());
    }

    @Override
    public void storeInContentValues(String key, ContentValues values, Object fieldValue, ADMEFieldConfig fieldConfig) throws IllegalArgumentException {
        Field field = getEnumFieldOrThrow(fieldConfig);
        if (fieldValue != null && !(fieldValue instanceof Enum)) {
            throw new IllegalArgumentException(String.format("Field value for entity %s field %s can't be considered an Enum for key %s: %s (%s)",
                    fieldConfig.getADMEEntityConfig().getEntityName(),
                    fieldConfig.getColumnName(),
                    key,
                    fieldValue,
                    fieldValue.getClass()));
        }
        if (fieldValue != null) {
            int ordinal = ((Enum<?>)fieldValue).ordinal();
            Enum<?> checkedEnum = getEnumByOrdinal(field, ordinal, fieldConfig);
            values.put(key, checkedEnum.ordinal());
        } else {
            values.putNull(key);
        }
    }

    private Field getEnumFieldOrThrow(ADMEFieldConfig fieldConfig) {
        Field field = fieldConfig.getJavaField();
        if (!field.isEnumConstant()) {
            throw new IllegalArgumentException(String.format("Field %s (%s) for entity %s is not an Enum",
                    field.getName(), field, fieldConfig.getADMEEntityConfig().getEntityName()));
        }
        return field;
    }

    private Enum<?> getEnumByOrdinal(Field field, int ordinal, ADMEFieldConfig fieldConfig) {
        try {
            return (Enum<?>) field.getType().getEnumConstants()[ordinal];
        } catch (IndexOutOfBoundsException e) {
            if (fieldConfig.getFallbackEnumName() == null || fieldConfig.getFallbackEnumName().length() == 0) {
                throw e;
            }
            Enum<?> fallbackEnum = null;
            try {
                fallbackEnum = Enum.valueOf((Class<? extends Enum>)field.getType(), fieldConfig.getFallbackEnumName());
            } catch (IllegalArgumentException notFound) {
                throw new IllegalArgumentException(String.format("Unknown enum for ordinal %d in field %s for entity %s, configured fallback replacement '%s' not found for enum %s",
                        ordinal, field.getName(), fieldConfig.getADMEEntityConfig().getEntityName(), fieldConfig.getFallbackEnumName(), field.getType()));
            }
            if (Log.isLoggable(LOGTAG, Log.DEBUG)) {
                Log.d(LOGTAG, String.format("Unknown enum ordinal %d in field %s for entity %s, replacing with configured fallback %d (%s)",
                        ordinal, field.getName(), fieldConfig.getADMEEntityConfig().getEntityName(), fallbackEnum.ordinal(), fallbackEnum.name()));
            }
            return fallbackEnum;
        }
    }
}
