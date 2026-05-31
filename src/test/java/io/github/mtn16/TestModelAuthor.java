package io.github.mtn16;

import io.github.mtn16.annotation.Primary;

public class TestModelAuthor {
    @Primary
    private int id;

    public TestModelAuthor() {}

    public TestModelAuthor(int id) {
        this.id = id;
    }
}
