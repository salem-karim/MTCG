-- Drop all tables if they exist
DROP TABLE IF EXISTS trading_deals CASCADE;
DROP TABLE IF EXISTS battles CASCADE;
DROP TABLE IF EXISTS decks CASCADE;
DROP TABLE IF EXISTS packages CASCADE;
DROP TABLE IF EXISTS stacks CASCADE;
DROP TABLE IF EXISTS cards CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Enable the uuid-ossp extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
-- Create Users Table
CREATE TABLE users
(
    id       UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) UNIQUE           NOT NULL,
    password VARCHAR(100)                 NOT NULL,
    coins    INT              DEFAULT 20  NOT NULL,
    elo      INT              DEFAULT 100 NOT NULL
);

-- Create Cards Table
CREATE TABLE cards
(
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name         VARCHAR(50)                                                     NOT NULL,
    damage       INT                                                             NOT NULL,
    element_type VARCHAR(20) CHECK (element_type IN ('fire', 'water', 'normal')),
    card_type    VARCHAR(20) CHECK (card_type IN ('monster', 'spell', 'normal')) NOT NULL
);

-- Create Stacks Table (to hold all cards of a user)
CREATE TABLE stacks
(
    id      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users (id) ON DELETE CASCADE,
    card_id UUID REFERENCES cards (id) ON DELETE CASCADE
);

-- Create Packages Table (to define card packages)
CREATE TABLE packages
(
    id      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users (id) ON DELETE CASCADE
);

-- Create Decks Table (to define the user's active deck)
CREATE TABLE decks
(
    id      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users (id) ON DELETE CASCADE,
    card_id UUID REFERENCES cards (id) ON DELETE CASCADE
);

-- Create Battles Table (to log battles)
CREATE TABLE battles
(
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user1_id         UUID REFERENCES users (id) ON DELETE CASCADE,
    user2_id         UUID REFERENCES users (id) ON DELETE CASCADE,
    result           VARCHAR(20) CHECK (result IN ('win', 'lose', 'draw')),
    elo_change_user1 INT,
    elo_change_user2 INT
);

-- Create Trading Deals Table (to manage trading)
CREATE TABLE trading_deals
(
    id                 UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id            UUID REFERENCES users (id) ON DELETE CASCADE,
    card_id            UUID REFERENCES cards (id) ON DELETE CASCADE,
    required_card_type VARCHAR(20) CHECK (required_card_type IN ('monster', 'spell', 'normal')),
    min_damage         INT
);
