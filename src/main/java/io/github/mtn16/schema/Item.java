package io.github.mtn16.schema;

/**
 * Database item
 * @param index object index
 * @param object object
 * @param <T> object type
 */
public record Item<T>(int index, T object) { }
