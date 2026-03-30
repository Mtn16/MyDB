package io.github.mtn16;

import io.github.mtn16.annotation.Primary;
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
    private final DatabaseInterface databaseInterface;

    public Serializer(Path file, DatabaseInterface databaseInterface) {
        this.file = file;
        this.databaseInterface = databaseInterface;
        try {
            if (!Files.exists(file)) {
                Files.createFile(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void insert(Object object) {
        Map<String, RawResult> allData = loadAllRaw();
        RawResult raw = allData.getOrDefault(object.getClass().getName(), new RawResult(-1, new ArrayList<>()));
        int index = raw.index();

        List<Object> objects = raw.data();

        validateUnique(object, objects);
        Item<Object> primaryItem = usePrimary(object, objects, raw.index());
        object = primaryItem.object();
        if(primaryItem.index() > index) index = primaryItem.index();
        System.out.println(primaryItem.index());

        objects.add(object);
        System.out.println(objects);
        allData.put(object.getClass().getName(), new RawResult(index, objects));
        System.out.println(allData);

        saveAll(allData);
    }

    public <T> List<T> find(Class<T> clazz) {
        return find(clazz, t -> true);
    }

    public <T> List<T> find(Class<T> clazz, Predicate<T> predicate) {
        Map<String, RawResult> allData = loadAllRaw();
        RawResult raw = allData.getOrDefault(clazz.getName(), new RawResult(-1, new ArrayList<>()));

        List<Object> objects = raw.data();

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
        Map<String, RawResult> allData = loadAllRaw();
        RawResult raw = allData.getOrDefault(clazz.getName(), new RawResult(-1, new ArrayList<>()));
        int index = raw.index();

        List<Object> objects = raw.data();

        for (int i = 0; i < objects.size(); i++) {
            T existing = clazz.cast(objects.get(i));

            if (predicate.test(existing)) {
                List<Object> copy = new ArrayList<>(objects);
                copy.remove(i);

                validateUnique(newValue, copy);
                Item<T> primaryItem = usePrimary(newValue, copy, raw.index());
                newValue = primaryItem.object();
                if(primaryItem.index() > index) index = primaryItem.index();

                objects.set(i, newValue);
            }
        }

        allData.put(clazz.getName(), new RawResult(index, objects));
        saveAll(allData);
    }

    public <T> void delete(Class<T> clazz, Predicate<T> predicate) {
        Map<String, RawResult> allData = loadAllRaw();
        RawResult raw = allData.getOrDefault(clazz.getName(), new RawResult(-1, new ArrayList<>()));

        List<Object> objects = raw.data();

        objects.removeIf(obj -> predicate.test(clazz.cast(obj)));

        if (objects.isEmpty()) {
            allData.remove(clazz.getName());
        } else {
            allData.put(clazz.getName(), new RawResult(raw.index(), objects));
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

    private <T> Item<T> usePrimary(T object, List<Object> existingObjects, int lastId) throws IllegalStateException {
        Class<?> clazz = object.getClass();
        int objectId = -1;
        int fields = 0;

        try {
            for (Field field : clazz.getDeclaredFields()) {
                System.out.println("FIELD " + field.getName() + ": " + field.isAnnotationPresent(Primary.class));
                if (field.isAnnotationPresent(Primary.class)) {
                    fields++;
                    if(fields > 1) throw new IllegalStateException("Only one primary field can be used");
                    field.setAccessible(true);

                    if(field.getType() != int.class) throw new IllegalStateException("Primary must be int");
                    if(field.getInt(object) > -1) {
                        try {
                            Object newValue = field.get(object);

                            if(existingObjects.isEmpty()) {
                                int id = lastId + 1;
                                if(lastId == -1) id = 1;
                                field.set(object, id);
                                objectId = id;
                                System.out.println("OBJ " + objectId);
                            } else {
                                for (Object existing : existingObjects) {
                                    Object existingValue = field.get(existing);

                                    if (Objects.equals(existingValue, newValue)) {
                                        throw new IllegalStateException(
                                                "Primary constraint violation on field: "
                                                        + field.getName()
                                                        + " value: " + newValue
                                        );
                                    }

                                    objectId = field.getInt(existing);
                                    System.out.println("OBJ " + objectId);
                                }
                            }

                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } else {
                        int id = lastId + 1;
                        if(lastId == -1) id = 1;
                        field.set(object, id);
                        objectId = id;
                        System.out.println("OBJ " + objectId);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e.getMessage());
        }
        return new Item<T>(objectId, object);
    }

    private Map<String, RawResult> loadAllRaw() {
        Map<String, RawResult> result = new LinkedHashMap<>();

        if (!Files.exists(file)) return result;

        try (BufferedReader reader = Files.newBufferedReader(file)) {

            String line;
            String currentClass = null;
            Map<String, String> fieldMap = new HashMap<>();

            while ((line = reader.readLine()) != null) {

                if (line.startsWith("#CLASS ")) {
                    currentClass = line.substring(7).split("\\s+")[0];
                    int currentIndex = 1;
                    try {
                        currentIndex = Integer.parseInt(line.substring(7).split("\\s+")[1]);
                    } catch (NumberFormatException ignored) {}
                    result.putIfAbsent(currentClass, new RawResult(currentIndex, new ArrayList<>()));
                }

                else if (line.equals("--")) {
                    if (currentClass != null && !fieldMap.isEmpty()) {
                        Object obj = createInstance(currentClass, fieldMap);
                        result.get(currentClass).data().add(obj);
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

    private void saveAll(Map<String, RawResult> data) {
        try (BufferedWriter writer = Files.newBufferedWriter(file,
                StandardOpenOption.TRUNCATE_EXISTING)) {

            for (String className : data.keySet()) {

                writer.write("#CLASS " + className + " " + data.get(className).index());
                writer.newLine();

                for (Object object : data.get(className).data()) {

                    for (Field field : object.getClass().getDeclaredFields()) {
                        field.setAccessible(true);
                        Object value = field.get(object);
                        writer.write(field.getName() + "=" + serializeValue(value));
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

        for (Map.Entry<String, String> entry : fields.entrySet()) {
            Field field = clazz.getDeclaredField(entry.getKey());
            field.setAccessible(true);

            Object parsedValue = parseValue(field.getType(), field.getGenericType(), entry.getValue());
            field.set(instance, parsedValue);
        }

        return instance;
    }

    private Object parseValue(Class<?> type, java.lang.reflect.Type genericType, String value) throws Exception {

        if (value.equals("null")) return null;

        if (type == int.class || type == Integer.class) return Integer.parseInt(value);
        if (type == long.class || type == Long.class) return Long.parseLong(value);
        if (type == double.class || type == Double.class) return Double.parseDouble(value);
        if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(value);
        if (type == String.class) return value;

        if (List.class.isAssignableFrom(type)) {

            List<Object> list = new ArrayList<>();

            String inner = value.substring(1, value.length() - 1);
            if (inner.isEmpty()) return list;

            String[] parts = splitSafe(inner);

            Class<?> elementType = Object.class;

            if (genericType instanceof java.lang.reflect.ParameterizedType pt) {
                elementType = (Class<?>) pt.getActualTypeArguments()[0];
            }

            for (String part : parts) {
                list.add(parseValue(elementType, elementType, part.trim()));
            }

            return list;
        }

        if (Set.class.isAssignableFrom(type)) {
            return new HashSet<>((Collection<?>) Objects.requireNonNull(parseValue(List.class, genericType, value)));
        }

        if (value.startsWith("{") && value.endsWith("}")) {

            String inner = value.substring(1, value.length() - 1);
            Map<String, String> nestedFields = new HashMap<>();

            for (String pair : splitSafe(inner)) {
                String[] kv = pair.split("=", 2);
                nestedFields.put(kv[0], kv[1]);
            }

            return createInstance(type.getName(), nestedFields);
        }

        throw new UnsupportedOperationException("Unsupported field type: " + type);
    }

    private String serializeValue(Object value) throws IllegalAccessException {

        if (value == null) return "null";

        if (value instanceof List<?> list) {
            return "[" + list.stream()
                    .map(v -> {
                        try { return serializeValue(v); }
                        catch (Exception e) { throw new RuntimeException(e); }
                    })
                    .reduce((a, b) -> a + "," + b)
                    .orElse("") + "]";
        }

        if (value instanceof Set<?> set) {
            return serializeValue(new ArrayList<>(set));
        }

        if (value.getClass().getDeclaredFields().length > 0
                && !value.getClass().getName().startsWith("java.")) {

            StringBuilder sb = new StringBuilder("{");

            for (Field f : value.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                sb.append(f.getName())
                        .append("=")
                        .append(serializeValue(f.get(value)))
                        .append(",");
            }

            if (sb.charAt(sb.length() - 1) == ',') {
                sb.deleteCharAt(sb.length() - 1);
            }

            sb.append("}");
            return sb.toString();
        }

        return value.toString();
    }

    private String[] splitSafe(String input) {
        List<String> parts = new ArrayList<>();
        int bracket = 0;
        int brace = 0;

        StringBuilder current = new StringBuilder();

        for (char c : input.toCharArray()) {
            if (c == '[') bracket++;
            if (c == ']') bracket--;
            if (c == '{') brace++;
            if (c == '}') brace--;

            if (c == ',' && bracket == 0 && brace == 0) {
                parts.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        parts.add(current.toString());
        return parts.toArray(new String[0]);
    }

}
