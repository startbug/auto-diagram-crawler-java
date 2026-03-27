package com.exportbot.crawler.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exportbot.crawler.dto.UserInfoDTO;
import com.exportbot.crawler.entity.SysUserEntity;
import com.exportbot.crawler.entity.common.R;
import com.exportbot.crawler.repository.SysUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/users")
public class SysUserController {

    private static final Logger logger = LoggerFactory.getLogger(SysUserController.class);

    private final SysUserRepository sysUserRepository;

    public SysUserController(SysUserRepository sysUserRepository) {
        this.sysUserRepository = sysUserRepository;
    }

    @GetMapping
    public ResponseEntity<R<IPage<SysUserEntity>>> listUsers(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(R.success(sysUserRepository.findPage(pageNum, pageSize, keyword)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<R<UserInfoDTO>> getUser(@PathVariable Long id) {
        return sysUserRepository.findById(id)
                .map(this::convertToDTO)
                .map(dto -> ResponseEntity.ok(R.success(dto)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<R<Void>> createUser(@RequestBody UserRequest request) {
        try {
            // 检查用户名是否已存在
            if (sysUserRepository.findByUsername(request.username).isPresent()) {
                return ResponseEntity.badRequest()
                        .body(R.error("用户名已存在"));
            }

            SysUserEntity user = new SysUserEntity();
            user.setUsername(request.username);
            user.setPassword(request.password); // TODO: 需要加密
            user.setNickname(request.nickname);
            user.setRole(request.role);
            user.setStatus(request.status != null ? request.status : 1);

            sysUserRepository.save(user);
            return ResponseEntity.ok(R.success(null));
        } catch (Exception e) {
            logger.error("Failed to create user", e);
            return ResponseEntity.internalServerError()
                    .body(R.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<R<Void>> updateUser(@PathVariable Long id, @RequestBody UserRequest request) {
        try {
            SysUserEntity user = sysUserRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 超级管理员不能被修改角色
            if ("SUPER_ADMIN".equals(user.getRole()) && request.role != null && !"SUPER_ADMIN".equals(request.role)) {
                return ResponseEntity.badRequest()
                        .body(R.error("超级管理员角色不能修改"));
            }

            if (request.nickname != null) {
                user.setNickname(request.nickname);
            }
            if (request.password != null && !request.password.isEmpty()) {
                user.setPassword(request.password); // TODO: 需要加密
            }
            if (request.role != null) {
                user.setRole(request.role);
            }
            if (request.status != null) {
                user.setStatus(request.status);
            }

            sysUserRepository.save(user);
            return ResponseEntity.ok(R.success(null));
        } catch (Exception e) {
            logger.error("Failed to update user", e);
            return ResponseEntity.internalServerError()
                    .body(R.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<R<Void>> deleteUser(@PathVariable Long id) {
        try {
            SysUserEntity user = sysUserRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 超级管理员不能删除
            if ("SUPER_ADMIN".equals(user.getRole())) {
                return ResponseEntity.badRequest()
                        .body(R.error("超级管理员不能删除"));
            }

            sysUserRepository.deleteById(id);
            return ResponseEntity.ok(R.success(null));
        } catch (Exception e) {
            logger.error("Failed to delete user", e);
            return ResponseEntity.internalServerError()
                    .body(R.error(e.getMessage()));
        }
    }

    private UserInfoDTO convertToDTO(SysUserEntity entity) {
        UserInfoDTO dto = new UserInfoDTO();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setNickname(entity.getNickname());
        dto.setRole(entity.getRole());
        dto.setStatus(entity.getStatus());
        return dto;
    }

    public static class UserRequest {
        public String username;
        public String password;
        public String nickname;
        public String role;
        public Integer status;
    }
}
