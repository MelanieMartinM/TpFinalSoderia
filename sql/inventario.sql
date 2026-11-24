CREATE TABLE IF NOT EXISTS stock_envase (
    id VARCHAR(50) NOT NULL,
    cliente_id VARCHAR(50) NULL,
    envase_id VARCHAR(50) NOT NULL,
    aptos INT NOT NULL DEFAULT 0,
    sucios INT NOT NULL DEFAULT 0,
    titular_key VARCHAR(50) GENERATED ALWAYS AS (COALESCE(cliente_id, 'FABRICA')) STORED,

    PRIMARY KEY (id),
    CONSTRAINT chk_stock_envase_aptos CHECK (aptos >= 0),
    CONSTRAINT chk_stock_envase_sucios CHECK (sucios >= 0),
    CONSTRAINT uq_stock_envase_titular UNIQUE (titular_key, envase_id),
    CONSTRAINT fk_stock_envase_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT fk_stock_envase_envase FOREIGN KEY (envase_id) REFERENCES envases(id)
);

-- 2. Stock de productos llenos listos para despacho/entrega
CREATE TABLE IF NOT EXISTS stock_producto (
    id VARCHAR(50) NOT NULL,
    cliente_id VARCHAR(50) NULL,
    presentacion_id VARCHAR(50) NOT NULL,
    disponibles INT NOT NULL DEFAULT 0,
    titular_key VARCHAR(50) GENERATED ALWAYS AS (COALESCE(cliente_id, 'FABRICA')) STORED,

    PRIMARY KEY (id),
    CONSTRAINT chk_stock_producto_disp CHECK (disponibles >= 0),
    CONSTRAINT uq_stock_producto_titular UNIQUE (titular_key, presentacion_id),
    CONSTRAINT fk_stock_prod_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT fk_stock_prod_presentacion FOREIGN KEY (presentacion_id) REFERENCES presentaciones(id)
);

-- 3. Historial de movimientos de envases (INGRESO, LAVADO, PRODUCCION, RETORNO, BAJA)
CREATE TABLE IF NOT EXISTS movimiento_envase (
    id VARCHAR(50) NOT NULL,
    cliente_id VARCHAR(50) NULL,
    envase_id VARCHAR(50) NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    cantidad INT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT chk_mov_envase_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_mov_envase_tipo CHECK (tipo IN ('INGRESO', 'LAVADO', 'PRODUCCION', 'RETORNO', 'BAJA')),
    CONSTRAINT fk_mov_envase_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT fk_mov_envase_envase FOREIGN KEY (envase_id) REFERENCES envases(id)
);

CREATE TABLE IF NOT EXISTS tipos_cajon (
    id VARCHAR(50) NOT NULL,
    codigo VARCHAR(30) NOT NULL UNIQUE,
    descripcion VARCHAR(120) NOT NULL,
    capacidad INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_tipo_cajon_cap CHECK (capacidad > 0)
);

CREATE TABLE IF NOT EXISTS stock_cajon (
    tipo_cajon_id VARCHAR(50) NOT NULL,
    disponibles_fabrica INT NOT NULL DEFAULT 0,
    PRIMARY KEY (tipo_cajon_id),
    CONSTRAINT chk_stock_cajon_disp CHECK (disponibles_fabrica >= 0),
    CONSTRAINT fk_stock_cajon_tipo FOREIGN KEY (tipo_cajon_id) REFERENCES tipos_cajon(id)
);

CREATE TABLE IF NOT EXISTS movimiento_cajon (
    id VARCHAR(50) NOT NULL,
    cliente_id VARCHAR(50) NULL,
    tipo_cajon_id VARCHAR(50) NOT NULL,
    cantidad INT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    es_retorno BOOLEAN NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_mov_cajon_cant CHECK (cantidad > 0),
    CONSTRAINT fk_mov_cajon_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT fk_mov_cajon_tipo FOREIGN KEY (tipo_cajon_id) REFERENCES tipos_cajon(id)
);