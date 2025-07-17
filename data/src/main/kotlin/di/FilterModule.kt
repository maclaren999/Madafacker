package di

import com.bbuddies.madafaker.common_domain.service.ContentFilterService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import service.ContentFilterServiceImpl
import javax.inject.Singleton

/**
 * Dagger module for content filtering dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class FilterModule {

    @Binds
    @Singleton
    abstract fun bindContentFilterService(
        contentFilterServiceImpl: ContentFilterServiceImpl
    ): ContentFilterService
}
