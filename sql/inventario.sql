-- PROPUESTA PARA REVISION DEL EQUIPO. NO ES UNA MIGRACION.
-- Para instalaciones nuevas. IF NOT EXISTS no actualiza tablas existentes.
-- ATENCION: PedidoRepositoryJdbc genera IDs numericos automaticamente, pero
-- los modelos y otros repositorios trabajan con String. Se usa INT temporalmente
-- en pedidos/detalle_pedido para compatibilidad con ese repositorio; acordar IDs.
CREATE DATABASE IF NOT EXISTS tpfinal_prueba CHARACTER SET utf8mb4;
USE tpfinal_prueba;

CREATE TABLE IF NOT EXISTS usuarios (
 id VARCHAR(50) PRIMARY KEY, nombre_usuario VARCHAR(100) NOT NULL UNIQUE,
 hash_contrasena VARCHAR(255) NOT NULL, rol VARCHAR(30) NOT NULL,
 activo BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE IF NOT EXISTS clientes (
 id VARCHAR(50) PRIMARY KEY, nombre VARCHAR(100) NOT NULL,
 telefono VARCHAR(30), direccion VARCHAR(150), activo BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE IF NOT EXISTS productos (
 id VARCHAR(50) PRIMARY KEY, nombre VARCHAR(100) NOT NULL,
 tipo_producto VARCHAR(30) NOT NULL, activo BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE IF NOT EXISTS envases (
 id VARCHAR(50) PRIMARY KEY, descripcion VARCHAR(150), tipo_envase VARCHAR(30),
 material VARCHAR(30), capacidad_lts DOUBLE
);
CREATE TABLE IF NOT EXISTS marcas (
 id VARCHAR(50) PRIMARY KEY, nombre VARCHAR(100) NOT NULL,
 es_propia_fabrica BOOLEAN NOT NULL DEFAULT FALSE, titular_id VARCHAR(50) NULL,
 activo BOOLEAN NOT NULL DEFAULT TRUE,
 FOREIGN KEY (titular_id) REFERENCES clientes(id)
);
CREATE TABLE IF NOT EXISTS presentaciones (
 id VARCHAR(50) PRIMARY KEY, nombre VARCHAR(100) NOT NULL,
 producto_id VARCHAR(50), envase_id VARCHAR(50), marca_id VARCHAR(50),
 precio_llenado DECIMAL(12,2) NOT NULL DEFAULT 0, activa BOOLEAN NOT NULL DEFAULT TRUE,
 FOREIGN KEY (producto_id) REFERENCES productos(id),
 FOREIGN KEY (envase_id) REFERENCES envases(id),
 FOREIGN KEY (marca_id) REFERENCES marcas(id)
);

-- El script del integrante 3 utiliza el nombre plural tipos_cajon.
-- Hay consultas Java que usan tipo_cajon: deben corregirse en el codigo.
CREATE TABLE IF NOT EXISTS tipos_cajon (
 id VARCHAR(50) PRIMARY KEY, codigo VARCHAR(30) NOT NULL UNIQUE,
 descripcion VARCHAR(120) NOT NULL, capacidad INT NOT NULL,
 CONSTRAINT chk_tipo_cajon_cap CHECK (capacidad > 0)
);
CREATE TABLE IF NOT EXISTS stock_envase (
 id VARCHAR(50) NOT NULL, cliente_id VARCHAR(50) NULL, envase_id VARCHAR(50) NOT NULL,
 aptos INT NOT NULL DEFAULT 0, sucios INT NOT NULL DEFAULT 0,
 titular_key VARCHAR(50) GENERATED ALWAYS AS (COALESCE(cliente_id, 'FABRICA')) STORED,
 PRIMARY KEY (id), CONSTRAINT chk_stock_envase_aptos CHECK (aptos >= 0),
 CONSTRAINT chk_stock_envase_sucios CHECK (sucios >= 0),
 CONSTRAINT uq_stock_envase_titular UNIQUE (titular_key, envase_id),
 CONSTRAINT fk_stock_envase_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
 CONSTRAINT fk_stock_envase_envase FOREIGN KEY (envase_id) REFERENCES envases(id)
);
CREATE TABLE IF NOT EXISTS stock_producto (
 id VARCHAR(50) NOT NULL, cliente_id VARCHAR(50) NULL, presentacion_id VARCHAR(50) NOT NULL,
 disponibles INT NOT NULL DEFAULT 0,
 titular_key VARCHAR(50) GENERATED ALWAYS AS (COALESCE(cliente_id, 'FABRICA')) STORED,
 PRIMARY KEY (id), CONSTRAINT chk_stock_producto_disp CHECK (disponibles >= 0),
 CONSTRAINT uq_stock_producto_titular UNIQUE (titular_key, presentacion_id),
 CONSTRAINT fk_stock_prod_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
 CONSTRAINT fk_stock_prod_presentacion FOREIGN KEY (presentacion_id) REFERENCES presentaciones(id)
);
CREATE TABLE IF NOT EXISTS movimiento_envase (
 id VARCHAR(50) NOT NULL, cliente_id VARCHAR(50) NULL, envase_id VARCHAR(50) NOT NULL,
 tipo VARCHAR(30) NOT NULL, cantidad INT NOT NULL, fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY (id), CONSTRAINT chk_mov_envase_cantidad CHECK (cantidad > 0),
 CONSTRAINT chk_mov_envase_tipo CHECK (tipo IN ('INGRESO','LAVADO','PRODUCCION','RETORNO','BAJA')),
 CONSTRAINT fk_mov_envase_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
 CONSTRAINT fk_mov_envase_envase FOREIGN KEY (envase_id) REFERENCES envases(id)
);
CREATE TABLE IF NOT EXISTS stock_cajon (
 tipo_cajon_id VARCHAR(50) PRIMARY KEY, disponibles_fabrica INT NOT NULL DEFAULT 0,
 CONSTRAINT chk_stock_cajon_disp CHECK (disponibles_fabrica >= 0),
 CONSTRAINT fk_stock_cajon_tipo FOREIGN KEY (tipo_cajon_id) REFERENCES tipos_cajon(id)
);
CREATE TABLE IF NOT EXISTS movimiento_cajon (
 id VARCHAR(50) PRIMARY KEY, cliente_id VARCHAR(50) NULL, tipo_cajon_id VARCHAR(50) NOT NULL,
 cantidad INT NOT NULL, fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, es_retorno BOOLEAN NOT NULL,
 CONSTRAINT chk_mov_cajon_cant CHECK (cantidad > 0),
 CONSTRAINT fk_mov_cajon_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
 CONSTRAINT fk_mov_cajon_tipo FOREIGN KEY (tipo_cajon_id) REFERENCES tipos_cajon(id)
);
-- IDs numericos provisionales hasta acordar con los integrantes.
CREATE TABLE IF NOT EXISTS pedidos (
 id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, cliente_id VARCHAR(50) NOT NULL,
 fecha DATETIME NOT NULL, estado VARCHAR(30) NOT NULL,
 FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);
CREATE TABLE IF NOT EXISTS detalle_pedido (
 id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, pedido_id INT NOT NULL,
 presentacion_id VARCHAR(50) NOT NULL, cantidad_unidades INT NOT NULL,