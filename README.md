# campuslab-ms-rabbit-admin

Microservicio Spring Boot encargado de administrar recursos RabbitMQ mediante una API REST.

Forma parte del proyecto **CampusLab** y permite crear y eliminar colas, exchanges y bindings, además de validar parámetros de entrada y manejar errores de forma centralizada.

## Tecnologías

- Java 17
- Spring Boot
- Spring Web
- Spring AMQP
- Jakarta Validation
- RabbitMQ
- Docker
- Docker Compose
- Gradle 8.5

## Puerto

El microservicio se ejecuta en:

```text
8082
```

URL base local:

```text
http://localhost:8082/api/rabbit
```

## Configuración de RabbitMQ

La conexión con RabbitMQ se configura mediante variables de entorno.

| Variable | Descripción | Valor local |
|---|---|---|
| `RABBITMQ_HOST` | Host de RabbitMQ | `rabbitmq` |
| `RABBITMQ_PORT` | Puerto AMQP | `5672` |
| `RABBITMQ_USERNAME` | Usuario | `campuslab` |
| `RABBITMQ_PASSWORD` | Contraseña | `campuslab123` |

Configuración utilizada en `application.properties`:

```properties
server.port=8082

spring.application.name=ms-campuslab-rabbit-admin

spring.rabbitmq.host=${RABBITMQ_HOST:localhost}
spring.rabbitmq.port=${RABBITMQ_PORT:5672}
spring.rabbitmq.username=${RABBITMQ_USERNAME:campuslab}
spring.rabbitmq.password=${RABBITMQ_PASSWORD:campuslab123}
```

Cuando se ejecuta mediante Docker Compose, `RABBITMQ_HOST` utiliza el nombre del servicio:

```text
rabbitmq
```

## API REST

La API permite administrar:

- colas
- exchanges
- bindings

URL base:

```text
http://localhost:8082/api/rabbit
```

---

## Colas

### Crear una cola

```http
POST /api/rabbit/queues
```

Ejemplo:

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

### Eliminar una cola

```http
DELETE /api/rabbit/queues/{name}
```

Ejemplo:

```text
DELETE http://localhost:8082/api/rabbit/queues/q.prueba.campuslab
```

Respuesta esperada:

```text
200 OK
Cola eliminada correctamente
```

---

## Exchanges

### Crear un exchange

```http
POST /api/rabbit/exchanges
```

Ejemplo:

```json
{
  "name": "ex.direct.campuslab",
  "type": "direct"
}
```

Tipos soportados:

- `direct`
- `topic`
- `fanout`

Respuesta esperada:

```text
200 OK
Exchange creado correctamente
```

### Eliminar un exchange

```http
DELETE /api/rabbit/exchanges/{name}
```

Ejemplo:

```text
DELETE http://localhost:8082/api/rabbit/exchanges/ex.direct.campuslab
```

Respuesta esperada:

```text
200 OK
Exchange eliminado correctamente
```

---

## Bindings

Los bindings permiten asociar una cola a un exchange.

El DTO utilizado incluye:

```text
queueName
exchangeName
exchangeType
routingKey
```

El campo `exchangeType` permite indicar si el binding corresponde a un exchange `direct`, `topic` o `fanout`.

### Crear binding direct

```http
POST /api/rabbit/bindings
```

```json
{
  "queueName": "q.prueba.campuslab",
  "exchangeName": "ex.direct.campuslab",
  "exchangeType": "direct",
  "routingKey": "prueba.route"
}
```

Respuesta esperada:

```text
200 OK
Binding creado correctamente
```

### Crear binding topic

```http
POST /api/rabbit/bindings
```

```json
{
  "queueName": "q.prueba.campuslab",
  "exchangeName": "ex.topic.campuslab",
  "exchangeType": "topic",
  "routingKey": "prueba.*"
}
```

Respuesta esperada:

```text
200 OK
Binding creado correctamente
```

### Crear binding fanout

```http
POST /api/rabbit/bindings
```

En un exchange `fanout` no es necesaria una routing key.

```json
{
  "queueName": "q.prueba.campuslab",
  "exchangeName": "ex.fanout.campuslab",
  "exchangeType": "fanout"
}
```

Respuesta esperada:

```text
200 OK
Binding creado correctamente
```

### Eliminar un binding direct

```http
DELETE /api/rabbit/bindings
```

