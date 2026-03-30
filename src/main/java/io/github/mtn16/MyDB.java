package io.github.mtn16;

import java.io.File;

public class MyDB {
    private final File file;
    private final Serializer schema;
    private final DatabaseInterface databaseInterface;

    public MyDB(File file) {
        this.file = file;
        this.databaseInterface = new DatabaseInterface();
        this.schema = new Serializer(file.toPath(), databaseInterface);
    }

    public Serializer getSchema() {
        return schema;
    }
}
