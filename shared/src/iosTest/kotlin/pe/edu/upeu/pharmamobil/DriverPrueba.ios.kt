package pe.edu.upeu.pharmamobil

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.inMemoryDriver
import pe.edu.upeu.pharmamobil.db.PharmaDatabase

actual fun crearDriverPrueba(): SqlDriver = inMemoryDriver(PharmaDatabase.Schema)
