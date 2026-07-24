--temp bootstrap organizer for dev
--replaced by security derived users in week 3
INSERT INTO users(first_name, last_name, email, password_hash, role)
VALUES ('Dev', 'Organizer', 'dev@savemyseat.local', 'notARealHash',
        'ORGANIZER');