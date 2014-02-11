package com.danielesegato.adme.config;

import com.danielesegato.adme.annotation.ADMEEntity;
import com.danielesegato.adme.annotation.ADMEField;
import com.danielesegato.adme.annotation.ADMEIndexConstraint;
import com.danielesegato.adme.db.ADMESerializer;
import com.danielesegato.adme.db.ADMESerializerMapping;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility methods to parse the {@link com.danielesegato.adme.annotation.ADMEEntity},
 * {@link com.danielesegato.adme.annotation.ADMEField} and
 * {@link com.danielesegato.adme.annotation.ADMEIndexConstraint} annotations on entity classes.
 * <p/>
 * This class will produce the {@link ADMEEntityConfig} starting from a class and cache it.
 * <p/>
 * The configuration will contain all the {@link ADMEFieldConfig} and {@link ADMEIndexConstraintConfig}.
 */
public class ADMEConfigUtils {
    public static final String INDEX_PREFIX = "__idx_";
    public static final String INDEX_PREFIX_UNIQUE = "__uidx_";
    public static final String INDEX_SEPARATOR = "_";
    private static final Map<Class<?>, ADMEEntityConfig<?>> entityConfigCacheMap = new HashMap<Class<?>, ADMEEntityConfig<?>>();

    /**
     * Look upon an entity config for the entityClass. If it doesn't find it it produce it from the annotations then cache it.
     *
     * @param entityClass the class annotated with {@link com.danielesegato.adme.annotation.ADMEEntity}
     * @param <T>         the type of the class
     * @return the entity configuration
     */
    @SuppressWarnings("unchecked")
    public static <T> ADMEEntityConfig<T> lookupADMEEntityConfig(Class<T> entityClass) {
        if (entityConfigCacheMap.containsKey(entityClass)) {
            return (ADMEEntityConfig<T>) entityConfigCacheMap.get(entityClass);
        }
        final ADMEEntityConfig<T> entity = fromClass(entityClass);
        entityConfigCacheMap.put(entityClass, entity);
        return entity;
    }

    static ADMEFieldConfig lookupADMEIDFieldConfig(Class<?> type) {
        final ADMEEntityConfig<?> entityConfig = lookupADMEEntityConfig(type);
        return entityConfig.getIdFieldConfig();
    }

    static ADMESerializer findADMESerializerForField(final Field field, boolean convertPrimitiveToWrapperObject) {
        return ADMESerializerMapping.getADMESerializerForClass(field.getType(), convertPrimitiveToWrapperObject);
    }

    private static <T> ADMEEntityConfig<T> fromClass(Class<T> entityClass) {
        final ADMEEntityConfig<T> entityConfig = new ADMEEntityConfig<T>();
        final ADMEEntity entity = entityClass.getAnnotation(ADMEEntity.class);
        if (entity == null) {
            throw new IllegalArgumentException(String.format("Class %s has no %s annotation", entityClass.getName(), ADMEEntity.class.getSimpleName()));
        }
        entityConfig.setJavaClass(entityClass);
        if (entity.entityName().length() > 0) {
            entityConfig.setEntityName(entity.entityName());
        } else {
            entityConfig.setEntityName(entityClass.getSimpleName());
        }

        final Map<String, ADMEFieldConfig> fieldNameConfigMap = new HashMap<String, ADMEFieldConfig>();
        final List<ADMEFieldConfig> fieldConfigList = new ArrayList<ADMEFieldConfig>();
        final List<ADMEIndexConstraintConfig> entityIndexConstraintList = new ArrayList<ADMEIndexConstraintConfig>();
        buildFieldsConfiguration(entityClass, entityConfig, fieldNameConfigMap, fieldConfigList, entityIndexConstraintList);
        entityConfig.setFieldsConfig(fieldConfigList);
        for (ADMEIndexConstraint indexConstraintEntity : entity.indexConstraints()) {
            buildMultipleIndexConstraintConfig(indexConstraintEntity, entityClass, entityConfig, fieldNameConfigMap, entityIndexConstraintList);
        }
        entityConfig.setIndexConstraintConfigList(entityIndexConstraintList);
        return entityConfig;
    }

    private static <T> void buildFieldsConfiguration(Class<T> entityClass, ADMEEntityConfig<T> entityConfig, Map<String, ADMEFieldConfig> fieldNameConfigMap, List<ADMEFieldConfig> fieldConfigList, List<ADMEIndexConstraintConfig> entityIndexConstraintList) {
        buildFieldsConfigurationRecursive(entityClass, entityClass, entityConfig, fieldNameConfigMap, fieldConfigList, entityIndexConstraintList);
        if (entityConfig.getIdFieldConfig() == null) {
            throw new IllegalArgumentException(String.format(
                    "Entity %s of class %s has no field marked as ID", entityConfig.getEntityName(), entityClass.getName()
            ));
        }
    }

