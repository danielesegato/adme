package com.danielesegato.adme.annotation;

import com.danielesegato.adme.config.OnForeignUpdateDelete;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotate a field of a class to be included as column of an Entity annotated with {@link ADMEEntity}.
 * <p/>
 * You can define a field {@link #columnName()}, by default the field name itself is used. If you do not specify a
 * {@link #columnName()} on fields marked as {@link #foreign()} a suffix is added to the column name:
 * {@link #FOREIGN_FIELD_SUFFIX}.
 * <p/>
 * The column type is inferred by the field definition, for a list of supported types have a look at
 * {@link com.danielesegato.adme.db.ADMESerializerMapping}.
 * <p/>
 * You can define a {@link #defaultValue()} for the column: define it as string even if numeric or boolean, keep in mind
 * that boolean are stored as integer 1 (true) and 0 (false) in SQLite.
 * <p/>
 * By default a column is {@link #nullable()} but you can specify otherwise.
 * <p/>
 * Exactly one column/field for every {@link ADMEEntity} should be defined an {@link #id()} or a runtime error will be
 * raised. This field will be used as <em>primary key</em> for the entity. Multiple fields primary key are not supported.
 * If the ID is an <em>long</em> or an <em>int</em> and you need to let the ADME auto increment it you just have to set
 * {@link #generatedId()}.
 * <p/>
 * This limitation is been introduced by design to simplify the library. We expect the {@link #id()} field to be the
 * Android _id column on almost every table.
 * <p/>
 * You can define unique constraints and/or index on a single field by adding an {@link ADMEIndexConstraint} annotation
 * to the field.
 * <p/>
 * To define a foreign field just use the class annotated as {@link ADMEEntity } as type of the field and mark it as {@link #foreign()}.
 * Keep in mind that if you do not specify a {@link #columnName()} for the foreign field the field name with the {@link #FOREIGN_FIELD_SUFFIX}
 * will be used as column name. The foreign key will be tied to the primary key of the {@link ADMEEntity } used as field type.
 * <p/>
 * {@link java.lang.Enum} fields will be mapped with a {@link com.danielesegato.adme.db.serializer.EnumStringADMESerializer},
 * in the database by default, this can be changed using {@link com.danielesegato.adme.ADME#registerADMESerializer(Class, com.danielesegato.adme.db.ADMESerializer)}
 * method to register your own {@link com.danielesegato.adme.db.ADMESerializer} or to use {@link com.danielesegato.adme.db.serializer.EnumIntADMESerializer}
 * if you prefer to store enums with integers in the database. You can define a fallback for enums when
 * the stored value in the database doesn't match any of the enum. This is useful for handling code
 * updates where you have to remove an Enum or cases like this but it is advised to fix this with an upgrade procedure.
 * <p/>
 * Currently, even if defined, this annotation are not supported and will do nothing, they are here because we plan to implement them:
 * <ul>
 * <li>{@link #useGetSet()}</li>
 * </ul>
 */
@Target(FIELD)
@Retention(RUNTIME)
@Documented()
@Inherited()
public @interface ADMEField {

    String DEFAULT_STRING = "_____no__default_____";
    String FOREIGN_FIELD_SUFFIX = "_id";

    /**
     * The name of the column in the database. If not set then the field name is used, adding a suffix for {@link #foreign()} fields.
     * <p/>
     * The suffix will not be added if you specify a column name.
     */
    String columnName() default "";

    /**
     * The default value of the field for creating the table. Default is none.
     */
    String defaultValue() default DEFAULT_STRING;

    /**
     * Whether the field can be assigned to null or have no value. Default is true.
     */
    boolean nullable() default true;

    /**
     * Whether the field is the id field or not. Default is false. Exactly one field must have this set in an entity.
     */
    boolean id() default false;

    /**
     * Whether the field is an auto-generated id field. Default is false. It doesn't imply {@link #id()} that should specified as well.
     * <p/>
     * Currently this is only supported for <em>long</em> and <em>int</em> fields.
     */
    boolean generatedId() default false;

    /**
     * Field is a non-primitive object that corresponds to another entity that is also stored in the database. The other
     * entity {@link #id} field will be used as reference to this column in this entity. If you do not specify a {@link #columnName()}
     * the column name for this field will be the field name with the {@link #FOREIGN_FIELD_SUFFIX} suffix. Default is false.
     * <p/>
     * Be sure to enable foreign in the database by adding this to your {@link android.database.sqlite.SQLiteOpenHelper}:
     * <pre>
     * {@literal @}Override
     * public void onOpen(SQLiteDatabase db) {
     *    super.onOpen(db);
     *    if (!db.isReadOnly()) {
     *        // Enable foreign key constraints
     *        db.execSQL("PRAGMA foreign_keys=ON;");
     *    }
     * }
     * </pre>
     */
    boolean foreign() default false;

    /**
     * Define the behavior when a foreign key is updated, by default it does nothing. Ignored if
     * {@link #foreign()} is <em>false</em>.
     * <p/>
     * Be sure to enable foreign in the database by adding this to your {@link android.database.sqlite.SQLiteOpenHelper}:
     * <pre>
     * {@literal @}Override
     * public void onOpen(SQLiteDatabase db) {
     *    super.onOpen(db);
     *    if (!db.isReadOnly()) {
     *        // Enable foreign key constraints
     *        db.execSQL("PRAGMA foreign_keys=ON;");
     *    }
     * }
     * </pre>
     */
    OnForeignUpdateDelete foreignOnUpdate() default OnForeignUpdateDelete.NO_ACTION;

    /**
     * Define the behavior when a foreign key is deleted, by default it does nothing. Ignored if
     * {@link #foreign()} is <em>false</em>.
     * <p/>
     * Be sure to enable foreign in the database by adding this to your {@link android.database.sqlite.SQLiteOpenHelper}:
     * <pre>
     * {@literal @}Override
     * public void onOpen(SQLiteDatabase db) {
     *    super.onOpen(db);
     *    if (!db.isReadOnly()) {
     *        // Enable foreign key constraints
     *        db.execSQL("PRAGMA foreign_keys=ON;");
     *    }
     * }
     * </pre>
     */
    OnForeignUpdateDelete foreignOnDelete() default OnForeignUpdateDelete.NO_ACTION;

    /**
     * Unsupported, will do nothing for now. In the future setting it will make the library avoid directly setting the field value with reflection,
     * it will use the Getters/Setters for the same porpoise. Default is false.
     */
    boolean useGetSet() default false;

    /**
     * If the field is an Enum and the database has a value that is not one of the names in the
     * enum then this indexName will be used instead. It must match one of the enum names.
     * Default is empty, meaning if the value extracted from the database doesn't match any of the enum an exception will be thrown.
     */
    String fallbackEnumName() default "";
}
