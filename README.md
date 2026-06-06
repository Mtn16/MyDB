# Java DB
Simple weird type-safe DB written in Java. 

> [!WARNING]  
> I do not recommend using this DB in production app. This project was never meant for production usage and may contain security vulnerabilities.

# Usage
To use this DB in another project, compile it to JAR file and add it as a local dependency to your project.<br />
Simple example:

```java
import java.io.File;

MyDB db = new MyDB(new File("FileName"));

db.getSchema().find(TestModel.class, model -> model.getName().equals("OldName")).forEach(value -> {
    System.out.println("ID:   " + value.getId());
    System.out.println("NAME: " + value.getName());
});

db.getSchema().update(TestModel.class, new TestModel(13456, "NewName"), testModel -> testModel.getName().equals("OldName"));

db.getSchema().find(TestModel.class, model -> model.getName().equals("NewName")).forEach(value -> {
    System.out.println("ID:   " + value.getId());
    System.out.println("NAME: " + value.getName());
});
```

```java
package com.yourdomain.yourapp;

public class TestModel {
    private int id;
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
```

## Basic rules
There is only one rule you have to follow with your models.
Every model has to have empty constructor - otherwise the db will throw an `UnsupportedOperationException`.
So make sure to always include an empty constructor like this:
```java
package com.yourdomain.yourapp;

import io.github.mtn16.annotation.Primary;

public class MyModel {
    
    public MyModel() {}
    
}
```

## Unique keys
The `@Unique` annotation simply enforces the value to be unique in all saved instances.
If you try to insert a duplicate value (using insert or update), you will get an exception.

## Primary keys
You can add a primary key to your class. This is required if you want to refer to the class (using relations).
To add a primary key you should simply create int variable and add a `@Primary` annotation.
You can initialize the value manually or set it to `-1` which will automatically fill in the value based on auto increment function.
This is recommended for most scenarios because if you try to insert a duplicate key, you will get an exception.
```java
package com.yourdomain.yourapp;

import io.github.mtn16.annotation.Primary;

public class MyModel {
    @Primary
    private int id = -1;

    public MyModel() {}
}
```

## Relations
Example with a post class and author class. The post has one author and every author can have infinite posts.
Relations are strictly 1:N (like in SQL databases) so if you want to create M:N relation, you should create
a custom class to store the relations.
```java
package com.yourdomain.yourapp;

import io.github.mtn16.annotation.Primary;
import io.github.mtn16.annotation.Relation;

public class Post {
    @Primary
    private int id = -1;

    @Relation(
            target = Author.class,
            targetKey = "id"
    )
    private int authorId;

    public Post(int authorId) {
        this.authorId = authorId;
    }

    public Post() {}
}
```
```java
package com.yourdomain.yourapp;

import io.github.mtn16.annotation.Primary;

public class Author {
    @Primary
    private int id;

    public Author() {}

    public Author(int id) {
        this.id = id;
    }
}
```
```java
MyDB db = new MyDB(new File("./devdb.db"));

Author author = new Author(1);

try {
    db.getSchema().insert(new Post(1));
} catch (Exception e) {
    e.printStackTrace();
}

try {
    db.getSchema().insert(new Post(1));
} catch (Exception e) {
    e.printStackTrace();
}

db.relations().findRelated(author, Post.class).forEach(System.out::println);
```

> [!WARNING]  
> You should check if the author is not null - if you deleted the author from your db, the query may return null


# Development
To modify this project you simply need to clone this repository and sync Gradle.

# License
This project is licensed under the MIT license.