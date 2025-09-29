-- ACCOUNTS
CREATE TABLE IF NOT EXISTS user_account (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    email VARCHAR(256) NOT NULL UNIQUE,
    password VARCHAR(256) NOT NULL,
    level VARCHAR(256) NOT NULL DEFAULT 'user',
    active BOOLEAN NOT NULL DEFAULT FALSE,
    banned BOOLEAN NOT NULL DEFAULT FALSE
);

-- DELETED ACCOUNTS
CREATE TABLE IF NOT EXISTS user_deleted_account (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    email VARCHAR(256) NOT NULL
);

-- PROFILE
CREATE TABLE IF NOT EXISTS user_profile (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    name VARCHAR(256),
    phone VARCHAR(25),
    identity_document VARCHAR(256),
    gender VARCHAR(256),
    birthdate VARCHAR(50),
    biography VARCHAR(256),
    profile_image VARCHAR(555),
    language VARCHAR(50)
);

-- USER LOGS
CREATE TABLE IF NOT EXISTS user_log (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    ip_address VARCHAR(256) NOT NULL,
    id_user UUID NOT NULL,
    agent VARCHAR(512) NOT NULL,
    update_type VARCHAR(256) NOT NULL,
    old_value TEXT NOT NULL,
    new_value TEXT NOT NULL
);

-- USER ADDRESS
CREATE TABLE IF NOT EXISTS user_address (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    address_name VARCHAR(256) NOT NULL,
    zip_code VARCHAR(50) NOT NULL,
    street VARCHAR(256) NOT NULL,
    number VARCHAR(50) NOT NULL,
    address_line_two VARCHAR(256),
    neighborhood VARCHAR(256) NOT NULL,
    city VARCHAR(256) NOT NULL,
    state VARCHAR(256) NOT NULL,
    country VARCHAR(256) NOT NULL,
    address_type VARCHAR(256) NOT NULL,
    is_primary BOOLEAN NOT NULL,
    landmark VARCHAR(256),
    id_user UUID NOT NULL
);