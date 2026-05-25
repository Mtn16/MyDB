package io.github.mtn16.schema;

import java.util.List;

/**
 * Raw DB content
 * @param index index value
 * @param data content
 */
public record RawResult(int index, List<Object> data) {
}
