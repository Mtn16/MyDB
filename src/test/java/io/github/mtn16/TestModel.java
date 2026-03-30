package io.github.mtn16;

import io.github.mtn16.annotation.Primary;
import io.github.mtn16.annotation.Unique;

import java.util.List;

public class TestModel {

    @Primary
    private int id;

    private String name;

    public TestModel(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public TestModel() {}

    public int getId() {
        return id;
    }

    public String getName() {return name;}
}
