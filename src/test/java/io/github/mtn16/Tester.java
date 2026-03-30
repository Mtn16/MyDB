package io.github.mtn16;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Tester {
    public static void main(String[] args) {
        MyDB db = new MyDB(new File("./devdb.db"));

        //db.getSchema().insert(new TestModel(1, "Přemysl"));

        /*db.getSchema().insert(new TestModel(-1, "Přemek"));

        db.getSchema().find(TestModel.class).forEach(record -> {
            System.out.println(record);
            System.out.println(record.getId());
            System.out.println(record.getName());
            System.out.println("------------");
        });*/

        db.getSchema().update(TestModel.class, new TestModel(-1, "Vaculdos"), row -> row.getId() == 2);
        //db.getSchema().delete(TestModel.class, row -> row.getId() == 2);

        db.getSchema().find(TestModel.class).forEach(record -> {
            System.out.println(record);
            System.out.println(record.getId());
            System.out.println(record.getName());
            System.out.println("------------");
        });

        //db.getSchema().update(TestModel.class, new TestModel(123, "Přemek"), testModel -> "".equals(""));
    }
}
