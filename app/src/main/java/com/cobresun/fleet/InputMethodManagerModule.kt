package com.cobresun.fleet

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.inputmethod.InputMethodManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object InputMethodManagerModule {

    @Provides
    @Singleton
    fun provideInputMethodManager(@ApplicationContext context: Context): InputMethodManager {
        return context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    }
}
