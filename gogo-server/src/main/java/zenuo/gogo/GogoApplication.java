package zenuo.gogo;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.multibindings.Multibinder;
import zenuo.gogo.core.Server;
import zenuo.gogo.core.config.ApplicationConfig;
import zenuo.gogo.core.processor.IIndexProcessor;
import zenuo.gogo.core.processor.ILintProcessor;
import zenuo.gogo.core.processor.ISearchProcessor;
import zenuo.gogo.core.processor.ISearchResultProvider;
import zenuo.gogo.core.processor.IStaticProcessor;
import zenuo.gogo.core.processor.ISubstituteProcessor;
import zenuo.gogo.core.processor.impl.GoogleSearchResultProviderImpl;
import zenuo.gogo.core.processor.impl.IndexProcessorImpl;
import zenuo.gogo.core.processor.impl.LintProcessorImpl;
import zenuo.gogo.core.processor.impl.SearchProcessorImpl;
import zenuo.gogo.core.processor.impl.StartPageSearchResultProviderImpl;
import zenuo.gogo.core.processor.impl.StaticProcessorImpl;
import zenuo.gogo.core.processor.impl.SubstituteProcessorImpl;
import zenuo.gogo.service.ICacheService;
import zenuo.gogo.service.impl.EhcacheCacheImpl;
import zenuo.gogo.web.IIndexPageBuilder;
import zenuo.gogo.web.IResultPageBuilder;
import zenuo.gogo.web.IndexPageBuilder;
import zenuo.gogo.web.ResultPageBuilder;

/**
 * 入口类
 *
 * @author zenuo
 * 2018-06-02 19:12:15
 */
public class GogoApplication {
    public static void main(String[] args) {
        Guice.createInjector(new GogoModule())
                .getInstance(Server.class).start();
    }
}

class GogoModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(IIndexProcessor.class).to(IndexProcessorImpl.class);
        bind(ILintProcessor.class).to(LintProcessorImpl.class);
        bind(ISearchProcessor.class).to(SearchProcessorImpl.class);
        bind(IStaticProcessor.class).to(StaticProcessorImpl.class);
        bind(ISubstituteProcessor.class).to(SubstituteProcessorImpl.class);
        bind(ICacheService.class).to(EhcacheCacheImpl.class);
        bind(IIndexPageBuilder.class).to(IndexPageBuilder.class);
        bind(IResultPageBuilder.class).to(ResultPageBuilder.class);

        bind(ApplicationConfig.class).toInstance(ApplicationConfig.INSTANCE);

        final Multibinder<ISearchResultProvider> iSearchResultProviderMultibinder
                = Multibinder.newSetBinder(binder(), ISearchResultProvider.class);
        iSearchResultProviderMultibinder.addBinding().to(GoogleSearchResultProviderImpl.class);
        iSearchResultProviderMultibinder.addBinding().to(StartPageSearchResultProviderImpl.class);
    }
}