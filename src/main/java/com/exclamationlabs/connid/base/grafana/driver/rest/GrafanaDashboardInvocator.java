package com.exclamationlabs.connid.base.grafana.driver.rest;




import com.exclamationlabs.connid.base.connector.driver.DriverInvocator;
import com.exclamationlabs.connid.base.connector.driver.rest.RestRequest;
import com.exclamationlabs.connid.base.connector.results.ResultsFilter;
import com.exclamationlabs.connid.base.connector.results.ResultsPaginator;
import com.exclamationlabs.connid.base.connector.driver.rest.RestResponseData;
import com.exclamationlabs.connid.base.grafana.model.GrafanaDashboard;
import com.exclamationlabs.connid.base.grafana.model.GrafanaDataSource;
import com.exclamationlabs.connid.base.grafana.model.GrafanaOrg;
import com.exclamationlabs.connid.base.grafana.model.response.GrafanaDashboardResponse;
import com.exclamationlabs.connid.base.grafana.model.response.GrafanaSearchResponse;
import com.exclamationlabs.connid.base.grafana.model.response.GrafanaStandardResponse;
import org.apache.commons.lang3.StringUtils;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

import java.util.*;

import static com.exclamationlabs.connid.base.grafana.driver.rest.GrafanaDataSourceInvocator.ORG_HEADER;
import static com.exclamationlabs.connid.base.grafana.driver.rest.GrafanaDataSourceInvocator.decomposeID;

public class GrafanaDashboardInvocator implements DriverInvocator<GrafanaDriver, GrafanaDashboard> {

    private static final Log LOG = Log.getLog(GrafanaDashboardInvocator.class);

    @Override
    public String create(GrafanaDriver driver, GrafanaDashboard dashboard) throws ConnectorException {
        GrafanaDashboardResponse result = null;
        String id = null;
        String template = null;

        if ( dashboard.getOrgId() == null  ) {
            throw new ConnectorException("Cannot create a dashboard without an Organization ID specified");
        }

        if ( dashboard.getDashboard() != null && !dashboard.getDashboard().trim().isEmpty()) {
            // Create a new dashboard from a completed JSON string
            template = dashboard.getDashboard();
            LOG.info("Creating dashboard for Org {0} with name {1}", dashboard.getOrgId(), dashboard.getOrgName());
        } else {
            LOG.info("Creating dashboard for Org {0} with template {1}", dashboard.getOrgId(), dashboard.getTemplateName());
            template = getTemplate(driver, dashboard.getTemplateName());
        }
        template = template.replace("<DataSourceUID>", dashboard.getDataSourceUid());
        template = template.replace("__DataSourceUID__", dashboard.getDataSourceUid());
        template = template.replace("__OrgName__", dashboard.getOrgName());
        template = template.replace("__DashboardId__", "null");
        if ( dashboard.getUid() != null && !dashboard.getUid().trim().isEmpty() )
        {
            template = template.replace("__DashboardUid__", String.format("\"%s\"",dashboard.getUid()));
        }
        else if ( dashboard.getDataSourceUid() != null && !dashboard.getDataSourceUid().trim().isEmpty())
        {
            template = template.replace("__DashboardUid__", String.format("\"%s\"",dashboard.getDataSourceUid()));
        }
        else
        {
            template = template.replace("__DashboardUid__", "null");
        }
        result = createDashboard(driver, String.valueOf(dashboard.getOrgId()), template);
        if ( result != null
                && result.getStatus() != null
                && result.getStatus().trim().equalsIgnoreCase("success"))
        {
            id = result.getUid();
        }
        return id;
    }
    /**
     * Creates or updates the dashboard for an associated organization whose datasource was created or updated
     * @param driver The Grafana ResT Driver
     * @param orgId  The Org whose Dashboard will be created or updated
     * @param dashboard The dashboard raw JSON with datasource uid already embedded
     * @return Dashboard uid
     */
    public static GrafanaDashboardResponse createDashboard(GrafanaDriver driver, String orgId, String dashboard)
    {
        GrafanaDashboardResponse dashboardInfo = null;
        RestResponseData<GrafanaDashboardResponse> rd;
        Map<String, String> headers = driver.getAdminHeaders();
        headers.put(ORG_HEADER, String.valueOf(orgId));

        RestRequest<GrafanaDashboardResponse> request = new RestRequest.Builder<>(GrafanaDashboardResponse.class)
                .withPost()
                .withRequestBody(dashboard)
                .withHeaders(headers)
                .withRequestUri("/dashboards/db")
                .build();

        rd = driver.executeRequest(request, false, 0);

        if ( rd != null  )
        {
            if ( rd.getResponseObject() != null )
            {
                dashboardInfo = rd.getResponseObject();
            }
            if ( rd.getResponseStatusCode() != 200 )
            {
                if ( dashboardInfo != null ) {
                    LOG.warn("Dashboard created failed for Org {0} with HTTP {1} status {2} message {3}.",
                            orgId,
                            rd.getResponseStatusCode(),
                            dashboardInfo.getStatus(),
                            dashboardInfo.getMessage());
                }
                else {
                    LOG.warn("Dashboard create failed for Org {0} with HTTP {1} .",
                            orgId,
                            rd.getResponseStatusCode());
                }
            }
        }
        return dashboardInfo ;
    }
    @Override
    public void delete(GrafanaDriver driver, String id) throws ConnectorException {
        RestResponseData<GrafanaStandardResponse> rd;
        Map<String, String> headers = driver.getAdminHeaders();
        GrafanaDashboard current = getOne(driver, id, null);
        if (current == null) {
            LOG.warn("Cannot delete Grafana Dashboard {0} since it is not found", id);
            return;
        }
        LOG.warn("Deleting Grafana Dashboard {0}", id);
        Integer orgId = current.getOrgId();
        headers.put(ORG_HEADER, orgId.toString());
        rd = driver.executeDeleteRequest("/dashboards/uid/" + current.getUid(),
                GrafanaStandardResponse.class,
                headers);
        if ( rd.getResponseStatusCode() != 200 ) {
            GrafanaStandardResponse data = rd.getResponseObject();
            String message = (data == null) ? null : data.getMessage();
            LOG.error("HTTP Error {0} deleting Grafana Dashboard {1}: {2}", rd.getResponseStatusCode(), id, message);
        }
    }

