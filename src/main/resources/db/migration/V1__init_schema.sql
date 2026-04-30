CREATE TABLE IF NOT EXISTS blockchain_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    chain_id VARCHAR(40) NOT NULL,
    block_number BIGINT NOT NULL,
    block_hash VARCHAR(80) NOT NULL,
    tx_hash VARCHAR(80) NOT NULL,
    log_index BIGINT NOT NULL,
    contract_address VARCHAR(60) NOT NULL,
    topic0 VARCHAR(80) NOT NULL,
    event_signature VARCHAR(255) NOT NULL,
    event_name VARCHAR(120) NOT NULL,
    from_address VARCHAR(60) NULL,
    to_address VARCHAR(60) NULL,
    removed BOOLEAN NOT NULL,
    raw_data TEXT NOT NULL,
    decoded_params_json TEXT NOT NULL,
    processed_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_chain_tx_log (chain_id, tx_hash, log_index),
    KEY idx_event_block (chain_id, block_number),
    KEY idx_event_name (event_name),
    KEY idx_event_from (from_address),
    KEY idx_event_to (to_address)
);

CREATE TABLE IF NOT EXISTS transaction_details (
    tx_hash VARCHAR(80) NOT NULL,
    chain_id VARCHAR(40) NOT NULL,
    block_number BIGINT NULL,
    from_address VARCHAR(60) NULL,
    to_address VARCHAR(60) NULL,
    tx_value DECIMAL(65,0) NULL,
    status VARCHAR(20) NULL,
    gas DECIMAL(65,0) NULL,
    gas_price DECIMAL(65,0) NULL,
    fetched_at DATETIME(6) NOT NULL,
    PRIMARY KEY (tx_hash),
    KEY idx_tx_detail_chain_block (chain_id, block_number)
);

CREATE TABLE IF NOT EXISTS listener_state (
    id BIGINT NOT NULL,
    last_processed_block BIGINT NOT NULL,
    last_safe_block BIGINT NOT NULL,
    last_ws_seen_block BIGINT NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
);
