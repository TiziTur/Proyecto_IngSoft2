package com.aprovecha.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class principal de Aprovecha!
 *
 * Requerida por Hilt para la inyección de dependencias en toda la app.
 */
@HiltAndroidApp
class AprovechaApplication : Application()
