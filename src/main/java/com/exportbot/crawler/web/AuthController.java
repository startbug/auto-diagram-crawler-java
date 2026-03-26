package com.exportbot.crawler.web;

import com.exportbot.crawler.auth.AuthData;
import com.exportbot.crawler.auth.AuthStore;
import com.exportbot.crawler.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthStore authStore;
    private final AppConfig config;

    public AuthController(AuthStore authStore, AppConfig config) {
        this.authStore = authStore;
        this.config = config;
    }

    @GetMapping
    public ResponseEntity<AuthData> getAuth() {
        AuthData auth = authStore.load(config.getAuth().getStorePath());
        return ResponseEntity.ok(auth);
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> saveAuth(@RequestBody AuthData authData) {
        authStore.save(config.getAuth().getStorePath(), authData);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Auth configuration saved");
        return ResponseEntity.ok(response);
    }
}
