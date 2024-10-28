CREATE SCHEMA IF NOT EXISTS test_schema;

SET SCHEMA test_schema;

-- Drop tables if they exist, with schema reference
DROP TABLE IF EXISTS test_schema.Account_user_rights CASCADE;
DROP TABLE IF EXISTS test_schema.Assets CASCADE;
DROP TABLE IF EXISTS test_schema.Categories CASCADE;
DROP TABLE IF EXISTS test_schema.Groups CASCADE;
DROP TABLE IF EXISTS test_schema.Standing_orders CASCADE;
DROP TABLE IF EXISTS test_schema.Transaction CASCADE;
DROP TABLE IF EXISTS test_schema.App_Users CASCADE;
DROP TABLE IF EXISTS test_schema.transactions_categories CASCADE;
DROP TABLE IF EXISTS test_schema.user_group_relation CASCADE;

-- Create tables within the schema
CREATE TABLE if not exists test_schema.Account (
                                     id VARCHAR(127) NOT NULL,
                                     user_id VARCHAR(127) NOT NULL,
                                     name CHAR(1023) NOT NULL,
                                     type VARCHAR(31) NOT NULL,
                                     currency VARCHAR(3) DEFAULT 'CZK' NOT NULL,
                                     initial_amount DECIMAL(8, 2) DEFAULT 0 NOT NULL,
                                     interest_rate INTEGER DEFAULT 0,
                                     goal_amount DECIMAL(8, 2) DEFAULT 0,
                                     already_paid_amount DECIMAL(8, 2) DEFAULT 0,
                                     is_removed BOOLEAN DEFAULT FALSE NOT NULL,
                                     removed_at TIMESTAMP,
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                     PRIMARY KEY (id)
);

CREATE TABLE if not exists test_schema.App_Users (
                                   id VARCHAR(127) NOT NULL,
                                   email VARCHAR(127) NOT NULL,
                                   username VARCHAR(255) NOT NULL,
                                   password VARCHAR(127) NOT NULL,
                                   pincode VARCHAR(127),
                                   is_blocked BOOLEAN DEFAULT FALSE NOT NULL,
                                   is_admin BOOLEAN DEFAULT FALSE NOT NULL,
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                   PRIMARY KEY (id)
);

CREATE TABLE if not exists test_schema.Account_user_rights (
                                                 user_id VARCHAR(127) NOT NULL,
                                                 account_id VARCHAR(127) NOT NULL,
                                                 is_owner BOOLEAN,
                                                 rights VARCHAR(64),
                                                 created_at TIMESTAMP,
                                                 updated_at TIMESTAMP,
                                                 PRIMARY KEY (user_id, account_id),
                                                 FOREIGN KEY (user_id) REFERENCES test_schema.App_Users(id),
                                                 FOREIGN KEY (account_id) REFERENCES test_schema.Account(id)
);

CREATE INDEX if not exists account_belong_to_group_FK ON test_schema.Account_user_rights (user_id);
CREATE INDEX if not exists group_has_account_FK ON test_schema.Account_user_rights (account_id);

CREATE TABLE if not exists test_schema.Assets (
                                    id VARCHAR(127) NOT NULL,
                                    account_id VARCHAR(127) NOT NULL,
                                    name VARCHAR(255) NOT NULL,
                                    acquisition_price DECIMAL(8, 2) NOT NULL,
                                    start_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                    end_date TIMESTAMP,
                                    color VARCHAR(31),
                                    icon VARCHAR(127),
                                    is_removed BOOLEAN DEFAULT FALSE,
                                    removed_at TIMESTAMP,
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                    PRIMARY KEY (id),
                                    FOREIGN KEY (account_id) REFERENCES test_schema.Account(id)
);

CREATE INDEX if not exists has_assets_FK ON test_schema.Assets (account_id);

CREATE TABLE if not exists test_schema.Categories (
                                        id VARCHAR(127) NOT NULL,
                                        icon VARCHAR(255),
                                        name VARCHAR(63) NOT NULL,
                                        color VARCHAR(31),
                                        created_at TIMESTAMP,
                                        updated_at TIMESTAMP,
                                        PRIMARY KEY (id)
);

CREATE TABLE if not exists test_schema.Groups (
                                    id VARCHAR(127) NOT NULL,
                                    group_name VARCHAR(1023) NOT NULL,
                                    group_code VARCHAR(16),
                                    created_at TIMESTAMP NOT NULL,
                                    updated_at TIMESTAMP NOT NULL,
                                    PRIMARY KEY (id)
);

CREATE TABLE if not exists test_schema.Transaction (
                                         id VARCHAR(127) NOT NULL,
                                         account_id VARCHAR(127) NOT NULL,
                                         for_asset_id VARCHAR(127),
                                         receiver_id VARCHAR(127),
                                         type VARCHAR(31) NOT NULL,
                                         amount DECIMAL(8, 2) NOT NULL,
                                         execution_date TIMESTAMP,
                                         note VARCHAR(2047),
                                         place_lat DOUBLE,
                                         place_lon DOUBLE,
                                         photo VARCHAR(511),
                                         is_removed BOOLEAN DEFAULT FALSE NOT NULL,
                                         removed_at TIMESTAMP,
                                         created_at TIMESTAMP NOT NULL,
                                         PRIMARY KEY (id),
                                         FOREIGN KEY (account_id) REFERENCES test_schema.Account(id),
                                         FOREIGN KEY (for_asset_id) REFERENCES test_schema.Assets(id)
);

CREATE TABLE if not exists test_schema.Standing_orders (
                                             id VARCHAR(127) NOT NULL,
                                             frequency VARCHAR(63) NOT NULL,
                                             transaction_sample_id VARCHAR(127) NOT NULL,
                                             last_repeated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                             remind_days_before INTEGER,
                                             created_at TIMESTAMP NOT NULL,
                                             updated_at TIMESTAMP NOT NULL,
                                             PRIMARY KEY (id),
                                             FOREIGN KEY (id) REFERENCES test_schema.Transaction(id)
);

CREATE INDEX if not exists has_transactions_FK ON test_schema.Transaction (account_id);
CREATE INDEX if not exists related_transactions_FK ON test_schema.Transaction (for_asset_id);

CREATE TABLE if not exists test_schema.transactions_categories (
                                                     transaction_id VARCHAR(127) NOT NULL,
                                                     category_id VARCHAR(127) NOT NULL,
                                                     PRIMARY KEY (transaction_id, category_id),
                                                     FOREIGN KEY (transaction_id) REFERENCES test_schema.Transaction(id),
                                                     FOREIGN KEY (category_id) REFERENCES test_schema.Categories(id)
);

CREATE INDEX if not exists tags_to_transaction_details_FK ON test_schema.transactions_categories (transaction_id);
CREATE INDEX if not exists transaction_details_to_tags_FK ON test_schema.transactions_categories (category_id);

CREATE TABLE if not exists test_schema.user_group_relation
(
    user_id  VARCHAR(127) NOT NULL,
    group_id VARCHAR(127) NOT NULL,
    PRIMARY KEY (user_id, group_id),
    FOREIGN KEY (group_id) REFERENCES test_schema.Groups (id),
    FOREIGN KEY (user_id) REFERENCES test_schema.App_Users (id)
);
