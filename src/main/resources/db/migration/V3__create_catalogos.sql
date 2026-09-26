-- ============================================================
-- V3: Tabla de Catálogos (sexo, estado civil, nacionalidad, etc.)
-- ============================================================
CREATE TABLE IF NOT EXISTS catalogos (
    id          BIGSERIAL PRIMARY KEY,
    tipo        VARCHAR(50)  NOT NULL,
    clave       VARCHAR(20)  NOT NULL,
    descripcion TEXT         NOT NULL,
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    orden       INTEGER      NOT NULL DEFAULT 0,
    CONSTRAINT uq_catalogo_tipo_clave UNIQUE (tipo, clave)
);

CREATE INDEX IF NOT EXISTS idx_catalogos_tipo ON catalogos (tipo, orden ASC);

-- Datos iniciales: Sexo
INSERT INTO catalogos (tipo, clave, descripcion, orden) VALUES
    ('SEXO', 'F', 'Femenino',  1),
    ('SEXO', 'M', 'Masculino', 2),
    ('SEXO', 'I', 'Indeterminado', 3)
ON CONFLICT DO NOTHING;

-- Datos iniciales: Estado Civil
INSERT INTO catalogos (tipo, clave, descripcion, orden) VALUES
    ('ESTADO_CIVIL', 'SOL', 'Soltero(a)',   1),
    ('ESTADO_CIVIL', 'CAS', 'Casado(a)',    2),
    ('ESTADO_CIVIL', 'DIV', 'Divorciado(a)',3),
    ('ESTADO_CIVIL', 'VIU', 'Viudo(a)',     4),
    ('ESTADO_CIVIL', 'UNI', 'Unión libre',  5)
ON CONFLICT DO NOTHING;

-- Datos iniciales: Ocupación
INSERT INTO catalogos (tipo, clave, descripcion, orden) VALUES
    ('OCUPACION', 'EMP', 'Empleado',       1),
    ('OCUPACION', 'IND', 'Independiente',  2),
    ('OCUPACION', 'EMP_PUB', 'Empleado Público', 3),
    ('OCUPACION', 'JUB', 'Jubilado',       4),
    ('OCUPACION', 'EST', 'Estudiante',     5),
    ('OCUPACION', 'OTR', 'Otro',           6)
ON CONFLICT DO NOTHING;
