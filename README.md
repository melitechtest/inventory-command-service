# inventory-command-service (Servicio de Comando y Escritura) ✍️

Este microservicio es el componente central de **escritura** de la arquitectura CQRS. Es responsable de recibir comandos (Restock, Venta), validar las reglas de negocio, actualizar la base de datos transaccional (PostgreSQL) y emitir eventos a través del *Message Broker* (ActiveMQ Artemis).

## 💡 Rol del Servicio

* **Patrón CQRS:** Eje del lado del **Comando (Write)**.
* **Responsabilidad:** Procesar mutaciones de estado (`POST /restock`, `POST /sale`).
* **Consistencia:** Garantizar la atomicidad y durabilidad de las transacciones en PostgreSQL.
* **Eventos:** Publicar el `StockUpdateEvent` tras cada cambio exitoso de inventario.

## ⚙️ Stack Tecnológico

* **Lenguaje:** Java 17
* **Framework:** Spring Boot 3.x
* **Persistencia:** Spring Data JPA + PostgreSQL
* **Mensajería:** **ActiveMQ Artemis (JMS)** usando `JmsTemplate`.

## 🛠️ Configuración Local y Endpoints

El servicio se despliega a través de **Docker Compose** y está diseñado para ser accedido mediante el **Nginx API Gateway** en el puerto **8080**.

| Endpoint | Método | Descripción |
| :--- | :--- | :--- |
| `/api/commands/restock` | `POST` | Inicializa o añade stock. |
| `/api/commands/sale` | `POST` | Reduce stock y emite un evento. |

## 🚀 Integración con GitHub Actions

Este repositorio utiliza GitHub Actions para automatizar el ciclo de vida de desarrollo continuo (CI/CD): **Build, Test, y Push**.

### Flujo de CI/CD:

1.  **Activación:** Se activa con cada *push* al *branch* principal (`main`).
2.  **Build & Test:** Compila el código, descarga dependencias y ejecuta las pruebas unitarias.
3.  **Docker Build:** Construye la imagen de Docker para el servicio (`melitechtest/inventory-command-service:latest`).
4.  **Push:** Autentica y sube la imagen al repositorio de Docker Hub.