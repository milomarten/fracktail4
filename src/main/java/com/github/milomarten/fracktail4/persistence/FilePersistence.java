package com.github.milomarten.fracktail4.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.io.File;

@RequiredArgsConstructor
@Slf4j
public class FilePersistence implements Persistence {
    private static final String FILE_SUFFIX = ".json";

    private final ObjectMapper om;
    private final String baseFolderName;
    private File baseFolder;

    private File getBaseFolder() {
        if (baseFolder == null) {
            baseFolder = new File(this.baseFolderName);
            if (baseFolder.exists() && baseFolder.isFile()) {
                throw new IllegalArgumentException("Base folder is an existing file. Cannot be used");
            } else if (!baseFolder.exists()) {
                baseFolder.mkdirs();
            }
        }
        return baseFolder;
    }

    private File getChildFile(String key) {
        return new File(getBaseFolder(), key + FILE_SUFFIX);
    }

    @Override
    public Mono<Void> store(String key, Object value) {
        return Mono.fromCallable(() -> {
            var child = getChildFile(key);
            if (!child.exists()) {
                child.createNewFile();
            }
            log.info("Storing in file {}", child.getAbsolutePath());
            om.writeValue(child, value);
            return null;
        });
    }

    @Override
    public <T> Mono<T> retrieve(String key, Class<T> clazz) {
        return Mono.fromCallable(() -> {
            var child = getChildFile(key);
            if (!child.exists()) {
                return null;
            }
            log.info("Retrieving from file {}", child.getAbsolutePath());
            return om.readValue(child, clazz);
        });
    }

    @Override
    public <T> Mono<T> retrieve(String key, TypeReference<T> clazz) {
        return Mono.fromCallable(() -> {
            var child = getChildFile(key);
            if (!child.exists()) {
                return null;
            }
            log.info("Retrieving from file {}", child.getAbsolutePath());
            return om.readValue(child, clazz);
        });
    }

    @Override
    public Mono<Boolean> hasKey(String key) {
        return Mono.fromCallable(() -> {
            var child = getChildFile(key);
            return child.exists() && child.isFile();
        });
    }
}
