package com.ly.lmsbackend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class DatabaseSchemaMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSchemaMigration.class);
    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Checking Stage 4 database schema and migrations...");

        // Ensure columns exist on courses table
        applySql("ALTER TABLE IF EXISTS courses ADD COLUMN IF NOT EXISTS level VARCHAR(50) DEFAULT 'ALL_LEVELS'");
        applySql("ALTER TABLE IF EXISTS courses ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'PUBLISHED'");
        applySql("ALTER TABLE IF EXISTS courses ADD COLUMN IF NOT EXISTS learning_outcomes TEXT");
        applySql("ALTER TABLE IF EXISTS courses ADD COLUMN IF NOT EXISTS requirements TEXT");

        // Backfill null values for legacy records if courses table exists
        applySql("UPDATE courses SET level = 'ALL_LEVELS' WHERE level IS NULL");
        applySql("UPDATE courses SET status = 'PUBLISHED' WHERE status IS NULL");

        // Ensure course_favorites table exists
        applySql("""
            CREATE TABLE IF NOT EXISTS course_favorites (
                favorite_id BIGSERIAL PRIMARY KEY,
                user_id BIGINT NOT NULL,
                course_id BIGINT NOT NULL,
                created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT uk_user_course UNIQUE (user_id, course_id),
                CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                CONSTRAINT fk_favorite_course FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE
            )
        """);

        // Ensure student points columns exist
        applySql("ALTER TABLE IF EXISTS students ADD COLUMN IF NOT EXISTS total_points DOUBLE PRECISION DEFAULT 0.0");
        applySql("UPDATE students SET total_points = 0.0 WHERE total_points IS NULL");

        // Ensure enrollment points columns exist
        applySql("ALTER TABLE IF EXISTS enrollments ADD COLUMN IF NOT EXISTS earned_points DOUBLE PRECISION DEFAULT 0.0");
        applySql("ALTER TABLE IF EXISTS enrollments ADD COLUMN IF NOT EXISTS total_points DOUBLE PRECISION DEFAULT 0.0");
        applySql("UPDATE enrollments SET earned_points = 0.0 WHERE earned_points IS NULL");
        applySql("UPDATE enrollments SET total_points = 0.0 WHERE total_points IS NULL");

        // Ensure certificates table exists
        applySql("""
            CREATE TABLE IF NOT EXISTS certificates (
                certificate_id BIGSERIAL PRIMARY KEY,
                certificate_code VARCHAR(100) NOT NULL UNIQUE,
                user_id BIGINT NOT NULL,
                course_id BIGINT NOT NULL,
                issued_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                certificate_url VARCHAR(500),
                final_score DOUBLE PRECISION DEFAULT 0.0,
                CONSTRAINT fk_certificate_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                CONSTRAINT fk_certificate_course FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE
            )
        """);

        // Ensure notifications table exists
        applySql("""
            CREATE TABLE IF NOT EXISTS notifications (
                id BIGSERIAL PRIMARY KEY,
                user_id BIGINT NOT NULL,
                title VARCHAR(255) NOT NULL,
                message TEXT NOT NULL,
                type VARCHAR(50) DEFAULT 'SYSTEM',
                is_read BOOLEAN DEFAULT FALSE,
                action_url VARCHAR(500),
                created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
            )
        """);

        // Ensure activity_logs table exists
        applySql("""
            CREATE TABLE IF NOT EXISTS activity_logs (
                id BIGSERIAL PRIMARY KEY,
                user_id BIGINT NOT NULL,
                activity_type VARCHAR(100) NOT NULL,
                description TEXT,
                timestamp TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT fk_activity_log_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
            )
        """);

        log.info("Stage 4 and Stage 6 database schema check completed.");
    }

    private void applySql(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception e) {
            log.warn("Database migration note for statement [{}]: {}", sql, e.getMessage());
        }
    }
}
