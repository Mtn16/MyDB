package io.github.mtn16;


import com.google.gson.*;
import io.leangen.geantyref.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Predicate;

public class JsonReader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final File file;
    private final Path path;
    private JsonObject jsonData;

    public JsonReader(File file) {
        this.file = file;
        this.path = file.toPath();
        System.out.println(this.path);
        load();
    }

    private void createDefaultConfig() {
        System.out.println(this.path.getParent());
        if (!Files.exists(this.path)) {
            try (InputStream inputStream = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("defaultDB.json"))) {
                Files.createDirectories(this.path.getParent());
                Files.copy(inputStream, this.path, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void load() {
        if (!Files.exists(path)) {
            createDefaultConfig();
        }

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            jsonData = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload() { load(); }

    public void save() {
        try (Writer writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            GSON.toJson(jsonData, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void insert(Object object) {
        String className = object.getClass().getCanonicalName().toLowerCase() + "s";

        JsonObject classesSection = getSection("classes");

        JsonArray array;
        if (classesSection.has(className) && classesSection.get(className).isJsonArray()) {
            array = classesSection.getAsJsonArray(className);
        } else {
            array = new JsonArray();
            classesSection.add(className, array);
        }

        JsonElement jsonElement = GSON.toJsonTree(object);
        array.add(jsonElement);

        save();
    }

    public <T> void update(Class<T> clazz, T updateValue, Predicate<T> filter) {
        List<T> list = getAll(clazz);


        for (int i = 0; i < list.size(); i++) {
            if(filter.test(list.get(i))){
                list.set(i, updateValue);
            }
        }

        String className = clazz.getCanonicalName().toLowerCase() + "s";
        JsonObject classesSection = getSection("classes");

        JsonArray array;
        if (classesSection.has(className) && classesSection.get(className).isJsonArray()) {
            array = classesSection.getAsJsonArray(className);
        } else {
            array = new JsonArray();
            classesSection.add(className, array);
        }

        JsonElement jsonElement = GSON.toJsonTree(list);
        array = jsonElement.getAsJsonArray();
        if(classesSection.has(className)) {
            classesSection.remove(className);
        }
        classesSection.add(className, array);

        save();
    }

    private <T> List<T> getAll(Class<T> clazz) {
        String className = clazz.getCanonicalName().toLowerCase() + "s";
        String path = "classes." + className;

        JsonObject classesSection = getSection("classes");

        if (!classesSection.has(className) || !classesSection.get(className).isJsonArray()) {
            return Collections.emptyList();
        }

        JsonArray array = classesSection.getAsJsonArray(className);
        List<T> result = new ArrayList<>();

        for (JsonElement element : array) {
            T obj = GSON.fromJson(element, clazz);
            result.add(obj);
        }

        return result;
    }

    public <T> List<T> find(
            Class<T> clazz,
            Predicate<T> filter
    ) {
        return getAll(clazz).stream()
                .filter(filter)
                .toList();
    }


    private JsonObject getSection(String sectionPath) {
        String[] keys = sectionPath.split("\\.");
        JsonObject current = jsonData;

        for (String key : keys) {
            if (current.has(key) && current.get(key).isJsonObject()) {
                current = current.getAsJsonObject(key);
            } else {
                JsonObject newSection = new JsonObject();
                current.add(key, newSection);
                current = newSection;
            }
        }
        return current;
    }
}