CREATE SEQUENCE holds_id_seq START WITH 1 INCREMENT BY 50;
CREATE TABLE holds(
    id BIGINT PRIMARY KEY DEFAULT nextval('holds_id_seq'),
    user_id BIGINT NOT NULL references users(id) ON DELETE CASCADE,
    tier_id BIGINT NOT NULL references ticket_tiers(id) ON DELETE RESTRICT,
    quantity INT NOT NULL,
    CONSTRAINT check_greater_than_zero
        check ( quantity > 0 ),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(100) NOT NULL,
    CONSTRAINT check_status
        check ( status IN('ACTIVE', 'EXPIRED', 'CONVERTED')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);
--Add a sweeper that delete holds with expires_at < now() AND status = 'ACTIVE'
CREATE INDEX idx_holds_active_by_tier
    on holds(tier_id)
    where status = 'ACTIVE';
CREATE INDEX idx_holds_active_by_user
    ON holds(user_id)
    WHERE status = 'ACTIVE';