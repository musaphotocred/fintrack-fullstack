CREATE TABLE accounts (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    name       VARCHAR(100) NOT NULL,
    type       ENUM('CHEQUE', 'SAVINGS', 'WALLET', 'CREDIT') NOT NULL,
    currency   VARCHAR(3) NOT NULL DEFAULT 'ZAR',
    balance    DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME NULL,
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_accounts_deleted_at ON accounts(deleted_at);
