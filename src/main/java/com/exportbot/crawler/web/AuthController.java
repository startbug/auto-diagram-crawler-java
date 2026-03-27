package com.exportbot.crawler.web;

import com.exportbot.crawler.auth.AuthData;
import com.exportbot.crawler.auth.AuthStore;
import com.exportbot.crawler.config.AppConfig;
import com.exportbot.crawler.dto.LoginResponseDTO;
import com.exportbot.crawler.dto.UserInfoDTO;
import com.exportbot.crawler.entity.SysUserEntity;
import com.exportbot.crawler.entity.common.R;
import com.exportbot.crawler.repository.SysUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<R<AuthData>> getAuth() {
        AuthData auth = authStore.load(config.getAuth().getStorePath());
        return ResponseEntity.ok(R.success(auth));
    }

    @PutMapping
    public ResponseEntity<R<Void>> saveAuth(@RequestBody AuthData authData) {
        authStore.save(config.getAuth().getStorePath(), authData);
        return ResponseEntity.ok(R.success(null));
    }

    @PostMapping("/login")
    public ResponseEntity<R<LoginResponseDTO>> login(@RequestBody LoginRequest request) {
        try {
            logger.info("Login attempt: username={}", request.username);

            Optional<SysUserEntity> userOpt = sysUserRepository.findByUsername(request.username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(R.error("用户名或密码错误"));
            }

            SysUserEntity user = userOpt.get();

            // 检查用户状态
            if (user.getStatus() == 0) {
                return ResponseEntity.badRequest()
                        .body(R.error("账号已被禁用"));
            }

            // 验证密码（明文比较，生产环境应该使用加密）
            if (!user.getPassword().equals(request.password)) {
                return ResponseEntity.badRequest()
                        .body(R.error("用户名或密码错误"));
            }

            // 生成简单 token
            String token = UUID.randomUUID().toString().replace("-", "");

            // 构建响应
            LoginResponseDTO response = new LoginResponseDTO();
            response.setToken(token);
            
            UserInfoDTO userInfo = new UserInfoDTO();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setNickname(user.getNickname());
            userInfo.setRole(user.getRole());
            response.setUserInfo(userInfo);

            logger.info("Login success: username={}", request.username);
            return ResponseEntity.ok(R.success(response));
        } catch (Exception e) {
            logger.error("Login failed", e);
            return ResponseEntity.internalServerError()
                    .body(R.error(e.getMessage()));
        }
    }

    public static class LoginRequest {
        public String username;
        public String password;
    }
}
