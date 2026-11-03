# PharmaMobil — proyecto integrador (copia de trabajo de Joan Rodriguez)

Copia de la app del curso **PharmaMobil** (https://github.com/dreyna/pharmaMobil, commit `fe7f420`), conectada a mi
copia del backend **PharmaSoft**. Kotlin Multiplatform (Android/iOS) · Compose Multiplatform · Ktor · Koin · SQLDelight.

## Cómo ejecutarla

1. Levanta el backend (`pharmasoft-joan`): `docker compose up -d` y luego `mvnw.cmd spring-boot:run`.
2. Abre **esta carpeta** en Android Studio y ejecuta **androidApp**.
   - Emulador: funciona tal cual (`http://10.0.2.2:8080/api/v1/`).
   - Celular por USB: en `shared/.../data/remote/ApiConfig.kt` usa `http://localhost:8080/api/v1/` y ejecuta
     `adb reverse tcp:8080 tcp:8080`.
3. Usuarios de PharmaSoft: `almacen / Almacen123` (escribe catálogo), `vendedor / Vendedor123` (solo lee), `admin / Admin123`.
4. Pruebas: `gradlew.bat :shared:testAndroidHostTest`.

## Sesión 13 · Guía práctica (rama `feature/integrador-13`)

| Paso | Qué se hizo | Dónde |
|------|-------------|-------|
| 3.1 | Requisitos previos que faltaban: Ktor + DTO + errores tipados, login JWT con almacén cifrado, SQLDelight offline-first, sincronización básica, CRUD de productos según permisos | `data/remote`, `data/local`, `data/repository`, `presentation/sesion`, `presentation/producto` |
| 1 | Recorrido del flujo y tabla de huecos con su decisión | `docs/SESION13_HUECOS.md` |
| 2 | Columnas `intentos` y `ultimoError` + migración | `PharmaDb.sq`, `1.sqm` |
| 3 | Fallo transitorio vs permanente, espera 1–16 s, máximo 5 intentos | `SincronizarUseCase`, `EnvioRepositorioRest`, `ResultadoEnvio` |
| 4 | Pendientes, fallidas y «Actualizado hace…» en la pantalla de productos | `presentation/sincronizacion/` |
| 5 | Gestor de permisos en común + Android con 3 estados; permiso real: notificaciones | `domain/permiso/`, `androidMain/.../permiso/`, Perfil |
| 6 | Firebase Crashlytics (GitLive 2.7.0) detrás de `Telemetria` | `domain/port/Telemetria.kt`, `data/firebase/`, `docs/SESION13_FIREBASE.md` |
| 7 | Pruebas de la cola de envío y de permisos con implementaciones falsas | `commonTest/SincronizacionTest.kt`, `PermisosTest.kt` |

> Ubicación: el gestor de permisos y su puente viven en `shared/src/androidMain/.../permiso/` (y no en `androidApp/`)
> porque en este proyecto el `platformModule` de Koin se declara con expect/actual dentro de `shared`.
