# SOSPrimeraPractica
SOSPrimeraPractica
Perfecto. Con base en el documento **"Práctica RESTful 2025"**, te explico cómo podéis **dividir el trabajo**, qué **requisitos necesitáis** y os doy una **guía paso a paso** para hacerla bien organizada.

---

## ✅ División del trabajo (2 personas sugerido)

**1. DISEÑO del servicio RESTful (Persona A)**  
Encargado de definir los recursos, URIs, operaciones HTTP, cuerpos JSON y esquemas.
- Modelo de recursos: usuarios, libros, préstamos.
- JSON Schema de cada entidad.
- Tabla resumen con URIs, métodos, parámetros, respuestas esperadas.
- Documentación en la memoria.

**2. IMPLEMENTACIÓN con Spring Boot (Persona B)**  
Encargado del código backend, base de datos y endpoints.
- Proyecto Spring Boot (crear con `spring initializr`).
- Entidades Java, repositorios JPA, controladores REST.
- Conexión con PostgreSQL.
- Validaciones y excepciones.

**3. Cliente Java + Postman (ambos)**  
Ambos probáis la API:
- Persona A: pruebas con Postman, capturas y análisis.
- Persona B: desarrollo del cliente Java para probar todos los endpoints automáticamente.

**4. MEMORIA y ENTREGA (ambos)**  
Redactan el PDF juntos:
- Diseño del servicio (persona A).
- Diagrama E/R y código Java (persona B).
- Capturas de pruebas (ambos).
- Incluir `.jar`, código fuente (`src`, `pom.xml`) y cliente Java en ZIPs.

---

## 🧩 Requisitos necesarios

### Técnicos:
- Java 17+.
- Spring Boot 3.x.
- PostgreSQL.
- Postman o REST Client.
- Maven.
- IDE (IntelliJ, Eclipse o VSCode con soporte Spring).

### Software que podéis usar:
- **PostgreSQL** como BBDD.
- **pgAdmin** para gestionar la base de datos.
- **Lombok** para simplificar getters/setters.
- **Spring Data JPA** para acceso a datos.

---

## 🛠️ Guía paso a paso para hacer la práctica

### 🌐 1. Diseño RESTful
- Crear modelos de recursos:
  - `Usuario`: username, matricula, nacimiento, correo.
  - `Libro`: título, autor, edición, ISBN, editorial, disponible.
  - `Préstamo`: usuario, libro, fechas de préstamo/devolución, estado, sanciones.

- Identificar URIs:
```plaintext
GET     /usuarios
POST    /usuarios
GET     /usuarios/{id}
PUT     /usuarios/{id}
DELETE  /usuarios/{id}

GET     /libros?titulo=rest&disponible=true
POST    /libros
PUT     /libros/{id}
DELETE  /libros/{id}

POST    /prestamos
PUT     /prestamos/{id}/devolver
PUT     /prestamos/{id}/ampliar
GET     /usuarios/{id}/prestamos?desde=2024-01-01
GET     /usuarios/{id}/historial
GET     /usuarios/{id}/actividad
```

- Añadir paginación y HATEOAS (en las listas: `?page=1&size=10` y enlaces `self`, `next`, `prev`).

---

### ⚙️ 2. Backend con Spring Boot
1. Crear proyecto con Spring Initializr:
   - Dependencias: Spring Web, Spring Data JPA, PostgreSQL Driver, Lombok.

2. Crear entidades (`Usuario`, `Libro`, `Prestamo`) y relaciones.

3. Crear repositorios con JPA (`UsuarioRepository`, etc.).

4. Crear servicios y controladores (`UsuarioController`, etc.).

5. Implementar:
   - Validaciones.
   - Paginación.
   - Filtros (título, disponibilidad, fechas).
   - Códigos HTTP correctos (200, 201, 404, 400, 403...).
   - Sanciones por retraso en devoluciones.

6. Crear el diagrama E/R (puede usarse dbdiagram.io o dibujarlo en la memoria).

---

### 🧪 3. Cliente Java y pruebas
- Crear un cliente Java con `HttpClient` o `RestTemplate` que:
  - Llame a todos los endpoints.
  - Muestre respuestas y códigos HTTP.
- Probar manualmente con Postman:
  - Capturas de respuestas correctas y errores (401, 403, 404...).

---

### 📦 4. Entrega y estructura
Subir a Moodle:
1. **Memoria en PDF**:
   - Diseño RESTful, JSON schemas.
   - Diagrama entidad-relación.
   - Capturas de Postman y cliente.
   - Tabla resumen de endpoints.

2. **JAR del proyecto (generado con Maven)**.

3. **ZIP del backend** con:
   - `/src`
   - `pom.xml`, `mvnw`, `mvnw.cmd`

4. **ZIP del cliente Java**.

---

## 🧠 Consejos finales
- Usa Postman desde el primer día para ir testeando cada endpoint.
- Haz commits frecuentes en Git.
- Documenta TODO en la memoria con capturas.
- Valida bien los errores para no perder puntos por falta de respuestas tipo `404`, `400`, `403`.

---

### ✅ **Resumen corto**
Divídanse: una persona diseña la API y documenta; la otra implementa en Spring Boot y crea el cliente. Usen PostgreSQL y validen todo con Postman. Entreguen memoria PDF, JAR, código fuente y cliente Java en ZIP.
