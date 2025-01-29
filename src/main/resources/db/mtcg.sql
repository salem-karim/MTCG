-- Grant privileges
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO mtcgdb;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO mtcgdb;
-- Drop all tables if they exist
DROP TABLE IF EXISTS trading_deals CASCADE;
DROP TABLE IF EXISTS battles CASCADE;
DROP TABLE IF EXISTS decks CASCADE;
DROP TABLE IF EXISTS packages CASCADE;
DROP TABLE IF EXISTS stacks CASCADE;
DROP TABLE IF EXISTS cards CASCADE;
DROP TABLE IF EXISTS users CASCADE;
-- Add Extension for generating UUIDv4
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
-- Create Users Table
CREATE TABLE users (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    username varchar(50) UNIQUE NOT NULL,
    password varchar(100) NOT NULL,
    coins int DEFAULT 20 NOT NULL CHECK (coins >= 0),
    elo int DEFAULT 100 NOT NULL,
    token varchar(255) NOT NULL,
    bio varchar(255) DEFAULT '' NOT NULL,
    image varchar(255) DEFAULT '' NOT NULL,
    name varchar(255) DEFAULT '' NOT NULL,
    wins int DEFAULT 0 NOT NULL,
    losses int DEFAULT 0 NOT NULL
);
-- Create Cards Table
CREATE TABLE cards (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    name varchar(50) NOT NULL,
    damage double precision CHECK (damage >= 0) NOT NULL,
    element_type varchar(20) CHECK (
        element_type IN ('fire', 'water', 'normal')
    ) NOT NULL,
    card_type varchar(20) CHECK (card_type IN ('monster', 'spell')) NOT NULL
);

-- Create Decks Table (to define the user's active deck)
CREATE TABLE decks (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id uuid UNIQUE REFERENCES users (id) ON DELETE CASCADE
);
CREATE TABLE deck_cards (
    deck_id uuid NOT NULL REFERENCES decks (id) ON DELETE CASCADE,
    card_id uuid NOT NULL REFERENCES cards (id) ON DELETE CASCADE,
    PRIMARY KEY (deck_id, card_id)
);

-- Create Trading Deals Table (to manage trading)
CREATE TABLE trading_deals (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id uuid REFERENCES users (id) ON DELETE CASCADE,
    card_id uuid REFERENCES cards (id) ON DELETE CASCADE,
    required_card_type varchar(20) CHECK (
        required_card_type IN ('monster', 'spell')
    ),
    min_damage double precision
);

-- Create Stacks Table (to hold all cards of a user)
CREATE TABLE stacks (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id uuid UNIQUE NOT NULL REFERENCES users (id) ON DELETE CASCADE
);
CREATE TABLE stack_cards (
    stack_id uuid NOT NULL REFERENCES stacks (id) ON DELETE CASCADE,
    card_id uuid NOT NULL REFERENCES cards (id) ON DELETE CASCADE,
    deck_id uuid REFERENCES decks (id) ON DELETE SET NULL,
    trade_id uuid REFERENCES trading_deals (id) ON DELETE SET NULL,
    PRIMARY KEY (stack_id, card_id)
);

-- Create Transactions Table without foreign key to packages
CREATE TABLE transactions (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id uuid NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    package_id uuid UNIQUE, -- Placeholder for foreign key
    purchase_date timestamp DEFAULT current_timestamp NOT NULL
);

-- Create Packages Table without foreign key to transactions
CREATE TABLE packages (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id uuid REFERENCES users (id) ON DELETE CASCADE,
    transaction_id uuid UNIQUE -- Placeholder for foreign key
);

-- Add the foreign key constraints to resolve circular dependency
ALTER TABLE transactions
ADD CONSTRAINT fk_transactions_package_id
FOREIGN KEY (package_id) REFERENCES packages (id);

ALTER TABLE packages
ADD CONSTRAINT fk_packages_transaction_id
FOREIGN KEY (transaction_id) REFERENCES transactions (id);

CREATE TABLE package_cards (
    package_id uuid NOT NULL REFERENCES packages (id) ON DELETE CASCADE,
    card_id uuid NOT NULL REFERENCES cards (id) ON DELETE CASCADE,
    PRIMARY KEY (package_id, card_id)
);
