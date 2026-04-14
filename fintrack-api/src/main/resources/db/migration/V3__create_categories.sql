CREATE TABLE categories (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    icon       VARCHAR(50) NOT NULL,
    type       ENUM('INCOME', 'EXPENSE', 'BOTH') NOT NULL,
    is_system  BOOLEAN NOT NULL DEFAULT TRUE,
    user_id    BIGINT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_categories_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_categories_user_id ON categories(user_id);
CREATE INDEX idx_categories_type ON categories(type);
