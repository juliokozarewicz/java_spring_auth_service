-- ACCOUNTS
CREATE TABLE IF NOT EXISTS accounts (
    id VARCHAR(255) PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    level VARCHAR(255) NOT NULL DEFAULT 'user',
    active BOOLEAN NOT NULL DEFAULT FALSE,
    banned BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT unique_email UNIQUE (email)
);

-- PROFILE
CREATE TABLE IF NOT EXISTS profile (
    id VARCHAR(255) PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    name VARCHAR(255),
    phone VARCHAR(25),
    identity_document VARCHAR(255),
    gender VARCHAR(255),
    profile_image VARCHAR(555),
    user_id VARCHAR(255) NOT NULL,
    CONSTRAINT unique_user UNIQUE (user_id)
);

-- CODES
CREATE TABLE IF NOT EXISTS code_entity (
    id VARCHAR(255) PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    email VARCHAR(255) NOT NULL,
    code VARCHAR(1024) NOT NULL
);
