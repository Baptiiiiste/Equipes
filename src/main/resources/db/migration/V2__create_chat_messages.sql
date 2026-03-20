CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGSERIAL PRIMARY KEY,
    room_id VARCHAR(64) NOT NULL REFERENCES rooms(room_id) ON DELETE CASCADE,
    sender_id VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    sent_at BIGINT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_chat_messages_room_sent_at
    ON chat_messages (room_id, sent_at DESC);

