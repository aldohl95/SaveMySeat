CREATE SEQUENCE refresh_tokens_id_seq INCREMENT BY 50;

CREATE TABLE refresh_tokens(
    id BIGINT PRIMARY KEY DEFAULT nextval('refresh_tokens_id_seq'),
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    replaced_by_id BIGINT REFERENCES refresh_tokens(id),
    user_id BIGINT NOT NULL REFERENCES users(id) on DELETE CASCADE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CHECK ( replaced_by_id != id )
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);