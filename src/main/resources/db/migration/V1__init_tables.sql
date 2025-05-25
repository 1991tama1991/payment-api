CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE "user" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    balance DOUBLE PRECISION NOT NULL DEFAULT 0
);

CREATE TABLE "transaction" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    amount DOUBLE PRECISION NOT NULL,
    recipient UUID NOT NULL,
    sender UUID NOT NULL,
    CONSTRAINT fk_recipient FOREIGN KEY (recipient) REFERENCES "user"(id),
    CONSTRAINT fk_sender FOREIGN KEY (sender) REFERENCES "user"(id)
);
