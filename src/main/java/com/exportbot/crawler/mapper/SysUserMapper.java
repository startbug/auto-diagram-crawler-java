package com.exportbot.crawler.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exportbot.crawler.entity.SysUserEntity;
import com.exportbot.crawler.schema.SchemaAware;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUserEntity>, SchemaAware {

    /**
     * 根据用户名查询用户
     */
    SysUserEntity selectByUsername(@Param("username") String username);

    /**
     * 分页查询用户列表
     */
    IPage<SysUserEntity> selectUserPage(IPage<SysUserEntity> page, @Param("keyword") String keyword);

    /**
     * 创建表（如果不存在）
     */
    @Override
    void createTableIfNotExists();

    /**
     * 返回表名
     */
    @Override
    default String tableName() {
        return "sys_users";
    }
}
