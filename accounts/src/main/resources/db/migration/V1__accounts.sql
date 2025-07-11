-- ACCOUNTS
CREATE TABLE IF NOT EXISTS user_account (
    id VARCHAR(256) PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    email VARCHAR(256) NOT NULL,
    password VARCHAR(256) NOT NULL,
    level VARCHAR(256) NOT NULL DEFAULT 'user',
    active BOOLEAN NOT NULL DEFAULT FALSE,
    banned BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT unique_email UNIQUE (email)
);

-- PROFILE
CREATE TABLE IF NOT EXISTS user_profile (
    id VARCHAR(256) PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    name VARCHAR(256),
    phone VARCHAR(25),
    identity_document VARCHAR(256),
    gender VARCHAR(256),
    birthdate VARCHAR(50),
    profile_image VARCHAR(555),
    language VARCHAR(50)
);

-- VERIFICATION TOKEN
CREATE TABLE IF NOT EXISTS user_verification_token (
    id VARCHAR(256) PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    email VARCHAR(256) NOT NULL,
    token VARCHAR(1024) NOT NULL
);

-- USER LOGS
CREATE TABLE IF NOT EXISTS user_log (
    id VARCHAR(256) PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    ip_address VARCHAR(256) NOT NULL,
    user_id VARCHAR(256) NOT NULL,
    agent VARCHAR(512) NOT NULL,
    update_type VARCHAR(256) NOT NULL,
    old_value TEXT NOT NULL,
    new_value TEXT NOT NULL
);

-- REFRESH LOGIN
CREATE TABLE IF NOT EXISTS user_refresh_login (
    id VARCHAR(256) PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    email VARCHAR(256) NOT NULL,
    token VARCHAR(1024) NOT NULL,
    CONSTRAINT unique_id UNIQUE (id)
);