# Sesión 13 · Paso 1 — Recorrido del flujo y huecos encontrados

**Autor:** Joan Rodriguez · Proyecto PharmaMobil (KMP) sobre PharmaSoft · Rama `feature/integrador-13`

Antes de esta sesión se cerraron los requisitos previos de la sección 3.1 que faltaban en la copia de
PharmaMobil (consumo REST con Ktor, errores tipados, sesión JWT con almacén seguro, base local SQLDelight,
sincronización básica e inyección por plataforma): commits `feat(red)`, `feat(sesion)`, `feat(local)`,
`feat(productos)` y `test(datos)` de esta rama.

Recorrido como usuario nuevo: instalación desde cero, inicio de sesión (`almacen / Almacen123`), registro de un
producto, pérdida de conexión, sincronización, error de validación y cierre de sesión.

## Resultado del recorrido

| N.º | Momento | Qué se observó | ¿Hueco? |
|-----|---------|----------------|---------|
| 1 | Primera apertura, sin sesión | Se muestra el login; no hay otra salida y no hace falta. | No |
| 2 | Credenciales incorrectas | Mensajes distintos: «Usuario o contraseña incorrectos» vs «No hay conexión con PharmaSoft». | No |
| 3 | Listado con la base vacía | Texto propio: «Sincroniza para descargar el catálogo…». | No |
| 4 | Creación con modo avión | Se guarda con id negativo y la marca «Pendiente de envío». | No |
| 5 | Sincronización al recuperar señal | Llega sin duplicarse. **Pero** si PharmaSoft lo rechaza (p. ej. 409 «nombre repetido»), la fila se borra y solo queda un aviso pasajero. | **Sí (H1)** |
| 6 | Error de validación del servidor | En línea llega bajo el campo. Si se registró sin red, el error aparece recién al sincronizar y el dato se pierde. | **Sí (H1)** |
| 7 | Token vencido a mitad de un formulario | El 401 cierra la sesión y lo escrito se pierde. | **Sí (H4)** |
| 8 | Cierre de sesión | Se borra el token, pero la base local (y sus pendientes) queda para el siguiente usuario. | **Sí (H5)** |

## Tabla de huecos y decisión

| ID | Hueco | Gravedad | Decisión |
|----|-------|----------|----------|
| H1 | Una fila rechazada por el servidor se borra sin que el usuario pueda corregirla. | Alta | **Se cierra hoy**: queda marcada como *no enviada*, visible con su motivo; se corrige, se reintenta o se descarta (pasos 2–4). |
| H2 | Sin límite de reintentos ni espera: una red intermitente reintenta todo y gasta batería. | Alta | **Se cierra hoy**: columna `intentos`, máximo 5, espera 1 s, 2 s, 4 s, 8 s, 16 s (paso 3). |
| H3 | La pantalla no dice cuántos cambios faltan ni de cuándo son los datos. | Media | **Se cierra hoy**: «Sin conexión · 3 cambios por enviar · Actualizado hace 2 h» y aviso de fallidas con «Reintentar ahora» (paso 4). |
| H6 | Un 401 durante la sincronización no debe marcar filas correctas como fallidas. | Media | **Se cierra hoy**: `ResultadoEnvio.SesionVencida` detiene la sincronización sin tocar las filas. |
| H7 | El usuario no se entera de que un producto no se envió si no está mirando la pantalla. | Media | **Se cierra hoy**: notificación del sistema con permiso `POST_NOTIFICATIONS` (paso 5). |
| H4 | Token vencido a mitad del formulario: se pierde lo escrito. | Media | **Sesión 14**: guardar el borrador antes de volver al login. |
| H5 | Al cerrar sesión la base local queda para el siguiente usuario. | Media | **Sesión 14**: limpiar la base al cerrar sesión, avisando si hay pendientes. |
| H8 | Clientes sigue en memoria (no usa PharmaSoft). | Media | **Sesión 14**: migrar Clientes con el mismo patrón offline-first de Productos. |
| H9 | iOS: el token va en NSUserDefaults y la red no se observa. | Baja (iOS no se evalúa hoy) | **Sesión 14**: Keychain y NWPathMonitor. |
| H10 | Reintentar una creación cuya respuesta se perdió duplica el registro. | Media | **Se mitiga hoy**: PharmaSoft exige nombre único, así que el reintento responde 409 y la fila queda *no enviada* y visible en vez de duplicarse. |
