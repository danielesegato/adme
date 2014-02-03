package com.danielesegato.adme.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotate an AndroidORM Entity.
 * <p/>
 * By default the class name is used as entity name in SQLite, but you can override it with {@link #entityName()}.
 * <p/>
 * Fields of the entity must be annotated with an {@link com.danielesegato.adme.annotation.ADMEField} annotation.
 * Exactly one field should be marked as an ID for the entity. Not specifying an ID or specifying multiple IDs
 * will give a runtime exception.
 * <p/>
 * If you need to add unique constraints and/or index on multiple fields you should do it providing a list of
 * {@link com.danielesegato.adme.annotation.ADMEIndexConstraint} in {@link #indexConstraints()}.
 * <p/>
 * Instead if the constraint is on a single field the {@link com.danielesegato.adme.annotation.ADMEIndexConstraint}
 * must be applied directly to that field.
 * <p/>
 * You can define an {@link com.danielesegato.adme.annotation.ADMEField} on a super class of the entity
 * it will be automaticlly used.
 * <p/>
 * The order in which the columns are created match the order they are defined in code (starting from
 * the first class after {@link java.lang.Object} containing a field and going down to the entity class.
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented()
public @interface ADMEEntity {

    /**
     * The name of the entity in the database..
     */
    String entityName() default "";

    /**
     * Define a list of index or uniqueness constraints over multiple fields of this entity.
     * This method can't be used for single fields index/constraints, apply the
     * {@link ADMEIndexConstraint} directly on the field already annotated with {@link ADMEField}
     * if you need that. This is just for multiple fields.
     *
     * @return an array of {@link ADMEIndexConstraint} for this entity, one for each multiple
     * constraint you need to define
     */
    ADMEIndexConstraint[] indexConstraints() default {};
}
