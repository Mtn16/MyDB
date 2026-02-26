package io.github.mtn16;

import io.github.mtn16.annotation.Unique;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Predicate;

public class Serializer {

    private final Path file;

    public Serializer(Path file) {
        this.file = file;
        try {
            if (!Files.exists(file)) {
                Files.createFile(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void insert(Object object) {
        Map<String, List<Object>> allData = loadAllRaw();
        List<Object> objects = allData.computeIfAbsent(object.getClass().getName(), k -> new ArrayList<>());

        validateUnique(object, objects);

        objects.add(object);

        saveAll(allData);
    }

    public <T> List<T> find(Class<T> clazz) {
        return find(clazz, t -> true);
    }

    public <T> List<T> find(Class<T> clazz, Predicate<T> predicate) {
        Map<String, List<Object>> allData = loadAllRaw();

        List<Object> objects = allData.getOrDefault(clazz.getName(), new ArrayList<>());

        List<T> result = new ArrayList<>();
        for (Object obj : objects) {
            T casted = clazz.cast(obj);
            if (predicate.test(casted)) {
                result.add(casted);
            }
        }

        return result;
    }

    public <T> void update(Class<T> clazz, T newValue, Predicate<T> predicate) {
        Map<String, List<Object>> allData = loadAllRaw();

        List<Object> objects = allData.getOrDefault(clazz.getName(), new ArrayList<>());

        for (int i = 0; i < objects.size(); i++) {
            T existing = clazz.cast(objects.get(i));

            if (predicate.test(existing)) {
                List<Object> copy = new ArrayList<>(objects);
                copy.remove(i);

                validateUnique(newValue, copy);

                objects.set(i, newValue);
            }
        }

        allData.put(clazz.getName(), objects);
        saveAll(allData);
    }

    public <T> void delete(Class<T> clazz, Predicate<T> predicate) {
        Map<String, List<Object>> allData = loadAllRaw();

        List<Object> objects = allData.getOrDefault(clazz.getName(), new ArrayList<>());

        objects.removeIf(obj -> predicate.test(clazz.cast(obj)));

        if (objects.isEmpty()) {
            allData.remove(clazz.getName());
        } else {
            allData.put(clazz.getName(), objects);
        }

        saveAll(allData);
    }

    private <T> void validateUnique(T object, List<Object> existingObjects) throws IllegalStateException {
        Class<?> clazz = object.getClass();

        for (Field field : clazz.getDeclaredFields()) {

            if (field.isAnnotationPresent(Unique.class)) {

                field.setAccessible(true);

                try {
                    Object newValue = field.get(object);

                    for (Object existing : existingObjects) {
                        Object existingValue = field.get(existing);

                        if (Objects.equals(existingValue, newValue)) {
                            throw new IllegalStateException(
                                    "Unique constraint violation on field: "
                                            + field.getName()
                                            + " value: " + newValue
                            );
                        }
                    }

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Map<String, List<Object>> loadAllRaw() {
        Map<String, List<Object>> result = new LinkedHashMap<>();

        if (!Files.exists(file)) return result;

        try (BufferedReader reader = Files.newBufferedReader(file)) {

            String line;
            String currentClass = null;
            Map<String, String> fieldMap = new HashMap<>();

            while ((line = reader.readLine()) != null) {

                if (line.startsWith("#CLASS ")) {
                    currentClass = line.substring(7);
                    result.putIfAbsent(currentClass, new ArrayList<>());
                }

                else if (line.equals("--")) {
                    if (currentClass != null && !fieldMap.isEmpty()) {
                        Object obj = createInstance(currentClass, fieldMap);
                        result.get(currentClass).add(obj);
                        fieldMap.clear();
                    }
                }

                else if (line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    fieldMap.put(parts[0], parts[1]);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private void saveAll(Map<String, List<Object>> data) {
        try (BufferedWriter writer = Files.newBufferedWriter(file,
                StandardOpenOption.TRUNCATE_EXISTING)) {

            for (String className : data.keySet()) {

                writer.write("#CLASS " + className);
                writer.newLine();

                for (Object object : data.get(className)) {

                    for (Field field : object.getClass().getDeclaredFields()) {
                        field.setAccessible(true);
                        Object value = field.get(object);
                        writer.write(field.getName() + "=" +
                                (value != null ? value : "null"));
                        writer.newLine();
                    }

                    writer.write("--");
                    writer.newLine();
                }

                writer.newLine();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object createInstance(String className,
                                  Map<String, String> fields) throws Exception {

        Class<?> clazz = Class.forName(className);
        Object instance = clazz.getDeclaredConstructor().newInstance();

        for (String fieldName : fields.keySet()) {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);

            String value = fields.get(fieldName);

            if (value.equals("null")) {
                field.set(instance, null);
            } else if (field.getType() == int.class || field.getType() == Integer.class) {
                field.set(instance, Integer.parseInt(value));
            } else if (field.getType() == long.class || field.getType() == Long.class) {
                field.set(instance, Long.parseLong(value));
            } else if (field.getType() == double.class || field.getType() == Double.class) {
                field.set(instance, Double.parseDouble(value));
            } else if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                field.set(instance, Boolean.parseBoolean(value));
            } else if (field.getType() == String.class) {
                field.set(instance, value);
            } else {
                throw new UnsupportedOperationException(
                        "Unsupported field type: " + field.getType());
            }
        }

        return instance;
    }

}
