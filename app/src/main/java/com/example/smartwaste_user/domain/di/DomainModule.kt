package com.example.smartwaste_user.domain.di

import com.example.smartwaste_user.data.repoimpl.AuthRepositryImpl
import com.example.smartwaste_user.domain.repo.auth.AuthRepositry
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class DomainModule{


    @Singleton
    @Binds
    abstract fun bindAuthRepositry(authRepositryImpl: AuthRepositryImpl) : AuthRepositry
}