    @Override
    public GrafanaDashboard getOne(GrafanaDriver driver, String id, Map<String, Object> options) throws ConnectorException {
        GrafanaDashboard dashboard = null;
        Map<String, String> headers = driver.getAdminHeaders();
        if ( id != null && id.trim().length() > 0 )
        {
            String[] ids = decomposeID(id);
            if ( ids != null && ids.length ==2 )
            {
                if (StringUtils.isNumeric(ids[0])) {
                    String orgId = ids[0];
                    String uid = ids[1];
                    headers.put(ORG_HEADER, orgId);
                    LOG.info("Lookup Dashboard for Org {0} with uid {0} ", orgId, uid);
                    RestResponseData<String> rd;
                    rd = driver.executeGetRequest("/dashboards/uid/" + uid.trim(),
                            String.class,
                            headers);
                    String data = rd.getResponseObject();
                    dashboard = new GrafanaDashboard();
                    if ( data != null ) {
                        dashboard.setDashboard(data);
                        dashboard.setUid(uid);
                        dashboard.setOrgId(Integer.valueOf(orgId));
                    }
                    GrafanaSearchResponse searchResult = findDashboardsForOrgAndUid(driver, orgId, uid);
                    if ( searchResult != null )
                    {
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
                    }

                    GrafanaDataSource dataSource = findDataSourceForDashboard(driver, orgId, uid);
                    if ( dataSource != null ) {
                        Map<String, String> jd = dataSource.getJsonData();
                        if ( jd != null )
                        {
                            String dashboardUid = jd.get(GrafanaDataSourceInvocator.DASHBOARD_UID);
                            if ( dashboardUid != null && dashboardUid.equalsIgnoreCase(uid))
                            {
                                dashboard.setDataSourceUid(dataSource.getUid());
                                if ( jd.containsKey(GrafanaDataSourceInvocator.ORG_NAME))
                                {
                                    dashboard.setOrgName(jd.get(GrafanaDataSourceInvocator.ORG_NAME));
                                }
                                dashboard.setTemplateName(jd.getOrDefault(GrafanaDataSourceInvocator.DASHBOARD_TEMPLATE_NAME,
                                        "default"));
                            }
                        }
                    }
                }
                else
                {
                    throw new ConnectorException("Grafana Dashboard orgId not numeric. Expected numerical OrgId prefix in Id Value");
                }
            }
            else
            {
                throw new ConnectorException("Grafana Dashboard ID not decomposable. Expected numerical Org Prefix");
            }
        }
        return dashboard;
    }

