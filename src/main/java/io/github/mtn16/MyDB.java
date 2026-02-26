package io.github.mtn16;

import java.io.File;

public class MyDB {
    private final File file;
    private final Serializer schema;

    public MyDB(File file) {
        this.file = file;
        this.schema = new Serializer(file.toPath());
    }

    public Serializer getSchema() {
        return schema;
    }
}
