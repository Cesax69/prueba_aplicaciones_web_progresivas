-- Tabla de respaldo (fallback) para almacenar la lista de productos de GestoPago
-- Se usa solo cuando Redis no está disponible.
CREATE TABLE IF NOT EXISTS gestopago_product_cache (
    id                  SERIAL PRIMARY KEY,
    productos_json      TEXT NOT NULL,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT NOW()
);
