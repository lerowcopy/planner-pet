package com.example.pet

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application класс приложения.
 * Требуется для инициализации Hilt.
 */
@HiltAndroidApp
class PetApplication : Application()

