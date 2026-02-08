# Seek Notifications SDK

SDK unificado para el envío de notificaciones multiplataforma (Email, SMS, Push). Diseñado bajo Arquitectura Hexagonal,
permitiendo un desacople total entre la lógica de negocio y los proveedores de mensajería.

## Instalación

Agregá la dependencia a tu build.gradle:

```
Gradle

implementation 'co.seekglobal.notification:seek-notifications:1.0.0'
```

## Arquitectura

La librería separa el dominio de la infraestructura mediante el uso de puertos y adaptadores. El envío se gestiona a
través de Providers, los cuales se inyectan mediante un patrón Builder.

Actualmente, el SDK provee Mock Factories para testing; en versiones futuras, estas factories permitirán la
configuración de credenciales externas de forma dinámica, desde el factory.

Los proveedores pueden implementar opcionalmente la interfaz `ValidableSender` para realizar una pre-validación
automática antes del envío. Esta validación se ejecuta respetando el polimorfismo, permitiendo que cada proveedor
defina sus propias reglas de validación (por ejemplo, verificar credenciales o formatos de destinatario).

### Agregar un Proveedor Nuevo
Para agregar un proveedor personalizado:
1. Implementá la interfaz `NotificationSenderProvider` en tu clase.
2. Opcionalmente, implementá `ValidableSender` para validación previa.
3. Registrá el proveedor en el builder de `SeekNotification`.

Ejemplo:
```java
public class CustomEmailProvider implements NotificationSenderProvider {
    // Implementá los métodos requeridos
}
```

Luego, en la inicialización:
```java
var seekNotification = SeekNotificationBuilder.builder()
    .provider(new CustomEmailProvider())
    .build();
```

### Estructura del Proyecto

```
seek-notifications/
└── src/
    ├── main/
    │   ├── java/
    │   │   ├── module-info.java
    │   │   └── co/
    │   │       └── seekglobal/
    │   │           └── notification/
    │   │               ├── application/    # Capa de aplicación (Use Cases, Inbound/Outbound)
    │   │               ├── domain/         # Capa de dominio (Entidades, Value Objects)
    │   │               └── infrastructure/ # Capa de infraestructura (Adaptadores, Decoradores)
    │   └── resources/
    │       └── logback.xml
    └── test/
        ├── java/
           └── co/
               └── seekglobal/
                   └── notification/
                       └── example/         # Ejemplos y pruebas
```

## Inicialización

Configurá la instancia principal de SeekNotification registrando los proveedores necesarios a través de sus factories:

```
Java

var seekNotification = SeekNotificationBuilder.builder()
.provider(MockEmailSenderProvider.factory().build())
.provider(MockEmailFallback.factory().build())
.provider(MockSmsSenderProvider.factory().build())
.provider(MockPushSenderProvider.factory().build())
.build();
```

**Nota:** Si agregás varios proveedores para el mismo canal, se almacenan en un `LinkedHashSet` ordenado. El SDK intentará enviar la notificación con el primer proveedor; si falla, usará el siguiente como fallback, respetando el orden de registro.

## Ejemplos de Uso

### Envío de Email

```
Java

SendNotificationRequest emailRequest = SendNotificationRequest.builder()
.channel(NotificationChannel.EMAIL)
.recipient("usuario@ejemplo.com")
.subject("Bienvenido a Seek Notifications")
.body("Hola, esta es una notificación de ejemplo.")
.priority(Priority.NORMAL)
.metadata(Map.of("source", "example"))
.build();

NotificationResult emailResult = seekNotification.send(emailRequest);
```

### Envío de SMS (Alta Prioridad)

```
Java

SendNotificationRequest smsRequest = SendNotificationRequest.builder()
.channel(NotificationChannel.SMS)
.recipient("+1234567890")
.body("Notificación SMS urgente.")
.priority(Priority.HIGH)
.build();

NotificationResult smsResult = seekNotification.send(smsRequest);
```

### Envío Push (Programado y con TTL)

```
Java

SendNotificationRequest pushRequest = SendNotificationRequest.builder()
.channel(NotificationChannel.PUSH)
.recipient("device-token-123")
.body("Notificación push con TTL.")
.priority(Priority.NORMAL)
.scheduledAt(OffsetDateTime.now().plusMinutes(5))
.ttlSeconds(3600)
.build();

NotificationResult pushResult = seekNotification.send(pushRequest);
```

## Rate Limiting

El SDK incluye un decorador para aplicar límites de tasa distribuidos utilizando Redisson. Esto permite controlar la
frecuencia de envío de notificaciones por proveedor, evitando sobrecargas.

### Dependencia

Agregá Redisson a tu build.gradle:

```
Gradle

implementation 'org.redisson:redisson:3.27.2'
```

### Uso del Decorador

Envolvé tus proveedores con el decorador `RedissonRateLimitSenderProviderDecorator` para aplicar rate limiting:

```
Java

var redissonClient = Redisson.create(); // Configurá tu cliente Redisson

var rateLimitConfig = DistributedRateLimitConfig.builder()
    .maxRequestsPerWindow(10)
    .rateIntervalUnit(RateIntervalUnit.MINUTES)
    .rate(1)
    .build();

var emailProvider = MockEmailSenderProvider.factory().build();
var rateLimitedEmailProvider = RedissonRateLimitSenderProviderDecorator.factory()
    .delegate(emailProvider)
    .config(rateLimitConfig)
    .redissonClient(redissonClient)
    .build();

var seekNotification = SeekNotificationBuilder.builder()
    .provider(rateLimitedEmailProvider)
    .build();
```

El decorador adquiere un permiso del rate limiter antes de delegar el envío al proveedor subyacente.

## Dockerfile test
```
docker build -f Dockerfile -t seek-notifications-example .
docker run seek-notifications-example
```
## Roadmap

- Inyección de credenciales externas en Factories con proveedores reales. Ej: SENDGRID / MAILGUN / .etc
- Soporte para Templating segun proveedor.
- Implementacion de TTL y Scheduled Send en todos los providers.
- Control de hilos para el decorador de Redisson en caso de no utilizar virtual threads.
- Implementación de un sistema de retry con backoff exponencial para envíos fallidos.
- Integración con sistemas de monitoreo para tracking de métricas de envío y fallos.


Se apoyo con el uso de IA para este proyecto, para tareas simples como generar codigo repetitivo luego de haber definido arquitectura
y patrones a seguir y para generar documentación, siempre realizando iteraciones y revisiones manuales para asegurar la calidad del código y la documentación generada.
