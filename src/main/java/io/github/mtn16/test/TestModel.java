package io.github.mtn16.test;

import io.github.mtn16.annotation.Unique;

import java.util.List;

public class TestModel {

    @Unique
    private int id;

    private String name;
    private List<String> strings;

    public TestModel(int id, String name, List<String> strings) {
        this.id = id;
        this.name = name;
        this.strings = strings;
    }

    public TestModel() {}

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public List<String> getStrings() {return strings;}
}
