-- ============================================================
-- V5: Insertar catálogo de Nacionalidades
-- ============================================================

INSERT INTO catalogos (tipo, clave, descripcion, orden) VALUES
    ('NACIONALIDAD', 'MEX', 'Mexicana',          1),
    ('NACIONALIDAD', 'USA', 'Estadounidense',     2),
    ('NACIONALIDAD', 'CAN', 'Canadiense',         3),
    ('NACIONALIDAD', 'GTM', 'Guatemalteca',       4),
    ('NACIONALIDAD', 'BLZ', 'Beliceña',           5),
    ('NACIONALIDAD', 'HND', 'Hondureña',          6),
    ('NACIONALIDAD', 'SLV', 'Salvadoreña',        7),
    ('NACIONALIDAD', 'NIC', 'Nicaragüense',       8),
    ('NACIONALIDAD', 'CRI', 'Costarricense',      9),
    ('NACIONALIDAD', 'PAN', 'Panameña',           10),
    ('NACIONALIDAD', 'CUB', 'Cubana',             11),
    ('NACIONALIDAD', 'COL', 'Colombiana',         12),
    ('NACIONALIDAD', 'VEN', 'Venezolana',         13),
    ('NACIONALIDAD', 'PER', 'Peruana',            14),
    ('NACIONALIDAD', 'BRA', 'Brasileña',          15),
    ('NACIONALIDAD', 'ARG', 'Argentina',          16),
    ('NACIONALIDAD', 'CHL', 'Chilena',            17),
    ('NACIONALIDAD', 'ESP', 'Española',           18),
    ('NACIONALIDAD', 'ITA', 'Italiana',           19),
    ('NACIONALIDAD', 'FRA', 'Francesa',           20),
    ('NACIONALIDAD', 'DEU', 'Alemana',            21),
    ('NACIONALIDAD', 'GBR', 'Británica',          22),
    ('NACIONALIDAD', 'CHN', 'China',              23),
    ('NACIONALIDAD', 'JPN', 'Japonesa',           24),
    ('NACIONALIDAD', 'OTR', 'Otra',               99)
ON CONFLICT DO NOTHING;
