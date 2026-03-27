package com.exportbot.crawler.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exportbot.crawler.entity.SchedulerConfigEntity;
import com.exportbot.crawler.schema.SchemaAware;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SchedulerConfigMapper extends BaseMapper<SchedulerConfigEntity>, SchemaAware {

    /**
     * 根据任务名称查询配置
     */
    SchedulerConfigEntity selectByJobName(@Param("jobName") String jobName);

    /**
     * 查询所有启用的配置
     */
    List<SchedulerConfigEntity> selectEnabledConfigs();

    /**
     * 分页查询配置列表
     */
    IPage<SchedulerConfigEntity> selectConfigPage(IPage<SchedulerConfigEntity> page,
                                                   @Param("jobName") String jobName);

    /**
     * 更新最后执行时间和结果
     */
    int updateLastExecute(@Param("id") Long id,
                          @Param("result") String result);

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
        return "scheduler_configs";
    }
}
