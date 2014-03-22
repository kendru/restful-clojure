CREATE OR REPLACE FUNCTION update_updated_at_column()
        RETURNS TRIGGER AS '
    BEGIN
        NEW.updated_at = NOW();
        RETURN NEW;
    END;
' LANGUAGE 'plpgsql';

CREATE TABLE users (
    id          serial PRIMARY KEY,
    name        varchar(40) NOT NULL CHECK (name <> ''),
    email       varchar(60) NOT NULL UNIQUE,
    created_at  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TRIGGER update_updated_at_users
    BEFORE UPDATE ON users FOR EACH ROW EXECUTE
    PROCEDURE update_updated_at_column();

CREATE TABLE lists (
    id          serial PRIMARY KEY,
    user_id     integer REFERENCES users(id) ON DELETE CASCADE,
    title       varchar(40) NOT NULL,
    created_at  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TRIGGER update_updated_at_lists
    BEFORE UPDATE ON users FOR EACH ROW EXECUTE
    PROCEDURE update_updated_at_column();

CREATE TABLE products (
    id          serial PRIMARY KEY,
    title       varchar(40) NOT NULL,
    description text NULL,
    created_at  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TYPE item_status AS ENUM ('incomplete', 'complete', 'deleted');
CREATE TABLE lists_products (
    list_id     integer REFERENCES lists(id) ON DELETE CASCADE,
    product_id  integer REFERENCES products(id) ON DELETE CASCADE,
    status      item_status NOT NULL DEFAULT 'incomplete',
    PRIMARY KEY (list_id, product_id)
);
