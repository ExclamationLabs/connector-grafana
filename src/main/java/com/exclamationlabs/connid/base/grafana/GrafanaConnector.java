package com.exclamationlabs.connid.base.grafana;

import com.exclamationlabs.connid.base.connector.BaseFullAccessConnector;
import com.exclamationlabs.connid.base.grafana.adapter.GrafanaDataSourceAdapter;
import com.exclamationlabs.connid.base.grafana.adapter.GrafanaOrgAdapter;
import com.exclamationlabs.connid.base.grafana.adapter.GrafanaUserAdapter;
import com.exclamationlabs.connid.base.grafana.configuration.GrafanaConfiguration;
import com.exclamationlabs.connid.base.grafana.driver.rest.GrafanaDriver;
import org.identityconnectors.framework.common.objects.SuggestedValues;
import org.identityconnectors.framework.common.objects.SuggestedValuesBuilder;

import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.operations.DiscoverConfigurationOp;

import java.util.HashMap;
import java.util.Map;



@ConnectorClass(displayNameKey = "grafana.connector.display", configurationClass = GrafanaConfiguration.class)
public class GrafanaConnector extends BaseFullAccessConnector<GrafanaConfiguration> implements DiscoverConfigurationOp
{
    @Override
    public void testPartialConfiguration()
    {
        test();
    }

    @Override
    public Map<String, SuggestedValues> discoverConfiguration()
    {
        Map<String, SuggestedValues> suggestions = new HashMap<>();

        if ( configuration.getSeparateOrgAssociation() != null )
        {
            suggestions.put("separateOrgAssociation", SuggestedValuesBuilder.buildOpen(configuration.getSeparateOrgAssociation()));
        }
        else
        {
            suggestions.put("separateOrgAssociation", SuggestedValuesBuilder.buildOpen(false));
        }
        // deepGet Suggestion
        if ( configuration.getDeepImport() != null )
        {
            suggestions.put("deepGet", SuggestedValuesBuilder.buildOpen(configuration.getDeepGet()));
        }
        else
        {
            suggestions.put("deepGet", SuggestedValuesBuilder.buildOpen(true));
        }
        // deepImport Suggestion
        if ( configuration.getDeepImport() != null )
        {
            suggestions.put("deepImport", SuggestedValuesBuilder.buildOpen(configuration.getDeepImport()));
        }
        else
        {
            suggestions.put("deepImport", SuggestedValuesBuilder.buildOpen(true));
        }
        // pagination Suggestion
        if ( configuration.getPagination() != null )
        {
            suggestions.put("pagination", SuggestedValuesBuilder.buildOpen(configuration.getPagination()));
        }
        else
        {
            suggestions.put("pagination", SuggestedValuesBuilder.buildOpen(true));
        }
        if ( configuration.getPagination() != null )
        {
            suggestions.put("pagination", SuggestedValuesBuilder.buildOpen(configuration.getPagination()));
        }
        else
        {
            suggestions.put("pagination", SuggestedValuesBuilder.buildOpen(true));
        }
        if ( configuration.getIoErrorRetries() != null )
        {
            suggestions.put("ioErrorRetries", SuggestedValuesBuilder.buildOpen(configuration.getIoErrorRetries()));
        }
        else
        {
            suggestions.put("ioErrorRetries", SuggestedValuesBuilder.buildOpen(3));
        }
        if ( configuration.getImportBatchSize() != null )
        {
            suggestions.put("importBatchSize", SuggestedValuesBuilder.buildOpen(configuration.getImportBatchSize()));
        }
        else
        {
            suggestions.put("importBatchSize", SuggestedValuesBuilder.buildOpen(20, 50, 100));
        }
        if ( configuration.getDefaultOrgRole() != null )
        {
            suggestions.put("defaultOrgRole", SuggestedValuesBuilder.buildOpen(configuration.getDefaultOrgRole()));
        }
        else
        {
            suggestions.put("defaultOrgRole", SuggestedValuesBuilder.buildOpen("Viewer", "Admin"));
        }
        if ( configuration.getDuplicateErrorReturnsId() != null )
        {
            suggestions.put("duplicateErrorReturnsId", SuggestedValuesBuilder.buildOpen(configuration.getDuplicateErrorReturnsId()));
        }
        else
        {
            suggestions.put("duplicateErrorReturnsId", SuggestedValuesBuilder.buildOpen(true));
        }
        return suggestions;
    }

    public GrafanaConnector()
    {
        super(GrafanaConfiguration.class);
        setDriver(new GrafanaDriver());
        setAdapters(new GrafanaUserAdapter(), new GrafanaOrgAdapter(), new GrafanaDataSourceAdapter());
    }
}
