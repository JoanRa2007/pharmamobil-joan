package pe.edu.upeu.pharmamobil

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import pe.edu.upeu.pharmamobil.db.PharmaDatabase

/** Base SQLite EN MEMORIA para las pruebas (se ejecutan en la JVM local). */
actual fun crearDriverPrueba(): SqlDriver =
    JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { PharmaDatabase.Schema.create(it) }
