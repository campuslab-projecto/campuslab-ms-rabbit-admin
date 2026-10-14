# campuslab-ms-rabbit-admin

Microservicio Spring Boot encargado de administrar recursos RabbitMQ mediante una API REST.

Este componente forma parte del proyecto **CampusLab** y permite crear y eliminar colas, exchanges y bindings de RabbitMQ, además de validar parámetros de entrada y manejar errores de forma centralizada.

---

## Tecnologías utilizadas

- Java 17
- Spring Boot
- Spring Web
- Spring AMQP
- Jakarta Validation
- RabbitMQ
- Docker
- Docker Compose
- Gradle

---

# Configuración general

## Puerto del microservicio

El microservicio utiliza el puerto:

```text
8082
```

## URL base

Para pruebas locales:

```text
http://localhost:8082
```

Por lo tanto, todos los endpoints administrativos parten desde:

```text
http://localhost:8082/api/rabbit
```

---

# Configuración de RabbitMQ

El microservicio se conecta a RabbitMQ utilizando las siguientes variables de entorno:

```text
RABBITMQ_HOST
RABBITMQ_PORT
RABBITMQ_USERNAME
RABBITMQ_PASSWORD
```

La configuración utilizada en `application.properties` es:

```properties
server.port=8082

spring.application.name=ms-campuslab-rabbit-admin

spring.rabbitmq.host=${RABBITMQ_HOST:localhost}
spring.rabbitmq.port=${RABBITMQ_PORT:5672}
spring.rabbitmq.username=${RABBITMQ_USERNAME:campuslab}
spring.rabbitmq.password=${RABBITMQ_PASSWORD:campuslab123}
```

En ejecución local sin Docker se utilizan por defecto:

```text
Host: localhost
Puerto AMQP: 5672
Usuario: campuslab
Contraseña: campuslab123
```

Cuando el servicio se ejecuta mediante Docker Compose, el host utilizado es:

```text
rabbitmq
```

porque ambos contenedores se encuentran dentro de la misma red de Docker.

---

# API REST

La API permite administrar recursos RabbitMQ mediante endpoints REST.

URL base:

```text
http://localhost:8082/api/rabbit
```

---

# Colas

## Crear una cola

Método:

```http
POST /api/rabbit/queues
```

URL completa:

```text
http://localhost:8082/api/rabbit/queues
```

Body:

```json
{
  "name": "q.prueba.campuslab",
  "durable": true
}
```

Respuesta esperada:

```text
200 OK
Cola creada correctamente
```

---

## Eliminar una cola

Método:

```http
DELETE /api/rabbit/queues/{name}
```

Ejemplo:

```text
http://localhost:8082/api/rabbit/queues/q.prueba.campuslab
```

Respuesta esperada:

```text
200 OK
Cola eliminada correctamente
```

Si la cola no existe, el servicio puede responder:

```text
404 Not Found
```

---

# Exchanges

## Crear un exchange

Método:

```http
POST /api/rabbit/exchanges
```

URL completa:

```text
http://localhost:8082/api/rabbit/exchanges
```

Body:

```json
{
  "name": "ex.prueba.campuslab",
  "type": "direct"
}
```

Tipos de exchange soportados:

```text
direct
topic
fanout
```

Respuesta esperada:

```text
200 OK
Exchange creado correctamente
```

---

## Eliminar un exchange

Método:

```http
DELETE /api/rabbit/exchanges/{name}
```

Ejemplo:

```text
http://localhost:8082/api/rabbit/exchanges/ex.prueba.campuslab
```

Respuesta esperada:

```text
200 OK
Exchange eliminado correctamente
```

---

# Bindings

## Crear un binding

Método:

```http
POST /api/rabbit/bindings
```

URL completa:

```text
http://localhost:8082/api/rabbit/bindings
```

Body:

```json
{
  "queueName": "q.prueba.campuslab",
  "exchangeName": "ex.prueba.campuslab",
  "routingKey": "prueba.route"
}
```

Respuesta esperada:

```text
200 OK
Binding creado correctamente
```

Para crear un binding correctamente deben existir previamente:

1. La cola.
2. El exchange.

Orden recomendado:

```text
Crear cola
    ↓
Crear exchange
    ↓
Crear binding
```

---

## Eliminar un binding

Método:

```http
DELETE /api/rabbit/bindings
```

URL completa:

```text
http://localhost:8082/api/rabbit/bindings
```

Body:

```json
{
  "queueName": "q.prueba.campuslab",
  "exchangeName": "ex.prueba.campuslab",
  "routingKey": "prueba.route"
}
```

Respuesta esperada:

```text
200 OK
Binding eliminado correctamente
```

---

# Validaciones

El microservicio utiliza DTOs junto con **Jakarta Validation** para impedir parámetros inválidos.

Por ejemplo:

```java
@NotBlank(message = "El nombre de la cola es obligatorio")
```

---

## Validación de una cola con nombre vacío

Request:

```http
POST http://localhost:8082/api/rabbit/queues
```

