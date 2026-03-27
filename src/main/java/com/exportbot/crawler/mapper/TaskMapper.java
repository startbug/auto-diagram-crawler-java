package com.exportbot.crawler.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exportbot.crawler.entity.TaskEntity;
import com.exportbot.crawler.schema.SchemaAware;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskMapper extends BaseMapper<TaskEntity>, SchemaAware {

    /**
     * 根据UUID查询任务
     */
    TaskEntity selectByUuid(@Param("uuid") String uuid);

    /**
     * 分页查询任务列表
     */
    IPage<TaskEntity> selectTaskPage(IPage<TaskEntity> page,
                                     @Param("orderId") Long orderId,
                                     @Param("status") Integer status,
                                     @Param("email") String email);

    /**
     * 查询待执行的任务（限制数量）
     */
    List<TaskEntity> selectPendingTasks(@Param("limit") int limit);

    /**
     * 重置任务状态
     */
    int resetTask(@Param("id") Long id);

    /**
     * 更新任务状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("errorMessage") String errorMessage);

    /**
     * 更新OSS地址
     */
    int updateOssUrl(@Param("id") Long id, @Param("ossUrl") String ossUrl);

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
        return "tasks";
    }
}
