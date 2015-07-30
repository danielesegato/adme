package com.danielesegato.adme.content;

import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.danielesegato.adme.ADME;
import com.danielesegato.adme.InternalADMEConsts;
import com.danielesegato.adme.annotation.ADMEEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Helper class to wrap {@link Cursor}s into a {@link ContentWrapper}.
 *
 * @see ADMEContentLoader
 * @see ADMEContentLoaderCompat
 */
public class ADMEWrapperUtil {
    /**
     * Wrap a Cursor and a generic content into a {@link CursorContentWrapper}.
     *
     * @param cursor  the Cursor to wrap
     * @param content the conversion of the Cursor in your custom content
     * @param <T>     the type of content this Cursor convert to
     * @return the {@link CursorContentWrapper}
     */
    public static
    @NonNull
    <T> ContentWrapper<T> wrap(@NonNull Cursor cursor, T content) {
        CursorContentWrapper<T> wrapper = new CursorContentWrapper<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Uri notificationUri = cursor.getNotificationUri();
            if (notificationUri == null) {
                Log.w(InternalADMEConsts.LOGTAG, "wrapping Cursor without notification Uri, did you set one in the ContentProvider?");
            }
        }
        wrapper.setCursor(cursor);
        wrapper.setContent(content);
        return wrapper;
    }

    /**
     * Wrap a Cursor and a generic content into a {@link CursorContentWrapper} using a custom
     * converter to obtain the object from the Cursor.
     *
     * @param cursor    the Cursor to wrap
     * @param converter a converter to obtain a java object from the Cursor
     * @param <T>       the type of content this Cursor convert to
     * @return the {@link CursorContentWrapper} with the converted object
     */
    public static
    @NonNull
    <T> ContentWrapper<T> wrapConvert(@NonNull Cursor cursor, @NonNull CursorToContentConverter<T> converter) {
        return wrap(cursor, converter.convert(cursor));
    }

    /**
     * Wrap the first element of the Cursor to an ADME entity class.
     * <p/>
     * It automatically convert the Cursor into the object using the data provided with the ADME
     * annotations.
     * <p/>
     * The converted object may be null if the Cursor is empty but this method will always return a
     * non-null wrapper.
     *
     * @param cursor      the Cursor
     * @param entityClass the entity class annotated with {@link ADMEEntity}
     * @param <T>         the type of content this Cursor convert to
     * @return the {@link CursorContentWrapper} with the converted object.
     * @throws IllegalArgumentException if the entity class has no default constructor or its initialization fails
     */
    public static
    @NonNull
    <T> ContentWrapper<T> wrapFirst(@NonNull Cursor cursor, @NonNull Class<T> entityClass) throws IllegalArgumentException {
        if (cursor.moveToFirst()) {
            return wrap(cursor, ADME.cursorToEntity(cursor, newEntity(entityClass)));
        }
        return wrap(cursor, null);
    }

    /**
     * Wrap a list of elements of the Cursor to a list of ADME entity class objects.
     * <p/>
     * It automatically convert the Cursor into the object using the data provided with the ADME
     * annotations.
     * <p/>
     * If the Cursor is empty the content of the wrapper will be an empty list, never null.
     *
     * @param cursor      the Cursor
     * @param entityClass the entity class annotated with {@link ADMEEntity}
     * @param <T>         the type of content each record of this Cursor convert to
     * @return the {@link CursorContentWrapper} with the list of converted objects.
     * @throws IllegalArgumentException if the entity class has no default constructor or its initialization fails
     */
    public static
    @NonNull
    <T> ContentWrapper<List<T>> wrapList(@NonNull Cursor cursor, @NonNull Class<T> entityClass) throws IllegalArgumentException {
        if (cursor.moveToFirst()) {
            List<T> list = new ArrayList<>(cursor.getCount());
            Set<String> columnsSet = ADME.getAllColumnsSet(entityClass, true, true);
            do {
                list.add(ADME.cursorToEntity(cursor, newEntity(entityClass), columnsSet));
            } while (cursor.moveToNext());
            return wrap(cursor, list);
        }
        return emptyWrap(cursor);
    }

    private static
    @NonNull
    <T> ContentWrapper<List<T>> emptyWrap(@NonNull Cursor cursor) {
        return wrap(cursor, Collections.<T>emptyList());
    }

    private static
    @NonNull
    <T> T newEntity(@NonNull Class<T> entityClass) throws IllegalArgumentException {
        T entity;
        try {
            entity = entityClass.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(String.format("Entity class %s has no default constructor", entityClass), e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(String.format("Couldn't create new instance of Entity class %s", entityClass), e);
        }
        return entity;
    }

    public interface CursorToContentConverter<T> {
        T convert(@NonNull Cursor cursor);
    }
}
