-- Las filas creadas antes del punto 5 no tienen lista_id.
-- Se backfillean a una lista por defecto y recién ahí se exige NOT NULL.
INSERT INTO listas (nombre)
SELECT 'Sin clasificar'
WHERE NOT EXISTS (SELECT 1 FROM listas WHERE nombre = 'Sin clasificar');

UPDATE favoritos
SET lista_id = (SELECT id FROM listas WHERE nombre = 'Sin clasificar' LIMIT 1)
WHERE lista_id IS NULL;

ALTER TABLE favoritos ALTER COLUMN lista_id SET NOT NULL;
