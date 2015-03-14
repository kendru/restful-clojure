ALTER TABLE users ADD COLUMN password_digest VARCHAR(162);
UPDATE users SET password_digest = 'invalid';
ALTER TABLE users ALTER COLUMN password_digest SET NOT NULL;