package com.exclamationlabs.connid.base.grafana.driver.rest;

import com.exclamationlabs.connid.base.connector.driver.DriverInvocator;
import com.exclamationlabs.connid.base.connector.driver.rest.RestResponseData;
import com.exclamationlabs.connid.base.connector.results.ResultsFilter;
import com.exclamationlabs.connid.base.connector.results.ResultsPaginator;
import com.exclamationlabs.connid.base.grafana.model.GrafanaDashboard;
import com.exclamationlabs.connid.base.grafana.model.GrafanaOrg;
import com.exclamationlabs.connid.base.grafana.model.response.GrafanaDashboardResponse;
import com.exclamationlabs.connid.base.grafana.model.response.GrafanaSearchResponse;
import com.exclamationlabs.connid.base.grafana.model.response.GrafanaStandardResponse;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedHashTreeMap;
import org.apache.commons.lang3.StringUtils;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

import java.net.URISyntaxException;
import java.util.*;
import java.net.URI;

import static com.exclamationlabs.connid.base.grafana.driver.rest.GrafanaDataSourceInvocator.ORG_HEADER;

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
        LOG.info("Creating organization {0} with id {1}", org.getName(), org.getId());

        if ( org == null || org.getName() == null || org.getName().trim().length() == 0 )
        {
            throw new ConnectorException("Cannot create an org whose name is not specified");
        }
        // Check First if the Org Exists
        GrafanaOrg existing = getOneByName(driver, org.getName());
        if ( existing != null && existing.getName() != null && existing.getName().trim().equalsIgnoreCase(org.getName().trim()))
        {
            LOG.warn("Attempting to Create an Org named {0} that already Exists", existing.getName());
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
        Set<GrafanaOrg> grafanaOrgs = new HashSet<>();
        GrafanaOrg[] orgs;
        String queryParameters = "";
        int startpage = 0;
        int pageSize  = 1000;
        boolean hasMore = false;
        if (paginator.hasPagination())
        {
            startpage = paginator.getCurrentPageNumber();
            if ( startpage > 0 )
            {
                startpage = startpage - 1;
            }
            pageSize = paginator.getPageSize();
            queryParameters = "?perpage=" + pageSize + "&page=" + startpage;
            LOG.info("Get {0} Grafana Organizations on page {1}", paginator.getPageSize(), paginator.getCurrentPageNumber());
            RestResponseData<GrafanaOrg[]> rd = driver.executeGetRequest("/orgs" + queryParameters,
                    GrafanaOrg[].class,
                    driver.getAdminHeaders());
            orgs = rd.getResponseObject();
            grafanaOrgs.addAll(Arrays.asList(orgs));
        }
        else
        {
            do
            {
                queryParameters = "?perpage=" + pageSize + "&page=" + startpage;
                LOG.info("Get {0} Grafana Organizations on page {1}", pageSize, startpage);
                RestResponseData<GrafanaOrg[]> rd = driver.executeGetRequest("/orgs" + queryParameters,
                        GrafanaOrg[].class,
                        driver.getAdminHeaders());
                orgs = rd.getResponseObject();
                if ( orgs == null || orgs.length < pageSize )
                {
                    hasMore = false;
                }
                else
                {
                    hasMore = true;
                    startpage++;
                }
                grafanaOrgs.addAll(Arrays.asList(orgs));

            } while ( hasMore );
        }
        return grafanaOrgs;
    }

    public List<String> getDashboards(GrafanaDriver driver, GrafanaOrg org, List<GrafanaSearchResponse> dashboardInfo)
    {
        List<String>  list = new ArrayList<>();
        if ( org != null && org.getId() != null )
        {
            RestResponseData<String> rd;
            Map<String, String> headers = driver.getAdminHeaders();
            headers.put(ORG_HEADER, String.valueOf(org.getId()));
            for( int i=0; dashboardInfo != null && i< dashboardInfo.size(); i++)
            {
                String uid = dashboardInfo.get(0).getUid();
                rd = driver.executeGetRequest("/dashboards/uid/" + uid,
                        String.class,
                        headers);
                String dashboard = rd.getResponseObject();
                if ( dashboard != null )
                {
                    list.add(dashboard);
                }
            }
        }
        return list;
    }

    public List<GrafanaSearchResponse> findDashboards(GrafanaDriver driver, GrafanaOrg org)
    {
        List<GrafanaSearchResponse> searchResults = null;
        if ( org != null && org.getId() != null )
        {
            int orgId = org.getId();
            RestResponseData<GrafanaSearchResponse[]> rd;
            Map<String, String> headers = driver.getAdminHeaders();
            headers.put(ORG_HEADER, String.valueOf(orgId));
            rd = driver.executeGetRequest("/search?folderIds=0&query=&starred=false&type=dash-db",
                    GrafanaSearchResponse[].class,
                    headers);
            if (rd.getResponseObject() != null)
            {
                searchResults = Arrays.asList(rd.getResponseObject());
            }
        }
        return searchResults;
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
                LOG.info("Lookup Org with ID {0} ", id);
                RestResponseData<GrafanaOrg> rd;
                rd = driver.executeGetRequest("/orgs/" + id.trim(),
                                                GrafanaOrg.class,
                                                driver.getAdminHeaders());
                org = rd.getResponseObject();

            }
            else if (  orgId > 0 )
            {
                LOG.info("Lookup Org with ID {0} ", id);
                RestResponseData<GrafanaOrg> rd;
                rd = driver.executeGetRequest("/orgs/" + orgId,
                        GrafanaOrg.class,
                        driver.getAdminHeaders());
                org = rd.getResponseObject();
            }
            else
            {
                LOG.info("Lookup Org with Name {0} ", id);
                String path = "/orgs/name/" + id.trim();
                path = driver.encodeURIPath(path);
                RestResponseData<GrafanaOrg> rd = driver.executeGetRequest(
                        path,
                        GrafanaOrg.class,
                        driver.getAdminHeaders());
                org = rd.getResponseObject();
            }
            if ( org != null )
            {
                List<GrafanaSearchResponse> dashboardList = findDashboards(driver, org);
                List<String> dashboards = getDashboards(driver, org, dashboardList);
                org.setDashboards(dashboards);
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
                LOG.info("Lookup Org with Name {0} ", name);
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
                LOG.info("Lookup Org with ID {0} ", name);
                RestResponseData<GrafanaOrg> rd;
                rd = driver.executeGetRequest("/orgs/" + name.trim(),
                        GrafanaOrg.class,
                        driver.getAdminHeaders());
                org = rd.getResponseObject();
            }
            if ( org != null )
            {
                List<GrafanaSearchResponse> dashboardList = findDashboards(driver, org);
                List<String> dashboards = getDashboards(driver, org, dashboardList);
                org.setDashboards(dashboards);
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