    /**
     * Get a Grafana Dashboard by name where the name is assumed to be the UID of the dashboard
     *
     * @param driver the Grafana Driver
     * @param id the name of the dashboard to search for.
     *           The id is composed of OrgId and UID separated by an underscore. eg "127_abc123efg"
     * @param options ignored
     * @return a GrafanaDashboard object
     * @throws ConnectorException when failure occurs
     */
    @Override
    public GrafanaDashboard getOneByName(GrafanaDriver driver, String id, Map<String, Object> options) throws ConnectorException
    {
        return getOne(driver, id, options);
    }

    @Override
    public Set<GrafanaDashboard> getAll(GrafanaDriver driver, ResultsFilter resultsFilter, ResultsPaginator paginator, Integer maxResultsRecords) throws ConnectorException {
        Set<GrafanaDashboard> dashboards = new HashSet<>();
        List<GrafanaSearchResponse> searchResults = null;
        List<GrafanaOrg> orgList = null;
        String queryParameters = "";
        String orgId = null;
        int startpage = 0;
        int pageSize = 1000;
        boolean hasMore = false;
        if ( resultsFilter != null
                && resultsFilter.hasFilter()
                && resultsFilter.getAttribute() != null
                && resultsFilter.getAttribute().equalsIgnoreCase("orgId")) {
            orgId = resultsFilter.getValue();
            GrafanaOrg item = GrafanaDataSourceInvocator.getOrgInfo(driver, orgId);
            if ( item != null )
            {
                orgList = new ArrayList<>();
                orgList.add(item);
            }
        } else {
            GrafanaOrgInvocator orgInvocator = new GrafanaOrgInvocator();
            Set<GrafanaOrg> orgSet = orgInvocator.getAll( driver, null, paginator, null);
            orgList  = new ArrayList<>(orgSet);
        }

        if ( orgList != null && !orgList.isEmpty())
        {
            for (GrafanaOrg org : orgList)
            {
                orgId = org.getId().toString();
                searchResults = findDashboardsForOrg(driver, orgId);
                if (searchResults != null && !searchResults.isEmpty())
                {
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
                        dashboard.setOrgId(Integer.valueOf(orgId));
                        dashboard.setOrgName(org.getName());
                        dashboards.add(dashboard);
                    }
                }
            }
        }
        else {
            paginator.setNoMoreResults(true);
            LOG.warn("No Grafana Dashboards found for Org {0}", orgId);
        }
        return dashboards;
    }

    public static String getDashboardByUID(GrafanaDriver driver, String orgId, String uid)
    {
        String dashboard = null;
        Map<String, String> headers = driver.getAdminHeaders();
        headers.put(ORG_HEADER, orgId);
        RestResponseData<String> rd;
        RestRequest<String> request = new RestRequest.Builder<>(String.class)
                .withGet()
                .withHeaders(headers)
                .withRequestUri("/dashboards/uid/" + uid.trim())
                .build();

        rd = driver.executeRequest(request, false, 0);

        if ( rd != null && rd.getResponseStatusCode() != 200 ) {
            LOG.warn("Dashboard {0} lookup failed for Org {1} with HTTP {2} .",
                    uid.trim(),
                    orgId,
                    rd.getResponseStatusCode());
        }
        if ( rd != null && rd.getResponseObject() != null )
        {
            dashboard = rd.getResponseObject();
        }
        return dashboard;
    }

    public static String getTemplate(GrafanaDriver driver, String templateName)
    {
        String template = null;
        String[] templates = driver.getConfiguration().getDashboardTemplate();
        if ( templateName == null || templateName.trim().isEmpty() || templateName.trim().equalsIgnoreCase("default"))
        {
            for (String t : templates)
            {
                if ( t.startsWith("{") || t.startsWith("default"))
                {
                    template = t;
                    break;
                }
            }
        }
        else
        {
            for (String t : templates)
            {
                if ( t.startsWith(templateName))
                {
                    template = t;
                    break;
                }
            }
        }

        // remove the name prefix from the template
        if (template != null && template.contains("{")) {
            template = template.substring(template.indexOf("{"));
        }

        return template;
    }
    /**
     * Find the data source associated with a dashboard by inspecting the jsonData field
     * for the value of the field with the key "dashboardUid"
     * @param driver the GrafanaDriver
     * @param orgId the organization ID to filter on
     * @param dashboardUid the dashboard UID to search for
     * @return the GrafanaDataSource object associated with the dashboard
     */
    public GrafanaDataSource findDataSourceForDashboard(GrafanaDriver driver, String orgId, String dashboardUid)
    {
        GrafanaDataSource dataSource = null;
        List<GrafanaDataSource> dataSources = GrafanaDataSourceInvocator.findByOrg(driver, orgId);
        if ( dataSources != null && !dataSources.isEmpty() )
        {
            for (GrafanaDataSource ds : dataSources)
            {
                Map<String, String> jd = ds.getJsonData();
                if ( jd != null )
                {
                    String dashboardUidValue = jd.get(GrafanaDataSourceInvocator.DASHBOARD_UID);
                    if ( dashboardUidValue != null && dashboardUidValue.equalsIgnoreCase(dashboardUid))
                    {
                        dataSource = ds;
                    }
                }
            }
        }
        return dataSource;
    }

    public static GrafanaSearchResponse findDashboardForDataSource( GrafanaDriver driver, String orgId, String dataSourceUid)
    {
        GrafanaSearchResponse dashboardMeta = null;
        List<GrafanaSearchResponse> dashboardList = findDashboardsForOrg(driver, orgId);
        if ( dashboardList != null && !dashboardList.isEmpty())
        {
            for (GrafanaSearchResponse dashboard : dashboardList)
            {
                String dashboardData = getDashboardByUID(driver, orgId, dashboard.getUid());
                if ( dashboardData != null && dashboardData.contains(dataSourceUid))
                {
                    dashboardMeta = dashboard;
                }
            }
        }
        return dashboardMeta ;
    }

    public static GrafanaSearchResponse findDashboardForDataSource( GrafanaDriver driver, String orgId, List<GrafanaSearchResponse> dashboardList, List<String> dashboards, String dataSourceUid)
    {
        GrafanaSearchResponse dashboardMeta = null;

        if ( dashboardList != null && !dashboardList.isEmpty())
        {
            for (GrafanaSearchResponse dashboard : dashboardList)
            {
                String dashboardData = getDashboardByUID(driver, orgId, dashboard.getUid());
                if ( dashboardData != null && dashboardData.contains(dataSourceUid))
                {
                    dashboardMeta = dashboard;
                }
            }
        }
        return dashboardMeta ;
    }
    /**
     * Search for the dashboards in a specified organization
     * @param driver GrafanaDriver
     * @param orgId the organization ID to filter on
     * @return a list of GrafanaSearchResponse objects representing the dashboards found
     */
    public static List<GrafanaSearchResponse> findDashboardsForOrg(GrafanaDriver driver, String orgId)
    {
        List<GrafanaSearchResponse> searchResults = null;
        if ( orgId != null && StringUtils.isNumeric(orgId.trim()) )
        {
            RestResponseData<GrafanaSearchResponse[]> rd;
            Map<String, String> headers = driver.getAdminHeaders();
            headers.put(ORG_HEADER, orgId.trim());
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

    public GrafanaSearchResponse findDashboardsForOrgAndUid(GrafanaDriver driver, String org, String uid)
    {
        GrafanaSearchResponse dashboardMeta = null;
        List<GrafanaSearchResponse> searchResults = null;
        searchResults = findDashboardsForOrg(driver, org);
        if ( uid != null && searchResults != null && !searchResults.isEmpty())
        {
            for (GrafanaSearchResponse searchResult : searchResults)
            {
                if ( searchResult.getUid().equalsIgnoreCase(uid))
                {
                    dashboardMeta =  searchResult;
                }
            }
        }
        return dashboardMeta;
    }

    /**
     * @param driver    Driver belonging to this Invocator and providing interaction with the applicable
     *                  destination system.
     * @param id        String holding the id to match the item being updated on the destination system.
     *                  the id is composed of OrgId and UID separated by an underscore. eg "127_abc123efg"
     * @param dashboard Model holding the data for the object to be updated. Null fields present are
     *                  to be treated as elements that should remain unchanged on the destination system.
     * @throws ConnectorException Exception thrown if the update operation fails.
     */
    @Override
    public void update(GrafanaDriver driver, String id, GrafanaDashboard dashboard) throws ConnectorException
    {
        GrafanaDashboardResponse result = null;
        String template = null;
        if ( id != null && !id.trim().isEmpty())
        {
            String[] ids = decomposeID(id);
            if ( ids != null && ids.length ==2 && StringUtils.isNumeric(ids[0].trim()))
            {
                String orgId = ids[0];
                String uid = ids[1];
                if ( dashboard.getOrgId() == null )
                {
                    dashboard.setOrgId(Integer.valueOf(orgId));
                }
                if ( dashboard.getUid() == null )
                {
                    dashboard.setUid(uid);
                }
            }
        }
        GrafanaDashboard current = getOne(driver, id, null);
        if ( current != null && current.getUid() != null && !current.getUid().trim().isEmpty())
        {
            dashboard.setUid(current.getUid());
        }
        if ( current != null && current.getOrgId() != null && current.getOrgId() > 0)
        {
            dashboard.setOrgId(current.getOrgId());
        }

        if ( dashboard.getDashboard() != null && !dashboard.getDashboard().trim().isEmpty()) {
            // Use the raw inbound JSON string to update the dashboard
            LOG.info("Updating dashboard {0} for Org {1} with name {2}", id, dashboard.getOrgId(), dashboard.getOrgName());
            template = dashboard.getDashboard();
        }
        else
        {
            LOG.info("Updating dashboard {0} for Org {1} with template {2}", dashboard.getOrgId(), dashboard.getTemplateName());
            template = getTemplate(driver, dashboard.getTemplateName());
        }
        if ( dashboard.getDataSourceUid() != null && !dashboard.getDataSourceUid().trim().isEmpty())
        {
            template = template.replace("<DataSourceUID>", dashboard.getDataSourceUid());
            template = template.replace("__DataSourceUID__", dashboard.getDataSourceUid());
        }
        else if ( current != null && current.getDataSourceUid() != null && !current.getDataSourceUid().trim().isEmpty())
        {
            template = template.replace("<DataSourceUID>", current.getDataSourceUid());
            template = template.replace("__DataSourceUID__", current.getDataSourceUid());
        }
        if ( dashboard.getOrgName() != null && !dashboard.getOrgName().trim().isEmpty())
        {
            template = template.replace("__OrgName__", dashboard.getOrgName());
        }
        else if ( current != null && current.getOrgName() != null && !current.getOrgName().trim().isEmpty())
        {
            template = template.replace("__OrgName__", current.getOrgName());
        }
        else
        {
            template = template.replace("__OrgName__", "");
        }
        if ( dashboard.getId() != null )
        {
            template = template.replace("__DashboardId__", dashboard.getId().toString());
        }
        else if ( current != null && current.getId() != null )
        {
            template = template.replace("__DashboardId__", current.getId().toString());
        }
        else
        {
            template = template.replace("__DashboardId__", "null");
        }

        if ( dashboard.getUid() != null )
        {
            template = template.replace("__DashboardUid__", String.format("\"%s\"",dashboard.getUid()));
        }
        else if ( current != null && current.getUid() != null )
        {
            template = template.replace("__DashboardUid__", String.format("\"%s\"", current.getUid()));
        }
        else if ( dashboard.getDataSourceUid() != null )
        {
            template = template.replace("__DashboardUid__", String.format("\"%s\"",dashboard.getDataSourceUid()));
        }
        else
        {
            template = template.replace("__DashboardUid__", "null");
        }
        result = createDashboard(driver, String.valueOf(dashboard.getOrgId()), template);
        if ( result != null && result.getStatus() != null )
        {
            LOG.info("Updated Grafana Dashboard {0} with status {1} and message {2}", id, result.getStatus(), result.getMessage());
        }
    }
}


