# TiendaApp - REST API con Autenticación JWT y Control de Roles

**TiendaApp** es la API REST de una tienda en línea donde administradores (**ADMIN**), vendedores (**VENDEDOR**) y clientes (**CLIENTE**) interactúan con un catálogo de productos y la gestión de pedidos. 

Toda la seguridad del sistema está implementada **manualmente** mediante un filtro HTTP (`OncePerRequestFilter`), un interceptor de Spring (`HandlerInterceptor`) y una anotación personalizada (`@RequiresRole`), cumpliendo con el estándar sin el uso del starter completo de Spring Security.

---

## 📌 Tabla de Contenidos
1. [Estructura del Proyecto](#estructura-del-proyecto)
2. [Requisitos Técnicos](#requisitos-técnicos)
3. [Credenciales Iniciales](#credenciales-iniciales)
4. [Configuración de Base de Datos](#configuración-de-base-de-datos)
5. [Variables de Entorno](#variables-de-entorno)
6. [Instrucciones de Ejecución](#instrucciones-de-ejecución)
7. [Matriz de Endpoints y Permisos](#matriz-de-endpoints-y-permisos)
8. [Colección de Postman](#colección-de-postman)
9. [Evidencias de Evaluación (Casos de Prueba)](#evidencias-de-evaluación)
10. [Retos Extra Implementados (Bonificación)](#retos-extra-implementados)

---

## 📁 Estructura del Proyecto

El proyecto sigue estrictamente la estructura de paquetes obligatoria bajo `com.tunombre.tienda`:

```
com.tunombre.tienda
├── config
│   ├── AppConfig.java            # Bean BCryptPasswordEncoder
│   ├── FilterConfig.java         # Registro del filtro con FilterRegistrationBean
│   └── WebConfig.java            # Registro de RoleInterceptor en WebMvcConfigurer
├── controller
│   ├── AuthController.java       # Registro, login y refresh token
│   ├── UsersController.java      # Perfil me y gestión de usuarios
│   ├── CategoriesController.java # Listado, creación y borrado de categorías
│   ├── ProductsController.java   # Catálogo público y gestión de productos
│   └── OrdersController.java     # Creación, consulta y cancelación de pedidos
├── dto
│   ├── HttpGlobalResponseDTO.java# Wrapper global { message, data }
│   ├── RegisterRequestDTO.java
│   ├── LoginRequestDTO.java
│   ├── AuthResponseDTO.java
│   ├── RefreshTokenResponseDTO.java
│   ├── UserResponseDTO.java
│   ├── UserCreateRequestDTO.java
│   ├── UserRoleUpdateRequestDTO.java
│   ├── UserStatusUpdateRequestDTO.java
│   ├── CategoryRequestDTO.java
│   ├── CategoryResponseDTO.java
│   ├── ProductRequestDTO.java
│   ├── ProductResponseDTO.java
│   ├── OrderCreateRequestDTO.java
│   ├── OrderStatusUpdateRequestDTO.java
│   └── OrderResponseDTO.java
├── entity
│   ├── Roles.java
│   ├── Users.java
│   ├── Categories.java
│   ├── Products.java
│   └── Orders.java
├── enums
│   ├── RoleEnum.java             # ADMIN=1, CLIENTE=2, VENDEDOR=3
│   └── OrderStatusEnum.java      # PENDIENTE, ENVIADO, ENTREGADO, CANCELADO
├── exception
│   ├── BadRequestException.java       # HTTP 400
│   ├── UnauthorizedException.java     # HTTP 401
│   ├── ForbiddenException.java        # HTTP 403
│   ├── ResourceNotFoundException.java # HTTP 404
│   ├── ConflictException.java         # HTTP 409
│   └── GlobalExceptionHandler.java    # @RestControllerAdvice centralizado
├── filter
│   └── JwtValidationFilter.java  # OncePerRequestFilter para validación de JWT
├── repository
│   ├── RolesRepository.java
│   ├── UsersRepository.java
│   ├── CategoriesRepository.java
│   ├── ProductsRepository.java
│   └── OrdersRepository.java
├── security
│   ├── RequiresRole.java         # Anotación a nivel METHOD y TYPE
│   └── RoleInterceptor.java      # HandlerInterceptor para autorización por rol
└── service
    ├── JwtService.java           # Generación, validación y refresh de tokens
    ├── AuthService.java          # Lógica de login, register y refresh
    ├── UsersService.java
    ├── CategoriesService.java
    ├── ProductsService.java
    └── OrdersService.java
```

---

## 🛠 Requisitos Técnicos

| Elemento | Requisito |
| :--- | :--- |
| **Lenguaje** | Java 17 o 21 |
| **Framework** | Spring Boot 3.x / 4.x (`spring-boot-starter-webmvc`, `spring-boot-starter-data-jpa`) |
| **Base de Datos** | MySQL 8.0+ |
| **JWT** | `io.jsonwebtoken` (`jjwt-api`, `jjwt-impl`, `jjwt-jackson` versión `0.12.5`) |
| **Cifrado** | `spring-security-crypto` (únicamente `BCryptPasswordEncoder`) |
| **Validaciones** | `spring-boot-starter-validation` (Jakarta Validation) |
| **Puerto y Contexto** | Puerto `9090` y Context Path `/api/v1/tienda` |

---

## 🔑 Credenciales Iniciales

El script de base de datos crea de forma automática el usuario administrador inicial con su contraseña cifrada con BCrypt:

* **Usuario:** `admin` (o por email: `admin@tienda.com`)
* **Contraseña en texto plano:** `admin123`
* **Hash en base de datos:** `$2a$10$837EsTYqKPpse.zc0qSMFOAouMxW5bd4PUIBleci8mptvopRYO0HG`
* **Rol:** `ADMIN` (rol_id: 1)
* **Estado:** `active: true`

---

## 🗄 Configuración de Base de Datos

En la raíz del proyecto se encuentra el script [`tienda.sql`](tienda.sql). Para crearlo en tu servidor MySQL:

```bash
mysql -u root -p < tienda.sql
```

O desde MySQL Workbench / CLI:
```sql
CREATE SCHEMA IF NOT EXISTS tienda_db;
USE tienda_db;
-- Ejecutar el contenido de tienda.sql
```

El script crea las tablas `roles`, `users`, `categories`, `products` y `orders`, e inserta los tres roles (`ADMIN`, `CLIENTE`, `VENDEDOR`) y el usuario inicial `admin`.

---

## ⚙️ Variables de Entorno

En [`src/main/resources/application.yaml`](src/main/resources/application.yaml) se configuran los valores requeridos, los cuales se leen de variables de entorno con valores por defecto listos para desarrollo local:

| Variable | Descripción | Valor por Defecto |
| :--- | :--- | :--- |
| `DB_HOST` | Host de MySQL | `localhost` |
| `DB_PORT` | Puerto de MySQL | `3306` |
| `DB_NAME` | Nombre de la base de datos | `tienda_db` |
| `DB_USER` | Usuario de MySQL | `root` |
| `DB_PASSWORD` | Contraseña de MySQL | `paulo3144615` (o configurar la propia) |
| `JWT_SECRET` | Clave secreta firmada en Base64 (256 bits) | `BEpJyFGQqWmobS4yY2fpHaDWxRQVq04OMd0CsMdG1Eo=` |
| `JWT_EXPIRATION` | Tiempo de expiración del token en ms | `600000` (10 minutos) |

### Ejemplo para configurar variables en PowerShell:
```powershell
$env:DB_USER = "root"
$env:DB_PASSWORD = "tu_password_aqui"
$env:JWT_SECRET = "BEpJyFGQqWmobS4yY2fpHaDWxRQVq04OMd0CsMdG1Eo="
$env:JWT_EXPIRATION = "600000"
```

---

## 🚀 Instrucciones de Ejecución

### 1. Compilar el proyecto
```powershell
.\mvnw.cmd clean compile
```

### 2. Ejecutar las pruebas unitarias
```powershell
.\mvnw.cmd test
```

### 3. Iniciar la aplicación
```powershell
.\mvnw.cmd spring-boot:run
```
La aplicación iniciará en:
`http://localhost:9090/api/v1/tienda`

---

## 🌐 Matriz de Endpoints y Permisos

Todas las rutas son relativas a `http://localhost:9090/api/v1/tienda`.
Todas las respuestas devuelven la estructura `HttpGlobalResponseDTO<T>` con `message` y `data`.

### Autenticación (Público)
| Método | Endpoint | Roles | Descripción |
| :--- | :--- | :--- | :--- |
| `POST` | `/auth/register` | Público | Registra usuario (rol CLIENTE siempre). Si usuario/email se repite → 409 |
| `POST` | `/auth/login` | Público | Recibe `user` y `password`. Devuelve `token` y datos de usuario. Credenciales malas → 401 |
| `POST` | `/auth/refreshtoken` | Público/Token | Recibe token en `Authorization: Bearer <token>` y devuelve un nuevo JWT firmado |

### Usuarios
| Método | Endpoint | Roles | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/users/me` | ADMIN, CLIENTE, VENDEDOR | Retorna el perfil del usuario dueño del token (usando `userId`) |
| `GET` | `/users` | ADMIN | Lista todos los usuarios registrados |
| `POST` | `/users` | ADMIN | Crea usuario con cualquier rol (`roleId`). Duplicados → 409 |
| `PATCH` | `/users/{id}/role` | ADMIN | Cambia el rol del usuario especificado |
| `PATCH` | `/users/{id}/status` | ADMIN | Activa o desactiva un usuario (`active: boolean`) |

### Categorías
| Método | Endpoint | Roles | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/categories` | ADMIN, CLIENTE, VENDEDOR | Lista todas las categorías |
| `POST` | `/categories` | ADMIN | Crea categoría. Si el nombre ya existe → 409 |
| `DELETE`| `/categories/{id}` | ADMIN | Elimina categoría si no tiene productos asociados; si tiene → 409 |

### Productos
| Método | Endpoint | Roles | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/products` | Público | Catálogo con filtro opcional `?categoryId=` y paginación `?page=&size=` |
| `GET` | `/products/{id}` | Público | Detalle de un producto por ID |
| `GET` | `/products/mine` | VENDEDOR | Productos del vendedor autenticado (obtenido del token) |
| `POST` | `/products` | VENDEDOR | Publica producto (`seller_id` se extrae del token, no del body) |
| `PUT` | `/products/{id}` | VENDEDOR, ADMIN | Edita producto. VENDEDOR solo puede si es el dueño; de lo contrario → 403 |
| `DELETE`| `/products/{id}` | VENDEDOR, ADMIN | Elimina producto. Misma regla de propiedad (VENDEDOR solo si es dueño) → 403 |

### Pedidos
| Método | Endpoint | Roles | Descripción |
| :--- | :--- | :--- | :--- |
| `POST` | `/orders` | CLIENTE | Crea pedido con `productId` y `quantity`. Total calculado en el servidor. Descuenta stock. Sin stock → 400 |
| `GET` | `/orders/mine` | CLIENTE | Pedidos realizados por el cliente autenticado |
| `GET` | `/orders/sales` | VENDEDOR | Pedidos sobre productos pertenecientes al vendedor autenticado |
| `GET` | `/orders` | ADMIN | Listado de todos los pedidos en la tienda |
| `PATCH` | `/orders/{id}/status` | VENDEDOR, ADMIN | Actualiza estado (PENDIENTE, ENVIADO, ENTREGADO, CANCELADO). VENDEDOR solo en pedidos de sus productos → 403 |
| `PATCH` | `/orders/{id}/cancel` | CLIENTE | Cancela pedido propio solo si está en PENDIENTE; devuelve el stock. Si no está en PENDIENTE → 400 |

---

## 📮 Colección de Postman

El archivo [`TiendaApp.postman_collection.json`](TiendaApp.postman_collection.json) se encuentra en la raíz del proyecto listo para importar en Postman o Thunder Client.

### Características de la colección:
* Variables de colección automáticas: `baseUrl`, `token`, `adminToken`, `clientToken`, `sellerToken`, etc.
* Scripts en tests de los logins que capturan y almacenan el JWT automáticamente para las peticiones subsecuentes.
* Carpetas organizadas con una petición por cada endpoint de la API.
* Carpeta especial **"6. Casos de Prueba (Evidencias de Evaluación)"** con las peticiones exactas para demostrar los códigos HTTP requeridos en la rúbrica.

---

## 📸 Evidencias de Evaluación

A continuación se documentan las respuestas reales del servidor obtenidas durante las pruebas automatizadas:

### 1. Login Correcto (HTTP 200)
**Petición:** `POST /api/v1/tienda/auth/login`
```json
{
  "user": "admin",
  "password": "admin123"
}
```
**Respuesta:**
```json
{
  "message": "Autenticación exitosa",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJJZCI6MSwicm9sSWQiOjEsImlhdCI6MTc5MzQxNDcwMywiZXhwIjoxNzkzNDE1MzAzfQ.Bg16LgATdcu3bBAOH_8sa70z_99m5t2CF5b0wU1xPOc",
    "user": {
      "id": 1,
      "username": "admin",
      "email": "admin@tienda.com",
      "active": true,
      "roleId": 1,
      "role": "ADMIN"
    }
  }
}
```

---

### 2. Token Ausente (HTTP 401)
**Petición:** `GET /api/v1/tienda/users/me` (Sin encabezado `Authorization`)
**Respuesta:**
```json
{
  "message": "Token de autorización ausente o formato inválido",
  "data": null
}
```

---

### 3. Token Expirado o Inválido (HTTP 401)
**Petición:** `GET /api/v1/tienda/users/me` (`Authorization: Bearer token_invalido_o_expirado`)
**Respuesta:**
```json
{
  "message": "Token inválido o expirado",
  "data": null
}
```

---

### 4. Rol No Permitido (HTTP 403)
**Petición:** `GET /api/v1/tienda/users` (Con token de rol `CLIENTE`, endpoint restringido a `@RequiresRole(RoleEnum.ADMIN)`)
**Respuesta:**
```json
{
  "message": "Acceso denegado: no posees el rol requerido para realizar esta acción",
  "data": null
}
```

---

### 5. Vendedor Editando Producto Ajeno (HTTP 403)
**Petición:** `PUT /api/v1/tienda/products/1` (Con token de `vendedor2`, intentando modificar el producto creado por `vendedor1`)
```json
{
  "name": "Laptop Hackeada",
  "description": "Intento no autorizado",
  "price": 999.00,
  "stock": 2,
  "categoryId": 1
}
```
**Respuesta:**
```json
{
  "message": "No tienes permisos para modificar un producto que no te pertenece",
  "data": null
}
```

---

### 6. Pedido Sin Stock Suficiente (HTTP 400)
**Petición:** `POST /api/v1/tienda/orders` (Producto con stock disponible de 5, solicitando 99 unidades)
```json
{
  "productId": 1,
  "quantity": 99
}
```
**Respuesta:**
```json
{
  "message": "Stock insuficiente para el producto 'Laptop Gamer'. Stock actual: 5",
  "data": null
}
```

---

### 7. Conflicto por Registro Duplicado (HTTP 409)
**Petición:** `POST /api/v1/tienda/auth/register` (Intentando registrar un username o email existente)
```json
{
  "username": "juan_cliente",
  "email": "juan@tienda.com",
  "password": "password123"
}
```
**Respuesta:**
```json
{
  "message": "El nombre de usuario 'juan_cliente' ya está registrado",
  "data": null
}
```

---

### 8. Conflicto al Eliminar Categoría con Productos Asociados (HTTP 409)
**Petición:** `DELETE /api/v1/tienda/categories/1` (Categoría que posee productos activos en catálogo)
**Respuesta:**
```json
{
  "message": "No es posible eliminar la categoría porque tiene productos asociados",
  "data": null
}
```

---

## 🌟 Retos Extra Implementados (Bonificación)

1. **Validación de DTOs con Spring Validation:**
   Todos los DTOs de entrada implementan anotaciones `@NotBlank`, `@Email`, `@Positive`, `@Min`, `@Size`. Si algún dato es inválido, el servidor responde automáticamente con HTTP 400 y el detalle de los campos con error.
2. **Centralización con `@RestControllerAdvice`:**
   Se implementó `GlobalExceptionHandler` capturando excepciones específicas (`BadRequestException`, `UnauthorizedException`, `ForbiddenException`, `ResourceNotFoundException`, `ConflictException`, `MethodArgumentNotValidException`) eliminando bloques repetitivos de `try/catch` en controladores.
3. **Soporte de `@RequiresRole` a nivel de Clase:**
   La anotación `@RequiresRole` soporta `ElementType.METHOD` y `ElementType.TYPE`. `RoleInterceptor` verifica primero el método y, en caso de no estar anotado, inspecciona la anotación a nivel de clase controladora.
4. **Paginación en Catálogo de Productos:**
   El endpoint `GET /api/v1/tienda/products` soporta paginación estándar mediante parámetros opcionales `?page=0&size=10&categoryId=1`, devolviendo un objeto `Page<ProductResponseDTO>` completo.
