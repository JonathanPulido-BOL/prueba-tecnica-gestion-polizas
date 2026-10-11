-- Datos de ejemplo para probar los endpoints de inmediato.

-- Póliza 1: COLECTIVA, ACTIVA (admite varios riesgos)
INSERT INTO poliza (id, tipo, estado, tomador, asegurado, beneficiario, vigencia_inicio, vigencia_fin, canon_mensual, prima, ipc)
VALUES (1, 'COLECTIVA', 'ACTIVA', 'Inmobiliaria Andina', 'Arrendatarios Edificio Central', 'Arrendadores Edificio Central', DATE '2026-01-01', DATE '2027-01-01', 2000000.00, 24000000.00, 12.00);

-- Póliza 2: INDIVIDUAL, ACTIVA (un solo riesgo)
INSERT INTO poliza (id, tipo, estado, tomador, asegurado, beneficiario, vigencia_inicio, vigencia_fin, canon_mensual, prima, ipc)
VALUES (2, 'INDIVIDUAL', 'ACTIVA', 'Juan Arrendatario', 'Juan Arrendatario', 'Maria Arrendadora', DATE '2026-03-01', DATE '2027-03-01', 1500000.00, 18000000.00, 10.00);

-- Póliza 3: COLECTIVA, CANCELADA (para probar reglas de cancelada)
INSERT INTO poliza (id, tipo, estado, tomador, asegurado, beneficiario, vigencia_inicio, vigencia_fin, canon_mensual, prima, ipc)
VALUES (3, 'COLECTIVA', 'CANCELADA', 'Administracion Los Robles', 'Arrendatarios Los Robles', 'Arrendadores Los Robles', DATE '2025-06-01', DATE '2026-06-01', 1800000.00, 21600000.00, 11.50);

-- Riesgos de la póliza 1 (colectiva, varios)
INSERT INTO riesgo (id, poliza_id, estado, datos_inmueble) VALUES (1, 1, 'ACTIVO', 'Apto 301, Edificio Central, Bogota');
INSERT INTO riesgo (id, poliza_id, estado, datos_inmueble) VALUES (2, 1, 'ACTIVO', 'Apto 302, Edificio Central, Bogota');

-- Riesgo de la póliza 2 (individual, uno solo)
INSERT INTO riesgo (id, poliza_id, estado, datos_inmueble) VALUES (3, 2, 'ACTIVO', 'Casa 15, Barrio Modelia, Bogota');

-- Riesgo de la póliza 3 (cancelada)
INSERT INTO riesgo (id, poliza_id, estado, datos_inmueble) VALUES (4, 3, 'CANCELADO', 'Local 5, Centro Comercial Los Robles');

-- Ajuste de secuencias de IDENTITY para evitar colisiones con los inserts manuales.
ALTER TABLE poliza ALTER COLUMN id RESTART WITH 100;
ALTER TABLE riesgo ALTER COLUMN id RESTART WITH 100;
