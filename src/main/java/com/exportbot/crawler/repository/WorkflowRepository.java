package com.exportbot.crawler.repository;

import com.exportbot.crawler.config.Workflow;
import com.exportbot.crawler.config.ConfigLoader;
import com.exportbot.crawler.entity.WorkflowEntity;
import com.exportbot.crawler.mapper.WorkflowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class WorkflowRepository {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowRepository.class);

    private final WorkflowMapper workflowMapper;
    private final ConfigLoader configLoader;

    public WorkflowRepository(WorkflowMapper workflowMapper, ConfigLoader configLoader) {
        this.workflowMapper = workflowMapper;
        this.configLoader = configLoader;
    }

    public List<WorkflowEntity> findAll() {
        return workflowMapper.selectAllOrderByUpdatedAt();
    }

    public Optional<WorkflowEntity> findByCode(String code) {
        WorkflowEntity entity = workflowMapper.selectByCode(code);
        return Optional.ofNullable(entity);
    }

    public Workflow loadWorkflow(String code) {
        WorkflowEntity entity = workflowMapper.selectByCode(code);
        if (entity != null) {
            return configLoader.loadWorkflowFromString(entity.getContent());
        }
        throw new RuntimeException("Workflow not found: " + code);
    }

    public void save(String code, String name, String description, String content) {
        save(code, name, description, content, null, null);
    }

    public void save(String code, String name, String description, String content, String creator, String modifier) {
        workflowMapper.insertOrUpdate(code, name, description, content, creator, modifier);
        logger.info("Workflow saved: {}", code);
    }

    public void delete(String code) {
        workflowMapper.deleteByCode(code);
        logger.info("Workflow deleted: {}", code);
    }
}
