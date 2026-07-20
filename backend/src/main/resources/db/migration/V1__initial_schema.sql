
CREATE SEQUENCE users_id_seq START WITH 1 INCREMENT BY 50;
CREATE TABLE users (
    id BIGINT PRIMARY KEY DEFAULT nextval('users_id_seq'),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    --email will be converted to lowercase before beign sent to database
    email VARCHAR(250) UNIQUE NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL ,
    CONSTRAINT check_user_role
        CHECK(role IN('ATTENDEE', 'ORGANIZER', 'ADMIN')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE SEQUENCE venues_id_seq START WITH 1 INCREMENT BY 50;
CREATE TABLE venues(
    id BIGINT PRIMARY KEY DEFAULT nextval('venues_id_seq'),
    organizer_id BIGINT NOT NULL references users(id) ON DELETE RESTRICT,
    name VARCHAR(100) NOT NULL,
    street_name VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    zip VARCHAR(20),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE SEQUENCE events_id_seq START WITH 1 INCREMENT BY 50;
CREATE TABLE events(
    id BIGINT PRIMARY KEY DEFAULT  nextval('events_id_seq'),
    venue_id BIGINT NOT NULL references venues(id) ON DELETE RESTRICT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    starts_at TIMESTAMP WITH TIME ZONE NOT NULL ,
    ends_at TIMESTAMP WITH TIME ZONE NOT NULL ,
    CONSTRAINT start_before_end
        check ( starts_at < ends_at ),
    status VARCHAR(100) NOT NULL,
    CONSTRAINT check_status
        CHECK ( status IN ('DRAFT', 'PUBLISHED', 'CANCELED')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE SEQUENCE ticket_tiers_id_seq START WITH 1 INCREMENT BY 50;
CREATE TABLE ticket_tiers(
    id BIGINT PRIMARY KEY DEFAULT nextval('ticket_tiers_id_seq'),
    -- intending for a soft delete rather than a full on delete
    event_id BIGINT NOT NULL references events(id) ON DELETE RESTRICT,
    price_cents BIGINT NOT NULL ,
    CONSTRAINT check_price_not_negative
        check( price_cents >= 0),
    capacity INT NOT NULL,
    CONSTRAINT capacity_not_zero
        check ( capacity > 0 ),
    reserved INT NOT NULL DEFAULT 0,
    sold INT NOT NULL DEFAULT 0,
    CONSTRAINT check_capacity_available
        check(reserved + sold <= capacity),
    tier_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()

);