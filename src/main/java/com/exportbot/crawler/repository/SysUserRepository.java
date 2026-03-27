package com.exportbot.crawler.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exportbot.crawler.entity.SysUserEntity;
import com.exportbot.crawler.mapper.SysUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SysUserRepository {

    private static final Logger logger = LoggerFactory.getLogger(SysUserRepository.class);

    private final SysUserMapper sysUserMapper;

    public SysUserRepository(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    public Optional<SysUserEntity> findById(Long id) {
        return Optional.ofNullable(sysUserMapper.selectById(id));
    }

    public Optional<SysUserEntity> findByUsername(String username) {
        return Optional.ofNullable(sysUserMapper.selectByUsername(username));
    }

    public IPage<SysUserEntity> findPage(int pageNum, int pageSize, String keyword) {
        Page<SysUserEntity> page = new Page<>(pageNum, pageSize);
        return sysUserMapper.selectUserPage(page, keyword);
    }

    public void save(SysUserEntity user) {
        if (user.getId() == null) {
            sysUserMapper.insert(user);
            logger.info("User created: {}", user.getUsername());
        } else {
            sysUserMapper.updateById(user);
            logger.info("User updated: {}", user.getUsername());
        }
    }

    public void deleteById(Long id) {
        sysUserMapper.deleteById(id);
        logger.info("User deleted: {}", id);
    }

    public boolean isSuperAdmin(Long id) {
        return findById(id)
                .map(user -> "SUPER_ADMIN".equals(user.getRole()))
                .orElse(false);
    }
}
