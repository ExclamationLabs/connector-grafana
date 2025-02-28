package com.exclamationlabs.connid.base.grafana.driver.rest;

import com.exclamationlabs.connid.base.connector.driver.DriverInvocator;
import com.exclamationlabs.connid.base.connector.driver.rest.RestRequest;
import com.exclamationlabs.connid.base.connector.driver.rest.RestResponseData;
import com.exclamationlabs.connid.base.connector.results.ResultsFilter;
import com.exclamationlabs.connid.base.connector.results.ResultsPaginator;
import com.exclamationlabs.connid.base.grafana.model.*;
import com.exclamationlabs.connid.base.grafana.model.response.GrafanaSearchResponse;
import com.exclamationlabs.connid.base.grafana.model.response.GrafanaStandardResponse;
import org.apache.commons.lang3.StringUtils;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;


import java.util.*;


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

        if ( org.getName() == null || org.getName().trim().isEmpty())
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
            if (createInfo == null || createInfo.getOrgId() == null || createInfo.getOrgId().trim().isEmpty())
            {
                String message = "HTTP status " + response.getResponseStatusCode() +
                        ". Failed to create Global Grafana Org: " + org.getName();
                LOG.error(message);
                throw new ConnectorException(message);
            }
            orgId = createInfo.getOrgId();
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

    /**
     * Find the Dashboard for a specific datasource.
     * Either the datasource uid is contained in the dashboard json or the dashboard uid is equal to the datasource uid
     * In older versions of the connector the dashboard uid is not set to be the same as the datasource uid
     * @param dashboardList List of Dashboards associated with the organization
     * @param dataSource The datasource to find the dashboard for
     * @return A GrafanaDashboard object
     */
    public GrafanaDashboard findDashboardForDataSource(List<GrafanaDashboard> dashboardList, GrafanaDataSource dataSource)
    {
        GrafanaDashboard dashboard = null;
        if ( dashboardList != null && dataSource != null )
        {
            for ( GrafanaDashboard db : dashboardList )
            {
                if ( db.getDashboard() != null && db.getDashboard().contains(dataSource.getUid().trim()))
                {
                    dashboard = db;
                    break;
                }
                else if ( db.getUid() != null && dataSource.getUid() != null && db.getUid().trim().equalsIgnoreCase(dataSource.getUid().trim()))
                {
                    dashboard = db;
                    break;
                }
            }
        }
        return dashboard;
    }

    /**
     * lookup the dashboards for a particular organization and populate a GrafanaDashboard Object
     * @param driver The Grafana Driver
     * @param org The Organization whose dashboards are to be retrieved
     * @return A list of Grafana Dashboards associated with the organization
     */
    public List<GrafanaDashboard> findDashboards(GrafanaDriver driver, GrafanaOrg org)
    {
        List<GrafanaDashboard> dashboards = new ArrayList<>();
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
                for (GrafanaSearchResponse searchResult : searchResults)
                {
                    GrafanaDashboard dashboard = new GrafanaDashboard();
                    dashboard.setId(searchResult.getId());
                    dashboard.setSlug(searchResult.getSlug());
                    dashboard.setSortMeta(searchResult.getSortMeta());
                    dashboard.setStarred(searchResult.getStarred());
                    dashboard.setTags(searchResult.getTags());
                    dashboard.setTitle(searchResult.getTitle());
                    dashboard.setType(searchResult.getType());
                    dashboard.setUid(searchResult.getUid());
                    dashboard.setUri(searchResult.getUri());
                    dashboard.setUrl(searchResult.getUrl());
                    dashboard.setOrgId(org.getId());
                    dashboard.setOrgName(org.getName());
                    dashboards.add(dashboard);
                    String dashboardRaw = GrafanaDashboardInvocator.getDashboardByUID(driver, String.valueOf(orgId), searchResult.getUid());
                    dashboard.setDashboard(dashboardRaw);
                }
            }
        }
        return dashboards;
    }

    @Override
    public Set<GrafanaOrg> getAll(GrafanaDriver driver, ResultsFilter resultsFilter, ResultsPaginator paginator, Integer maxResultsRecords) throws ConnectorException
    {
        Set<GrafanaOrg> grafanaOrgs = new HashSet<>();
        GrafanaOrg[] orgs;
        String queryParameters = "";
        RestResponseData<GrafanaOrg[]> rd = null;
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
            RestRequest<GrafanaOrg[]> restRequest = new RestRequest.Builder<>(GrafanaOrg[].class)
                    .withRequestUri("/orgs" + queryParameters)
                    .withGet()
                    .withHeaders(driver.getAdminHeaders())
                    .build();
            rd = driver.executeRequest(restRequest);
            orgs = rd.getResponseObject();
            grafanaOrgs.addAll(Arrays.asList(orgs));
        }
        else
        {
            do
            {
                queryParameters = "?perpage=" + pageSize + "&page=" + startpage;
                LOG.info("Get {0} Grafana Organizations on page {1}", pageSize, startpage);
                RestRequest<GrafanaOrg[]> restRequest = new RestRequest.Builder<>(GrafanaOrg[].class)
                        .withRequestUri("/orgs" + queryParameters)
                        .withGet()
                        .withHeaders(driver.getAdminHeaders())
                        .build();
                rd = driver.executeRequest(restRequest);
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

    /**
     * Gets the Grafana Health which includes the version number
     * @param driver The Grafana Driver
     * @return GrafanaHealth Object
     */
    public static GrafanaHealth getHealth(GrafanaDriver driver)
    {
        RestResponseData<GrafanaHealth> rd;
        Map<String, String> headers = driver.getAdminHeaders();
        rd = driver.executeGetRequest("/health",
                GrafanaHealth.class,
                headers);
        GrafanaHealth health = rd.getResponseObject();
        return health;
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

        if ( id != null && !id.trim().isEmpty())
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
                // Get dashboards and preferences
                // Update when necessary
                getAdditionalOrgAttributes(driver, org);
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

            LOG.info("Lookup Org with Name {0} ", name);
            String path = "/orgs/name/" + name.trim();
            path = GrafanaDriver.encodeURIPath(path);
            RestResponseData<GrafanaOrg> rd = driver.executeGetRequest(
                    path,
                    GrafanaOrg.class,
                    driver.getAdminHeaders());
            org = rd.getResponseObject();
            if ( org != null )
            {
                // Get dashboards and preferences
                // Update when necessary
                getAdditionalOrgAttributes(driver, org);
            }
        }
        else
        {
            throw new ConnectorException("Grafana Org name not specified");
        }
        return org;
    }

    /**
     * Get the additional attributes for an organization and update the dashboard and preferences when necessary
     * @param driver The Grafana REST Driver
     * @param org The Organization whose additional attributes are to be retrieved
     */
    public void getAdditionalOrgAttributes(GrafanaDriver driver, GrafanaOrg org)
    {
        // Retrieve the dashboard list
        List<GrafanaDashboard> dashboardList = findDashboards(driver, org);
        List<GrafanaDataSource> dataSources = GrafanaDataSourceInvocator.findByOrg(driver, String.valueOf(org.getId()));
        // Identify the default datasource
        for ( GrafanaDataSource dataSource : dataSources )
        {
            GrafanaDashboard dashboard = findDashboardForDataSource(dashboardList, dataSource);
            if ( dashboard != null )
            {
                dashboard.setDataSourceUid(dataSource.getUid());

                if ( dataSource.getJsonData() != null && dataSource.getJsonData().containsKey("dashboardTemplateName"))
                {
                    dashboard.setTemplateName(dataSource.getJsonData().get("dashboardTemplateName"));
                }
                else {
                    dashboard.setTemplateName(null);
                }
                // when the dashboard is the default or home dashboard, update the organization preferences
                if ( dashboard.getTemplateName() == null
                        || dashboard.getTemplateName().trim().isEmpty()
                        || dashboard.getTemplateName().trim().equalsIgnoreCase("default"))
                {
                    // Get the Org Preferences and potentially update the home dashboard
                    updateOrganizationPreferences(driver, org, dashboard);
                }
                GrafanaDataSourceInvocator.updateDashboardAndPreferences(driver,
                        String.valueOf(org.getId()),
                        dashboard.getDataSourceUid(),
                        org.getName(),
                        dashboard.getTemplateName());
            }
        }
        // Update the GrafanaOrg Object Type
        List<String> dashboards = new ArrayList<>();
        for ( GrafanaDashboard dashboard : dashboardList )
        {
            dashboards.add(dashboard.getDashboard());
        }
        org.setDashboards(dashboards);
    }

    /**
     * Returns an organization's preferences
     * @param driver The Grafana REST Driver
     * @param orgId  The Organization whose preferences updated
     * @return Dashboard uid
     */
    public static GrafanaOrgPreferences getOrganizationPreferences(GrafanaDriver driver, Integer orgId)
    {
        GrafanaOrgPreferences preferences = null;
        RestResponseData<GrafanaOrgPreferences> rd;
        Map<String, String> headers = driver.getAdminHeaders();
        headers.put(ORG_HEADER, String.valueOf(orgId));

        rd = driver.executeGetRequest("/org/preferences",
                GrafanaOrgPreferences.class,
                headers);
        if ( rd != null && rd.getResponseObject() != null )
        {
            preferences = rd.getResponseObject();
        }
        return preferences ;
    }

    /**
     * Update the current organization preferences with dashboard and timezone
     * @param driver The Grafana Driver
     * @param current The current Organization preferences
     * @param dashboardId The Dashboard ID to be set
     * @param dashboardUid the dashboard UID to be set
     * @return An updated organization preferences
     */
    public static GrafanaOrgPreferences setOrganizationPreferences(GrafanaDriver driver,
                                                                    GrafanaOrgPreferences current,
                                                                    Integer dashboardId,
                                                                    String dashboardUid)
    {
        GrafanaOrgPreferences preferences = new GrafanaOrgPreferences();
        preferences.setHomeDashboardUID(dashboardUid);
        preferences.setHomeDashboardId(dashboardId);
        preferences.setTheme(current.getTheme());
        String timezone = driver.getConfiguration().getDefaultTimeZone();
        if ( current.getTimezone() == null || current.getTimezone().trim().isEmpty())
        {
            if ( timezone != null && !timezone.trim().isEmpty())
            {
                preferences.setTimezone(timezone);
            }
            else
            {
                preferences.setTimezone("UTC");
            }
        }
        else
        {
            preferences.setTimezone(current.getTimezone());
        }
        return preferences;
    }

    /**
     * Updates a Grafana Organization
     * @param driver The Grafana REST Driver
     * @param orgId The Id of the Organization
     * @param org The Organization information to update
     * @throws ConnectorException An exception thrown when the request fails
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

    /**
     * Updates an organization preferences with a dashboard that was created or updated
     * @param driver The Grafana REST Driver
     * @param orgId  The Organization whose preferences updated
     * @return Dashboard uid
     */
    public static boolean updateOrganizationPreferences(GrafanaDriver driver, Integer orgId, GrafanaOrgPreferences preferences)
    {
        boolean success = true;
        RestResponseData<GrafanaStandardResponse> rd;
        Map<String, String> headers = driver.getAdminHeaders();
        headers.put(ORG_HEADER, String.valueOf(orgId));

        rd = driver.executePutRequest("/org/preferences",
                GrafanaStandardResponse.class,
                preferences,
                headers);
        if ( rd != null && rd.getResponseStatusCode() != 200 )
        {
            success = false;
            LOG.warn("Failed to update Grafana Org {0} preferences. Returned HTTP status {1}", orgId, rd.getResponseStatusCode());
        }
        return success;
    }

    /**
     * Update the organization preferences especially to set the home dashboard.
     * Also captures the timezone and dashboard UID from the specified dashboard to represent the home dashboard.
     * @param driver The Grafana Driver
     * @param org the Grafana Organization whose preferences we are requested to update
     * @param dashboard The dashboard that becomes the home dashboard
     */
    public static void updateOrganizationPreferences(GrafanaDriver driver, GrafanaOrg org,  GrafanaDashboard dashboard  )
    {
        if ( org != null && dashboard != null  )
        {
            // Get the Org Preferences and potentially update
            GrafanaOrgPreferences current = getOrganizationPreferences(driver, org.getId());
            if ( current != null )
            {
                if ( current.getHomeDashboardId() == null || current.getHomeDashboardId() == 0)
                {
                    // The organization has no home dashboard set
                    GrafanaOrgPreferences preferences = setOrganizationPreferences(driver, current, dashboard.getId(), dashboard.getUid());
                    if (updateOrganizationPreferences(driver, org.getId(), preferences))
                    {
                        org.setHomeDashboardId(preferences.getHomeDashboardId());
                        org.setHomeDashboardUID(preferences.getHomeDashboardUID());
                        org.setTimezone(preferences.getTimezone());
                    }
                    else
                    {
                        org.setHomeDashboardId(current.getHomeDashboardId());
                        org.setHomeDashboardUID(current.getHomeDashboardUID());
                        org.setTimezone(current.getTimezone());
                    }
                }
                else
                {
                    // The organization has a home dashboard set
                    org.setHomeDashboardId(current.getHomeDashboardId());
                    org.setTimezone(current.getTimezone());
                    if ( current.getHomeDashboardUID() != null )
                    {
                        org.setHomeDashboardUID(current.getHomeDashboardUID());
                    }
                    else
                    {
                        // Version 8 does not return dashboard UID
                        org.setHomeDashboardUID(dashboard.getUid());
                    }
                }
            }
        }
        return;
    }
}
