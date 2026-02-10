package io.github.mtn16;

import io.github.mtn16.test.TestModel;

import java.io.File;

public class Tester {
    public static void main(String[] args) {
        MyDB db = new MyDB(new File("./devdb.db"));

        db.getSchema().update(TestModel.class, new TestModel(13456, "Přemek"), testModel -> testModel.getName().equals("Vaculík"));

        db.getSchema().find(TestModel.class, model -> model.getName().equals("Vaculík"))
                .forEach(value -> {
                    System.out.println("ID:   " + value.getId());
                    System.out.println("NAME: " + value.getName());
                });

        db.getSchema().find(TestModel.class, model -> model.getName().equals("Přemek"))
                .forEach(value -> {
                    System.out.println("ID:   " + value.getId());
                    System.out.println("NAME: " + value.getName());
                });
    }
}
