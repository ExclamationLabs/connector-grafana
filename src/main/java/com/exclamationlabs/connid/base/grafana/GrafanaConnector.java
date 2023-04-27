package com.exclamationlabs.connid.base.grafana;

import com.exclamationlabs.connid.base.connector.BaseFullAccessConnector;
import com.exclamationlabs.connid.base.connector.authenticator.Authenticator;
import com.exclamationlabs.connid.base.connector.configuration.basetypes.security.HttpBasicAuthConfiguration;
import com.exclamationlabs.connid.base.grafana.adapter.GrafanaDataSourceAdapter;
import com.exclamationlabs.connid.base.grafana.adapter.GrafanaOrgAdapter;
import com.exclamationlabs.connid.base.grafana.adapter.GrafanaUserAdapter;
import com.exclamationlabs.connid.base.grafana.configuration.GrafanaConfiguration;
import com.exclamationlabs.connid.base.grafana.driver.rest.GrafanaDriver;
import org.identityconnectors.framework.spi.ConnectorClass;

@ConnectorClass(displayNameKey = "grafana.connector.display", configurationClass = GrafanaConfiguration.class)
public class GrafanaConnector extends BaseFullAccessConnector<GrafanaConfiguration>
{
    public GrafanaConnector()
    {
        super(GrafanaConfiguration.class);
        setDriver(new GrafanaDriver());
        setAdapters(new GrafanaUserAdapter(), new GrafanaOrgAdapter(), new GrafanaDataSourceAdapter());
    }
}
