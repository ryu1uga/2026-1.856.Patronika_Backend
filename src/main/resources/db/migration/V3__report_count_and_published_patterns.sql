ALTER TABLE comments
    ADD COLUMN report_count INTEGER NOT NULL DEFAULT 0;

CREATE TABLE published_patterns (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    pattern_id UUID NOT NULL REFERENCES patterns(id) ON DELETE CASCADE,
    published_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (user_id, pattern_id)
);
