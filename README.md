# API de Gestión de Clientes

Esta aplicación es una API RESTful construida con Spring Boot para la gestión de clientes, integrando validaciones estrictas y conexión a bases de datos en la nube.

## 🛠️ Integración del Módulo de Clientes

Durante la implementación del módulo de clientes se desarrollaron las siguientes características clave para asegurar la integridad de los datos y cumplir con los requerimientos de QA:

### 1. Migración y Base de Datos (Neon Postgres)
*   Se configuró una base de datos **PostgreSQL** alojada en la nube (Neon).
*   Se implementó **Flyway** para el control de versiones de la base de datos, creando tablas para clientes, domicilios, cuentas, saldos, logins, datos biométricos y catálogos.
*   El catálogo de `NACIONALIDADES` se inserta automáticamente al levantar la aplicación mediante el script `V5__insert_nacionalidades.sql`.

### 2. Catálogos Dinámicos
*   Se desarrolló un `CatalogoController` (`/api/v1/catalogos/{tipo}`) que permite al frontend consultar los IDs válidos para campos que dependen de catálogos (ej. Nacionalidades). 
*   Esto asegura que el usuario (o el frontend) nunca tenga que "adivinar" el ID correcto.

### 3. Validaciones Estrictas (QA Proof)
*   **A nivel JSON (Jackson):** Se configuró una directiva estricta (`CoercionAction.Fail`) para evitar que campos numéricos (como `ingresoMensual`) se envíen entre comillas como texto, devolviendo un error 400 específico indicando que no debe ir entre comillas.
*   **Formato de Fechas:** Se estandarizó y validó el formato estricto `yyyy-MM-dd` para campos como `fechaNacimiento`, controlando cualquier desviación.
*   **Validaciones de Query Params y Path Variables:** Todos los endpoints de búsqueda (`/buscar/curp`, `/buscar/rfc`, etc.) y operaciones CRUD (`PUT /{curp}`, `DELETE /{curp}`) validan mediante expresiones regulares (`@Pattern`), `@NotBlank` y `@NotNull` que los datos ingresados tengan el formato exacto de CURP, RFC, Correo o Cuenta, previniendo el envío de datos en blanco o maliciosos.
*   **Identificadores Naturales (Security/UX):** Se eliminó la dependencia del `ID` interno de la base de datos en las URLs públicas. Las operaciones de actualización, búsqueda y eliminación ahora se realizan utilizando la **CURP**, ya que es el identificador natural que el usuario conoce, cumpliendo con la regla de diseño de no exponer IDs de base de datos.
*   **Campos Vacíos:** Se implementó una respuesta genérica y clara `[campo: mensaje]` cuando campos requeridos se dejan en blanco o rompen alguna regla de negocio.

### 4. Manejo Global de Excepciones
*   Un `GlobalExceptionHandler` unifica todas las respuestas de error.
*   Toda excepción (Validaciones Jakarta, errores de formato JSON, violaciones de constraints en parámetros) se intercepta y devuelve un objeto estandarizado con `status: 400`, un mensaje humano legible que identifica el campo exacto que falló, y un `timestamp`.

### 5. Despliegue Automatizado en Railway
*   Se incluyó un `Dockerfile` multi-etapa para construir el ejecutable localmente sin dependencia de la versión de Gradle de Railway (solucionando el error de `Unsupported Gradle version 9`).
*   Configuración automática de puertos inyectada por Railway mediante `railway.toml`.
*   La aplicación está expuesta públicamente con la documentación interactiva Swagger disponible.
