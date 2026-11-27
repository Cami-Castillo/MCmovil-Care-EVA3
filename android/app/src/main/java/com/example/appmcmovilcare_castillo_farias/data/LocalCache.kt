package com.example.appmcmovilcare_castillo_farias.data

import com.mcmovilcare.logic.data.model.*
import com.mcmovilcare.logic.data.remote.*
import com.mcmovilcare.logic.repository.*
import com.mcmovilcare.logic.domain.*

import android.content.Context
import androidx.room.*

@Entity(tableName = "cache")
data class CacheRegistro(@PrimaryKey val clave: String, val valor: String)

@Dao
interface CacheDao {
    @Query("SELECT valor FROM cache WHERE clave=:clave") suspend fun leer(clave: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun guardar(registro: CacheRegistro)
}

@Database(entities = [CacheRegistro::class], version = 1, exportSchema = true)
abstract class CareDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
}

class LocalCache(context: Context, private val usuario: String) : Cache {
    private val dao =
        Room.databaseBuilder(
                context.applicationContext,
                CareDatabase::class.java,
                "mccare_eva4_cache.db",
            )
            .build()
            .cacheDao()

    override suspend fun leer(clave: String) = dao.leer("$usuario:$clave")

    override suspend fun guardar(clave: String, valor: String) =
        dao.guardar(CacheRegistro("$usuario:$clave", valor))
}
