# Sesión 13 · Paso 6 — Firebase Crashlytics detrás de una interfaz

**Autor:** Joan Rodriguez

## Configurar el proyecto en Firebase
1. <https://console.firebase.google.com> → **Agregar proyecto** → `pharmamobil` (plan Spark gratuito).
2. **Agregar app → Android** con el paquete **`pe.edu.upeu.pharmamobil`**.
3. Descarga **`google-services.json`** y cópialo en **`androidApp/`** (junto a `androidApp/build.gradle.kts`).
4. Menú **Crashlytics → Habilitar**.
5. Sincroniza Gradle. Al detectar el archivo, `androidApp/build.gradle.kts` aplica los complementos
   `com.google.gms.google-services` y `com.google.firebase.crashlytics`. Sin el archivo la app compila igual
   y la telemetría no se envía.

> Si al agregar el archivo Gradle se queja de la versión de un complemento con AGP 9, sube
> `google-services` y `firebase-crashlytics-plugin` en `gradle/libs.versions.toml` a la última versión que ofrezca Android Studio.

## Dónde vive cada pieza

| Pieza | Archivo | Capa |
|-------|---------|------|
| Contrato | `shared/.../domain/port/Telemetria.kt` | Dominio (no conoce Firebase) |
| Implementación | `shared/.../data/firebase/TelemetriaFirebase.kt` | Datos (único lugar con el SDK) |
| Registro | `di/AppModule.kt` → `single<Telemetria> { TelemetriaFirebase() }` | Koin |
| Uso | `SincronizarUseCase`: rechazos de envío y fallos del servidor al descargar | Dominio, vía la interfaz |

Se registra solo contexto técnico (pantalla, operación); **nunca** datos personales ni el token.
SDK: `dev.gitlive:firebase-crashlytics:2.7.0` (el de la guía). En iOS además hay que agregar el SDK nativo de
Firebase por Swift Package Manager en Xcode (pendiente para la sesión 14).
