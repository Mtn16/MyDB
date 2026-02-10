package io.github.mtn16.test;

import io.github.mtn16.schema.Unique;

public class TestModel {
    private int id;
    @Unique
    private String name;

    public TestModel(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