Body inválido:

```json
{
  "name": "",
  "durable": true
}
```

Respuesta:

```text
400 Bad Request
```

Esto impide crear colas con nombres vacíos.

---

## Validación del tipo de exchange

Request:

```http
POST http://localhost:8082/api/rabbit/exchanges
```

Body inválido:

```json
{
  "name": "ex.invalido",
  "type": "cualquier-cosa"
}
```

Respuesta:

```json
{
  "error": "Bad Request",
  "message": "Tipo de exchange no soportado. Use direct, topic o fanout",
  "status": 400
}
```

Los únicos tipos válidos son:

```text
direct
topic
fanout
```

---

# Manejo de errores

El manejo de errores se centraliza mediante:

```text
GlobalExceptionHandler
```

Esta clase utiliza:

```java
@RestControllerAdvice
```

y permite transformar errores internos en respuestas HTTP claras.

Por ejemplo, un tipo de exchange inválido genera:

```text
400 Bad Request
```

en lugar de:

```text
500 Internal Server Error
```

Ejemplo de respuesta:

```json
{
  "error": "Bad Request",
  "message": "Tipo de exchange no soportado. Use direct, topic o fanout",
  "status": 400
}
```

---

# Arquitectura interna

El microservicio está separado en capas.

La lógica de administración RabbitMQ se encuentra encapsulada en:

```text
RabbitAdminService
```

Mientras que los endpoints REST se encuentran en:

```text
RabbitAdminController
```

Los datos de entrada se representan mediante DTOs:

```text
QueueRequest
ExchangeRequest
BindingRequest
```

La configuración de RabbitMQ se encuentra en:

```text
RabbitAdminConfig
```

Y el manejo centralizado de errores en:

```text
GlobalExceptionHandler
```

---

# Estructura del proyecto

```text
campuslab-ms-rabbit-admin
│
├── src
│   ├── main
│   │   ├── java
│   │   │   └── cl
│   │   │       └── duoc
│   │   │           └── campuslab
│   │   │               └── mscampuslabrabbitadmin
│   │   │                   │
│   │   │                   ├── config
│   │   │                   │   └── RabbitAdminConfig.java
│   │   │                   │
│   │   │                   ├── controller
│   │   │                   │   └── RabbitAdminController.java
│   │   │                   │
│   │   │                   ├── dto
│   │   │                   │   ├── QueueRequest.java
│   │   │                   │   ├── ExchangeRequest.java
│   │   │                   │   └── BindingRequest.java
│   │   │                   │
│   │   │                   ├── exception
│   │   │                   │   └── GlobalExceptionHandler.java
│   │   │                   │
│   │   │                   ├── service
│   │   │                   │   └── RabbitAdminService.java
│   │   │                   │
│   │   │                   └── CampuslabMsRabbitAdminApplication.java
│   │   │
│   │   └── resources
│   │       └── application.properties
│   │
│   └── test
│
├── gradle
│   └── wrapper
│
├── Dockerfile
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
├── .gitignore
└── README.md
```

---

# Compilación

El proyecto utiliza Gradle.

La versión utilizada para el proyecto es:

```text
Gradle 8.5
```

Java:

```text
Java 17
```

## Compilar en Windows

```powershell
.\gradlew.bat clean build
```

Resultado esperado:

```text
BUILD SUCCESSFUL
```

El archivo JAR generado se encuentra en:

```text
build/libs/ms-campuslab-rabbit-admin-0.0.1-SNAPSHOT.jar
```

---

# Docker

El proyecto incluye un archivo:

```text
Dockerfile
```

Contenido:

```dockerfile
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY build/libs/ms-campuslab-rabbit-admin-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## Construir la imagen Docker

Ejecutar desde la raíz del proyecto:

```powershell
docker build -t lukmezac/campuslab-ms-rabbit-admin:latest .
```

Imagen generada:

```text
lukmezac/campuslab-ms-rabbit-admin:latest
```

---

# Docker Compose

El microservicio se integra con la infraestructura general de CampusLab mediante Docker Compose.

Configuración utilizada:

```yaml
campuslab-ms-rabbit-admin:
  image: lukmezac/campuslab-ms-rabbit-admin:latest
  container_name: campuslab-ms-rabbit-admin
  ports:
    - "8082:8082"
  environment:
    RABBITMQ_HOST: rabbitmq
    RABBITMQ_PORT: 5672
    RABBITMQ_USERNAME: campuslab
    RABBITMQ_PASSWORD: campuslab123
  depends_on:
    - rabbitmq
