package com.focusguard.di

import javax.inject.Qualifier

/** Scope that lives as long as the process; used for app-wide state like the focus session. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