```json
{
  "queueName": "q.prueba.campuslab",
  "exchangeName": "ex.direct.campuslab",
  "exchangeType": "direct",
  "routingKey": "prueba.route"
}
```

Respuesta esperada:

```text
200 OK
Binding eliminado correctamente
```

### Eliminar un binding topic

```http
DELETE /api/rabbit/bindings
```

```json
{
  "queueName": "q.prueba.campuslab",
  "exchangeName": "ex.topic.campuslab",
  "exchangeType": "topic",
  "routingKey": "prueba.*"
}
```

### Eliminar un binding fanout

```http
DELETE /api/rabbit/bindings
```

```json
{
  "queueName": "q.prueba.campuslab",
  "exchangeName": "ex.fanout.campuslab",
  "exchangeType": "fanout"
}
```

---

## Orden recomendado de creación

Para crear un binding correctamente deben existir previamente la cola y el exchange.

```text
Crear cola
    |
    v
Crear exchange
    |
    v
Crear binding
```

---

## Validaciones

El microservicio utiliza DTOs con **Jakarta Validation**.

### Nombre de cola obligatorio

Ejemplo de validación:

```java
@NotBlank(message = "El nombre de la cola es obligatorio")
```

Request inválido:

```json
{
  "name": "",
  "durable": true
}
```

Respuesta esperada:

```text
400 Bad Request
```

### Tipo de exchange inválido

Request:

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

### Tipo de exchange obligatorio en bindings

El campo:

```text
exchangeType
```

es obligatorio para crear o eliminar bindings.

Tipos válidos:

- `direct`
- `topic`
- `fanout`

### Routing key

La `routingKey` es obligatoria para:

- `direct`
- `topic`

En bindings `fanout` no es necesaria.

---

## Manejo de errores

El manejo de errores se centraliza mediante:

```text
GlobalExceptionHandler
```

La clase utiliza:

```java
@RestControllerAdvice
```

De esta forma, configuraciones inválidas generan respuestas HTTP claras como:

```text
400 Bad Request
```

en lugar de errores internos `500`.

---

## Arquitectura interna

El microservicio mantiene separadas las responsabilidades.

```text
RabbitAdminController
        |
        v
RabbitAdminService
        |
        v
Spring AMQP / RabbitAdmin
        |
        v
RabbitMQ
```

### Controller

```text
RabbitAdminController
```

Expone los endpoints REST.

### Service

```text
RabbitAdminService
```

Encapsula la lógica de:

- creación de colas
- eliminación de colas
- creación de exchanges
- eliminación de exchanges
- creación de bindings
- eliminación de bindings
- manejo de exchanges `direct`
- manejo de exchanges `topic`
- manejo de exchanges `fanout`

El controller no necesita conocer los detalles internos de Spring AMQP.

### DTOs

```text
QueueRequest
ExchangeRequest
BindingRequest
```

### Configuración

```text
RabbitAdminConfig
```

### Manejo de errores

```text
GlobalExceptionHandler
```

---

## Estructura del proyecto

```text
campuslab-ms-rabbit-admin
|
├── src
│   ├── main
│   │   ├── java
│   │   │   └── cl
│   │   │       └── duoc
│   │   │           └── campuslab
│   │   │               └── mscampuslabrabbitadmin
│   │   │                   |
│   │   │                   ├── config
│   │   │                   │   └── RabbitAdminConfig.java
│   │   │                   |
│   │   │                   ├── controller
│   │   │                   │   └── RabbitAdminController.java
│   │   │                   |
│   │   │                   ├── dto
│   │   │                   │   ├── QueueRequest.java
│   │   │                   │   ├── ExchangeRequest.java
│   │   │                   │   └── BindingRequest.java
│   │   │                   |
│   │   │                   ├── exception
│   │   │                   │   └── GlobalExceptionHandler.java
│   │   │                   |
│   │   │                   ├── service
│   │   │                   │   └── RabbitAdminService.java
│   │   │                   |
│   │   │                   └── CampuslabMsRabbitAdminApplication.java
│   │   |
│   │   └── resources
│   │       └── application.properties
│   |
│   └── test
|
├── gradle
│   └── wrapper
|
├── Dockerfile
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
├── .gitignore
└── README.md
```

---

## Compilación

El proyecto utiliza:

```text
Java 17
Gradle 8.5
```

Para compilar en Windows:

```powershell
.\gradlew.bat clean build
```

Resultado esperado:

```text
BUILD SUCCESSFUL
```

JAR generado:

