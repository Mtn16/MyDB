package io.github.mtn16;

import io.github.mtn16.util.ExportFormat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Tester {
    public static void main(String[] args) throws IOException {
        MyDB db = new MyDB(new File("./devdb.db"));

        TestModelAuthor author = new TestModelAuthor(1);
        /*try {
            db.getSchema().insert(author);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        try {
            db.getSchema().insert(new TestModelPost(1));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            db.getSchema().insert(new TestModelPost(1));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            db.getSchema().update(
                    TestModelAuthor.class,
                    new TestModelAuthor(1),
                    a -> a.getId() == 1
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        db.relations().findRelated(author, TestModelPost.class).forEach(System.out::println);
    }
}
