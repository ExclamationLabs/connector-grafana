package com.exclamationlabs.connid.base.grafana.driver.rest;

import com.exclamationlabs.connid.base.connector.driver.DriverInvocator;
import com.exclamationlabs.connid.base.connector.driver.rest.RestResponseData;
import com.exclamationlabs.connid.base.connector.results.ResultsFilter;
import com.exclamationlabs.connid.base.connector.results.ResultsPaginator;
import com.exclamationlabs.connid.base.grafana.model.GrafanaOrg;
import com.exclamationlabs.connid.base.grafana.model.response.GrafanaStandardResponse;
import org.apache.commons.lang3.StringUtils;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.net.URI;

public class GrafanaOrgInvocator implements DriverInvocator<GrafanaDriver, GrafanaOrg>
{
    private static final Log LOG = Log.getLog(GrafanaOrgInvocator.class);

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
    public String create(GrafanaDriver driver, GrafanaOrg org) throws ConnectorException
    {
        RestResponseData<GrafanaStandardResponse> response;
        GrafanaStandardResponse createInfo;
        response = driver.executePostRequest("/api/orgs",
                                                GrafanaStandardResponse.class,
                                                org,
                                                driver.getAdminHeaders());
        createInfo = response.getResponseObject();
        if ( createInfo == null || createInfo.getOrgId() == null || createInfo.getOrgId().trim().length() == 0 )
        {
            String message = "HTTP status " + response.getResponseStatusCode() +
                    ". Failed to create Global Grafana Org: " + org.getName() ;
            LOG.warn( message);
            throw new ConnectorException(message);
        }
        return createInfo.getOrgId();
    }

    /**
     * Delete an Organization
     * @param driver The rest driver
     * @param orgId The id of the organization to be deleted
     * @throws ConnectorException An exception thrown when the request fails
     */
    @Override
    public void delete(GrafanaDriver driver, String orgId) throws ConnectorException
    {

        GrafanaStandardResponse response;
        RestResponseData<GrafanaStandardResponse> data;
        data = driver.executeDeleteRequest("/api/orgs/" + orgId,
                                            GrafanaStandardResponse.class,
                                            driver.getAdminHeaders());
        response = data.getResponseObject();
        String message = response.getMessage();

        if ( message != null  )
        {
            LOG.info("Grafana Organization " + orgId + ": " +  message);
        }
    }

    @Override
    public Set<GrafanaOrg> getAll(GrafanaDriver driver, ResultsFilter resultsFilter, ResultsPaginator paginator, Integer integer) throws ConnectorException
    {
        String queryParameters = "";
        if (paginator.hasPagination())
        {
            queryParameters = "?perpage=" + paginator.getPageSize() + "&page=" + paginator.getCurrentPageNumber();
        }
        GrafanaOrg[] orgs;
        RestResponseData<GrafanaOrg[]> rd = driver.executeGetRequest("/api/orgs" + queryParameters,
                                                                    GrafanaOrg[].class,
                                                                    driver.getAdminHeaders());
        orgs = rd.getResponseObject();

        Set<GrafanaOrg> grafanaOrgs = new HashSet<>(Arrays.asList(orgs));

        return grafanaOrgs;
    }

    /**
     * Get a Single Org from the Grafana Service
     * @param driver The Rest Driver for the Connector
     * @param id the unique id of the User
     * @param options Operation Options Map
     * @return A Single Org or null
     * @throws ConnectorException when a failure occurs
     */
    @Override
    public GrafanaOrg getOne(GrafanaDriver driver, String id, Map<String, Object> options) throws ConnectorException
    {
        GrafanaOrg org = null;
        if ( id != null && id.trim().length() > 0 )
        {
            if (StringUtils.isNumeric(id.trim()))
            {
                RestResponseData<GrafanaOrg> rd;
                rd = driver.executeGetRequest("/api/orgs/" + id.trim(),
                                                GrafanaOrg.class,
                                                driver.getAdminHeaders());
                org = rd.getResponseObject();
            }
            else
            {
                org = getOneByName(driver, id);
            }
        }
        return org;
    }

    @Override
    public GrafanaOrg getOneByName(GrafanaDriver driver, String name) throws ConnectorException
    {
        GrafanaOrg org;
        if ( name != null && name.trim().length() > 0 )
        {
            String path = "/api/orgs/name/" + name.trim();
            path = encodeURIPath(path);
            RestResponseData<GrafanaOrg> rd = driver.executeGetRequest(
                    path,
                    GrafanaOrg.class,
                    driver.getAdminHeaders());
            org = rd.getResponseObject();
        }
        else
        {
            throw new ConnectorException("Grafana Org name not specified");
        }
        return org;
    }

    /**
     * Updates a Grafana Organization
     * @param driver The Grafana REST Driver
     * @param orgId The Id of the Organization
     * @param org The Organization information to update
     * @throws ConnectorException
     */
    @Override
    public void update(GrafanaDriver driver, String orgId, GrafanaOrg org) throws ConnectorException
    {
        GrafanaStandardResponse response;
        RestResponseData<GrafanaStandardResponse> responseData;
        if ( StringUtils.isNumeric(orgId) )
        {
            responseData = driver.executePutRequest(
                    "/api/orgs/" + orgId,
                    GrafanaStandardResponse.class,
                    org,
                    driver.getAdminHeaders());

            response = responseData.getResponseObject();
            LOG.info("Updated Grafana Org {0} with name {1}", orgId, org.getName());
        }
    }
}