```text
build/libs/ms-campuslab-rabbit-admin-0.0.1-SNAPSHOT.jar
```

---

## Docker

Construcción de imagen:

```powershell
docker build -t lukmezac/campuslab-ms-rabbit-admin:latest .
```

El contenedor expone:

```text
8082
```

Dockerfile utilizado:

```dockerfile
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY build/libs/ms-campuslab-rabbit-admin-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## Docker Compose

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

## Servicios locales de CampusLab

| Servicio | Puerto |
|---|---:|
| `campuslab-bff` | 8080 |
| `campuslab-ms-bookings` | 8081 |
| `campuslab-ms-rabbit-admin` | 8082 |
| `campuslab-ms-notify` | 8083 |
| RabbitMQ AMQP | 5672 |
| RabbitMQ Management | 15672 |

Servicios Docker utilizados:

```text
campuslab-rabbitmq
campuslab-ms-bookings
campuslab-ms-rabbit-admin
campuslab-ms-notify
campuslab-bff
```

---

## RabbitMQ Management

Disponible localmente en:

```text
http://localhost:15672
```

Credenciales:

```text
Usuario: campuslab
Contraseña: campuslab123
```

RabbitMQ Management permite verificar visualmente:

- colas
- exchanges
- bindings
- routing keys
- DLQ

---

## Pruebas realizadas

Las pruebas se realizaron utilizando:

- Postman
- RabbitMQ Management
- Docker
- Docker Compose

Se verificaron:

- creación de cola
- eliminación de cola
- creación de exchange `direct`
- creación de exchange `topic`
- creación de exchange `fanout`
- eliminación de exchange
- creación de binding `direct`
- creación de binding `topic`
- creación de binding `fanout`
- eliminación de bindings
- validación de cola con nombre vacío
- validación de tipo de exchange inválido

---

## Secuencia de prueba en Postman

```text
1. Crear cola
       |
       v
2. Crear exchange
       |
       v
3. Crear binding
       |
       v
4. Verificar binding en RabbitMQ Management
       |
       v
5. Eliminar binding
       |
       v
6. Eliminar cola
       |
       v
7. Eliminar exchange
```

### Ejemplo completo con exchange direct

Crear cola:

```http
POST http://localhost:8082/api/rabbit/queues
```

```json
{
  "name": "q.prueba.campuslab",
  "durable": true
}
```

Crear exchange:

```http
POST http://localhost:8082/api/rabbit/exchanges
```

```json
{
  "name": "ex.direct.campuslab",
  "type": "direct"
}
```

Crear binding:

```http
POST http://localhost:8082/api/rabbit/bindings
```

```json
{
  "queueName": "q.prueba.campuslab",
  "exchangeName": "ex.direct.campuslab",
  "exchangeType": "direct",
  "routingKey": "prueba.route"
}
```

El binding puede verificarse en:

```text
RabbitMQ Management
→ Exchanges
→ ex.direct.campuslab
→ Bindings
```

Eliminar binding:

```http
DELETE http://localhost:8082/api/rabbit/bindings
```

```json
{
  "queueName": "q.prueba.campuslab",
  "exchangeName": "ex.direct.campuslab",
  "exchangeType": "direct",
  "routingKey": "prueba.route"
}
```

Eliminar cola:

```http
DELETE http://localhost:8082/api/rabbit/queues/q.prueba.campuslab
```

Eliminar exchange:

```http
DELETE http://localhost:8082/api/rabbit/exchanges/ex.direct.campuslab
```

---

## Estado

Actualmente están implementadas y probadas localmente:

- creación y eliminación de colas
- creación y eliminación de exchanges
- creación y eliminación de bindings
- soporte para `direct`
- soporte para `topic`
- soporte para `fanout`
- validación de nombres
- validación de tipos de exchange
- validación de `exchangeType`
- validación de routing key para `direct` y `topic`
- DTOs de entrada
- Jakarta Validation
- manejo centralizado de errores
- separación Controller / Service
- integración con RabbitMQ
- ejecución mediante Docker
- pruebas con Postman
- verificación mediante RabbitMQ Management

---

## Proyecto CampusLab

Este microservicio pertenece a la arquitectura CampusLab:

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

Cada componente mantiene una responsabilidad independiente dentro de la arquitectura de microservicios.

## Asignatura

```text
DSY1107 - Desarrollo Cloud Native I
```

Proyecto:

```text
CampusLab
```