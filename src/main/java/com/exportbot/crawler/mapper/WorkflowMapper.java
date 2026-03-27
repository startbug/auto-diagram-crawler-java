package com.exportbot.crawler.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exportbot.crawler.entity.WorkflowEntity;
import com.exportbot.crawler.schema.SchemaAware;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WorkflowMapper extends BaseMapper<WorkflowEntity>, SchemaAware {

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
     * 分页查询工作流列表
     */
    IPage<WorkflowEntity> selectWorkflowPage(IPage<WorkflowEntity> page,
                                              @Param("keyword") String keyword);

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
        return "workflows";
    }
}
