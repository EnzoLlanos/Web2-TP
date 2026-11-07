-- Las listas del TP2 no tenían dueño. Para no perder datos, si existen
-- listas previas se crea un usuario legado ADMIN sin login utilizable y
-- se le asignan; después la columna pasa a NOT NULL.
ALTER TABLE listas
    ADD COLUMN usuario_id BIGINT REFERENCES usuarios(id);

INSERT INTO usuarios (email, password_hash, rol)
SELECT 'legacy@demo.local', 'legacy-sin-login', 'ADMIN'
WHERE EXISTS (SELECT 1 FROM listas)
  AND NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'legacy@demo.local');

UPDATE listas
SET usuario_id = (SELECT id FROM usuarios WHERE email = 'legacy@demo.local')
WHERE usuario_id IS NULL;

ALTER TABLE listas ALTER COLUMN usuario_id SET NOT NULL;

CREATE INDEX idx_listas_usuario_id ON listas(usuario_id);
