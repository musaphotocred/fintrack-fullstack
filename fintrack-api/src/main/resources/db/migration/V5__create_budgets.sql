CREATE TABLE budgets (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT NOT NULL,
    category_id  BIGINT NOT NULL,
    month        DATE NOT NULL,
    limit_amount DECIMAL(15, 2) NOT NULL,
    created_at   DATETIME NOT NULL,
    updated_at   DATETIME NOT NULL,
    CONSTRAINT fk_budgets_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_budgets_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT uq_budget UNIQUE (user_id, category_id, month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_budgets_user_id ON budgets(user_id);
CREATE INDEX idx_budgets_category_id ON budgets(category_id);
CREATE INDEX idx_budgets_month ON budgets(month);
