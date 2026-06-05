-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица продуктов
CREATE TABLE IF NOT EXISTS products (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    coordinates_x INTEGER NOT NULL,
    coordinates_y DOUBLE PRECISION NOT NULL,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    price INTEGER NOT NULL CHECK (price > 0),
    unit_of_measure VARCHAR(50),
    owner_id INTEGER REFERENCES users(id),
    manufacturer_name VARCHAR(255),
    manufacturer_full_name VARCHAR(255),
    manufacturer_annual_turnover DOUBLE PRECISION,
    manufacturer_employees INTEGER NOT NULL DEFAULT 0,
    inserted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sequence для генерации ID продуктов
CREATE SEQUENCE IF NOT EXISTS products_id_seq START WITH 1;

-- Sequence для генерации ID пользователей
CREATE SEQUENCE IF NOT EXISTS users_id_seq START WITH 1;

-- Индексы для ускорения поиска
CREATE INDEX IF NOT EXISTS idx_products_owner ON products(owner_id);
CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);