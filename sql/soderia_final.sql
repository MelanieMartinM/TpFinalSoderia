-- Instalacion de demostracion en un esquema NUEVO. No ejecutar sobre datos existentes.
-- Usuarios demo: admin / DemoAdmin2026! ; planta1 / DemoPlanta2026!
-- No usar estas credenciales en produccion.
CREATE DATABASE `soderia_entrega` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `soderia_entrega`;

-- MySQL dump 10.13  Distrib 8.0.38, for Win64 (x86_64)
--
-- Host: localhost    Database: tpfinal_prueba
-- ------------------------------------------------------
-- Server version	9.0.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `clientes`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `clientes` (
  `id` varchar(50) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `telefono` varchar(30) DEFAULT NULL,
  `direccion` varchar(150) DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clientes`
--

LOCK TABLES `clientes` WRITE;
/*!40000 ALTER TABLE `clientes` DISABLE KEYS */;
INSERT INTO `clientes` VALUES ('1b5bb9a7-6cf7-4793-927f-cb20790ae349','Cliente demo 1',NULL,'Domicilio demo 1',0),('5caec774-ad19-4d63-a9bc-2cef127ec066','Cliente demo 2',NULL,'Domicilio demo 2',0),('81491bd6-8bfc-4d84-a0ec-f4eae8ba8845','Cliente demo 3',NULL,'Domicilio demo 3',1),('822fcc7a-b3cd-4aa8-a35a-7510a0a16235','Cliente demo 4',NULL,'Domicilio demo 4',0),('992512d7-5a08-478e-832c-1904def95758','Cliente demo 5',NULL,'Domicilio demo 5',1),('a6ba196c-f747-4e89-9588-029174a7764d','Cliente demo 6',NULL,'Domicilio demo 6',0),('c26a7c6c-02af-4d9b-a27a-66352f8f6d5b','Cliente demo 7',NULL,'Domicilio demo 7',1),('CLI001','Distribuidora del Sur',NULL,'Domicilio demo 8',1),('CLI002','Agua Cristal',NULL,'Domicilio demo 9',1),('CLI003','Repartos Fernandez',NULL,'Domicilio demo 10',1),('CLI004','Distribuidora Atlantica',NULL,'Domicilio demo 11',1),('CLI005','Agua Marina',NULL,'Domicilio demo 12',1),('CLI006','Repartos Gonzalez',NULL,'Domicilio demo 13',1),('CLI007','Soda del Puerto',NULL,'Domicilio demo 14',1),('CLI008','Distribuidora Norte',NULL,'Domicilio demo 15',1),('CLI009','Agua Pura',NULL,'Domicilio demo 16',1),('CLI010','Distribuidora Inactiva',NULL,'Domicilio demo 17',0);
/*!40000 ALTER TABLE `clientes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cobros`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cobros` (
  `id` varchar(100) NOT NULL,
  `retiro_id` varchar(100) DEFAULT NULL,
  `monto` decimal(12,2) NOT NULL,
  `medio_pago` varchar(50) DEFAULT NULL,
  `fecha` datetime DEFAULT NULL,
  `sin_cobro_autorizado` tinyint(1) NOT NULL DEFAULT '0',
  `responsable_autorizacion` varchar(150) DEFAULT NULL,
  `motivo_autorizacion` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cobros`
--

LOCK TABLES `cobros` WRITE;
/*!40000 ALTER TABLE `cobros` DISABLE KEYS */;
INSERT INTO `cobros` VALUES ('0c5ccca1-f02c-4090-bc8c-e1157fba4ab2','ffd50821-d2a1-435d-9e51-76598e6f24eb',5000.00,'EFECTIVO','2026-11-24 22:32:23',0,NULL,NULL),('133851e6-c09e-44cb-9a68-905f1888aceb','62915787-9f37-4743-a0dd-e5b9f4c895a0',5500.00,'EFECTIVO','2026-11-25 00:08:56',0,NULL,NULL),('1c695058-48b8-4884-8884-7354e905343f','a9be2723-dde2-40be-bcd3-63fcda74c586',11000.00,'EFECTIVO','2026-11-24 23:50:04',0,NULL,NULL),('22e70eed-6bad-4252-9c95-bc8070f620a5','bc9c86d8-c732-4981-88a5-50a2f4b102cd',4000.00,'EFECTIVO','2026-11-24 22:29:35',0,NULL,NULL),('3cf89d2c-c368-451b-825f-37f8f10f9a00','bedd0384-d0d5-47b9-bada-7dee7b7f2443',2500.00,'EFECTIVO','2026-11-24 22:34:19',0,NULL,NULL),('4a4ce407-42c3-4163-974e-dc17b9068186','18349abf-878f-467c-8d01-5ecd3398aff8',15000.00,'EFECTIVO','2026-11-24 22:22:52',0,NULL,NULL),('4b3b1779-b62b-4ace-94ed-bcc906cbf05c','22da253e-4a6d-4f06-8bbc-beabca1fc96c',22000.00,'EFECTIVO','2026-11-25 00:02:59',0,NULL,NULL),('52c3dd99-f80b-42d4-a41f-5500f8d937a5','d58ac0a0-8c50-4c97-b86f-8387b606f9be',4000.00,'EFECTIVO','2026-11-24 23:58:20',0,NULL,NULL),('544a1a32-5458-4eab-b6c1-cdd1b312e361','RET001',68000.00,'EFECTIVO','2026-11-24 23:58:54',0,NULL,NULL),('7b85c78d-f3eb-43bb-8ede-220745166109','d58ac0a0-8c50-4c97-b86f-8387b606f9be',7000.00,'TRANSFERENCIA','2026-11-24 23:58:47',0,NULL,NULL),('94f27694-42a6-48c0-a081-949088664dca','52d2f074-3d1c-4fb6-8e41-0a0e16b15abe',10800.00,'EFECTIVO','2026-11-24 22:37:44',0,NULL,NULL),('9668f598-7bb5-4498-8ff5-a16cb09456ad','c1cd9b10-b48e-4b2e-9547-d2661e019b49',33000.00,'EFECTIVO','2026-11-25 00:02:24',0,NULL,NULL),('d8a8d40d-0ef7-48e8-b3c9-42e2e8f11fc2','d58ac0a0-8c50-4c97-b86f-8387b606f9be',0.00,NULL,'2026-11-24 23:52:24',1,'admin','Autorizacion de demostracion'),('f41d57c7-ea6b-48bc-946c-93dec053e4b0','9a3d45fe-4a4f-4672-a4f1-af6a32813bf1',2500.00,'TARJETA','2026-11-24 22:35:31',0,NULL,NULL),('f8272d1d-9b68-40dd-8751-b62d014befd1','8837dbd4-b7e4-4a12-b998-83bb2ca0887b',25500.00,'EFECTIVO','2026-11-25 01:24:05',0,NULL,NULL),('fc38baf6-cf51-4c72-8626-82692772f982','bc9c86d8-c732-4981-88a5-50a2f4b102cd',6000.00,'TRANSFERENCIA','2026-11-24 22:30:42',0,NULL,NULL);
/*!40000 ALTER TABLE `cobros` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contratos`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contratos` (
  `id` varchar(50) NOT NULL,
  `cliente_id` varchar(50) NOT NULL,
  `fecha_inicio` date NOT NULL,
  `fecha_fin` date DEFAULT NULL,
  `activo` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `cliente_id` (`cliente_id`),
  CONSTRAINT `contratos_ibfk_1` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contratos`
--

LOCK TABLES `contratos` WRITE;
/*!40000 ALTER TABLE `contratos` DISABLE KEYS */;
INSERT INTO `contratos` VALUES ('CON001','CLI001','2026-01-10',NULL,1),('CON002','CLI002','2026-02-15',NULL,1),('CON003','CLI003','2026-03-01',NULL,1),('CON004','CLI004','2026-04-05',NULL,1),('CON005','CLI005','2026-05-10',NULL,1),('CON006','CLI006','2026-06-15',NULL,1),('CON007','CLI007','2026-07-01',NULL,1),('CON008','CLI008','2026-08-05',NULL,1),('CON009','CLI009','2026-09-10',NULL,1),('CON010','CLI010','2026-01-01','2026-10-01',0);
/*!40000 ALTER TABLE `contratos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_contrato`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalle_contrato` (
  `id` varchar(50) NOT NULL,
  `contrato_id` varchar(50) NOT NULL,
  `envase_id` varchar(50) NOT NULL,
  `cantidad` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `contrato_id` (`contrato_id`),
  KEY `envase_id` (`envase_id`),
  CONSTRAINT `detalle_contrato_ibfk_1` FOREIGN KEY (`contrato_id`) REFERENCES `contratos` (`id`),
  CONSTRAINT `detalle_contrato_ibfk_2` FOREIGN KEY (`envase_id`) REFERENCES `envases` (`id`),
  CONSTRAINT `detalle_contrato_chk_1` CHECK ((`cantidad` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_contrato`
--

LOCK TABLES `detalle_contrato` WRITE;
/*!40000 ALTER TABLE `detalle_contrato` DISABLE KEYS */;
INSERT INTO `detalle_contrato` VALUES ('DC001','CON001','ENV001',100),('DC002','CON001','ENV002',60),('DC003','CON001','ENV007',20),('DC004','CON002','ENV001',80),('DC005','CON002','ENV003',120),('DC006','CON003','ENV004',180),('DC007','CON003','ENV007',30),('DC008','CON004','ENV001',100),('DC009','CON005','ENV002',90),('DC010','CON006','ENV003',120),('DC011','CON007','ENV004',150),('DC012','CON008','ENV005',120),('DC013','CON009','ENV006',120),('DC014','CON010','ENV001',50);
/*!40000 ALTER TABLE `detalle_contrato` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_pedido`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalle_pedido` (
  `id` varchar(50) NOT NULL,
  `pedido_id` varchar(50) NOT NULL,
  `presentacion_id` varchar(50) NOT NULL,
  `cantidad_unidades` int NOT NULL,
  `precio_unitario_pactado` decimal(12,2) NOT NULL,
  `subtotal` decimal(12,2) GENERATED ALWAYS AS ((`cantidad_unidades` * `precio_unitario_pactado`)) STORED,
  `cantidad_cajones` int NOT NULL DEFAULT '0',
  `tipo_cajon_id` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `pedido_id` (`pedido_id`),
  KEY `presentacion_id` (`presentacion_id`),
  KEY `fk_detalle_tipo_cajon` (`tipo_cajon_id`),
  CONSTRAINT `detalle_pedido_ibfk_1` FOREIGN KEY (`pedido_id`) REFERENCES `pedidos` (`id`),
  CONSTRAINT `detalle_pedido_ibfk_2` FOREIGN KEY (`presentacion_id`) REFERENCES `presentaciones` (`id`),
  CONSTRAINT `fk_detalle_tipo_cajon` FOREIGN KEY (`tipo_cajon_id`) REFERENCES `tipos_cajon` (`id`),
  CONSTRAINT `chk_cantidad_cajones` CHECK ((`cantidad_cajones` >= 0)),
  CONSTRAINT `chk_cantidad_unidades` CHECK ((`cantidad_unidades` > 0)),
  CONSTRAINT `chk_precio_unitario_pactado` CHECK ((`precio_unitario_pactado` >= 0)),
  CONSTRAINT `detalle_pedido_chk_3` CHECK ((`subtotal` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_pedido`
--

LOCK TABLES `detalle_pedido` WRITE;
/*!40000 ALTER TABLE `detalle_pedido` DISABLE KEYS */;
INSERT INTO `detalle_pedido` (`id`, `pedido_id`, `presentacion_id`, `cantidad_unidades`, `precio_unitario_pactado`, `cantidad_cajones`, `tipo_cajon_id`) VALUES ('0847e0c8-c5ae-4e8a-a761-5c6ecb75dc2f','ac1f0e99-608d-4f88-ad06-0ea3499bd3d2','PRE002',6,1800.00,1,'CAJ001'),('13e79ff1-4299-4b95-b086-89b34e027b91','2ab21d35-21b4-455f-a37d-e728761a8ccc','PRE001',1,6000.00,0,NULL),('256d4f02-11b1-4781-8b9b-97bbd9bcdff2','a4d700e6-0b4f-4e78-aca5-50e3168e709b','PRE002',5,1800.00,0,NULL),('32e26c54-2adc-4825-8968-1f2b7c3e47ce','b1947960-3d32-4b7d-b0fb-6778ea77c230','PRE003',6,900.00,1,'CAJ001'),('4bc38618-7a0f-4f50-b710-51a86f5ed89e','bffb5a09-9818-4f0d-9f4e-5801888fc287','PRE002',3,1800.00,0,NULL),('774a0b06-a000-4f82-9af8-659e5a38d6e5','a4d700e6-0b4f-4e78-aca5-50e3168e709b','PRE001',3,5500.00,0,NULL),('b162b80c-e9f6-46cc-a1c8-40990a18738c','be16524a-ee87-4d49-a8a0-a5a87303583f','PRE002',5,1800.00,0,NULL),('c3ba959e-1d39-451b-a0bf-b61dab26115a','8344af70-1661-4502-a358-4caf16147f6d','PRE001',10,5500.00,0,NULL),('d852a7f7-880c-4b04-8260-f2ad99a6a452','f5e2e726-d8fb-42aa-a0a4-94aa55c5ecac','PRE001',10,2500.00,0,NULL),('DP001','PED001','PRE001',20,2500.00,0,NULL),('DP002','PED001','PRE002',10,1800.00,0,NULL),('DP003','PED002','PRE001',2,2500.00,0,NULL),('DP004','PED002','PRE003',30,900.00,0,NULL),('DP005','PED003','PRE004',60,850.00,0,NULL),('DP006','PED004','PRE001',15,2500.00,0,NULL),('DP007','PED005','PRE002',20,1800.00,0,NULL),('DP008','PED006','PRE003',60,900.00,0,NULL),('DP009','PED007','PRE004',30,850.00,0,NULL),('DP010','PED008','PRE005',60,500.00,0,NULL),('DP011','PED009','PRE006',30,550.00,0,NULL),('DP012','PED010','PRE001',10,2500.00,0,NULL),('e25e1328-9f70-4841-817b-fd8671719755','be16524a-ee87-4d49-a8a0-a5a87303583f','PRE001',3,6000.00,0,NULL),('f2f55148-b6d4-4d8a-8473-547086e9c42c','e616a4dd-6970-4409-aced-54421b32e3c2','PRE001',2,6000.00,0,NULL),('f78ea76a-5ef0-40cb-a9fd-597c0c1d3f58','b1947960-3d32-4b7d-b0fb-6778ea77c230','PRE004',1,850.00,0,NULL);
/*!40000 ALTER TABLE `detalle_pedido` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_retiro`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalle_retiro` (
  `id` varchar(50) NOT NULL,
  `retiro_id` varchar(50) NOT NULL,
  `presentacion_id` varchar(50) NOT NULL,
  `cantidad_retirada` int NOT NULL,
  `precio_aplicado` decimal(12,2) NOT NULL,
  `pedido_id` varchar(50) DEFAULT NULL,
  `detalle_pedido_id` varchar(50) DEFAULT NULL,
  `cantidad_pedida` int NOT NULL DEFAULT '0',
  `tipo_cajon_id` varchar(50) DEFAULT NULL,
  `cantidad_cajones` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `retiro_id` (`retiro_id`),
  KEY `presentacion_id` (`presentacion_id`),
  KEY `fk_detalle_retiro_pedido` (`pedido_id`),
  KEY `fk_retiro_detalle_pedido` (`detalle_pedido_id`),
  KEY `fk_retiro_tipo_cajon` (`tipo_cajon_id`),
  CONSTRAINT `detalle_retiro_ibfk_1` FOREIGN KEY (`retiro_id`) REFERENCES `retiros` (`id`),
  CONSTRAINT `detalle_retiro_ibfk_2` FOREIGN KEY (`presentacion_id`) REFERENCES `presentaciones` (`id`),
  CONSTRAINT `fk_detalle_retiro_pedido` FOREIGN KEY (`pedido_id`) REFERENCES `pedidos` (`id`),
  CONSTRAINT `fk_retiro_detalle_pedido` FOREIGN KEY (`detalle_pedido_id`) REFERENCES `detalle_pedido` (`id`),
  CONSTRAINT `fk_retiro_tipo_cajon` FOREIGN KEY (`tipo_cajon_id`) REFERENCES `tipos_cajon` (`id`),
  CONSTRAINT `chk_cantidad_pedida` CHECK ((`cantidad_pedida` >= 0)),
  CONSTRAINT `chk_cantidad_retirada` CHECK ((`cantidad_retirada` > 0)),
  CONSTRAINT `chk_precio_aplicado` CHECK ((`precio_aplicado` >= 0)),
  CONSTRAINT `chk_retiro_cantidad_cajones` CHECK ((`cantidad_cajones` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_retiro`
--

LOCK TABLES `detalle_retiro` WRITE;
/*!40000 ALTER TABLE `detalle_retiro` DISABLE KEYS */;
INSERT INTO `detalle_retiro` VALUES ('1793836b-451d-4c7a-bab8-bd6dda0cca4f','22da253e-4a6d-4f06-8bbc-beabca1fc96c','PRE001',4,5500.00,'8344af70-1661-4502-a358-4caf16147f6d','c3ba959e-1d39-451b-a0bf-b61dab26115a',10,NULL,0),('23ff4556-eb25-4e92-ad6b-a2ace42ee706','a9be2723-dde2-40be-bcd3-63fcda74c586','PRE001',2,5500.00,NULL,NULL,0,NULL,0),('288208f7-eafa-47cf-85a1-e0ed234c2971','62915787-9f37-4743-a0dd-e5b9f4c895a0','PRE001',1,5500.00,NULL,NULL,0,NULL,0),('2e136e81-e2c4-4612-8beb-cbc9d9866443','bc9c86d8-c732-4981-88a5-50a2f4b102cd','PRE001',4,2500.00,'f5e2e726-d8fb-42aa-a0a4-94aa55c5ecac','d852a7f7-880c-4b04-8260-f2ad99a6a452',10,NULL,0),('59f14e0c-bd3c-4217-840d-037bbf11300d','d58ac0a0-8c50-4c97-b86f-8387b606f9be','PRE001',2,5500.00,NULL,NULL,0,NULL,0),('649d9653-2d22-443a-a12b-816f3ee1a9e9','c1cd9b10-b48e-4b2e-9547-d2661e019b49','PRE001',6,5500.00,'8344af70-1661-4502-a358-4caf16147f6d','c3ba959e-1d39-451b-a0bf-b61dab26115a',10,NULL,0),('682811da-408d-4ac3-a392-9b21181a18ef','8837dbd4-b7e4-4a12-b998-83bb2ca0887b','PRE001',3,5500.00,'a4d700e6-0b4f-4e78-aca5-50e3168e709b','774a0b06-a000-4f82-9af8-659e5a38d6e5',3,NULL,0),('6839e489-52cb-43a2-bb7b-805d7c290927','8837dbd4-b7e4-4a12-b998-83bb2ca0887b','PRE002',5,1800.00,'a4d700e6-0b4f-4e78-aca5-50e3168e709b','256d4f02-11b1-4781-8b9b-97bbd9bcdff2',5,NULL,0),('a1f85f69-792a-4aae-9edb-883efb316570','9a3d45fe-4a4f-4672-a4f1-af6a32813bf1','PRE001',1,2500.00,NULL,NULL,0,NULL,0),('c349762e-f9b2-4d06-abfb-d6e8a6e9fe51','bedd0384-d0d5-47b9-bada-7dee7b7f2443','PRE001',1,2500.00,NULL,NULL,0,NULL,0),('c6b42e78-a52c-4dc9-8fb4-89e59c6b0d46','18349abf-878f-467c-8d01-5ecd3398aff8','PRE001',6,2500.00,'f5e2e726-d8fb-42aa-a0a4-94aa55c5ecac','d852a7f7-880c-4b04-8260-f2ad99a6a452',10,NULL,0),('DR001','RET001','PRE001',20,2500.00,'PED001','DP001',20,NULL,0),('DR002','RET001','PRE002',10,1800.00,'PED001','DP002',10,NULL,0),('DR003','RET002','PRE001',2,2500.00,'PED002','DP003',2,NULL,0),('DR004','RET002','PRE003',30,900.00,'PED002','DP004',30,NULL,0),('DR005','RET003','PRE004',60,850.00,'PED003','DP005',60,NULL,0),('DR006','RET004','PRE001',15,2500.00,'PED004','DP006',15,NULL,0),('e6dfbc5b-e4c0-4eb5-ac23-e22ed79763b2','ffd50821-d2a1-435d-9e51-76598e6f24eb','PRE001',2,2500.00,NULL,NULL,0,NULL,0),('fff5d205-8431-4c44-8fea-f4f8403e2eed','52d2f074-3d1c-4fb6-8e41-0a0e16b15abe','PRE003',12,900.00,NULL,NULL,0,'CAJ001',2);
/*!40000 ALTER TABLE `detalle_retiro` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `empleados`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `empleados` (
  `id` varchar(50) NOT NULL,
  `legajo` varchar(50) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `telefono` varchar(30) DEFAULT NULL,
  `direccion` varchar(200) DEFAULT NULL,
  `usuario_id` varchar(50) DEFAULT NULL,
  `activo` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `legajo` (`legajo`),
  KEY `usuario_id` (`usuario_id`),
  CONSTRAINT `empleados_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `empleados`
--

LOCK TABLES `empleados` WRITE;
/*!40000 ALTER TABLE `empleados` DISABLE KEYS */;
INSERT INTO `empleados` VALUES ('EMP001','LEG001','Empleado demo 001',NULL,'Domicilio demo','USR001',1),('EMP002','LEG002','Empleado demo 002',NULL,'Domicilio demo','USR002',1),('EMP003','LEG003','Empleado demo 003',NULL,'Domicilio demo','USR003',1),('EMP004','LEG004','Empleado demo 004',NULL,'Domicilio demo','USR004',1),('EMP005','LEG005','Empleado demo 005',NULL,'Domicilio demo','USR005',1),('EMP006','LEG006','Empleado demo 006',NULL,'Domicilio demo','USR006',1);
/*!40000 ALTER TABLE `empleados` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `envases`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `envases` (
  `id` varchar(50) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `material` varchar(30) NOT NULL,
  `capacidad_ml` int NOT NULL,
  `retornable` tinyint(1) DEFAULT '1',
  `activo` tinyint(1) DEFAULT '1',
  `descripcion` varchar(100) DEFAULT NULL,
  `tipo_envase` varchar(50) DEFAULT NULL,
  `capacidad_lts` decimal(8,3) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `envases`
--

LOCK TABLES `envases` WRITE;
/*!40000 ALTER TABLE `envases` DISABLE KEYS */;
INSERT INTO `envases` VALUES ('ENV001','Bidon 20 litros','PLASTICO',20000,1,1,'Bidon 20 litros','BIDON',20.000),('ENV002','Bidon 12 litros','PLASTICO',12000,1,1,'Bidon 12 litros','BIDON',12.000),('ENV003','Sifon 1.25 litros','PLASTICO',1250,1,1,'Sifon 1.25 litros','SIFON',1.250),('ENV004','Sifon 1 litro','VIDRIO',1000,1,1,'Sifon 1 litro','SIFON',1.000),('ENV005','Sifon 500 ml plastico','PLASTICO',500,1,1,'Sifon 500 ml plastico','SIFON',0.500),('ENV006','Sifon 500 ml vidrio','VIDRIO',500,1,1,'Sifon 500 ml vidrio','SIFON',0.500),('ENV007','Cajon plastico','PLASTICO',0,1,1,'Cajon plastico',NULL,0.000);
/*!40000 ALTER TABLE `envases` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `marcas`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `marcas` (
  `id` varchar(50) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `titular_id` varchar(50) DEFAULT NULL,
  `es_propia_fabrica` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fk_marca_titular` (`titular_id`),
  CONSTRAINT `fk_marca_titular` FOREIGN KEY (`titular_id`) REFERENCES `clientes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `marcas`
--

LOCK TABLES `marcas` WRITE;
/*!40000 ALTER TABLE `marcas` DISABLE KEYS */;
INSERT INTO `marcas` VALUES ('MAR001','Agua Martin',NULL,1),('MAR002','Marca de terceros','CLI001',0);
/*!40000 ALTER TABLE `marcas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `movimiento_cajon`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `movimiento_cajon` (
  `id` varchar(50) NOT NULL,
  `cliente_id` varchar(50) NOT NULL,
  `tipo_cajon_id` varchar(50) NOT NULL,
  `cantidad` int NOT NULL,
  `fecha` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `es_retorno` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `cliente_id` (`cliente_id`),
  KEY `tipo_cajon_id` (`tipo_cajon_id`),
  CONSTRAINT `movimiento_cajon_ibfk_1` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`),
  CONSTRAINT `movimiento_cajon_ibfk_2` FOREIGN KEY (`tipo_cajon_id`) REFERENCES `tipos_cajon` (`id`),
  CONSTRAINT `movimiento_cajon_chk_1` CHECK ((`cantidad` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movimiento_cajon`
--

LOCK TABLES `movimiento_cajon` WRITE;
/*!40000 ALTER TABLE `movimiento_cajon` DISABLE KEYS */;
INSERT INTO `movimiento_cajon` VALUES ('00a1f1c0-051e-4cb9-bbd8-ccec2f122f44','CLI001','CAJ001',9,'2026-11-25 02:14:13',1),('27f257ab-9238-492a-8ff1-89ea70a0ccd7','CLI001','CAJ001',10,'2026-11-24 22:02:52',0),('83d40c6d-882e-4d05-bacb-9fad208718f1','CLI001','CAJ001',2,'2026-11-25 02:08:38',0),('8d1c78e1-5e5e-4952-ab07-1e0c6f2d98e3','CLI001','CAJ001',5,'2026-11-25 02:06:38',1),('b9b0c141-3629-438b-9ef9-102cf52d4299','CLI001','CAJ001',5,'2026-11-25 02:05:34',1),('c2015ff1-39de-46ca-a78f-2f60b362e807','CLI002','CAJ001',2,'2026-11-24 22:37:44',0),('c3eea3a6-f3b3-4ab1-8633-03d01b812dbd','CLI001','CAJ001',2,'2026-11-25 02:06:16',0),('c4961b37-c922-4c19-b5a2-f8c6f7a742d3','CLI001','CAJ001',5,'2026-11-25 02:08:03',1),('d7e8a537-42cf-4fb6-bf33-c6de6718f2c5','CLI001','CAJ001',5,'2026-11-24 22:01:29',1);
/*!40000 ALTER TABLE `movimiento_cajon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `movimiento_envase`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `movimiento_envase` (
  `id` varchar(50) NOT NULL,
  `cliente_id` varchar(50) NOT NULL,
  `envase_id` varchar(50) NOT NULL,
  `tipo` varchar(50) NOT NULL,
  `cantidad` int NOT NULL,
  `fecha` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `cliente_id` (`cliente_id`),
  KEY `envase_id` (`envase_id`),
  CONSTRAINT `movimiento_envase_ibfk_1` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`),
  CONSTRAINT `movimiento_envase_ibfk_2` FOREIGN KEY (`envase_id`) REFERENCES `envases` (`id`),
  CONSTRAINT `movimiento_envase_chk_1` CHECK ((`cantidad` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movimiento_envase`
--

LOCK TABLES `movimiento_envase` WRITE;
/*!40000 ALTER TABLE `movimiento_envase` DISABLE KEYS */;
INSERT INTO `movimiento_envase` VALUES ('029b77ad-8fa9-4672-9988-a0ae1ac9e4c3','CLI001','ENV001','LAVADO',10,'2026-11-24 21:48:51'),('073d16ac-a1a1-4f84-bdff-43ba536d318e','CLI001','ENV001','INGRESO',10,'2026-11-25 02:40:49'),('1d267d60-880e-410f-b80d-0ea44509a5d1','CLI001','ENV001','RETORNO',15,'2026-11-25 02:42:35'),('3ae117da-5412-4c63-a9f7-a758f813d466','CLI001','ENV001','RETORNO',5,'2026-11-24 22:06:11'),('3e518548-9e47-4c06-b2ee-3922ce26a8c6','CLI001','ENV001','PRODUCCION',20,'2026-11-25 02:45:39'),('6a8a351d-a511-4cc7-be3b-81f5867524bc','CLI001','ENV001','PRODUCCION',10,'2026-11-24 21:57:10'),('83523aef-f4e9-4b7d-aa0f-00abc5cecb4d','CLI001','ENV001','INGRESO',10,'2026-11-24 21:47:23'),('e26f83e5-b467-4b44-a13e-d9e70e058537','CLI001','ENV001','PRODUCCION',2,'2026-11-25 02:51:22'),('f474eddd-c14a-43e8-a867-626d3cdbfc14','CLI001','ENV001','LAVADO',15,'2026-11-25 02:48:52');
/*!40000 ALTER TABLE `movimiento_envase` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `movimientos_stock`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `movimientos_stock` (
  `id` varchar(50) NOT NULL,
  `cliente_id` varchar(50) DEFAULT NULL,
  `envase_id` varchar(50) DEFAULT NULL,
  `presentacion_id` varchar(50) DEFAULT NULL,
  `tipo` varchar(40) NOT NULL,
  `cantidad` int NOT NULL,
  `fecha` datetime DEFAULT CURRENT_TIMESTAMP,
  `observacion` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `cliente_id` (`cliente_id`),
  KEY `envase_id` (`envase_id`),
  KEY `presentacion_id` (`presentacion_id`),
  CONSTRAINT `movimientos_stock_ibfk_1` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`),
  CONSTRAINT `movimientos_stock_ibfk_2` FOREIGN KEY (`envase_id`) REFERENCES `envases` (`id`),
  CONSTRAINT `movimientos_stock_ibfk_3` FOREIGN KEY (`presentacion_id`) REFERENCES `presentaciones` (`id`),
  CONSTRAINT `movimientos_stock_chk_1` CHECK ((`cantidad` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movimientos_stock`
--

LOCK TABLES `movimientos_stock` WRITE;
/*!40000 ALTER TABLE `movimientos_stock` DISABLE KEYS */;
INSERT INTO `movimientos_stock` VALUES ('MOV001','CLI001','ENV001',NULL,'INGRESO',20,'2026-11-20 08:00:00','Recepcion de vacios'),('MOV002','CLI001','ENV001',NULL,'LAVADO',10,'2026-11-20 09:00:00','Lavado de bidones'),('MOV003','CLI001','ENV001','PRE001','PRODUCCION',30,'2026-11-20 10:00:00','Llenado de agua'),('MOV004','CLI002','ENV003',NULL,'INGRESO',20,'2026-11-21 08:30:00','Recepcion de sifones'),('MOV005','CLI003','ENV004','PRE004','PRODUCCION',60,'2026-11-21 10:00:00','Produccion de soda'),('MOV006','CLI004','ENV001','PRE001','RETIRO',15,'2026-11-22 09:30:00','Retiro de repartidor');
/*!40000 ALTER TABLE `movimientos_stock` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pedidos`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pedidos` (
  `id` varchar(50) NOT NULL,
  `cliente_id` varchar(50) NOT NULL,
  `fecha` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `estado` varchar(30) NOT NULL DEFAULT 'BORRADOR',
  `total` decimal(12,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`id`),
  KEY `cliente_id` (`cliente_id`),
  CONSTRAINT `pedidos_ibfk_1` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`),
  CONSTRAINT `pedidos_chk_1` CHECK ((`total` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pedidos`
--

LOCK TABLES `pedidos` WRITE;
/*!40000 ALTER TABLE `pedidos` DISABLE KEYS */;
INSERT INTO `pedidos` VALUES ('1d895f2e-10ff-4269-94e3-c8336d77618e','CLI001','2026-11-24 22:08:15','BORRADOR',0.00),('2ab21d35-21b4-455f-a37d-e728761a8ccc','CLI001','2026-11-25 01:11:43','BORRADOR',6000.00),('8344af70-1661-4502-a358-4caf16147f6d','CLI001','2026-11-25 00:01:09','COMPLETADO',55000.00),('a4d700e6-0b4f-4e78-aca5-50e3168e709b','CLI001','2026-11-25 00:57:29','COMPLETADO',25500.00),('ac1f0e99-608d-4f88-ad06-0ea3499bd3d2','CLI001','2026-11-25 00:37:26','CONFIRMADO',10800.00),('b1947960-3d32-4b7d-b0fb-6778ea77c230','CLI001','2026-11-25 00:33:58','BORRADOR',6250.00),('be16524a-ee87-4d49-a8a0-a5a87303583f','CLI001','2026-11-25 01:18:08','BORRADOR',27000.00),('bffb5a09-9818-4f0d-9f4e-5801888fc287','CLI001','2026-11-25 00:50:45','BORRADOR',5400.00),('e616a4dd-6970-4409-aced-54421b32e3c2','CLI001','2026-11-25 01:13:02','CONFIRMADO',12000.00),('f5e2e726-d8fb-42aa-a0a4-94aa55c5ecac','CLI001','2026-11-24 22:12:04','COMPLETADO',25000.00),('PED001','CLI001','2026-11-20 08:00:00','COMPLETADO',68000.00),('PED002','CLI002','2026-11-20 09:00:00','COMPLETADO',32000.00),('PED003','CLI003','2026-11-21 08:00:00','COMPLETADO',51000.00),('PED004','CLI004','2026-11-21 09:00:00','COMPLETADO',37500.00),('PED005','CLI005','2026-11-24 10:00:00','CONFIRMADO',36000.00),('PED006','CLI006','2026-11-24 11:00:00','CONFIRMADO',54000.00),('PED007','CLI007','2026-11-24 12:00:00','CANCELADO',25500.00),('PED008','CLI008','2026-11-24 13:00:00','CONFIRMADO',30000.00),('PED009','CLI009','2026-11-24 14:00:00','CONFIRMADO',16500.00),('PED010','CLI001','2026-11-24 15:00:00','CONFIRMADO',25000.00);
/*!40000 ALTER TABLE `pedidos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `presentaciones`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `presentaciones` (
  `id` varchar(50) NOT NULL,
  `producto_id` varchar(50) NOT NULL,
  `envase_id` varchar(50) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `precio` decimal(12,2) NOT NULL,
  `unidades_por_cajon` int DEFAULT '1',
  `activo` tinyint(1) DEFAULT '1',
  `precio_llenado` decimal(12,2) DEFAULT NULL,
  `activa` tinyint(1) NOT NULL DEFAULT '1',
  `marca_id` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `producto_id` (`producto_id`),
  KEY `envase_id` (`envase_id`),
  KEY `fk_presentacion_marca` (`marca_id`),
  CONSTRAINT `fk_presentacion_marca` FOREIGN KEY (`marca_id`) REFERENCES `marcas` (`id`),
  CONSTRAINT `presentaciones_ibfk_1` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`),
  CONSTRAINT `presentaciones_ibfk_2` FOREIGN KEY (`envase_id`) REFERENCES `envases` (`id`),
  CONSTRAINT `presentaciones_chk_1` CHECK ((`precio` >= 0)),
  CONSTRAINT `presentaciones_chk_2` CHECK ((`unidades_por_cajon` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `presentaciones`
--

LOCK TABLES `presentaciones` WRITE;
/*!40000 ALTER TABLE `presentaciones` DISABLE KEYS */;
INSERT INTO `presentaciones` VALUES ('PRE001','PROD001','ENV001','Agua 20 L',2500.00,1,1,6000.00,1,'MAR001'),('PRE002','PROD001','ENV002','Agua 12 L',1800.00,1,1,1800.00,1,'MAR001'),('PRE003','PROD002','ENV003','Soda 1.25 L plastico',900.00,6,1,900.00,1,'MAR001'),('PRE004','PROD002','ENV004','Soda 1 L vidrio',850.00,6,1,850.00,1,'MAR001'),('PRE005','PROD002','ENV005','Soda 500 ml plastico',500.00,12,1,500.00,1,'MAR001'),('PRE006','PROD002','ENV006','Soda 500 ml vidrio',550.00,12,1,550.00,0,'MAR001');
/*!40000 ALTER TABLE `presentaciones` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `productos`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `productos` (
  `id` varchar(50) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `tipo_producto` varchar(30) NOT NULL,
  `activo` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `productos`
--

LOCK TABLES `productos` WRITE;
/*!40000 ALTER TABLE `productos` DISABLE KEYS */;
INSERT INTO `productos` VALUES ('PROD001','Agua de mesa','AGUA',1),('PROD002','Soda','SODA',1),('PROD003','Agua discontinuada','AGUA',0);
/*!40000 ALTER TABLE `productos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `retiros`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `retiros` (
  `id` varchar(50) NOT NULL,
  `pedido_id` varchar(50) DEFAULT NULL,
  `cliente_id` varchar(50) NOT NULL,
  `empleado_id` varchar(50) DEFAULT NULL,
  `fecha` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `total` decimal(12,2) NOT NULL,
  `estado` varchar(30) NOT NULL DEFAULT 'COMPLETADO',
  PRIMARY KEY (`id`),
  KEY `pedido_id` (`pedido_id`),
  KEY `cliente_id` (`cliente_id`),
  KEY `empleado_id` (`empleado_id`),
  CONSTRAINT `retiros_ibfk_1` FOREIGN KEY (`pedido_id`) REFERENCES `pedidos` (`id`),
  CONSTRAINT `retiros_ibfk_2` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`),
  CONSTRAINT `retiros_ibfk_3` FOREIGN KEY (`empleado_id`) REFERENCES `empleados` (`id`),
  CONSTRAINT `retiros_chk_1` CHECK ((`total` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `retiros`
--

LOCK TABLES `retiros` WRITE;
/*!40000 ALTER TABLE `retiros` DISABLE KEYS */;
INSERT INTO `retiros` VALUES ('18349abf-878f-467c-8d01-5ecd3398aff8','f5e2e726-d8fb-42aa-a0a4-94aa55c5ecac','CLI001',NULL,'2026-11-24 22:22:52',15000.00,'COMPLETADO'),('22da253e-4a6d-4f06-8bbc-beabca1fc96c','8344af70-1661-4502-a358-4caf16147f6d','CLI001',NULL,'2026-11-25 00:02:59',22000.00,'COMPLETADO'),('52d2f074-3d1c-4fb6-8e41-0a0e16b15abe',NULL,'CLI002',NULL,'2026-11-24 22:37:44',10800.00,'COMPLETADO'),('62915787-9f37-4743-a0dd-e5b9f4c895a0',NULL,'CLI001',NULL,'2026-11-25 00:08:56',5500.00,'COMPLETADO'),('8837dbd4-b7e4-4a12-b998-83bb2ca0887b','a4d700e6-0b4f-4e78-aca5-50e3168e709b','CLI001',NULL,'2026-11-25 01:24:05',25500.00,'COMPLETADO'),('9a3d45fe-4a4f-4672-a4f1-af6a32813bf1',NULL,'CLI001',NULL,'2026-11-24 22:35:31',2500.00,'COMPLETADO'),('a9be2723-dde2-40be-bcd3-63fcda74c586',NULL,'CLI001',NULL,'2026-11-24 23:50:04',11000.00,'COMPLETADO'),('bc9c86d8-c732-4981-88a5-50a2f4b102cd','f5e2e726-d8fb-42aa-a0a4-94aa55c5ecac','CLI001',NULL,'2026-11-24 22:27:38',10000.00,'COMPLETADO'),('bedd0384-d0d5-47b9-bada-7dee7b7f2443',NULL,'CLI001',NULL,'2026-11-24 22:34:19',2500.00,'COMPLETADO'),('c1cd9b10-b48e-4b2e-9547-d2661e019b49','8344af70-1661-4502-a358-4caf16147f6d','CLI001',NULL,'2026-11-25 00:02:24',33000.00,'COMPLETADO'),('d58ac0a0-8c50-4c97-b86f-8387b606f9be',NULL,'CLI001',NULL,'2026-11-24 23:52:24',11000.00,'COMPLETADO'),('ffd50821-d2a1-435d-9e51-76598e6f24eb',NULL,'CLI001',NULL,'2026-11-24 22:32:23',5000.00,'COMPLETADO'),('RET001','PED001','CLI001','EMP003','2026-11-20 11:00:00',68000.00,'COMPLETADO'),('RET002','PED002','CLI002','EMP003','2026-11-20 12:00:00',32000.00,'COMPLETADO'),('RET003','PED003','CLI003','EMP004','2026-11-21 11:00:00',51000.00,'COMPLETADO'),('RET004','PED004','CLI004','EMP004','2026-11-22 09:30:00',37500.00,'COMPLETADO');
/*!40000 ALTER TABLE `retiros` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stock_cajon`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock_cajon` (
  `id` int NOT NULL AUTO_INCREMENT,
  `cliente_id` varchar(50) NOT NULL,
  `tipo_cajon_id` varchar(50) NOT NULL,
  `disponibles` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `cliente_id` (`cliente_id`,`tipo_cajon_id`),
  KEY `tipo_cajon_id` (`tipo_cajon_id`),
  CONSTRAINT `stock_cajon_ibfk_1` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`),
  CONSTRAINT `stock_cajon_ibfk_2` FOREIGN KEY (`tipo_cajon_id`) REFERENCES `tipos_cajon` (`id`),
  CONSTRAINT `stock_cajon_chk_1` CHECK ((`disponibles` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=66 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stock_cajon`
--

LOCK TABLES `stock_cajon` WRITE;
/*!40000 ALTER TABLE `stock_cajon` DISABLE KEYS */;
INSERT INTO `stock_cajon` VALUES (1,'CLI001','CAJ001',35),(2,'CLI001','CAJ002',15),(3,'CLI002','CAJ001',28),(4,'CLI003','CAJ002',25),(5,'CLI004','CAJ001',10),(6,'CLI005','CAJ003',20),(7,'CLI006','CAJ001',35),(8,'CLI007','CAJ002',30),(9,'CLI008','CAJ003',15),(10,'CLI009','CAJ004',0);
/*!40000 ALTER TABLE `stock_cajon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stock_envase`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock_envase` (
  `id` int NOT NULL AUTO_INCREMENT,
  `cliente_id` varchar(50) DEFAULT NULL,
  `envase_id` varchar(50) NOT NULL,
  `aptos` int NOT NULL DEFAULT '0',
  `sucios` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `cliente_id` (`cliente_id`,`envase_id`),
  KEY `envase_id` (`envase_id`),
  CONSTRAINT `stock_envase_ibfk_1` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`),
  CONSTRAINT `stock_envase_ibfk_2` FOREIGN KEY (`envase_id`) REFERENCES `envases` (`id`),
  CONSTRAINT `chk_stock_envase_aptos` CHECK ((`aptos` >= 0)),
  CONSTRAINT `chk_stock_envase_sucios` CHECK ((`sucios` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stock_envase`
--

LOCK TABLES `stock_envase` WRITE;
/*!40000 ALTER TABLE `stock_envase` DISABLE KEYS */;
INSERT INTO `stock_envase` VALUES (1,NULL,'ENV001',150,40),(2,NULL,'ENV002',120,30),(3,NULL,'ENV003',300,80),(4,NULL,'ENV004',250,60),(5,NULL,'ENV005',400,100),(6,NULL,'ENV006',350,90),(7,NULL,'ENV007',100,20),(8,'CLI001','ENV001',33,35),(9,'CLI001','ENV002',20,10),(10,'CLI001','ENV007',5,3),(11,'CLI002','ENV001',20,10),(12,'CLI002','ENV003',50,20),(13,'CLI003','ENV004',70,30),(14,'CLI003','ENV007',10,5),(15,'CLI004','ENV001',25,15),(16,'CLI005','ENV002',30,20),(17,'CLI006','ENV003',40,20),(18,'CLI007','ENV004',50,30),(19,'CLI008','ENV005',0,30),(20,'CLI009','ENV006',40,20);
/*!40000 ALTER TABLE `stock_envase` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stock_producto`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock_producto` (
  `id` int NOT NULL AUTO_INCREMENT,
  `cliente_id` varchar(50) DEFAULT NULL,
  `presentacion_id` varchar(50) NOT NULL,
  `disponibles` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `cliente_id` (`cliente_id`,`presentacion_id`),
  KEY `presentacion_id` (`presentacion_id`),
  CONSTRAINT `stock_producto_ibfk_1` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`),
  CONSTRAINT `stock_producto_ibfk_2` FOREIGN KEY (`presentacion_id`) REFERENCES `presentaciones` (`id`),
  CONSTRAINT `chk_stock_producto_disponibles` CHECK ((`disponibles` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=92 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stock_producto`
--

LOCK TABLES `stock_producto` WRITE;
/*!40000 ALTER TABLE `stock_producto` DISABLE KEYS */;
INSERT INTO `stock_producto` VALUES (1,NULL,'PRE001',200),(2,NULL,'PRE002',150),(3,NULL,'PRE003',300),(4,NULL,'PRE004',250),(5,NULL,'PRE005',400),(6,NULL,'PRE006',0),(7,'CLI001','PRE001',30),(8,'CLI001','PRE002',15),(9,'CLI002','PRE001',40),(10,'CLI002','PRE003',18),(11,'CLI003','PRE004',60),(12,'CLI004','PRE001',35),(13,'CLI005','PRE002',25),(14,'CLI006','PRE003',40),(15,'CLI007','PRE004',50),(16,'CLI008','PRE005',60),(17,'CLI009','PRE006',30);
/*!40000 ALTER TABLE `stock_producto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tipos_cajon`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tipos_cajon` (
  `id` varchar(50) NOT NULL,
  `descripcion` varchar(100) NOT NULL,
  `capacidad` int NOT NULL,
  `codigo` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `tipos_cajon_chk_1` CHECK ((`capacidad` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tipos_cajon`
--

LOCK TABLES `tipos_cajon` WRITE;
/*!40000 ALTER TABLE `tipos_cajon` DISABLE KEYS */;
INSERT INTO `tipos_cajon` VALUES ('CAJ001','Cajon soda plastico 1.25 L',6,'CAJ-125P'),('CAJ002','Cajon soda vidrio 1 L',6,'CAJ-100V'),('CAJ003','Cajon soda plastico 500 ml',12,'CAJ-500P'),('CAJ004','Cajon soda vidrio 500 ml',12,'CAJ-500V');
/*!40000 ALTER TABLE `tipos_cajon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuarios`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `id` varchar(50) NOT NULL,
  `nombre_usuario` varchar(100) NOT NULL,
  `hash_contrasena` varchar(255) NOT NULL,
  `rol` varchar(30) NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `nombre_usuario` (`nombre_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios`
--

LOCK TABLES `usuarios` WRITE;
/*!40000 ALTER TABLE `usuarios` DISABLE KEYS */;
INSERT INTO `usuarios` VALUES ('prueba-001','usuario_prueba','wAJZfmox7dUUatlqxdIOKg==:ZgK7Av1KX3iTYyUiPq27II7IzMaWYP2RfasrGWPYzWA=','OPERARIO',0),('USR001','admin','1nmzXUb0YWPOrSq/+csX+Q==:yacHDBcgP/QEF1oBlpNrbUjNUZ8pQAOsCSFObPEOwOY=','ADMINISTRADOR',1),('USR002','gerente','b4bWw5oW/PM7gg3stwRMoA==:0P75w+KdR+tcZmHeyleiXfaELzxWBubwlkbXYliOwyY=','ADMINISTRADOR',0),('USR003','planta1','CX4vYye0srGhRNy8Rla7mA==:eBf/3hdlOL/kUmPk9usFPZHn4LDdgk4O9Uir4EtMeCQ=','OPERARIO',1),('USR004','planta2','OQ/uCJbDjFGarolVsCkYEw==:he9ahnL/v0/zb51S7SRZOmNiy4LSJDsmxd/mcUEy7d8=','OPERARIO',0),('USR005','repartidor1','nURccdE59tr358yadHAdvA==:ZEn9t0yNOGnYGs8YA+vi8hFnR11TAuJtEYnGHK8HYXw=','OPERARIO',0),('USR006','repartidor2','8aVZ5qruHaMHPh+wuT4Z6A==:9/EFbVOy4HAdrQDZ/RhOcY3uysZKBycna7y7ca1aS7c=','OPERARIO',0),('USR007','inactivo','kVoYRHzv3Bs3psJsdyNH+A==:Z0UuTfkkjLXk4bn43eWtS6jg4UrCjKtmZPXmDcPJNZE=','OPERARIO',0);
/*!40000 ALTER TABLE `usuarios` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Fin de la exportacion adaptada para demostracion.
