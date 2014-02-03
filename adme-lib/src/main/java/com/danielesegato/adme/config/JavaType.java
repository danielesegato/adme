package com.danielesegato.adme.config;

import com.danielesegato.adme.db.ADMESerializer;
import com.danielesegato.adme.db.serializer.BooleanADMESerializer;
import com.danielesegato.adme.db.serializer.BooleanObjectADMESerializer;
import com.danielesegato.adme.db.serializer.DateAsStringADMESerializer;
import com.danielesegato.adme.db.serializer.DateAsTimestampADMESerializer;
import com.danielesegato.adme.db.serializer.DoubleADMESerializer;
import com.danielesegato.adme.db.serializer.DoubleObjectADMESerializer;
import com.danielesegato.adme.db.serializer.EnumIntADMESerializer;
import com.danielesegato.adme.db.serializer.EnumStringADMESerializer;
import com.danielesegato.adme.db.serializer.IntADMESerializer;
import com.danielesegato.adme.db.serializer.IntObjectADMESerializer;
import com.danielesegato.adme.db.serializer.LongADMESerializer;
import com.danielesegato.adme.db.serializer.LongObjectADMESerializer;
import com.danielesegato.adme.db.serializer.StringADMESerializer;

/**
 * Enumeration of java types with the mapping to their SQLite serializer
 */
public enum JavaType {

    /**
     * Persists the {@link String} Java class.
     */
    STRING(StringADMESerializer.getSingleton()),
    /**
     * Persists the boolean Java primitive.
     */
    BOOLEAN(BooleanADMESerializer.getSingleton()),
    /**
     * Persists the {@link Boolean} object Java class.
     */
    BOOLEAN_OBJ(BooleanObjectADMESerializer.getSingleton()),

    /**
     * Persists the {@link java.util.Date} Java class as long milliseconds since epoch.
     */
    DATE_LONG(DateAsTimestampADMESerializer.getSingleton()),
    /**
     * Persists the {@link java.util.Date} Java class as a string of a format.
     */
    DATE_STRING(DateAsStringADMESerializer.getSingleton()),
    /**
     * Persists the int primitive.
     */
    INTEGER(IntADMESerializer.getSingleton()),
    /**
     * Persists the {@link Integer} object Java class.
     */
    INTEGER_OBJ(IntObjectADMESerializer.getSingleton()),
    /**
     * Persists the long primitive.
     */
    LONG(LongADMESerializer.getSingleton()),
    /**
     * Persists the {@link Long} object Java class.
     */
    LONG_OBJ(LongObjectADMESerializer.getSingleton()),
    /**
     * Persists the double primitive.
     */
    DOUBLE(DoubleADMESerializer.getSingleton()),
    /**
     * Persists the {@link Double} object Java class.
     */
    DOUBLE_OBJ(DoubleObjectADMESerializer.getSingleton()),
    /**
     * Persists an Enum Java class as its string value. You can also specify the {@link #ENUM_INTEGER} as the type.
     */
    ENUM_STRING(EnumStringADMESerializer.getSingleton()),
    /**
     * Persists an Enum Java class as its ordinal integer value. You can also specify the {@link #ENUM_STRING} as the
     * type.
     */
    ENUM_INTEGER(EnumIntADMESerializer.getSingleton()),
    /**
     * Marker for fields that are unknown.
     */
    UNKNOWN(null),
    // end
    ;

    private final ADMESerializer admeSerializer;

    private JavaType(ADMESerializer admeSerializer) {
        this.admeSerializer = admeSerializer;
    }

    public ADMESerializer getADMESerializer() {
        return admeSerializer;
    }
}
