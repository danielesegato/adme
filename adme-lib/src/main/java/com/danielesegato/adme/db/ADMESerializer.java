package com.danielesegato.adme.db;

import android.content.ContentValues;
import android.database.Cursor;

import com.danielesegato.adme.config.ADMEFieldConfig;
import com.danielesegato.adme.config.SQLiteType;

/**
 * A Serializer to convert from/to the SQLite database and Java classes/primitives.
 */
public interface ADMESerializer {
    /**
     * Null value for SQLite
     */
    String NULL_RAW = "NULL";

    /**
     * @return the SQLite data type associated to this serializer.
     */
    SQLiteType getSQLiteType();

    /**
     * Convert an SQL value into it's java Object / primitive type.
     *
     * @param cursor      the Android Cursor.
     * @param columnPos   the column position to read
     * @param fieldConfig the configuration to the associated field (can be used by the serializer)
     * @return the java value of the object
     */
    Object sqlToJava(Cursor cursor, int columnPos, ADMEFieldConfig fieldConfig);

    /**
     * Convert a String value to it's raw string value ready for insertion in a statement for the
     * SQLite database
     *
     * @param val         the string value of the content to put in the database
     * @param fieldConfig the configuration to the associated field (can be used by the serializer)
     * @return the raw string value ready to be placed in the SQLite statement.
     */
    String stringToSqlRaw(String val, ADMEFieldConfig fieldConfig);

    /**
     * Put a fieldValue into an Android {@link android.content.ContentValues}.
     *
     * @param key         the key this value should have in the ContentValues
     * @param values      the non null ContentValues container
     * @param fieldValue  the field value, it should be compatible with the type of this serializer
     * @param fieldConfig the configuration to the associated field (can be used by the serializer)
     * @throws java.lang.IllegalArgumentException if the fieldValue is not cast-able to a type handled
     *                                            by this serializer or, for example, if the field should be a primitive non null and a null value
     *                                            is passed
     */
    void storeInContentValues(String key, ContentValues values, Object fieldValue, ADMEFieldConfig fieldConfig) throws IllegalArgumentException;
}
