/**
 *
 */
package com.danielesegato.adme.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Help with dates. Careful with {@link Date} methods, the date should be in UTC.
 */
public final class DateHelper {

    private static final ThreadLocal<DateFormat> ISO8601_UTC_FORMATTER = new ThreadLocal<DateFormat>();

    /**
     * The ISO8601 format specification (from w3c) without the timezone part.
     */
    private static final String ISO8601_CUSTOM_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    /**
     * Create a {@link SimpleDateFormat} per thread (not thread safe).
     *
     * @return create and store in the local thread an instance of {@link SimpleDateFormat}.
     */
    private static final DateFormat getIso8601UTCDateFormat() {
        DateFormat iso8601UTCDateFormat = ISO8601_UTC_FORMATTER.get();
        if (iso8601UTCDateFormat == null) {
            iso8601UTCDateFormat = new SimpleDateFormat(ISO8601_CUSTOM_FORMAT, Locale.UK);
            iso8601UTCDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            ISO8601_UTC_FORMATTER.set(iso8601UTCDateFormat);
        }
        return iso8601UTCDateFormat;
    }

    /**
     * Parse the date time string and return a {@link Date} object. If the parsing fail an exception is raised, if you
     * don't want to handle it see {@link #parseUTCDateTime(String, Date)} instead. The string is expected in the
     * ISO8601 CUSTOM format, see {@link #ISO8601_CUSTOM_FORMAT} for details.
     *
     * @param iso8601_datetime the string in the ISO8601 CUSTOM format
     * @return the {@link Date} object parsed
     * @throws ParseException if the iso8601_datetime is not in a valid format
     * @see #ISO8601_CUSTOM_FORMAT
     */
    public static final Date parseUTCDateTime(String iso8601_datetime) throws ParseException {
        return getIso8601UTCDateFormat().parse(iso8601_datetime);
    }

    /**
     * Parse the date time string and return a {@link Date} object. If the parsing fail or the iso8601_datetime is null
     * the _default value is returned. The string is expected in the ISO8601 CUSTOM format, see
     * {@link #ISO8601_CUSTOM_FORMAT} for details.
     *
     * @param iso8601_datetime the string in the ISO8601 CUSTOM format
     * @return the {@link Date} object parsed or _default in case of failure
     * @see #ISO8601_CUSTOM_FORMAT
     */
    public static final Date parseUTCDateTime(String iso8601_datetime, Date _default) {
        try {
            return iso8601_datetime != null ? parseUTCDateTime(iso8601_datetime) : _default;
        } catch (ParseException e) {
            return _default;
        }
    }

    /**
     * Format the datetime {@link Date} object into a Custom ISO8601 format string.
     *
     * @param datetime the {@link Date} object
     * @return the formatted string
     * @see #ISO8601_CUSTOM_FORMAT
     */
    public static final String formatUTCDateTime(Date datetime) {
        return getIso8601UTCDateFormat().format(datetime);
    }

}
