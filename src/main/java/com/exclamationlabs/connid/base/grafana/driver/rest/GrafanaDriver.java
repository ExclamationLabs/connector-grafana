package com.exclamationlabs.connid.base.grafana.driver.rest;

import com.exclamationlabs.connid.base.connector.driver.rest.BaseRestDriver;
import com.exclamationlabs.connid.base.connector.driver.rest.RestFaultProcessor;
import com.exclamationlabs.connid.base.connector.results.ResultsFilter;
import com.exclamationlabs.connid.base.connector.results.ResultsPaginator;
import com.exclamationlabs.connid.base.grafana.configuration.GrafanaConfiguration;
import com.exclamationlabs.connid.base.grafana.model.GrafanaOrg;
import com.exclamationlabs.connid.base.grafana.model.GrafanaUser;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class GrafanaDriver extends BaseRestDriver<GrafanaConfiguration>
{
    public GrafanaDriver()
    {
        super();
        addInvocator(GrafanaUser.class, new GrafanaUserInvocator());
        addInvocator(GrafanaOrg.class, new GrafanaOrgInvocator());
    }
    @Override
    protected RestFaultProcessor getFaultProcessor()
    {
        return GrafanaFaultProcessor.getInstance();
    }

    @Override
    protected String getBaseServiceUrl()
    {
        return getConfiguration().getServiceUrl();
    }

    Map<String, String> getAdminHeaders()
    {
        HashMap<String, String> headers = new HashMap();
        headers.put("Content-Type", "application/json; charset=UTF-8");
        headers.put("Accept", "application/json");
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
}