```

---

# RabbitMQ utilizado en CampusLab

RabbitMQ utiliza los siguientes puertos:

| Servicio | Puerto |
|---|---:|
| RabbitMQ AMQP | 5672 |
| RabbitMQ Management | 15672 |
| Rabbit Admin API | 8082 |

RabbitMQ Management se puede abrir localmente en:

```text
http://localhost:15672
```

Credenciales utilizadas:

```text
Usuario: campuslab
Contraseña: campuslab123
```

---

# Servicios Docker del proyecto

La infraestructura local de CampusLab utiliza actualmente:

```text
campuslab-rabbitmq
campuslab-ms-bookings
campuslab-ms-rabbit-admin
campuslab-bff
```

Puertos:

| Servicio | Puerto |
|---|---:|
| campuslab-bff | 8080 |
| campuslab-ms-bookings | 8081 |
| campuslab-ms-rabbit-admin | 8082 |
| RabbitMQ AMQP | 5672 |
| RabbitMQ Management | 15672 |

---

# Ejecución mediante Docker Compose

Desde el repositorio de infraestructura:

```powershell
docker compose up -d
```

Para comprobar los contenedores:

```powershell
docker ps
```

Ejemplo de servicios esperados:

```text
campuslab-rabbitmq
campuslab-ms-bookings
campuslab-ms-rabbit-admin
campuslab-bff
```

---

# Pruebas realizadas

Las pruebas se realizaron utilizando:

- Postman
- RabbitMQ Management
- Docker
- Docker Compose

Se verificaron correctamente las siguientes operaciones:

```text
Crear cola
Crear exchange
Crear binding
Eliminar binding
Eliminar cola
Eliminar exchange
```

También se verificaron las validaciones:

```text
Nombre de cola vacío
        ↓
400 Bad Request
```

y:

```text
Tipo de exchange inválido
        ↓
400 Bad Request
```

---

# Secuencia de prueba en Postman

Para probar correctamente la administración RabbitMQ se recomienda ejecutar los endpoints en este orden:

```text
1. Crear cola
       ↓
2. Crear exchange
       ↓
3. Crear binding
       ↓
4. Verificar binding en RabbitMQ Management
       ↓
5. Eliminar binding
       ↓
6. Eliminar cola
       ↓
7. Eliminar exchange
```

---

# Ejemplo completo de prueba

## 1. Crear cola

```http
POST http://localhost:8082/api/rabbit/queues
```

```json
{
  "name": "q.prueba.campuslab",
  "durable": true
}
```

---

## 2. Crear exchange

```http
POST http://localhost:8082/api/rabbit/exchanges
```

```json
{
  "name": "ex.prueba.campuslab",
  "type": "direct"
}
```

---

## 3. Crear binding

```http
POST http://localhost:8082/api/rabbit/bindings
```

```json
{
  "queueName": "q.prueba.campuslab",
  "exchangeName": "ex.prueba.campuslab",
  "routingKey": "prueba.route"
}
```

El binding puede comprobarse en:

```text
RabbitMQ Management
→ Exchanges
→ ex.prueba.campuslab
→ Bindings
```

Debe aparecer:

```text
Exchange:
ex.prueba.campuslab

Queue:
q.prueba.campuslab

Routing key:
prueba.route
```

---

## 4. Eliminar binding

```http
DELETE http://localhost:8082/api/rabbit/bindings
```

```json
{
  "queueName": "q.prueba.campuslab",
  "exchangeName": "ex.prueba.campuslab",
  "routingKey": "prueba.route"
}
```

---

## 5. Eliminar cola

```http
DELETE http://localhost:8082/api/rabbit/queues/q.prueba.campuslab
```

---

## 6. Eliminar exchange

```http
DELETE http://localhost:8082/api/rabbit/exchanges/ex.prueba.campuslab
```

---

# Estado actual

Actualmente se encuentran implementadas y operativas las siguientes funcionalidades:

- Creación de colas RabbitMQ mediante REST.
- Eliminación de colas RabbitMQ mediante REST.
- Creación de exchanges mediante REST.
- Eliminación de exchanges mediante REST.
- Creación de bindings mediante REST.
- Eliminación de bindings mediante REST.
- Soporte para exchanges `direct`.
- Soporte para exchanges `topic`.
- Soporte para exchanges `fanout`.
- Validación de nombres de colas.
- Validación de tipos de exchange.
- Uso de DTOs.
- Uso de Jakarta Validation.
- Manejo centralizado de errores.
- Separación entre Controller y Service.
- Integración con RabbitMQ.
- Compilación mediante Gradle.
- Ejecución mediante Docker.
- Integración mediante Docker Compose.
- Pruebas mediante Postman.
- Verificación mediante RabbitMQ Management.

---

# Proyecto CampusLab

Este microservicio pertenece al proyecto **CampusLab**, compuesto por distintos repositorios independientes:

```text
campuslab-frontend
campuslab-bff
campuslab-ms-bookings
campuslab-ms-catalog
campuslab-ms-notify
campuslab-ms-report
campuslab-ms-audit
campuslab-ms-rabbit-admin
campuslab-infra
```

Cada componente posee una responsabilidad independiente dentro de la arquitectura basada en microservicios.

---

# Autor

Proyecto desarrollado para la asignatura:

```text
DSY1107 - Desarrollo Cloud Native I
```

Proyecto:

```text
CampusLab
```
