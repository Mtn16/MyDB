package io.github.mtn16;

import java.io.File;

public class MyDB {
    private final File file;
    private final JsonReader schema;

    public MyDB(File file) {
        this.file = file;
        this.schema = new JsonReader(file);
    }

    public JsonReader getSchema() {
        return schema;
    }
}
