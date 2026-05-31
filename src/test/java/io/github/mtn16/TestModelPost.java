package io.github.mtn16;

import io.github.mtn16.annotation.Primary;
import io.github.mtn16.annotation.Relation;

public class TestModelPost {
    @Primary
    private int id = -1;

    @Relation(
            target = TestModelAuthor.class,
            targetKey = "id"
    )
    private int authorId;

    public TestModelPost(int authorId) {
        this.authorId = authorId;
    }

    public TestModelPost() {}
}
