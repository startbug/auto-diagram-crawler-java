package com.exportbot.crawler.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class AuthStore {

    private static final Logger logger = LoggerFactory.getLogger(AuthStore.class);
    private final ObjectMapper objectMapper;

    public AuthStore() {
        this.objectMapper = new ObjectMapper();
    }

    public AuthData load(String storePath) {
        Path path = Paths.get(storePath);
        File file = path.toFile();

        if (!file.exists()) {
            logger.warn("Auth file not found: {}, returning empty auth data", storePath);
            return new AuthData();
        }

        try {
            AuthData authData = objectMapper.readValue(file, AuthData.class);
            logger.info("Loaded auth data: {} cookies, token present: {}",
                    authData.getCookies().size(), authData.getToken() != null);
            return authData;
        } catch (IOException e) {
            logger.error("Failed to load auth data from: {}", storePath, e);
            return new AuthData();
        }
    }

    public void save(String storePath, AuthData authData) {
        try {
            Path path = Paths.get(storePath);
            path.getParent().toFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), authData);
            logger.info("Saved auth data to: {}", storePath);
        } catch (IOException e) {
            logger.error("Failed to save auth data to: {}", storePath, e);
            throw new RuntimeException("Failed to save auth data", e);
        }
    }
}
