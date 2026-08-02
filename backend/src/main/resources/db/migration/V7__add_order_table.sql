CREATE SEQUENCE orders_id_seq START WITH 1 INCREMENT BY 50;
CREATE TABLE orders(
    id BIGINT PRIMARY KEY Default nextval('orders_id_seq'),
    user_id BIGINT NOT NULL references users(id) ON DELETE RESTRICT,
    hold_id BIGINT NOT NULL references holds(id) ON DELETE RESTRICT,
    CONSTRAINT unique_hold_order UNIQUE (hold_id),
    tier_id BIGINT NOT NULL references ticket_tiers(id) ON DELETE RESTRICT,
    quantity INT NOT NULL,
    CONSTRAINT check_quantity
        check(quantity > 0),
    total_cents BIGINT NOT NULL,
    CONSTRAINT check_total_positive
        check ( total_cents >= 0 ),
    stripe_session_id VARCHAR(255),
    status VARCHAR(100) NOT NULL,
    CONSTRAINT check_status
            check ( status in ('PENDING', 'CANCELLED', 'PAID',
                               'REFUNDED') ),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_orders_pending_by_user
    ON orders(user_id)
    WHERE status = 'PENDING';
CREATE INDEX idx_orders_paid_by_user
    ON orders(user_id)
    WHERE status = 'PAID';