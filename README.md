# TP Final Sodería — Agua Martín

Sistema de gestión de escritorio para una fábrica de agua y soda retornable.

**Universidad Tecnológica Nacional (UTN) — Programación II — 2026**

## Objetivo

Centralizar la gestión de repartidores (clientes de la fábrica), pedidos, producción, inventario de envases y cajones, retiros de productos y cobros. El sistema registra el precio de llenado cobrado al repartidor y mantiene el stock separado por titular.

## Funcionalidades

| Módulo | Descripción |
| --- | --- |
| Acceso | Inicio y cierre de sesión, usuarios activos y permisos por rol. |
| Clientes | Alta, consulta, modificación y baja lógica. |
| Catálogo | Consulta de productos y presentaciones; actualización de precios por administrador. |
| Pedidos | Registro de pedidos con múltiples productos, cantidades y precios pactados. |
| Inventario | Recepción y devolución de vacíos, lavado, producción, movimientos de cajones y consulta de existencias. |
| Retiros y cobros | Retiros directos o asociados a pedidos, retiros parciales, cobros, autorizaciones de deuda y pagos posteriores. |
| Reportes | Consulta administrativa de envases, cajones e historial de cobros. |

Los medios de pago contemplados son efectivo, transferencia y tarjeta. Los pedidos conservan sus precios históricos aunque cambie el catálogo.

## Tecnologías y requisitos

- JDK 22.
- JavaFX 22.0.1 (Controls y FXML).
- Apache Maven (incluye Maven Wrapper).
- MySQL Server (probado con 9.0.1) y MySQL Connector/J 9.5.0.
- IntelliJ IDEA y MySQL Workbench recomendados.
- Git / GitHub Desktop para el trabajo colaborativo.

## Instalación y ejecución

### 1. Obtener el proyecto

Clonar el repositorio con GitHub Desktop o desde una terminal:

```bash
git clone https://github.com/MelanieMartinM/TpFinalSoderia.git
cd TpFinalSoderia
git switch develop
```

La rama `develop` contiene la versión integrada y probada al 25/11/2026. Abrir la carpeta del proyecto en IntelliJ IDEA, importar `pom.xml` como proyecto Maven y seleccionar JDK 22 como SDK del proyecto.

### 2. Preparar la base de datos

1. Iniciar MySQL Server y conectarse desde MySQL Workbench.
2. Abrir `sql/soderia_final.sql` y ejecutar el script completo.
3. Actualizar el panel Schemas.
4. Comprobar la instalación con:

```sql
SHOW TABLES FROM soderia_entrega;
```

El script crea el esquema `soderia_entrega` con 21 tablas y datos de demostración. Está pensado para una instalación nueva: no volver a ejecutarlo sobre un esquema existente ni sobre datos que se quieran conservar. No requiere importar `inventario.sql` ni utilizar `tpfinal_prueba`.

### 3. Configurar la conexión

En IntelliJ IDEA, entrar a **Run > Edit Configurations**, seleccionar o crear una configuración Application para la clase `ar.edu.utn.tpfinalsoderia.HelloApplication` y completar las variables de entorno:

| Variable | Valor de ejemplo |
| --- | --- |
| `SODERIA_DB_URL` | `jdbc:mysql://localhost:3306/soderia_entrega` |
| `SODERIA_DB_USUARIO` | Usuario local de MySQL |
| `SODERIA_DB_CLAVE` | Contraseña local de MySQL |

El usuario MySQL debe tener permisos de lectura y escritura sobre el esquema. No guardar ni publicar credenciales reales en GitHub. Las credenciales MySQL son distintas de las de acceso a la aplicación.

### 4. Ejecutar

Ejecutar `HelloApplication` desde IntelliJ IDEA con la configuración anterior.

Alternativamente, desde PowerShell, en la raíz del proyecto:

```powershell
$env:SODERIA_DB_URL = 'jdbc:mysql://localhost:3306/soderia_entrega'
$env:SODERIA_DB_USUARIO = Read-Host 'Usuario MySQL'
$env:SODERIA_DB_CLAVE = Read-Host 'Contraseña MySQL'
.\mvnw.cmd clean javafx:run
```

Esta alternativa usa el plugin JavaFX configurado en el POM. Mantener el servidor MySQL activo mientras se utiliza la aplicación.

## Usuarios de demostración

| Usuario | Contraseña de demostración | Rol |
| --- | --- | --- |
| `admin` | `DemoAdmin2026!` | ADMINISTRADOR |
| `planta1` | `DemoPlanta2026!` | OPERARIO |

Estas cuentas corresponden al SQL de demostración y no deben utilizarse en producción. Las contraseñas se almacenan como hashes PBKDF2 con salt, no como texto plano.

El administrador dispone de operaciones de gestión y acceso a Reportes. El operario puede consultar clientes y catálogo, y realizar las operaciones habilitadas de pedidos, inventario y retiros/cobros; no puede actualizar precios, gestionar clientes ni autorizar retiros sin pago.

## Arquitectura

El proyecto separa la interfaz gráfica, la lógica de negocio y el acceso a datos.

```text
JavaFX / FXML
      ↓
Controllers
      ↓
Servicios
      ↓
Repositorios JDBC / DAO
      ↓
MySQL
```

Estructura principal:

```text
TpFinalSoderia/
├── pom.xml
├── mvnw.cmd
├── sql/
│   └── soderia_final.sql
└── src/main/
    ├── java/
    │   ├── module-info.java
    │   └── ar/edu/utn/tpfinalsoderia/
    │       ├── HelloApplication.java
    │       ├── controllers/
    │       ├── services/
    │       ├── DAO/
    │       ├── models/
    │       ├── interfaces/
    │       ├── enums/
    │       ├── exceptions/
    │       └── utils/
    └── resources/
        └── ar/edu/utn/tpfinalsoderia/
```

Conceptos de Programación II aplicados: encapsulamiento, herencia (`Persona`, `Cliente`, `Empleado`), interfaces (`IRepositorio<T>`, `Retornable`, `Facturable`), genericidad (`RepositorioJdbc<T>`), colecciones, enumeraciones, excepciones personalizadas, JDBC con consultas parametrizadas y transacciones con commit y rollback.

## Flujo de trabajo

1. Seleccionar un cliente y consultar el catálogo.
2. Registrar envases vacíos, lavado y producción cuando corresponda.
3. Crear un pedido o registrar un retiro directo.
4. Validar existencias y registrar el retiro y su cobro.
5. Consultar saldos, movimientos y reportes según el rol.

## Estado y alcance

Se realizaron pruebas manuales de acceso por roles, clientes, precios, pedidos, retiros parciales, cobros, deuda autorizada, rollback por stock insuficiente, producción e inventario. Se importaron las 21 tablas y se comprobó el acceso a las pantallas habilitadas para ambos usuarios después de integrar las correcciones en `develop` (PR #16).

La versión es una demostración académica. No se realizó una validación exhaustiva de concurrencia, marcas privadas ni compatibilidad entre todos los tipos de cajones y presentaciones. Las tablas de contratos existen, pero no se incluye una pantalla independiente de gestión de contratos. El instalador de escritorio no forma parte de esta versión.

## Equipo y repositorio

Trabajo práctico grupal de Programación II, UTN. Repositorio: [TpFinalSoderia](https://github.com/MelanieMartinM/TpFinalSoderia).

- `main`: rama destinada a la entrega.
- `develop`: integración del equipo.
- Ramas de funcionalidades y correcciones: desarrollo y revisión mediante pull requests.
