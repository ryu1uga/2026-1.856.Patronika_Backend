CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    hashed_password VARCHAR(255) NOT NULL,
    image_url TEXT,
    is_admin BOOLEAN NOT NULL DEFAULT FALSE,
    logged_in BOOLEAN NOT NULL DEFAULT FALSE,
    status INTEGER NOT NULL DEFAULT 0,
    registered_date DATE NOT NULL,
    activate_notification BOOLEAN NOT NULL DEFAULT TRUE,
    suspension_end_date DATE,
    token VARCHAR(255)
);

CREATE TABLE patterns (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    grid_data JSONB,
    size INTEGER NOT NULL,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    published_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE publications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    pattern_id UUID NOT NULL REFERENCES patterns(id),
    description TEXT NOT NULL,
    technique INTEGER NOT NULL,
    image_url TEXT,
    published_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE tutorials (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    difficulty INTEGER NOT NULL,
    url VARCHAR(255) NOT NULL
);

CREATE TABLE tutorial_progresses (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    tutorial_id UUID NOT NULL REFERENCES tutorials(id),
    status INTEGER NOT NULL,
    registered_date DATE
);

CREATE TABLE comments (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    publication_id UUID NOT NULL REFERENCES publications(id),
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token TEXT NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE email_verification_codes (
    id UUID PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    hashed_code TEXT NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE email_verification_tokens (
    id UUID PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    hashed_token TEXT NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);