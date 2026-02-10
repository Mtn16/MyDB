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

db.getSchema().find(TestModel.class, model -> model.getName().equals("Přemek")).forEach(value -> {
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

# Development
To modify this project you simply need to clone this repository and sync Gradle.

# License
This project is licensed under the MIT license.