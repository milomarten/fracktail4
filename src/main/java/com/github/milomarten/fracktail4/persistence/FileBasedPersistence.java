package com.github.milomarten.fracktail4.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.File;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileBasedPersistence implements Persistence {
    private static final String PERSISTENCE_ROOT = "persistence";

    private final ObjectMapper mapper;

    @Override
    public Mono<Void> store(String key, Object value) {
        return Mono.fromCallable(() -> {
            var file = getOrCreateFile(key);
            mapper.writeValue(file, value);
            return null;
        });
    }

    @Override
    public <T> Mono<T> retrieve(String key, Class<T> clazz) {
        return Mono.fromCallable(() -> {
            var file = getFile(key);
            if (file.exists()) {
                return mapper.readValue(file, clazz);
            } else {
                return null;
            }
        });
    }

    @Override
    public <T> Mono<T> retrieve(String key, TypeReference<T> clazz) {
        return Mono.fromCallable(() -> {
            var file = getFile(key);
            if (file.exists()) {
                return mapper.readValue(file, clazz);
            } else {
                return null;
            }
        });
    }

    @Override
    public Mono<Boolean> hasKey(String key) {
        return Mono.fromCallable(() -> getFile(key).exists());
    }

    private File getFile(String key) throws Exception {
        return new File(PERSISTENCE_ROOT, key + ".json");
    }

    private File getOrCreateFile(String key) throws Exception {
        var file = getFile(key);
        var created = file.createNewFile();
        if (created) {
            log.info("Created file " + file.getCanonicalPath());
        }
        return file;
    }
}
