package com.aprovecha.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aprovecha.app.data.local.dao.PackDao
import com.aprovecha.app.data.local.dao.ReservationDao
import com.aprovecha.app.data.local.dao.UserDao
import com.aprovecha.app.data.local.entity.CommerceEntity
import com.aprovecha.app.data.local.entity.PackEntity
import com.aprovecha.app.data.local.entity.ReservationEntity
import com.aprovecha.app.data.local.entity.UserEntity

/**
 * Base de datos local de la app utilizando Room.
 *
 * Incluye todas las entidades del proyecto Aprovecha!
 * Cada entidad implementa uno o más requerimientos funcionales:
 *
 * | Entidad            | Requerimientos       |
 * |--------------------|----------------------|
 * | UserEntity         | REQ-F01              |
 * | CommerceEntity     | REQ-F01, REQ-F03     |
 * | PackEntity         | REQ-F02, REQ-F03, REQ-NF01 |
 * | ReservationEntity  | REQ-F04, REQ-F05, REQ-F06, REQ-NF01 |
 *
 * @see UserDao
 * @see PackDao
 * @see ReservationDao
 */
@Database(
    entities = [
        UserEntity::class,
        CommerceEntity::class,
        PackEntity::class,
        ReservationEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AprovechaDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun packDao(): PackDao
    abstract fun reservationDao(): ReservationDao

    companion object {
        const val DATABASE_NAME = "aprovecha_db"
    }
}
