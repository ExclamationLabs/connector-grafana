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

/**
 * Invocator for the Organization Object Type. This invocator supports CRUD operations
 */
public class GrafanaOrgInvocator implements DriverInvocator<GrafanaDriver, GrafanaOrg>
{
    private static final Log LOG = Log.getLog(GrafanaOrgInvocator.class);

    @Override
    public String create(GrafanaDriver driver, GrafanaOrg org) throws ConnectorException
    {
        RestResponseData<GrafanaStandardResponse> response;
        GrafanaStandardResponse createInfo;
        String orgId = null;
        LOG.warn("Creating organization {0} with id {1}", org.getName(), org.getId());

        if ( org == null || org.getName() == null || org.getName().trim().length() == 0 )
        {
            throw new ConnectorException("Cannot create an org whose name is not specified");
        }
        // Check First if the Org Exists
        GrafanaOrg existing = getOneByName(driver, org.getName());
        if ( existing != null && existing.getName() != null && existing.getName().trim().equalsIgnoreCase(org.getName().trim()))
        {
            LOG.error("Attempting to Create an Org named {0} that already Exists", existing.getName());
            orgId = existing.getId().toString();
        }
        else
        {
            response = driver.executePostRequest("/orgs",
                    GrafanaStandardResponse.class,
                    org,
                    driver.getAdminHeaders());
            createInfo = response.getResponseObject();
            orgId = createInfo.getOrgId();
            if (createInfo == null || createInfo.getOrgId() == null || createInfo.getOrgId().trim().length() == 0)
            {
                String message = "HTTP status " + response.getResponseStatusCode() +
                        ". Failed to create Global Grafana Org: " + org.getName();
                LOG.error(message);
                throw new ConnectorException(message);
            }
        }
        return orgId;
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
        LOG.warn("Deleting Grafana Organization {0}", orgId );
        data = driver.executeDeleteRequest("/orgs/" + orgId,
                                            GrafanaStandardResponse.class,
                                            driver.getAdminHeaders());
        response = data.getResponseObject();
        String message = response.getMessage();

        if ( message != null  )
        {
            LOG.warn("Grafana Organization " + orgId + ": " +  message);
        }
    }

    @Override
    public Set<GrafanaOrg> getAll(GrafanaDriver driver, ResultsFilter resultsFilter, ResultsPaginator paginator, Integer maxResultsRecords) throws ConnectorException
    {
        String queryParameters = "";
        LOG.warn("Lookup Organizations");
        if (paginator.hasPagination())
        {
            int startpage = paginator.getCurrentPageNumber();
            if ( startpage > 0 )
            {
                startpage = startpage - 1;
            }
            queryParameters = "?perpage=" + paginator.getPageSize() + "&page=" + startpage;
            LOG.warn("Get {0} Grafana Organizations on page {1}", paginator.getPageSize(), paginator.getCurrentPageNumber());
        }
        GrafanaOrg[] orgs;
        RestResponseData<GrafanaOrg[]> rd = driver.executeGetRequest("/orgs" + queryParameters,
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
            int orgId = GrafanaUserInvocator.decomposeOrgId(id);
            if ( StringUtils.isNumeric(id.trim()) )
            {
                LOG.warn("Lookup Org with ID {0} ", id);
                RestResponseData<GrafanaOrg> rd;
                rd = driver.executeGetRequest("/orgs/" + id.trim(),
                                                GrafanaOrg.class,
                                                driver.getAdminHeaders());
                org = rd.getResponseObject();
            }
            else if (  orgId > 0 )
            {
                LOG.warn("Lookup Org with ID {0} ", id);
                RestResponseData<GrafanaOrg> rd;
                rd = driver.executeGetRequest("/orgs/" + orgId,
                        GrafanaOrg.class,
                        driver.getAdminHeaders());
                org = rd.getResponseObject();
            }
            else
            {
                LOG.warn("Lookup Org with Name {0} ", id);
                String path = "/orgs/name/" + id.trim();
                path = driver.encodeURIPath(path);
                RestResponseData<GrafanaOrg> rd = driver.executeGetRequest(
                        path,
                        GrafanaOrg.class,
                        driver.getAdminHeaders());
                org = rd.getResponseObject();
            }
        }
        return org;
    }

    @Override
    public GrafanaOrg getOneByName(GrafanaDriver driver, String name) throws ConnectorException
    {
        GrafanaOrg org = null;
        if ( name != null && name.trim().length() > 0 )
        {
            if ( !StringUtils.isNumeric(name))
            {
                LOG.warn("Lookup Org with Name {0} ", name);
                String path = "/orgs/name/" + name.trim();
                path = driver.encodeURIPath(path);
                RestResponseData<GrafanaOrg> rd = driver.executeGetRequest(
                        path,
                        GrafanaOrg.class,
                        driver.getAdminHeaders());
                org = rd.getResponseObject();
            }
            else
            {
                LOG.warn("Lookup Org with ID {0} ", name);
                RestResponseData<GrafanaOrg> rd;
                rd = driver.executeGetRequest("/orgs/" + name.trim(),
                        GrafanaOrg.class,
                        driver.getAdminHeaders());
                org = rd.getResponseObject();
            }
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
        LOG.warn("Updating Grafana Org {0} with name {1}", orgId, org.getName());
        if ( StringUtils.isNumeric(orgId) )
        {
            responseData = driver.executePutRequest(
                    "/orgs/" + orgId,
                    GrafanaStandardResponse.class,
                    org,
                    driver.getAdminHeaders());

            response = responseData.getResponseObject();
            LOG.warn("Updated Grafana Org {0} with name {1}", orgId, org.getName());
        }
    }
}
