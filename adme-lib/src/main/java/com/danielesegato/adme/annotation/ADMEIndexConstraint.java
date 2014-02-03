package com.danielesegato.adme.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Define an index and/or a constraint on one or more columns. Can be applied to a single field or a
 * class containing fields.
 * <p/>
 * At least one of {@link #index()} or {@link #unique()} should be defined.
 * <p/>
 * If the index/constraint is to be applied on a single field the annotation must be applied to that
 * field directly without specifying {@link #columns()}. This annotation will have no effect if the
 * field has no {@link ADMEField} annotation.
 * <p/>
 * If the index/constraint is to be applied on multiple fields the annotation must be added to the
 * array of {@link ADMEEntity#indexConstraints()} on the entity itself. And a list of
 * {@link #columns()} names must be specified (at least two columns). The column name is used, not
 * the field name, so be sure to consider the fact that fields marked with {@link ADMEField#foreign()}
 * end up with a suffix in the column name unless you overridden the column name with
 * {@link ADMEField#columnName()}. If a column name is not find a runtime error will be raised when
 * parsing the entity configuration.
 * <p/>
 * You can specify the {@link #indexName()} manually or let the system generate one for you.
 */
@Target(FIELD)
@Retention(RUNTIME)
@Documented()
@Inherited()
public @interface ADMEIndexConstraint {
    /**
     * Add an index to the field this is applied or to the list of columns defined, default is <em>false</em>
     *
     * @return <em>true</em> if this is an index, <em>false</em> otherwise
     */
    boolean index() default false;

    /**
     * Define a custom indexName for this index. If not defined it will be auto-generated.
     *
     * @return the indexName of the index in the database.
     */
    String indexName() default "";

    /**
     * Add an unique constraint to the field this is applied or to the list of columns defined, default is <em>false</em>
     *
     * @return <em>true</em> if this is an unique, <em>false</em> otherwise
     */
    boolean unique() default false;

    /**
     * List of columns this index apply. Be aware that foreign key column names do not match the field indexName by default.
     * <p/>
     * It is mandatory to include this array when defining an index which is not on a field, and it must have at least 2 columns.
     * An exception will be raised if you define it directly on a field.
     * <p/>
     * Be aware the foreign key columns, by default, are suffixed by {@link ADMEField.FOREIGN_FIELD_SUFFIX}
     * unless you specified a column name with {@link ADMEField#foreign()}
     *
     * @return an array of strings with the columns indexName being part of this index
     */
    String[] columns() default {};
}
