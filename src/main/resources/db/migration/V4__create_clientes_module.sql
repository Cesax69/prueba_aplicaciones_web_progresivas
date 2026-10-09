-- ============================================================
-- V4: Tablas principales del módulo de Clientes
-- clientes, domicilios, cuentas, saldos, login
-- ============================================================

-- Clientes (persona física)
CREATE TABLE IF NOT EXISTS clientes (
    id                  BIGSERIAL       PRIMARY KEY,
    nombre              TEXT            NOT NULL,
    segundo_nombre      TEXT,
    apellido_paterno    TEXT            NOT NULL,
    apellido_materno    TEXT            NOT NULL,
    fecha_nacimiento    DATE            NOT NULL,
    curp                TEXT            NOT NULL,
    rfc                 TEXT            NOT NULL,
    sexo                TEXT            NOT NULL,
    nacionalidad        TEXT            NOT NULL DEFAULT 'MEXICANA',
    estado_civil        TEXT            NOT NULL,
    correo              TEXT            NOT NULL,
    telefono_movil      TEXT            NOT NULL,
    telefono_alternativo TEXT,
    ocupacion           TEXT            NOT NULL,
    empresa             TEXT            NOT NULL,
    ingreso_mensual     NUMERIC(18,2)   NOT NULL,
    activo              BOOLEAN         NOT NULL DEFAULT TRUE,
    fecha_registro      TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP       NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_clientes_curp   UNIQUE (curp),
    CONSTRAINT uq_clientes_rfc    UNIQUE (rfc),
    CONSTRAINT uq_clientes_correo UNIQUE (correo),
    CONSTRAINT ck_clientes_ingreso CHECK (ingreso_mensual > 0)
);

CREATE INDEX IF NOT EXISTS idx_clientes_curp   ON clientes (curp);
CREATE INDEX IF NOT EXISTS idx_clientes_rfc    ON clientes (rfc);
CREATE INDEX IF NOT EXISTS idx_clientes_correo ON clientes (correo);
CREATE INDEX IF NOT EXISTS idx_clientes_activo ON clientes (activo);
CREATE INDEX IF NOT EXISTS idx_clientes_fecha_registro ON clientes (fecha_registro ASC);

-- Domicilios
CREATE TABLE IF NOT EXISTS domicilios (
    id                  BIGSERIAL   PRIMARY KEY,
    cliente_id          BIGINT      NOT NULL,
    calle               TEXT        NOT NULL,
    numero_exterior     TEXT        NOT NULL,
    numero_interior     TEXT,
    colonia             TEXT        NOT NULL,
    municipio           TEXT        NOT NULL,
    estado              TEXT        NOT NULL,
    codigo_postal       TEXT        NOT NULL,
    pais                TEXT        NOT NULL DEFAULT 'México',
    activo              BOOLEAN     NOT NULL DEFAULT TRUE,
    fecha_registro      TIMESTAMP   NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_domicilios_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT ck_domicilios_cp CHECK (codigo_postal ~ '^\d{5}$')
);

CREATE INDEX IF NOT EXISTS idx_domicilios_cliente_id ON domicilios (cliente_id);

-- Cuentas bancarias
CREATE TABLE IF NOT EXISTS cuentas (
    id              BIGSERIAL   PRIMARY KEY,
    cliente_id      BIGINT      NOT NULL,
    numero_cuenta   TEXT        NOT NULL,
    estatus         TEXT        NOT NULL DEFAULT 'ACTIVA',
    fecha_apertura  TIMESTAMP   NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_cuentas_numero   UNIQUE (numero_cuenta),
    CONSTRAINT fk_cuentas_cliente  FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);

CREATE INDEX IF NOT EXISTS idx_cuentas_numero    ON cuentas (numero_cuenta);
CREATE INDEX IF NOT EXISTS idx_cuentas_cliente   ON cuentas (cliente_id);
CREATE INDEX IF NOT EXISTS idx_cuentas_estatus   ON cuentas (estatus);

-- Saldos (subtabla separada de cuentas)
CREATE TABLE IF NOT EXISTS saldos (
    id              BIGSERIAL       PRIMARY KEY,
    cuenta_id       BIGINT          NOT NULL,
    saldo           NUMERIC(18,2)   NOT NULL DEFAULT 0.00,
    fecha_movimiento TIMESTAMP      NOT NULL DEFAULT NOW(),
    concepto        TEXT            NOT NULL DEFAULT 'SALDO INICIAL',
    CONSTRAINT fk_saldos_cuenta FOREIGN KEY (cuenta_id) REFERENCES cuentas(id),
    CONSTRAINT ck_saldos_positivo CHECK (saldo >= 0)
);

CREATE INDEX IF NOT EXISTS idx_saldos_cuenta_id ON saldos (cuenta_id);

-- Login de clientes
CREATE TABLE IF NOT EXISTS login_clientes (
    id                      BIGSERIAL   PRIMARY KEY,
    cliente_id              BIGINT      NOT NULL,
    usuario                 TEXT        NOT NULL,
    contrasena_hash         TEXT        NOT NULL,
    activo                  BOOLEAN     NOT NULL DEFAULT TRUE,
    ultimo_acceso           TIMESTAMP,
    ultimo_intento_fallido  TIMESTAMP,
    intentos_fallidos       INTEGER     NOT NULL DEFAULT 0,
    bloqueado               BOOLEAN     NOT NULL DEFAULT FALSE,
    fecha_registro          TIMESTAMP   NOT NULL DEFAULT NOW(),
    fecha_actualizacion     TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_login_usuario   UNIQUE (usuario),
    CONSTRAINT fk_login_cliente   FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);

CREATE INDEX IF NOT EXISTS idx_login_usuario    ON login_clientes (usuario);
CREATE INDEX IF NOT EXISTS idx_login_cliente_id ON login_clientes (cliente_id);

-- Datos biométricos del cliente
CREATE TABLE IF NOT EXISTS datos_biometricos (
    id              BIGSERIAL       PRIMARY KEY,
    cliente_id      BIGINT          NOT NULL,
    tipo            TEXT            NOT NULL,
    valor_decimal   NUMERIC(18,8),
    valor_entero    BIGINT,
    hash_biometrico TEXT            NOT NULL,
    activo          BOOLEAN         NOT NULL DEFAULT TRUE,
    fecha_registro  TIMESTAMP       NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_biometrico_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);

CREATE INDEX IF NOT EXISTS idx_biometrico_cliente ON datos_biometricos (cliente_id);