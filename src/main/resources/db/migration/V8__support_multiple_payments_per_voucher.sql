UPDATE payments
SET amount = 420.00
WHERE amount IS NULL;

ALTER TABLE payments
    ALTER COLUMN amount SET NOT NULL;

ALTER TABLE payments
    ADD CONSTRAINT chk_payments_amount CHECK (amount > 0);

ALTER TABLE vouchers
    ADD COLUMN declared_amount NUMERIC(10, 2);

UPDATE vouchers voucher
SET declared_amount = payment.amount
FROM payments payment
WHERE payment.id = voucher.id_payment;

ALTER TABLE vouchers
    ALTER COLUMN declared_amount SET NOT NULL,
    ADD CONSTRAINT chk_vouchers_declared_amount CHECK (declared_amount > 0);

CREATE TABLE voucher_payments (
    id_voucher UUID NOT NULL,
    id_payment UUID NOT NULL,
    PRIMARY KEY (id_voucher, id_payment),
    CONSTRAINT fk_voucher_payments_voucher
        FOREIGN KEY (id_voucher) REFERENCES vouchers(id),
    CONSTRAINT fk_voucher_payments_payment
        FOREIGN KEY (id_payment) REFERENCES payments(id)
);

INSERT INTO voucher_payments (id_voucher, id_payment)
SELECT id, id_payment
FROM vouchers;

DROP INDEX idx_vouchers_payment;

ALTER TABLE vouchers
    DROP CONSTRAINT fk_vouchers_payment,
    DROP COLUMN id_payment;

CREATE INDEX idx_voucher_payments_payment
    ON voucher_payments(id_payment);
