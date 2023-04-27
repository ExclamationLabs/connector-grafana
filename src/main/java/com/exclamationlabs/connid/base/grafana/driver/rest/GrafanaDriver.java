package com.exclamationlabs.connid.base.grafana.driver.rest;

import com.exclamationlabs.connid.base.connector.driver.rest.BaseRestDriver;
import com.exclamationlabs.connid.base.connector.driver.rest.RestFaultProcessor;
import com.exclamationlabs.connid.base.connector.model.IdentityModel;
import com.exclamationlabs.connid.base.connector.results.ResultsFilter;
import com.exclamationlabs.connid.base.connector.results.ResultsPaginator;
import com.exclamationlabs.connid.base.grafana.configuration.GrafanaConfiguration;
import com.exclamationlabs.connid.base.grafana.model.GrafanaDataSource;
import com.exclamationlabs.connid.base.grafana.model.GrafanaOrg;
import com.exclamationlabs.connid.base.grafana.model.GrafanaUser;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class GrafanaDriver extends BaseRestDriver<GrafanaConfiguration>
{
    private static final Log LOG = Log.getLog(GrafanaDriver.class);
    public GrafanaDriver()
    {
        super();
        addInvocator(GrafanaUser.class, new GrafanaUserInvocator());
        addInvocator(GrafanaOrg.class, new GrafanaOrgInvocator());
        addInvocator(GrafanaDataSource.class, new GrafanaDataSourceInvocator());
    }
    public static String encodeURIPath( String path)
    {
        try
        {
            URI uri = new URI("http", "localhost", path, "", "");
            path = uri.getRawPath();
        }
        catch ( URISyntaxException exception)
        {
            LOG.error(exception.getMessage(), exception);
        }
        return path;
    }
    @Override
    protected RestFaultProcessor getFaultProcessor()
    {
        return GrafanaFaultProcessor.getInstance();
    }

    @Override
    protected String getBaseServiceUrl()
    {
        String serviceURL =  getConfiguration().getServiceUrl();
        if ( serviceURL != null && serviceURL.endsWith("/"))
        {
            serviceURL = serviceURL.substring(0,serviceURL.length()-1);
        }
        return serviceURL;
    }

    Map<String, String> getAdminHeaders()
    {
        HashMap<String, String> headers = new HashMap();
        // headers.put("Content-Type", "application/json; charset=UTF-8");
        // headers.put("Accept", "application/json");
        String info = this.configuration.getBasicUsername() + ":" + configuration.getBasicPassword();
        String encoded = Base64.getEncoder().encodeToString(info.getBytes());
        headers.put("Authorization", "Basic " + encoded);
        return headers;
    }

    @Override
    public void test() throws ConnectorException
    {
        try
        {
            ResultsPaginator paginator = new ResultsPaginator();
            paginator.setPageSize(3);
            paginator.setCurrentPageNumber(1);
            getInvocator(GrafanaUser.class).getAll(this, new ResultsFilter(), paginator, null);
        }
        catch (Exception e)
        {
            throw new ConnectorException("Failed test for Grafana user listing.", e);
        }
    }

    @Override
    public void close()
    {

    }

    @Override
    public IdentityModel getOneByName(Class<? extends IdentityModel> identityModelClass, String nameValue) throws ConnectorException
    {
        return this.getInvocator(identityModelClass).getOneByName(this, nameValue);
    }
}
