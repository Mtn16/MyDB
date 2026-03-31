package io.github.mtn16;

import io.github.mtn16.schema.RawResult;
import io.github.mtn16.util.ExportFormat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

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

    public void export(Path filename, ExportFormat format) throws IOException {
        if(format == ExportFormat.CSV) {
            String eol = System.lineSeparator();

            if(!Files.exists(filename)) {
                Files.createFile(filename);
            }

            try (Writer writer = new FileWriter(filename.toFile())) {
                for (Map.Entry<String, String> entry : schema.rawContent().entrySet()) {
                    writer.append(entry.getKey())
                            .append(',')
                            .append(entry.getValue())
                            .append(eol);
                }
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }
}
