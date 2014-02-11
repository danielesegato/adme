package com.danielesegato.adme.config;

import com.danielesegato.adme.db.ADMESerializer;

import java.lang.reflect.Field;

/**
 * Store the configuration of an ADME field annotated with {@link com.danielesegato.adme.annotation.ADMEField}
 */
public class ADMEFieldConfig {
    private String columnName;
    private boolean id;
    private boolean generatedId;
    private boolean nullable;
    private String defaultValue;
    private boolean useGetSet;
    private boolean foreign;
    private OnForeignUpdateDelete foreignOnDelete;
    private OnForeignUpdateDelete foreignOnUpdate;
    private ADMEFieldConfig foreignFieldConfig;
    private Field javaField;
    private ADMEEntityConfig<?> ADMEEntityConfig;
    private ADMEIndexConstraintConfig indexConstraint;
    private ADMESerializer admeSerializer;
    private String fallbackEnumName;

    public boolean isId() {
        return id;
    }

    public void setId(boolean id) {
        this.id = id;
    }

    public boolean isGeneratedId() {
        return generatedId;
    }

    public void setGeneratedId(boolean generatedId) {
        this.generatedId = generatedId;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isUseGetSet() {
        return useGetSet;
    }

    public void setUseGetSet(boolean useGetSet) {
        this.useGetSet = useGetSet;
    }

    public boolean isForeign() {
        return foreign;
    }

    public void setForeign(boolean foreign) {
        this.foreign = foreign;
    }

    public OnForeignUpdateDelete getForeignOnDelete() {
        return foreignOnDelete;
    }

    public void setForeignOnDelete(OnForeignUpdateDelete foreignOnDelete) {
        this.foreignOnDelete = foreignOnDelete;
    }

    public OnForeignUpdateDelete getForeignOnUpdate() {
        return foreignOnUpdate;
    }

    public void setForeignOnUpdate(OnForeignUpdateDelete foreignOnUpdate) {
        this.foreignOnUpdate = foreignOnUpdate;
    }

    public ADMEFieldConfig getForeignFieldConfig() {
        // lazy initialize to avoid recursions
        if (foreign && foreignFieldConfig == null) {
            this.foreignFieldConfig = ADMEConfigUtils.lookupADMEIDFieldConfig(getJavaField().getType());
        }
        return foreignFieldConfig;
    }

    public void setForeignFieldConfig(ADMEFieldConfig foreignFieldConfig) {
        this.foreignFieldConfig = foreignFieldConfig;
    }

    public Field getJavaField() {
        return javaField;
    }

    public void setJavaField(Field javaField) {
        this.javaField = javaField;
    }

    public ADMEEntityConfig<?> getADMEEntityConfig() {
        return ADMEEntityConfig;
    }

    public void setADMEEntityConfig(ADMEEntityConfig<?> ADMEEntityConfig) {
        this.ADMEEntityConfig = ADMEEntityConfig;
    }

    public ADMEIndexConstraintConfig getIndexConstraint() {
        return indexConstraint;
    }

    public void setIndexConstraint(ADMEIndexConstraintConfig indexConstraint) {
        this.indexConstraint = indexConstraint;
    }

    public void setDefault(String aDefault) {
        this.defaultValue = aDefault;
    }

    public ADMESerializer getADMESerializer() {
        if (this.admeSerializer == null) {
            // lazy initialize to avoid recursions
            assert foreign == true : "We did something wrong in configuring this field, the serializer should always be set unless this was a foreign field";
            this.admeSerializer = ADMEConfigUtils.findADMESerializerForField(getForeignFieldConfig().getJavaField(), isNullable());
        }
        return admeSerializer;
    }

    public void setADMESerializer(ADMESerializer admeSerializer) {
        this.admeSerializer = admeSerializer;
    }

    public String getColumnName() {

        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getFallbackEnumName() {
        return fallbackEnumName;
    }

    public void setFallbackEnumName(String fallbackEnumName) {
        this.fallbackEnumName = fallbackEnumName;
    }
}
