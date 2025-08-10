package com.example.smartwaste_user.domain.di

import com.example.smartwaste_user.data.repoimpl.AuthRepositryImpl
import com.example.smartwaste_user.data.repoimpl.reportrepoimpl.ReportRepositryImpl
import com.example.smartwaste_user.data.repoimpl.routeprogressimpl.RouteProgressRepositryImpl
import com.example.smartwaste_user.data.repoimpl.routerepoimpl.RouteRepositryImpl
import com.example.smartwaste_user.data.repoimpl.userrepoimpl.UserRepositryImpl
import com.example.smartwaste_user.data.repoimpl.workerfeedbackrepoimpl.WorkerFeedBackRepositryImpl
import com.example.smartwaste_user.domain.repo.auth.AuthRepositry
import com.example.smartwaste_user.domain.repo.reportrepo.ReportRepositry
import com.example.smartwaste_user.domain.repo.routeprogressmodel.RouteProgressRepositry
import com.example.smartwaste_user.domain.repo.routerepo.RouteRepositry
import com.example.smartwaste_user.domain.repo.userrepo.UserRepositry
import com.example.smartwaste_user.domain.repo.workerfedbackrepo.WorkerFeedBackRepositry
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

    @Singleton
    @Binds
    abstract fun bindUserRepositry(userRepositryImpl: UserRepositryImpl) : UserRepositry

    @Singleton
    @Binds
    abstract fun bindRouteProgressRepositry(routeProgressRepositryImpl: RouteProgressRepositryImpl) : RouteProgressRepositry

    @Singleton
    @Binds
    abstract fun bindRouteRepositry(routeProgressRepositryImpl: RouteRepositryImpl) : RouteRepositry

    @Singleton
    @Binds
    abstract fun bindReportRepositry(reportRepositryImpl: ReportRepositryImpl) : ReportRepositry

    @Singleton
    @Binds
    abstract fun bindWorkerFeedBackRepositry(RepositryImpl: WorkerFeedBackRepositryImpl) : WorkerFeedBackRepositry

}