**Tema 1 - Jorge Leiva**
# Message Store

![Message Store Diagram](./MessageStore.gif)

## ¿Cómo podemos generar informes sobre la información de los mensajes sin perturbar la naturaleza transitoria y débilmente acoplada de un sistema de mensajería?

Las mismas propiedades que hacen que la mensajería sea poderosa también pueden dificultar su gestión. La mensajería asincrónica garantiza la entrega, pero no garantiza cuándo se entregará el mensaje. Para muchas aplicaciones prácticas, el tiempo de respuesta de un sistema puede ser crítico. También puede ser importante saber cuántos mensajes pasaron por el sistema dentro de un cierto intervalo de tiempo.

Para solucionar esto, se propone usar un **Almacén de Mensajes (Message Store)**, donde se registre información clave de cada mensaje (como ID, canal y timestamp) en un lugar central y persistente.

## ¿Cómo funciona el patrón Message Store?

Este patrón consiste en la duplicación del mensaje hacia un canal secundario mediante un *Wire Tap* o estableciendo una lógica, sin afectar el flujo principal. Sin embargo, hay que balancear la cantidad de datos almacenados con el tráfico de red y el espacio de almacenamiento.
Cuando se envia un mensaje a un canal, enviamos un duplicado del mensaje a un canal especial para que sea recopilado por el Almacén de Mensajes. Enviar un segundo mensaje en modo de “send and forget” no ralentizará el flujo de los mensajes principales de la aplicación. Sin embargo, sí aumenta el tráfico de red. Por eso, puede que no almacenemos el mensaje completo, sino solo algunos campos clave necesarios para el análisis posterior, como un ID de mensaje, el canal por el cual se envió y una marca de tiempo.

### Existen dos estrategias de almacenamiento:

1. **Crear un esquema separado para cada tipo de mensaje**, con mejor capacidad de consulta, pero más difícil de mantener.  
   Si creamos un esquema de almacenamiento separado (por ejemplo, tablas) para cada tipo de mensaje que coincida con la estructura de datos de ese tipo de mensaje, podremos aplicar índices y realizar búsquedas complejas sobre el contenido del mensaje. Sin embargo, esto supone que tenemos una estructura de almacenamiento separada para cada tipo de mensaje. Esto podría convertirse en una carga de mantenimiento.

2. **Usar un esquema genérico y guardar los mensajes como XML**, con menos capacidad de consulta pero mayor flexibilidad.  
   Esto nos permite un esquema de almacenamiento genérico. Aún podríamos hacer consultas sobre los campos del encabezado, pero no podríamos generar informes sobre los campos del cuerpo del mensaje. Sin embargo, una vez identificado un mensaje, podemos recrear su contenido en base al documento XML almacenado.

Finalmente, como el almacén puede crecer mucho, es recomendable implementar un mecanismo de depuración o respaldo para gestionar los mensajes antiguos.

### Justificación de uso

El patrón **Message Store** es útil en sistemas donde los mensajes se transmiten de manera asíncrona entre componentes desacoplados y existe la necesidad de auditar, monitorear o generar reportes sobre el flujo de mensajes. Permite registrar información clave de cada mensaje sin afectar el rendimiento ni la naturaleza transitoria del sistema de mensajería.

#### ¿En qué contexto o problema empresarial resulta útil?

Este patrón es especialmente valioso en empresas que manejan procesos críticos o regulados, donde es necesario:
- Auditar el flujo de mensajes entre sistemas (por ejemplo, en banca, salud o logística).
- Generar reportes históricos de actividad.
- Analizar tiempos de entrega y detectar cuellos de botella.
- Cumplir con normativas de trazabilidad y retención de información.

### Caso de uso propuesto

**Escenario:**  
Una empresa de logística utiliza un sistema de mensajería para coordinar la entrega de paquetes entre diferentes centros de distribución. Cada vez que un paquete cambia de estado (recibido, en tránsito, entregado), se envía un mensaje a través del sistema.

**Aplicación del patrón:**  
Para poder auditar el recorrido de cada paquete y generar reportes de desempeño, se implementa un Message Store. Cada mensaje enviado por el sistema se duplica y almacena en una base de datos central, registrando el ID del paquete, el canal y la marca de tiempo.

**Entidades o sistemas que intervienen:**
- **Productores de mensajes:** Sistemas que generan eventos de cambio de estado del paquete.
- **Sistema de mensajería:** Middleware que transporta los mensajes.
- **Message Store:** Base de datos central donde se almacenan los mensajes duplicados.
- **Herramientas de monitoreo y reporte:** Consultan el Message Store para generar