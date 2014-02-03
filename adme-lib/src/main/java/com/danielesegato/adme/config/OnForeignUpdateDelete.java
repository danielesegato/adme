package com.danielesegato.adme.config;

/**
 * Enumeration of possibilities with Foreign key ON DELETE, ON UPDATE clause.
 * <p/>
 * See http://sqlite.org/foreignkeys.html
 */
public enum OnForeignUpdateDelete {
    /**
     * when the foreign key is deleted/updated set null
     */
    SET_NULL("SET NULL"),
    /**
     * when the foreign key is deleted/update set the default for this field
     */
    SET_DEFAULT("SET DEFAULT"),
    /**
     * when the foreign key is deleted/updated update/delete this row too
     */
    CASCADE("CASCADE"),
    /**
     * when the foreign key is trying to be deleted/updated and this reference exist prohibit it (exception)
     */
    RESTRICT("RESTRICT"),
    /**
     * this is the default, it does absolutely nothing if a foreign key is deleted/updated
     */
    NO_ACTION("NO ACTION"),
    // end
    ;

    private final String sql;

    private OnForeignUpdateDelete(final String sql) {
        this.sql = sql;
    }

    public String sql() {
        return sql;
    }
}
