CREATE TABLE reports (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT NOT NULL,
    type         ENUM('MONTHLY_STATEMENT', 'ANNUAL_SUMMARY') NOT NULL,
    status       ENUM('PENDING', 'PROCESSING', 'COMPLETE', 'FAILED') NOT NULL DEFAULT 'PENDING',
    period_start DATE NOT NULL,
    period_end   DATE NOT NULL,
    s3_key       VARCHAR(512) NULL,
    error_msg    VARCHAR(255) NULL,
    created_at   DATETIME NOT NULL,
    updated_at   DATETIME NOT NULL,
    CONSTRAINT fk_reports_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_reports_user_id ON reports(user_id);
CREATE INDEX idx_reports_status ON reports(status);
