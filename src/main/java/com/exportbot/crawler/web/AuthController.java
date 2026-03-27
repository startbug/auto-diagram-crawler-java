package com.exportbot.crawler.web;

import com.exportbot.crawler.auth.AuthData;
import com.exportbot.crawler.auth.AuthStore;
import com.exportbot.crawler.config.AppConfig;
import com.exportbot.crawler.entity.SysUserEntity;
import com.exportbot.crawler.repository.SysUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthStore authStore;
    private final AppConfig config;
    private final SysUserRepository sysUserRepository;

    public AuthController(AuthStore authStore, AppConfig config, SysUserRepository sysUserRepository) {
        this.authStore = authStore;
        this.config = config;
        this.sysUserRepository = sysUserRepository;
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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            logger.info("Login attempt: username={}", request.username);

            Optional<SysUserEntity> userOpt = sysUserRepository.findByUsername(request.username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "用户名或密码错误"));
            }

            SysUserEntity user = userOpt.get();

            // 检查用户状态
            if (user.getStatus() == 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "账号已被禁用"));
            }

            // 验证密码（明文比较，生产环境应该使用加密）
            if (!user.getPassword().equals(request.password)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "用户名或密码错误"));
            }

            // 生成简单 token
            String token = UUID.randomUUID().toString().replace("-", "");

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("role", user.getRole());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("token", token);
            response.put("userInfo", userInfo);

            logger.info("Login success: username={}", request.username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Login failed", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    public static class LoginRequest {
        public String username;
        public String password;
    }
}