    private static <T> void buildFieldsConfigurationRecursive(Class<T> entityClass, Class<?> currentClass, ADMEEntityConfig<T> entityConfig, Map<String, ADMEFieldConfig> fieldNameConfigMap, List<ADMEFieldConfig> fieldConfigList, List<ADMEIndexConstraintConfig> entityIndexConstraintList) {
        Class<?> superClass = currentClass.getSuperclass();
        if (superClass != null) {
            buildFieldsConfigurationRecursive(entityClass, superClass, entityConfig, fieldNameConfigMap, fieldConfigList, entityIndexConstraintList);
        }
        for (Field field : currentClass.getDeclaredFields()) {
            buildFieldConfiguration(field, entityClass, entityConfig, fieldNameConfigMap, fieldConfigList, entityIndexConstraintList);
        }
    }

    private static <T> void buildFieldConfiguration(Field field, Class<T> entityClass, ADMEEntityConfig<T> entityConfig, Map<String, ADMEFieldConfig> fieldNameConfigMap, List<ADMEFieldConfig> fieldConfigList, List<ADMEIndexConstraintConfig> entityIndexConstraintList) {
        final ADMEField entityField = field.getAnnotation(ADMEField.class);
        if (entityField != null) {
            final ADMEFieldConfig fieldConfig = new ADMEFieldConfig();
            fieldConfig.setJavaField(field);
            field.setAccessible(true);
            fieldConfig.setADMEEntityConfig(entityConfig);
            fieldConfig.setId(entityField.id());
            fieldConfig.setGeneratedId(entityField.id() && entityField.generatedId());
            if (fieldConfig.isGeneratedId()) {
                final Class<?> idFieldType = field.getType();
                if (idFieldType != int.class && idFieldType != long.class) {
                    throw new IllegalArgumentException(String.format(
                            "Auto-Generated ID type for entity %s on field %s is not supported: %s",
                            entityClass.getName(), field.getName(), idFieldType.getName()
                    ));
                }
            }
            String columnName = entityField.columnName();
            if (columnName.length() == 0) {
                columnName = field.getName();
                if (entityField.foreign()) {
                    columnName = columnName + ADMEField.FOREIGN_FIELD_SUFFIX;
                }
            }
            fieldConfig.setColumnName(columnName);
            fieldConfig.setNullable(entityField.nullable());
            if (!entityField.defaultValue().equals(ADMEField.DEFAULT_STRING)) {
                fieldConfig.setDefault(entityField.defaultValue());
            }
            fieldConfig.setUseGetSet(entityField.useGetSet());
            if (fieldConfig.getFallbackEnumName().length() > 0) {
                if (!field.isEnumConstant()) {
                    throw new IllegalArgumentException(String.format(
                            "Entity class %s declare field %s with fallback enum %s but the field is not an enum",
                            entityClass.getName(), field.getName(), entityField.fallbackEnumName()
                    ));
                }
                try {
                    Enum.valueOf((Class<? extends Enum>)field.getType(), fieldConfig.getFallbackEnumName());
                } catch (IllegalArgumentException notFound) {
                    throw new IllegalArgumentException(String.format(
                            "Entity class %s declare field %s with fallback enum %s which doesn't exist",
                            entityClass.getName(), field.getName(), entityField.fallbackEnumName()
                    ));
                }
            }
            fieldConfig.setFallbackEnumName(entityField.fallbackEnumName());

            // Foreign key handling
            fieldConfig.setForeign(entityField.foreign());
            fieldConfig.setForeignOnDelete(entityField.foreignOnDelete());
            fieldConfig.setForeignOnUpdate(entityField.foreignOnUpdate());
            // check foreign
            if (entityField.foreign() && field.getType().getAnnotation(ADMEEntity.class) == null) {
                throw new IllegalArgumentException(String.format(
                        "Entity class %s declare field %s as foreign but it's type class %s is not annotated with %s ",
                        entityClass.getName(), field.getName(), field.getType().getName(), ADMEEntity.class.getSimpleName()));
            }

            // discover this field type and assign a serializer
            if (!entityField.foreign()) {
                fieldConfig.setADMESerializer(findADMESerializerForField(field, false));
            }

            // Unique / Index handling
            final ADMEIndexConstraint indexConstraintField = field.getAnnotation(ADMEIndexConstraint.class);
            if (indexConstraintField != null) {
                buildSingleIndexConstraintConfig(indexConstraintField, field, entityClass, entityConfig, entityIndexConstraintList, fieldConfig);
            }

            // final linking to other configurations
            if (fieldConfig.isId()) {
                if (entityConfig.getIdFieldConfig() != null) {
                    throw new IllegalArgumentException(String.format(
                            "Entity class %s declare multiple ID fields: %s and %s: only one field as ID is supported",
                            entityClass.getName(), entityConfig.getIdFieldConfig().getJavaField().getName(), field.getName()
                    ));
                }
                entityConfig.setIdFieldConfig(fieldConfig);
            }
            fieldNameConfigMap.put(fieldConfig.getColumnName(), fieldConfig);
            fieldConfigList.add(fieldConfig);
        }
    }

