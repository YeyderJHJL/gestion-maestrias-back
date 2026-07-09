-- Añade el concepto y el monto del pago directamente a la tabla payments para
-- que el voucher pueda mostrar por qué concepto y por cuánto se subió el comprobante.
ALTER TABLE payments
    ADD COLUMN concept VARCHAR(255),
    ADD COLUMN amount  NUMERIC(10, 2);
