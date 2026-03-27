package com.exportbot.crawler.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exportbot.crawler.entity.SchedulerConfigEntity;
import com.exportbot.crawler.mapper.SchedulerConfigMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SchedulerConfigRepository {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerConfigRepository.class);

    private final SchedulerConfigMapper schedulerConfigMapper;

    public SchedulerConfigRepository(SchedulerConfigMapper schedulerConfigMapper) {
        this.schedulerConfigMapper = schedulerConfigMapper;
    }

    public Optional<SchedulerConfigEntity> findById(Long id) {
        return Optional.ofNullable(schedulerConfigMapper.selectById(id));
    }

    public Optional<SchedulerConfigEntity> findByJobName(String jobName) {
        return Optional.ofNullable(schedulerConfigMapper.selectByJobName(jobName));
    }

    public List<SchedulerConfigEntity> findEnabledConfigs() {
        return schedulerConfigMapper.selectEnabledConfigs();
    }

    public IPage<SchedulerConfigEntity> findPage(int pageNum, int pageSize, String jobName) {
        Page<SchedulerConfigEntity> page = new Page<>(pageNum, pageSize);
        return schedulerConfigMapper.selectConfigPage(page, jobName);
    }

    public void save(SchedulerConfigEntity config) {
        if (config.getId() == null) {
            schedulerConfigMapper.insert(config);
            logger.info("Scheduler config created: {}", config.getJobName());
        } else {
            schedulerConfigMapper.updateById(config);
            logger.info("Scheduler config updated: {}", config.getJobName());
        }
    }

    public void deleteById(Long id) {
        schedulerConfigMapper.deleteById(id);
        logger.info("Scheduler config deleted: {}", id);
    }

    public boolean updateLastExecute(Long id, String result) {
        int rows = schedulerConfigMapper.updateLastExecute(id, result);
        if (rows > 0) {
            logger.info("Scheduler last execute updated: id={}, result={}", id, result);
        }
        return rows > 0;
    }
}
