package io.github.mtn16.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
/**
 * Relation to other object
 * Allows the other object to be stored independently in the database instead of serializing directly in this object
 * Requires primary key
 *
 * @param target Target class
 * @param key Variable name inside this class
 * @param targetKey Target key inside the target class
 */
public @interface Relation {
    Class<?> target();
    String targetKey();
}