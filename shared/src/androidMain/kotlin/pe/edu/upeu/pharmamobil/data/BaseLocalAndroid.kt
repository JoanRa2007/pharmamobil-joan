package pe.edu.upeu.pharmamobil.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import pe.edu.upeu.pharmamobil.data.local.BaseLocal
import pe.edu.upeu.pharmamobil.db.PharmaDatabase

/** Controlador de SQLDelight para Android (archivo pharmamobil.db). */
class BaseLocalAndroid(private val context: Context) : BaseLocal {
    override fun crearDriver(): SqlDriver = AndroidSqliteDriver(PharmaDatabase.Schema, context, "pharmamobil.db")
}
