package com.danielesegato.adme.config;

import java.util.List;

/**
 * Store the configuration for a class annotated with {@link com.danielesegato.adme.annotation.ADMEEntity}.
 */
public class ADMEEntityConfig<T> {
    private String entityName;
    private List<ADMEFieldConfig> fieldsConfig;
    private Class<T> javaClass;
    private ADMEFieldConfig idFieldConfig;
    private List<ADMEIndexConstraintConfig> indexConstraintConfigList;

    /**
     * @return the entity name on the database.
     */
    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    /**
     * @return the list of fields configurations for this entity
     */
    public List<ADMEFieldConfig> getFieldsConfig() {
        return fieldsConfig;
    }

    public void setFieldsConfig(List<ADMEFieldConfig> fieldsConfig) {
        this.fieldsConfig = fieldsConfig;
    }

    /**
     * @return the {@link java.lang.Class} to which this entity is associated.
     */
    public Class<T> getJavaClass() {
        return javaClass;
    }

    public void setJavaClass(Class<T> javaClass) {
        this.javaClass = javaClass;
    }

    /**
     * @return the configuration of the id (or primary key) field.
     */
    public ADMEFieldConfig getIdFieldConfig() {
        return idFieldConfig;
    }

    public void setIdFieldConfig(ADMEFieldConfig idFieldConfig) {
        this.idFieldConfig = idFieldConfig;
    }

    /**
     * @return the list of indexes and constraints for this entity.
     */
    public List<ADMEIndexConstraintConfig> getIndexConstraintConfigList() {
        return indexConstraintConfigList;
    }

    public void setIndexConstraintConfigList(List<ADMEIndexConstraintConfig> indexConstraintConfigList) {
        this.indexConstraintConfigList = indexConstraintConfigList;
    }
}
