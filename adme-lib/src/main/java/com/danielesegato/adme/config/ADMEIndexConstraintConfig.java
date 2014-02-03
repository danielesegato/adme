package com.danielesegato.adme.config;

/**
 * Store the configuration for an index and/or constraint for the annotation {@link com.danielesegato.adme.annotation.ADMEIndexConstraint}.
 */
public class ADMEIndexConstraintConfig {
    private ADMEEntityConfig<?> ADMEEntityConfig;
    private ADMEFieldConfig[] fields;
    private boolean index;
    private String indexName;
    private boolean unique;
    private boolean singleField;

    /**
     * @return the ADME Entity to which this index / constraint is applied
     */
    public ADMEEntityConfig<?> getADMEEntityConfig() {
        return ADMEEntityConfig;
    }

    public void setADMEEntityConfig(ADMEEntityConfig<?> ADMEEntityConfig) {
        this.ADMEEntityConfig = ADMEEntityConfig;
    }

    /**
     * @return the array of fields configurations to which this index/constraint is applied
     */
    public ADMEFieldConfig[] getFields() {
        return fields;
    }

    public void setFields(ADMEFieldConfig[] fields) {
        this.fields = fields;
    }

    /**
     * @return if this configuration define an index on the fields from {@link #getFields()}
     */
    public boolean isIndex() {
        return index;
    }

    public void setIndex(boolean index) {
        this.index = index;
    }

    /**
     * @return the index name on the database, this is returned even if this is not an index
     */
    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    /**
     * @return if this configuration define a unique constraint on the fields from {@link #getFields()}
     */
    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    /**
     * @return <em>true</em> if this constraint is applied to a single field, <em>false</em> if
     * applied on multiple fields
     */
    public boolean isSingleField() {
        return singleField;
    }

    public void setSingleField(boolean singleField) {
        this.singleField = singleField;
    }
}
