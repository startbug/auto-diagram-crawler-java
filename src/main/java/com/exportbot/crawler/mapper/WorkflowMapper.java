package com.exportbot.crawler.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exportbot.crawler.entity.WorkflowEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WorkflowMapper extends BaseMapper<WorkflowEntity> {

    /**
     * 查询所有工作流，按更新时间倒序
     */
    List<WorkflowEntity> selectAllOrderByUpdatedAt();

    /**
     * 根据 code 查询工作流
     */
    WorkflowEntity selectByCode(@Param("code") String code);

    /**
     * 插入或更新工作流（存在则更新，不存在则插入）
     */
    int insertOrUpdate(@Param("code") String code,
                       @Param("name") String name,
                       @Param("description") String description,
                       @Param("content") String content,
                       @Param("creator") String creator,
                       @Param("modifier") String modifier);

    /**
     * 根据 code 删除工作流
     */
    int deleteByCode(@Param("code") String code);

    /**
     * 创建数据库
     */
    void createDatabase();

    /**
     * 删除表
     */
    void dropTable();

    /**
     * 创建表
     */
    void createTable();

    /**
     * 如果字段不存在则添加字段
     */
    void addColumnIfNotExists(@Param("columnName") String columnName,
                              @Param("columnType") String columnType,
                              @Param("comment") String comment);

    /**
     * 如果字段存在则重命名字段
     */
    void renameColumnIfExists(@Param("oldName") String oldName,
                              @Param("newName") String newName,
                              @Param("columnType") String columnType,
                              @Param("comment") String comment);

    /**
     * 如果索引不存在则添加索引
     */
    void addIndexIfNotExists(@Param("indexName") String indexName,
                             @Param("columnName") String columnName);

    /**
     * 更新字段备注
     */
    void updateColumnComment(@Param("columnName") String columnName,
                             @Param("comment") String comment);

    /**
     * 更新表备注
     */
    void updateTableComment(@Param("comment") String comment);
}
