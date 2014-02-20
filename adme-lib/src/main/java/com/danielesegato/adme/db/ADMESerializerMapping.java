package com.danielesegato.adme.db;

import com.danielesegato.adme.db.serializer.BigDecimalADMESerializer;
import com.danielesegato.adme.db.serializer.BooleanADMESerializer;
import com.danielesegato.adme.db.serializer.BooleanObjectADMESerializer;
import com.danielesegato.adme.db.serializer.CurrencyADMESerializer;
import com.danielesegato.adme.db.serializer.DateAsStringADMESerializer;
import com.danielesegato.adme.db.serializer.DoubleADMESerializer;
import com.danielesegato.adme.db.serializer.DoubleObjectADMESerializer;
import com.danielesegato.adme.db.serializer.EnumStringADMESerializer;
import com.danielesegato.adme.db.serializer.IntADMESerializer;
import com.danielesegato.adme.db.serializer.IntObjectADMESerializer;
import com.danielesegato.adme.db.serializer.LongADMESerializer;
import com.danielesegato.adme.db.serializer.LongObjectADMESerializer;
import com.danielesegato.adme.db.serializer.StringADMESerializer;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Define the default mapping between Java types and serializers.
 * <p/>
 * Currently this are the supported java types:
 * <ul>
 * <li>long</li>
 * <li>int</li>
 * <li>double</li>
 * <li>boolean</li>
 * <li>String</li>
 * <li>Long</li>
 * <li>Integer</li>
 * <li>Double</li>
 * <li>Boolean</li>
 * <li>Date: as ISO8601 string (UTC)</li>
 * <li>Enum: as String</li>
 * <li>{@link java.math.BigDecimal}</li>
 * <li>{@link java.util.Currency}</li>
 * </ul>
 * <p/>
 * But you can register your own serializer or override the default ones.
 */
public class ADMESerializerMapping {
    private static final Map<Class<?>, ADMESerializer> DEFAULT_TYPE_MAP;
    private static final Map<Class<?>, ADMESerializer> CUSTOM_TYPE_MAP;

    static {
        DEFAULT_TYPE_MAP = new HashMap<Class<?>, ADMESerializer>();
        DEFAULT_TYPE_MAP.put(long.class, LongADMESerializer.getSingleton());
        DEFAULT_TYPE_MAP.put(int.class, IntADMESerializer.getSingleton());
        DEFAULT_TYPE_MAP.put(double.class, DoubleADMESerializer.getSingleton());
        DEFAULT_TYPE_MAP.put(boolean.class, BooleanADMESerializer.getSingleton());

        DEFAULT_TYPE_MAP.put(String.class, StringADMESerializer.getSingleton());
        DEFAULT_TYPE_MAP.put(Long.class, LongObjectADMESerializer.getSingleton());
        DEFAULT_TYPE_MAP.put(Integer.class, IntObjectADMESerializer.getSingleton());
        DEFAULT_TYPE_MAP.put(Double.class, DoubleObjectADMESerializer.getSingleton());
        DEFAULT_TYPE_MAP.put(Boolean.class, BooleanObjectADMESerializer.getSingleton());

        // prefer readability and %like% search on date
        DEFAULT_TYPE_MAP.put(Date.class, DateAsStringADMESerializer.getSingleton());
//        prefer performance (no %like% search)
//        DEFAULT_TYPE_MAP.put(Date.class, DateAsTimestampADMESerializer.getSingleton());

//        prefer performance (careful in adding an enum)
//        DEFAULT_TYPE_MAP.put(Enum.class, EnumIntADMESerializer.getSingleton());
//        prefer readability and ease up upgrades (using the name doesn't link the enum order to the DB content)
        DEFAULT_TYPE_MAP.put(Enum.class, EnumStringADMESerializer.getSingleton());

        DEFAULT_TYPE_MAP.put(BigDecimal.class, BigDecimalADMESerializer.getSingleton());
        DEFAULT_TYPE_MAP.put(Currency.class, CurrencyADMESerializer.getSingleton());

        CUSTOM_TYPE_MAP = new HashMap<Class<?>, ADMESerializer>();
    }

    public static ADMESerializer getADMESerializerForClass(Class<?> clazz, boolean convertPrimitiveToWrapperObject) {
        if (convertPrimitiveToWrapperObject) {
            clazz = convertPrimitiveToWrapperObject(clazz);
        }
        ADMESerializer admeSerializer = getCustomADMESerializer(clazz);
        if (admeSerializer != null) {
            return admeSerializer;
        }
        admeSerializer = getDefaultADMESerializer(clazz);
        if (admeSerializer != null) {
            return admeSerializer;
        }
        throw new IllegalArgumentException(String.format(
                "Couldn't find a ADME serializer for class %s", clazz.getName()
        ));
    }

    private static Class<?> convertPrimitiveToWrapperObject(Class<?> clazz) {
        if (!clazz.isPrimitive()) {
            return clazz;
        }
        else if (clazz == long.class) {
            clazz = Long.class;
        } else if (clazz == int.class) {
            clazz = Integer.class;
        } else if (clazz == double.class) {
            clazz = Double.class;
        } else if (clazz == boolean.class) {
            clazz = Boolean.class;
        } else {
            throw new UnsupportedOperationException("Unsupported primitive to wrapper type conversion: " + clazz);
        }
        return clazz;
    }

    private static ADMESerializer getADMESerializerForClass(Class<?> clazz) {
        ADMESerializer admeSerializer = getCustomADMESerializer(clazz);
        if (admeSerializer != null) {
            return admeSerializer;
        }
        admeSerializer = getDefaultADMESerializer(clazz);
        if (admeSerializer != null) {
            return admeSerializer;
        }
        return null;
    }

    private static ADMESerializer getDefaultADMESerializer(Class<?> clazz) {
        return DEFAULT_TYPE_MAP.get(clazz);
    }

    private static ADMESerializer getCustomADMESerializer(Class<?> clazz) {
        return CUSTOM_TYPE_MAP.get(clazz);
    }

    public static void registerSerializer(Class<?> clazz, ADMESerializer serializer) {
        CUSTOM_TYPE_MAP.put(clazz, serializer);
    }

    public static void unregisterSerializer(Class<?> clazz) {
        CUSTOM_TYPE_MAP.remove(clazz);
    }
}
