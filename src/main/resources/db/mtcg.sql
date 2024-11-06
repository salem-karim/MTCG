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

-- Enable the uuid-ossp extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
-- Create Users Table
CREATE TABLE users (
    id       UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) UNIQUE           NOT NULL,
    password VARCHAR(100)                 NOT NULL,
    coins    INT              DEFAULT 20  NOT NULL CHECK (coins >= 0),
    elo      INT              DEFAULT 100 NOT NULL,
    token    VARCHAR(255)                 NOT NULL
);

-- Create Cards Table
CREATE TABLE cards (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name         VARCHAR(50)                                                     NOT NULL,
    damage       INT         CHECK (damage >= 0)                                 NOT NULL,
    element_type VARCHAR(20) CHECK (element_type IN ('fire', 'water', 'normal')) NOT NULL,
    card_type    VARCHAR(20) CHECK (card_type IN ('monster', 'spell'))           NOT NULL
);

-- Create Stacks Table (to hold all cards of a user)
CREATE TABLE stacks (
    id      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID UNIQUE NOT NULL REFERENCES users (id) ON DELETE CASCADE,
);

CREATE TABLE stack_cards ( 
    stack_id UUID NOT NULL REFERENCES stacks (id) ON DELETE CASCADE,
    card_id  UUID NOT NULL REFERENCES cards (id) ON DELETE CASCADE,
    PRIMARY KEY(stack_id, card_id)
);

-- Create Packages Table (to define card packages)
CREATE TABLE packages (
    id      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE package_cards (
    package_id UUID NOT NULL REFERENCES packages (id) ON DELETE CASCADE,
    card_id UUID NOT NULL REFERENCES cards (id) ON DELETE CASCADE,
    PRIMARY KEY(package_id, card_id)
);

-- Create Decks Table (to define the user's active deck)
CREATE TABLE decks (
    id      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users (id) ON DELETE CASCADE,
    card_id UUID REFERENCES cards (id) ON DELETE CASCADE
);

CREATE TABLE deck_cards (
    deck_id UUID NOT NULL REFERENCES decks (id) ON DELETE CASCADE,
    card_id UUID NOT NULL REFERENCES cards (id) ON DELETE CASCADE,
    PRIMARY KEY(deck_id, card_id)
);

-- Create Battles Table (to log battles)
CREATE TABLE battles (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user1_id         UUID REFERENCES users (id) ON DELETE CASCADE,
    user2_id         UUID REFERENCES users (id) ON DELETE CASCADE,
    result           VARCHAR(20) CHECK (result IN ('win', 'lose', 'draw')),
    elo_change_user1 INT,
    elo_change_user2 INT
);

-- Create Trading Deals Table (to manage trading)
CREATE TABLE trading_deals (
    id                 UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id            UUID REFERENCES users (id) ON DELETE CASCADE,
    card_id            UUID REFERENCES cards (id) ON DELETE CASCADE,
    required_card_type VARCHAR(20) CHECK (required_card_type IN ('monster', 'spell')),
    min_damage         INT
);


-- Trigger function to limit package size to 5 cards
CREATE OR REPLACE FUNCTION check_package_card_count() RETURNS TRIGGER AS $$
BEGIN
    IF (SELECT COUNT(*) FROM package_cards WHERE package_id = NEW.package_id) >= 5 THEN
        RAISE EXCEPTION 'A package can only contain 5 cards';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER limit_package_size
BEFORE INSERT ON package_cards
FOR EACH ROW
EXECUTE FUNCTION check_package_card_count();

-- Trigger function to limit deck size to 4 cards
CREATE OR REPLACE FUNCTION check_deck_card_count() RETURNS TRIGGER AS $$
BEGIN
    IF (SELECT COUNT(*) FROM deck_cards WHERE deck_id = NEW.deck_id) >= 4 THEN
        RAISE EXCEPTION 'A deck can only contain 4 cards';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER limit_deck_size
BEFORE INSERT ON deck_cards
FOR EACH ROW
EXECUTE FUNCTION check_deck_card_count();
