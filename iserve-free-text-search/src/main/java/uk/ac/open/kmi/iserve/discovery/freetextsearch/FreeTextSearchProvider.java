package uk.ac.open.kmi.iserve.discovery.freetextsearch;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import uk.ac.open.kmi.iserve.core.ConfigurationModule;
import uk.ac.open.kmi.iserve.core.ConfigurationProperty;
import uk.ac.open.kmi.iserve.core.iServeProperty;

/**
 * Created by Luca Panziera on 26/08/2014.
 */
public class FreeTextSearchProvider extends AbstractModule {

    private ConfigurationModule configurationModule = new ConfigurationModule();

    @Override
    protected void configure() {

    }

    @Provides
    FreeTextSearchPlugin provideFreeTextSearchPlugin(@iServeProperty(ConfigurationProperty.FREE_TEXT_SEARCH) String pluginClassName) {
        Injector injector = Guice.createInjector(configurationModule);
        try {
            return (FreeTextSearchPlugin) injector.getInstance(Class.forName(pluginClassName));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
