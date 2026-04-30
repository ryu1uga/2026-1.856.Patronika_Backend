CREATE TABLE users (
    id UUID PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    user_code VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(50) NOT NULL,
    state INTEGER NOT NULL,
    user_type INTEGER NOT NULL,
    rpta INTEGER,
    token VARCHAR(150),
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE
);

CREATE TABLE options (
    id UUID PRIMARY KEY,
    text VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE
);

CREATE TABLE tests (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE
);

CREATE TABLE questions (
    id UUID PRIMARY KEY,
    text VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE,
    test_id UUID REFERENCES tests(id)
);

CREATE TABLE question_options (
    id UUID PRIMARY KEY,
    score INTEGER NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE,
    question_id UUID REFERENCES questions (id),
    option_id UUID REFERENCES options(id)
);

CREATE TABLE answers (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE,
    user_id UUID REFERENCES users(id) NOT NULL,
    question_option_id UUID REFERENCES question_options(id) NOT NULL
);

CREATE TABLE contacts (
    id UUID PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE,
    user_id UUID REFERENCES users(id) NOT NULL
);

CREATE TABLE emotions (
    id UUID PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE,
    basic_emotion BOOLEAN NOT NULL,
    hex_code VARCHAR(10),
    photo_url VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE
);

CREATE TABLE emotion_phrases (
    id UUID PRIMARY KEY,
    phrase VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE,
    emotion_id UUID REFERENCES emotions(id) NOT NULL
);

CREATE TABLE user_emotions (
    id UUID PRIMARY KEY,
    reason VARCHAR(255),
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE,
    user_id UUID REFERENCES users(id),
    emotion_id UUID REFERENCES emotions(id)
);

CREATE TABLE goals (
    id UUID PRIMARY KEY,
    want VARCHAR(255) NOT NULL,
    for_what VARCHAR(255) NOT NULL,
    before TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    state INTEGER NOT NULL NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE,
    user_id UUID REFERENCES users(id)
);

CREATE TABLE tasks (
    id UUID PRIMARY KEY,
    description VARCHAR(255) NOT NULL UNIQUE,
    state INTEGER NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE,
    user_id UUID REFERENCES users(id)
);
