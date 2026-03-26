package com.exportbot.crawler.repository;

import com.exportbot.crawler.config.Workflow;
import com.exportbot.crawler.config.ConfigLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class WorkflowRepository {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowRepository.class);

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final ConfigLoader configLoader;

    public WorkflowRepository(JdbcTemplate jdbcTemplate, ConfigLoader configLoader) {
        this.jdbcTemplate = jdbcTemplate;
        this.configLoader = configLoader;
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void init() {
        // Create database if not exists (for first run)
        try {
            jdbcTemplate.execute("CREATE DATABASE IF NOT EXISTS auto_diagram");
            logger.info("Database auto_diagram created/verified");
        } catch (Exception e) {
            logger.debug("Database already exists or error creating: {}", e.getMessage());
        }
        
        // Drop old table if exists (to migrate from old schema)
        try {
            jdbcTemplate.execute("DROP TABLE IF EXISTS workflows");
            logger.info("Old workflows table dropped");
        } catch (Exception e) {
            logger.debug("No old table to drop: {}", e.getMessage());
        }
        
        // Create table (id: bigint auto_increment, code: unique)
        jdbcTemplate.execute("""
            CREATE TABLE workflows (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                code VARCHAR(64) NOT NULL UNIQUE,
                name VARCHAR(255) NOT NULL,
                description TEXT,
                content TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                INDEX idx_code (code)
            )
        """);
        logger.info("Workflow table initialized with new schema");
    }

    public List<WorkflowEntity> findAll() {
        return jdbcTemplate.query(
            "SELECT id, code, name, description, content, created_at, updated_at FROM workflows ORDER BY updated_at DESC",
            new WorkflowRowMapper()
        );
    }

    public Optional<WorkflowEntity> findByCode(String code) {
        List<WorkflowEntity> results = jdbcTemplate.query(
            "SELECT id, code, name, description, content, created_at, updated_at FROM workflows WHERE code = ?",
            new WorkflowRowMapper(),
            code
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Workflow loadWorkflow(String code) {
        Optional<WorkflowEntity> entity = findByCode(code);
        if (entity.isPresent()) {
            return configLoader.loadWorkflowFromString(entity.get().content());
        }
        throw new RuntimeException("Workflow not found: " + code);
    }

    public void save(String code, String name, String description, String content) {
        jdbcTemplate.update("""
            INSERT INTO workflows (code, name, description, content)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                name = VALUES(name),
                description = VALUES(description),
                content = VALUES(content),
                updated_at = CURRENT_TIMESTAMP
        """, code, name, description, content);
        logger.info("Workflow saved: {}", code);
    }

    public void delete(String code) {
        jdbcTemplate.update("DELETE FROM workflows WHERE code = ?", code);
        logger.info("Workflow deleted: {}", code);
    }

    public record WorkflowEntity(
        Long id,
        String code,
        String name,
        String description,
        String content,
        java.time.LocalDateTime createdAt,
        java.time.LocalDateTime updatedAt
    ) {}

    private static class WorkflowRowMapper implements RowMapper<WorkflowEntity> {
        @Override
        public WorkflowEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new WorkflowEntity(
                rs.getLong("id"),
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("content"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime()
            );
        }
    }
}
