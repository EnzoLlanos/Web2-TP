ALTER TABLE favoritos
    ADD COLUMN lista_id BIGINT REFERENCES listas(id);

CREATE INDEX idx_favoritos_lista_id ON favoritos(lista_id);
