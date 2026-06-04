CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(256) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE SEQUENCE IF NOT EXISTS products_id_seq;


CREATE TABLE IF NOT EXISTS products (
    id INTEGER PRIMARY KEY,


    name VARCHAR(255) NOT NULL,
    coordinates_x INTEGER,
    coordinates_y DOUBLE PRECISION,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    price DOUBLE PRECISION NOT NULL,
    unit_of_measure VARCHAR(50),


    owner_id INTEGER REFERENCES users(id) ON DELETE CASCADE,


    manufacturer_name VARCHAR(255),
    manufacturer_full_name VARCHAR(255),
    manufacturer_annual_turnover DOUBLE PRECISION,
    manufacturer_employees INTEGER,


    inserted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- индексы для ускорения
CREATE INDEX IF NOT EXISTS idx_products_owner ON products(owner_id);
CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);
CREATE INDEX IF NOT EXISTS idx_products_price ON products(price);


-- Тест для отладки

-- INSERT INTO users (username, password_hash) VALUES
--     ('test', 'd60151c3655983564419917259a92a72'); -- MD2 от "test"