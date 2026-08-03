-- Criar schema pix_received_publisher
CREATE SCHEMA IF NOT EXISTS pix_received_publisher;

-- Criar tabela outbox_events
CREATE TABLE IF NOT EXISTS pix_received_publisher.outbox_events (
    id UUID PRIMARY KEY,
    topico VARCHAR(100) NOT NULL,
    chave_particao VARCHAR(64) NOT NULL,
    payload VARCHAR(5000) NOT NULL,
    status VARCHAR(20) NOT NULL,
    tentativas INTEGER NOT NULL DEFAULT 0,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL,
    publicado_em TIMESTAMP WITH TIME ZONE
);

-- Criar índices para melhorar performance de consultas
CREATE INDEX IF NOT EXISTS idx_outbox_status ON pix_received_publisher.outbox_events(status);
CREATE INDEX IF NOT EXISTS idx_outbox_criado_em ON pix_received_publisher.outbox_events(criado_em);
CREATE INDEX IF NOT EXISTS idx_outbox_tentativas ON pix_received_publisher.outbox_events(tentativas);
