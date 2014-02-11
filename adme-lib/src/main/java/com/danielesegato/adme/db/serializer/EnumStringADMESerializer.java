package com.danielesegato.adme.db.serializer;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.danielesegato.adme.config.ADMEFieldConfig;
import com.danielesegato.adme.config.SQLiteType;

import java.lang.reflect.Field;

/**
 * Persist an {@link java.lang.Enum} in the database, as string. This storage data type is less
 * performance-efficient then storing integers but provide better flexibility in developing and upgrading
 * your enum because you do not have to worry about their order, you just have to avoid renaming them.
 *
 * This is the default configured way of storing an Enum in the database but you can override it with
 * your own {@link com.danielesegato.adme.db.ADMESerializer}.
 *
 * @see com.danielesegato.adme.db.serializer.EnumIntADMESerializer
 * @see com.danielesegato.adme.ADME#registerADMESerializer(Class, com.danielesegato.adme.db.ADMESerializer)
 */
public class EnumStringADMESerializer extends BaseADMESerializer {
    private static final String LOG_TAG = EnumStringADMESerializer.class.getSimpleName();
    private static EnumStringADMESerializer singleton = new EnumStringADMESerializer();

    public static EnumStringADMESerializer getSingleton() {
        return singleton;
    }

    @Override
    public SQLiteType getSQLiteType() {
        return SQLiteType.TEXT;
    }

    @Override
    public Object sqlToJava(Cursor cursor, int columnPos, ADMEFieldConfig fieldConfig) {
        Field field = getEnumFieldOrThrow(fieldConfig);
        if (cursor.isNull(columnPos)) {
            return null;
        }
        String enumName = cursor.getString(columnPos);
        return getEnumByName(field, enumName, fieldConfig);
    }

    @Override
    public String stringToSqlRaw(String val, ADMEFieldConfig fieldConfig) {
        Enum<?> enumByName = getEnumByName(getEnumFieldOrThrow(fieldConfig), val, fieldConfig);
        return String.format("'%s'", enumByName.name().replaceAll("'", "\\'"));
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
            Enum<?> checkedEnum = getEnumByName(field, ((Enum<?>)fieldValue).name(), fieldConfig);
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

    private Enum<?> getEnumByName(Field field, String enumName, ADMEFieldConfig fieldConfig) {
        try {
            return Enum.valueOf((Class<? extends Enum>)field.getType(), enumName);
        } catch (IllegalArgumentException notFound) {
            if (fieldConfig.getFallbackEnumName() == null || fieldConfig.getFallbackEnumName().length() == 0) {
                throw notFound;
            }
            Enum<?> fallbackEnum = null;
            try {
                fallbackEnum = Enum.valueOf((Class<? extends Enum>)field.getType(), fieldConfig.getFallbackEnumName());
            } catch (IllegalArgumentException notFoundUnknown) {
                throw new IllegalArgumentException(String.format("Unknown enum for name %s in field %s for entity %s, configured fallback replacement '%s' not found for enum %s",
                        enumName, field.getName(), fieldConfig.getADMEEntityConfig().getEntityName(), fieldConfig.getFallbackEnumName(), field.getType()));
            }
            if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
                Log.d(LOG_TAG, String.format("Unknown enum name %s in field %s for entity %s, replacing with configured fallback %d (%s)",
                        enumName, field.getName(), fieldConfig.getADMEEntityConfig().getEntityName(), fallbackEnum.ordinal(), fallbackEnum.name()));
            }
            return fallbackEnum;
        }
    }
}