    private static <T> void buildSingleIndexConstraintConfig(ADMEIndexConstraint indexConstraintField, Field field, Class<T> entityClass, ADMEEntityConfig<T> entityConfig, List<ADMEIndexConstraintConfig> entityIndexConstraintList, ADMEFieldConfig fieldConfig) {
        if (indexConstraintField.columns().length > 0) {
            throw new IllegalArgumentException(String.format(
                    "Entity class %s declare field %s with invalid %s annotation: columns array can't be specified on a field, specify it on the entity class",
                    entityClass.getName(), field.getName(), ADMEIndexConstraint.class.getSimpleName()
            ));
        }
        if (!indexConstraintField.index() && !indexConstraintField.unique()) {
            throw new IllegalArgumentException(String.format(
                    "Entity class %s declare field %s with invalid %s annotation: at least one of index or unique must be set as true",
                    entityClass.getName(), field.getName(), ADMEIndexConstraint.class.getSimpleName()
            ));
        }
        ADMEIndexConstraintConfig indexConstraintConfig = new ADMEIndexConstraintConfig();
        indexConstraintConfig.setADMEEntityConfig(entityConfig);
        indexConstraintConfig.setFields(new ADMEFieldConfig[]{fieldConfig});
        indexConstraintConfig.setIndex(indexConstraintField.index());
        if (indexConstraintField.indexName().length() > 0) {
            indexConstraintConfig.setIndexName(indexConstraintField.indexName());
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(indexConstraintField.unique() ? INDEX_PREFIX_UNIQUE : INDEX_PREFIX);
            sb.append(fieldConfig.getADMEEntityConfig().getEntityName());
            sb.append(INDEX_SEPARATOR);
            sb.append(fieldConfig.getColumnName());
            indexConstraintConfig.setIndexName(sb.toString());
        }
        indexConstraintConfig.setUnique(indexConstraintField.unique());
        indexConstraintConfig.setSingleField(true);
        fieldConfig.setIndexConstraint(indexConstraintConfig);
        entityIndexConstraintList.add(indexConstraintConfig);
    }

    private static <T> void buildMultipleIndexConstraintConfig(ADMEIndexConstraint indexConstraintEntity, Class<T> entityClass, ADMEEntityConfig<T> entityConfig, Map<String, ADMEFieldConfig> fieldNameConfigMap, List<ADMEIndexConstraintConfig> entityIndexConstraintList) {
        if (indexConstraintEntity.columns().length < 2) {
            throw new IllegalArgumentException(String.format(
                    "Entity class %s declare an %s annotation with no columns or a single column, this is invalid on an entity, declare it on the field itself",
                    entityClass.getName(), ADMEIndexConstraint.class.getSimpleName()
            ));
        }
        ADMEIndexConstraintConfig indexConstraintConfig = new ADMEIndexConstraintConfig();
        indexConstraintConfig.setADMEEntityConfig(entityConfig);
        ADMEFieldConfig[] fieldConfigArray = new ADMEFieldConfig[indexConstraintEntity.columns().length];
        for (int i = 0; i < indexConstraintEntity.columns().length; i++) {
            ADMEFieldConfig fieldConfig = fieldNameConfigMap.get(indexConstraintEntity.columns()[i]);
            if (fieldConfig == null) {
                throw new IllegalArgumentException(String.format(
                        "Column '%s' not found in entity class %d for %s annotation, check a field with that column name is available",
                        indexConstraintEntity.columns()[i], entityClass.getName(), ADMEIndexConstraint.class.getSimpleName()
                ));
            }
            fieldConfigArray[i] = fieldConfig;
        }
        indexConstraintConfig.setFields(fieldConfigArray);
        indexConstraintConfig.setIndex(indexConstraintEntity.index());
        if (indexConstraintEntity.indexName().length() > 0) {
            indexConstraintConfig.setIndexName(indexConstraintEntity.indexName());
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(indexConstraintEntity.unique() ? INDEX_PREFIX_UNIQUE : INDEX_PREFIX);
            sb.append(entityConfig.getEntityName());
            for (final ADMEFieldConfig fieldConfig : indexConstraintConfig.getFields()) {
                sb.append(INDEX_SEPARATOR);
                sb.append(fieldConfig.getColumnName());
            }
            indexConstraintConfig.setIndexName(sb.toString());
        }
        indexConstraintConfig.setUnique(indexConstraintEntity.unique());
        entityIndexConstraintList.add(indexConstraintConfig);
    }
}
