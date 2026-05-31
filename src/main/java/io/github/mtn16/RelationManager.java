package io.github.mtn16;

import io.github.mtn16.annotation.Primary;
import io.github.mtn16.annotation.Relation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RelationManager {

    private final MyDB database;

    public RelationManager(MyDB database) {
        this.database = database;
    }

    /**
     * Simplified version of get(Object, String) - finds the first relation field
     * @param source Source class
     * @return Related class instance
     * @param <T> Related class
     */
    public <T> T get(Object source) {
        List<Field> relations = getRelationFields(source.getClass());

        if(relations.size() != 1) {
            throw new IllegalStateException(
                    "Object must contain exactly one relation field"
            );
        }

        return get(source, relations.getFirst().getName());
    }

    /**
     * Loads the specified annotation
     * @param source Source class
     * @param relationFieldName Relation field
     * @return Related class instance
     * @param <T> Related class
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Object source, String relationFieldName) {

        try {

            Field relationField =
                    source.getClass()
                            .getDeclaredField(relationFieldName);

            relationField.setAccessible(true);

            Relation relation =
                    relationField.getAnnotation(Relation.class);

            if(relation == null) {
                throw new IllegalStateException(
                        "Field " + relationFieldName + " is not relation"
                );
            }

            Object keyValue =
                    relationField.get(source);

            List<?> targets =
                    database.getSchema()
                            .find(relation.target());

            Field targetField =
                    relation.target()
                            .getDeclaredField(
                                    relation.targetKey()
                            );

            targetField.setAccessible(true);

            for(Object target : targets) {

                if(Objects.equals(
                        targetField.get(target),
                        keyValue
                )) {
                    return (T) target;
                }
            }

            return null;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> findRelated(
            Object source,
            Class<T> targetClass
    ) {

        List<T> result = new ArrayList<>();

        try {

            Object sourcePrimary =
                    getPrimaryValue(source);

            List<T> all =
                    database.getSchema()
                            .find(targetClass);

            for(T target : all) {

                for(Field field :
                        targetClass.getDeclaredFields()) {

                    Relation relation =
                            field.getAnnotation(Relation.class);

                    if(relation == null) {
                        continue;
                    }

                    if(!relation.target()
                            .equals(source.getClass())) {
                        continue;
                    }

                    field.setAccessible(true);

                    if(Objects.equals(
                            field.get(target),
                            sourcePrimary
                    )) {
                        result.add(target);
                    }
                }
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void validate(Object object) {

        try {

            for(Field field :
                    object.getClass()
                            .getDeclaredFields()) {

                Relation relation =
                        field.getAnnotation(Relation.class);

                if(relation == null) {
                    continue;
                }

                field.setAccessible(true);

                Object keyValue =
                        field.get(object);

                List<?> targets =
                        database.getSchema()
                                .find(relation.target());

                Field targetField =
                        relation.target()
                                .getDeclaredField(
                                        relation.targetKey()
                                );

                targetField.setAccessible(true);

                boolean found = false;

                for(Object target : targets) {

                    if(Objects.equals(
                            targetField.get(target),
                            keyValue
                    )) {
                        found = true;
                        break;
                    }
                }

                if(!found) {

                    throw new IllegalStateException(
                            "Relation target not found: "
                                    + relation.target()
                                    .getSimpleName()
                                    + "."
                                    + relation.targetKey()
                                    + "="
                                    + keyValue
                    );
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Finds object primary key
     * @param object Class
     * @return Primary key
     * @throws IllegalAccessException Exception when primary key is not set
     */
    private Object getPrimaryValue(Object object)
            throws IllegalAccessException {

        for(Field field :
                object.getClass().getDeclaredFields()) {

            if(field.isAnnotationPresent(
                    Primary.class
            )) {

                field.setAccessible(true);
                return field.get(object);
            }
        }

        throw new IllegalStateException(
                "No primary key found"
        );
    }

    /**
     * Returns all fields with relation
     * @param clazz Source class
     * @return Fields used for relation
     */
    private List<Field> getRelationFields(
            Class<?> clazz
    ) {

        List<Field> result =
                new ArrayList<>();

        for(Field field :
                clazz.getDeclaredFields()) {

            if(field.isAnnotationPresent(
                    Relation.class
            )) {

                result.add(field);
            }
        }

        return result;
    }
}