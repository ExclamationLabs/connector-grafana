package com.exclamationlabs.connid.base.grafana;

import com.exclamationlabs.connid.base.connector.authenticator.Authenticator;
import com.exclamationlabs.connid.base.connector.configuration.ConnectorConfiguration;
import com.exclamationlabs.connid.base.connector.configuration.basetypes.security.HttpBasicAuthConfiguration;
import com.exclamationlabs.connid.base.grafana.configuration.GrafanaConfiguration;
import org.identityconnectors.framework.common.exceptions.ConnectorSecurityException;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class GrafanaBasicAuthenticator implements Authenticator<GrafanaConfiguration>
{

    public String authenticate(GrafanaConfiguration configuration) throws ConnectorSecurityException
    {
        String info = configuration.getBasicUsername() + ":" + configuration.getBasicPassword();
        return Base64.getEncoder().encodeToString(info.getBytes());
    }

    @Override
    public Map<String, String> getAdditionalAuthenticationHeaders(ConnectorConfiguration configuration)
    {
        HashMap<String, String> headers = new HashMap<>();
        if (configuration instanceof HttpBasicAuthConfiguration)
        {
            String info = ((HttpBasicAuthConfiguration)configuration).getBasicUsername() + ":" + ((HttpBasicAuthConfiguration)configuration).getBasicPassword();
            String encoded = Base64.getEncoder().encodeToString(info.getBytes());
            headers.put("Authorization", "Basic " + encoded);
        }
        return headers;
    }
}
