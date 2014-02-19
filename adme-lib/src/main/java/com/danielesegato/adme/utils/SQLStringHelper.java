package com.danielesegato.adme.utils;

/**
 * Helper for SQL String manipulation
 */
public class SQLStringHelper {
    /**
     * Utility method to add to the sb StringBuilder an escaped entity, field for SQLite (encapsulate it in single quote)
     *
     * @param sb            the StringBuilder
     * @param entityOrField the name of the entity or field
     * @return the quoted entity/field
     */
    public static StringBuilder appendEscapedEntityOrField(final StringBuilder sb, final String entityOrField) {
        return sb.append('\'').append(entityOrField).append('\'');
    }
}
