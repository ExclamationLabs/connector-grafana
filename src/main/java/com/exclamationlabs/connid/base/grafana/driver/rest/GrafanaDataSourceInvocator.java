package com.exclamationlabs.connid.base.grafana.driver.rest;

import com.exclamationlabs.connid.base.connector.driver.DriverInvocator;
import com.exclamationlabs.connid.base.connector.results.ResultsFilter;
import com.exclamationlabs.connid.base.connector.results.ResultsPaginator;
import com.exclamationlabs.connid.base.grafana.model.GrafanaDataSource;
import com.exclamationlabs.connid.base.connector.driver.rest.RestResponseData;
import com.exclamationlabs.connid.base.grafana.model.GrafanaOrg;
import com.exclamationlabs.connid.base.grafana.model.GrafanaOrgPreferences;
import com.exclamationlabs.connid.base.grafana.model.response.GrafanaDashboardResponse;
import com.exclamationlabs.connid.base.grafana.model.response.GrafanaDatasourceResponse;
import com.exclamationlabs.connid.base.grafana.model.response.GrafanaStandardResponse;
import org.apache.commons.lang3.StringUtils;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

import java.util.*;

public class GrafanaDataSourceInvocator implements DriverInvocator<GrafanaDriver, GrafanaDataSource>
{
    private static final Log LOG = Log.getLog(GrafanaDataSourceInvocator.class);
    public static final String ORG_HEADER = "X-Grafana-Org-Id";
    public static final String DASHBOARD_TEMPLATE_NAME = "dashboardTemplateName";

    /**
     * Decomposed an identity value into orgId and Datasource ID or DataSource Name.
     * The orgId is expected to be followed by underscore "_"
     * @param composed An identity value that is prefixed with the orgId and the actual ID
     * @return String array of 2 decomposed ids or null when no underscore separator is found
     */
    public static String[] decomposeID(String composed)
    {
        String[] items = null;
        if ( composed != null )
        {
            int idx = composed.indexOf("_");
            if ( idx > 0 && composed.length() >= idx+1)
            {
                items = new String[2];
                String orgId = composed.substring(0,idx);
                String id    = composed.substring(idx+1);
                items[0] = orgId;
                items[1] = id;
            }
        }
        return items;
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

        rd = driver.executePostRequest("/dashboards/db",
                GrafanaDashboardResponse.class,
                dashboard,
                headers);

        if ( rd != null && rd.getResponseObject() != null )
        {
            dashboardInfo = rd.getResponseObject();
        }
        return dashboardInfo ;
    }

    public static String getTemplate(GrafanaDriver driver, String templateName)
    {
        String template = null;
        String[] templates = driver.getConfiguration().getDashboardTemplate();
        if ( templateName == null || templateName.trim().isEmpty() || templateName.equalsIgnoreCase("default"))
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

        if (template != null && template.contains("{")) {
            template = template.substring(template.indexOf("{"));
        }

        return template;
    }
    /**
     * Update the Dashboard and Org Preferences with a new or update datasource
     * @param driver Grafana Driver
     * @param orgId  The orgId of the Datasource
     * @param dataSourceUid The dataSourceId
     */
    public static void updateDashboardAndPreferences(GrafanaDriver driver, String orgId, String dataSourceUid, String orgName, String dashboardTemplateName)
    {
        try
        {
            if ( driver.getConfiguration().getUpdateDashBoards() != null
                    && driver.getConfiguration().getUpdateDashBoards()
                    && driver.getConfiguration().getDashboardTemplate() != null
                    && driver.getConfiguration().getDashboardTemplate().length > 0 )
            {
                String[] templates = driver.getConfiguration().getDashboardTemplate();
                String template = getTemplate(driver, dashboardTemplateName);
                template = template.replace("<DataSourceUID>", dataSourceUid);
                template = template.replace("__DataSourceUID__", dataSourceUid);
                template = template.replace("__OrgName__", orgName);
                GrafanaDashboardResponse dashboardInfo = createDashboard(driver, orgId, template);
                if ( dashboardInfo != null )
                {
                    GrafanaOrgPreferences current = GrafanaOrgInvocator.getOrganizationPreferences(driver, Integer.valueOf(orgId));
                    GrafanaOrgPreferences preferences = GrafanaOrgInvocator.setOrganizationPreferences(driver,
                            current, dashboardInfo.getId(), dashboardInfo.getUid());
                    GrafanaOrgInvocator.updateOrganizationPreferences(driver, Integer.valueOf(orgId), preferences);
                }
            }
        }
        catch (Exception exception)
        {
            LOG.warn(exception.getMessage());
        }
    }

    /**
     * Create a Grafana Datasource
     * @param driver The Grafana Driver
     * @param dataSource The Datasource to create
     * @return The UID of the created Datasource
     * @throws ConnectorException when a failure occurs
     */
    @Override
    public String create(GrafanaDriver driver, GrafanaDataSource dataSource) throws ConnectorException
    {
        RestResponseData<GrafanaDatasourceResponse> response;
        GrafanaDatasourceResponse createInfo;
        String UID = null;
        Map<String, String> headers = driver.getAdminHeaders();

        // we will not create a datasource for an unspecified organization
        if ( dataSource.getOrgId() != null && dataSource.getOrgId() > 0  && dataSource.getName() != null )
        {
            if ( dataSource.getOrgName() == null || dataSource.getOrgName().trim().isEmpty())
            {
                GrafanaOrg org = getOrgInfo(driver, String.valueOf(dataSource.getOrgId()));
                if ( org != null )
                {
                    dataSource.setOrgName(org.getName());
                }
            }
            headers.put(ORG_HEADER, String.valueOf(dataSource.getOrgId()));
            if (dataSource.getBasicAuth() == null )
            {
                dataSource.setBasicAuth(true);
            }

            if ( dataSource.getType() == null || dataSource.getType().trim().isEmpty())
            {
                dataSource.setType("loki");
            }

            if ( dataSource.getBasicAuth()  )
            {
                if ( dataSource.getBasicAuthUser() == null || dataSource.getBasicAuthUser().trim().isEmpty())
                {
                    dataSource.setBasicAuthUser(driver.getConfiguration().getLokiUser());
                }
                if ( dataSource.getBasicAuthPassword() == null
                        || dataSource.getBasicAuthPassword().trim().isEmpty())
                {
                    if ( dataSource.getType() != null && dataSource.getType().equalsIgnoreCase("loki"))
                    {
                        dataSource.setBasicAuthPassword(driver.getConfiguration().getLokiPassword());
                        if (dataSource.getSecureJsonData()  == null )
                        {
                            Map<String, String> secureJsonData = new HashMap<>();
                            secureJsonData.put("basicAuthPassword", driver.getConfiguration().getLokiPassword());
                            dataSource.setSecureJsonData(secureJsonData);
                        }
                        else
                        {
                            dataSource.getSecureJsonData().putIfAbsent("basicAuthPassword", driver.getConfiguration().getLokiPassword());
                        }
                    }
                }
            }

            if ( dataSource.getJsonData() == null || dataSource.getJsonData().isEmpty())
            {
                Map<String, String> jsonData = new HashMap<>();
                jsonData.put("httpHeaderName1", "X-Scope-OrgID");
                dataSource.setJsonData(jsonData);
            }

            if ( dataSource.getDashboardTemplateName() != null && !dataSource.getDashboardTemplateName().trim().isEmpty())
            {
                dataSource.getJsonData().putIfAbsent(DASHBOARD_TEMPLATE_NAME, dataSource.getDashboardTemplateName());
            }

            if ( dataSource.getSecureJsonData() == null || dataSource.getSecureJsonData().isEmpty())
            {
                Map<String, String> secureJsonData = new HashMap<>();
                dataSource.setSecureJsonData(secureJsonData);
            }

            dataSource.getSecureJsonData().putIfAbsent("httpHeaderValue1", dataSource.getOrgName());

            if ( dataSource.getUrl() == null || dataSource.getUrl().trim().isEmpty())
            {
                dataSource.setUrl(driver.getConfiguration().getLokiURL());
            }

            if ( dataSource.getAccess() == null || dataSource.getAccess().trim().isEmpty())
            {
                dataSource.setAccess("proxy");
            }
            if ( dataSource.getDefault() == null )
            {
                dataSource.setDefault(true);
            }
            String message = String.format("Creating Datasource  with name %s for Org %s", dataSource.getName(), dataSource.getOrgId());
            LOG.info(message);
        }
        else
        {
            String message = String.format("Cannot create Datasource for invalid Org %s with name %s", dataSource.getOrgId(), dataSource.getName());
            LOG.error(message);
            throw new ConnectorException(message);
        }

        response = driver.executePostRequest("/datasources",
                                            GrafanaDatasourceResponse.class,
                                            dataSource,
                                            headers);


        createInfo = response.getResponseObject();

        if ( createInfo == null || createInfo.getId() == null || createInfo.getId().trim().isEmpty())
        {
            String message = "HTTP " + response.getResponseStatusCode() +
                    ". Failed to create Grafana Datasource: " + createInfo.getMessage() ;
            LOG.warn( message);
            throw new ConnectorException(message);
        }
        else
        {
            // Construct __UID__ of the datasource
            UID = createInfo.getDatasource().getOrgId() + "_" + createInfo.getDatasource().getUid();

            // If a datasource is created, also create a dashboard for that datasource using the existing
            // dashboard template and then set the org preferences
            updateDashboardAndPreferences(driver,
                    String.valueOf(createInfo.getDatasource().getOrgId()),
                    createInfo.getDatasource().getUid(),
                    dataSource.getOrgName(),
                    dataSource.getDashboardTemplateName());
        }

        return UID;
    }

    /**
     * @param driver The Grafana Driver
     * @param id The Identity of the Grafana Datasource to update
     * @param dataSource The changes in the Datasource
     * @throws ConnectorException
     */
    @Override
    public void update(GrafanaDriver driver, String id, GrafanaDataSource dataSource) throws ConnectorException
    {
        Map<String, String> headers = driver.getAdminHeaders();
        LOG.info(String.format("Processing request to update Grafana Datasource uid %s", id));
        GrafanaDataSource actual = getOne(driver, id, null);
        if ( actual != null && id != null && id.trim().length() > 0 )
        {
            String[] ids = decomposeID(id);
            if ( ids != null && ids.length == 2)
            {
                if (StringUtils.isNumeric(ids[0]))
                {
                    String orgId = ids[0];
                    String idDatasource = ids[1];
                    dataSource.setOrgId(Integer.valueOf(orgId));
                    GrafanaOrg org = getOrgInfo(driver, orgId);
                    if ( org != null )
                    {
                        dataSource.setOrgName(org.getName());
                    }
                    if ( dataSource.getName() == null || dataSource.getName().trim().length() == 0 )
                    {
                        dataSource.setName(actual.getName());
                    }
                    if ( dataSource.getUid() == null || dataSource.getUid().trim().length() == 0 )
                    {
                        dataSource.setUid(actual.getUid());
                    }
                    else
                    {
                        // just in case uid is composed outbound we will peel off the actual Uid
                        String[] uids = decomposeID(dataSource.getUid());
                        if ( uids != null && uids.length == 2 && StringUtils.isNumeric(uids[0]))
                        {
                            dataSource.setUid(uids[1]);
                        }
                    }
                    if ( dataSource.getId() == null || dataSource.getId() <= 0 )
                    {
                        // id not required on update but setting anyway
                        dataSource.setId(actual.getId());
                    }
                    if ( dataSource.getAccess() == null || dataSource.getAccess().trim().length() == 0 )
                    {
                        // access is required on update
                        dataSource.setAccess(actual.getAccess());
                    }
                    if ( dataSource.getType() == null || dataSource.getType().trim().length() == 0 )
                    {
                        // type is required on update
                        dataSource.setType(actual.getType());
                    }
                    if ( dataSource.getUrl() == null || dataSource.getUrl().trim().length() == 0 )
                    {
                        // url is required on update
                        dataSource.setUrl(actual.getUrl());
                    }

                    if ( dataSource.getJsonData() == null || dataSource.getJsonData().isEmpty())
                    {
                        dataSource.setJsonData(actual.getJsonData());
                    }
                    else if ( actual.getJsonData() != null && !actual.getJsonData().isEmpty())
                    {
                        actual.getJsonData().putAll(dataSource.getJsonData());
                        dataSource.setJsonData(actual.getJsonData());
                    }

                    if ( dataSource.getDashboardTemplateName() == null || dataSource.getDashboardTemplateName().trim().isEmpty())
                    {
                        if ( dataSource.getJsonData().get(DASHBOARD_TEMPLATE_NAME) != null && !dataSource.getJsonData().get(DASHBOARD_TEMPLATE_NAME).trim().isEmpty())
                        {
                            dataSource.setDashboardTemplateName(dataSource.getJsonData().get(DASHBOARD_TEMPLATE_NAME));
                        }
                    }

                    if ( dataSource.getSecureJsonData() == null || dataSource.getSecureJsonData().isEmpty())
                    {
                        dataSource.setSecureJsonData(actual.getSecureJsonData());
                    }
                    else if ( actual.getSecureJsonData() != null && actual.getSecureJsonData().size() > 0 )
                    {
                        actual.getSecureJsonData().putAll(dataSource.getSecureJsonData());
                        dataSource.setSecureJsonData(actual.getSecureJsonData());
                    }

                    if ( dataSource.getBasicAuth() == null )
                    {
                        dataSource.setBasicAuth(actual.getBasicAuth());
                    }

                    if ( dataSource.getBasicAuthUser() == null || dataSource.getBasicAuthUser().trim().length() == 0 )
                    {
                        dataSource.setBasicAuthUser(actual.getBasicAuthUser());
                    }

                    if ( dataSource.getBasicAuthPassword()== null || dataSource.getBasicAuthPassword().trim().length()==0)
                    {
                        dataSource.setBasicAuthPassword(actual.getBasicAuthPassword());
                    }

                    if ( dataSource.getDefault() == null )
                    {
                        dataSource.setDefault(actual.getDefault());
                    }

                    if ( dataSource.getDatabase() == null || dataSource.getDatabase().trim().length()==0 )
                    {
                        dataSource.setDatabase(actual.getDatabase());
                    }

                    if ( dataSource.getTypeLogoURL() == null || dataSource.getTypeLogoURL().trim().length()==0 )
                    {
                        dataSource.setTypeLogoURL(actual.getTypeLogoURL());
                    }

                    headers.put(ORG_HEADER, orgId);
                    RestResponseData<GrafanaDatasourceResponse> rd;
                    if ( !id.equalsIgnoreCase(actual.getIdentityIdValue()))
                    {
                        idDatasource = actual.getUid();
                    }
                    rd = driver.executePutRequest("/datasources/uid/" + idDatasource,
                            GrafanaDatasourceResponse.class,
                            dataSource,
                            headers);
                    // GrafanaDatasourceResponse response = rd.getResponseObject();
                    LOG.info(String.format("Grafana Datasource Update response status %d for uid %s", rd.getResponseStatusCode(), id));
                    if ( rd.getResponseStatusCode() == 200 )
                    {
                        updateDashboardAndPreferences(driver,
                                String.valueOf(dataSource.getOrgId()),
                                dataSource.getUid(),
                                dataSource.getOrgName(),
                                dataSource.getDashboardTemplateName());
                    }
                }
                else
                {
                    throw new ConnectorException("Grafana DataSource orgId not numeric. Expected numerical OrgId prefix in Id Value");
                }
            }
            else
            {
                throw new ConnectorException("Grafana DataSource ID not decomposable. Expected numerical Org Prefix");
            }
        }
        else
        {
            throw new ConnectorException("Grafana DataSource ID not found");
        }
    }

    /**
     * Delete a Grafana Datasource
     * @param driver The Grafana REST Driver
     * @param id The composed ID of the datasource prefixed by orgId adn sepat
     * @throws ConnectorException
     */
    @Override
    public void delete(GrafanaDriver driver, String id) throws ConnectorException
    {
        LOG.info(String.format("Processing request to Delete Grafana Datasource uid %s", id));
        Map<String, String> headers = driver.getAdminHeaders();
        GrafanaDataSource dataSource = getOne(driver, id, null);
        if ( dataSource != null )
        {
            Integer orgId = dataSource.getOrgId();
            String name = dataSource.getName();
            headers.put(ORG_HEADER, orgId.toString());
            RestResponseData<GrafanaStandardResponse> rd;
            rd = driver.executeDeleteRequest("/datasources/name/" + name,
                    GrafanaStandardResponse.class,
                    headers);
            GrafanaStandardResponse response = rd.getResponseObject();
            String message = response.getMessage();
        }
        else
        {
            LOG.warn(String.format("Grafana Datasource %s not found", id));
        }
    }

    public static List<GrafanaDataSource> findByOrg(GrafanaDriver driver, int orgId)
            throws ConnectorException
    {
        GrafanaDataSource[] dataSourceArray;
        List<GrafanaDataSource> dataSources = null;
        RestResponseData<GrafanaDataSource[]> rd;
        Map<String, String> headers = driver.getAdminHeaders();
        if ( orgId > 0 )
        {
            headers.put(ORG_HEADER, Integer.valueOf(orgId).toString()); // orgId is a number
            rd = driver.executeGetRequest("/datasources", GrafanaDataSource[].class, headers);
            dataSourceArray = rd.getResponseObject();
            dataSources = Arrays.asList(dataSourceArray);
        }
        return dataSources;
    }
    /**
     * Retrieves all Data Sources or handles a results filter request for data sources related to an organization
     * @param driver The grafana REST driver
     * @param resultsFilter A filter which can be used to select the datasources to retrieve
     * @param paginator Paginator information is ignored
     * @param maxResultRecords
     * @return Hashset of Data Sources;
     * @throws ConnectorException
     */
    @Override
    public Set<GrafanaDataSource> getAll(GrafanaDriver driver, ResultsFilter resultsFilter, ResultsPaginator paginator, Integer maxResultRecords)
            throws ConnectorException
    {
        GrafanaDataSource[] dataSourceArray;
        Set<GrafanaDataSource> dataSources = null;
        RestResponseData<GrafanaDataSource[]> rd;
        Map<String, String> headers = driver.getAdminHeaders();
        if ( resultsFilter != null && resultsFilter.hasFilter() && resultsFilter.getAttribute().equalsIgnoreCase("orgId"))
        {
            headers.put(ORG_HEADER, resultsFilter.getValue());
            rd = driver.executeGetRequest("/datasources", GrafanaDataSource[].class, headers);
            dataSourceArray = rd.getResponseObject();
            dataSources = new HashSet<>(Arrays.asList(dataSourceArray));
        }
        else
        {
            // This may be a heavy lift
            dataSources = new HashSet<GrafanaDataSource>();
            LOG.info("Downloading Grafana Organizations");
            GrafanaOrgInvocator orgInvocator = new GrafanaOrgInvocator();
            Set<GrafanaOrg> orgSet = orgInvocator.getAll( driver, null, paginator, null);
            List<GrafanaOrg> list  = new ArrayList<>(orgSet);
            for (GrafanaOrg org : list )
            {
                try
                {
                    headers.put(ORG_HEADER, String.valueOf(org.getId()));
                    LOG.info("Downloading Datasource for Org {0}",  org.getId());
                    rd = driver.executeGetRequest("/datasources", GrafanaDataSource[].class, headers);
                    dataSourceArray = rd.getResponseObject();
                    if ( dataSourceArray != null && dataSourceArray.length > 0 )
                    {
                        dataSources.addAll(new HashSet<>(Arrays.asList(dataSourceArray)));
                    }
                }
                catch (Exception e)
                {
                    LOG.error(e.getMessage());
                }
            }
        }
        return dataSources;
    }

    @Override
    public GrafanaDataSource getOne(GrafanaDriver driver, String id, Map<String, Object> map) throws ConnectorException
    {
        GrafanaDataSource datasource = null;
        Map<String, String> headers = driver.getAdminHeaders();
        LOG.info(String.format("Processing request to Lookup Grafana Datasource by uid %s", id));
        if ( id != null && id.trim().length() > 0 )
        {
            String[] ids = decomposeID(id);
            if ( ids != null && ids.length == 2)
            {
                if (StringUtils.isNumeric(ids[0]))
                {
                    String orgId = ids[0];
                    String idDatasource = ids[1];
                    headers.put(ORG_HEADER, orgId);
                    RestResponseData<GrafanaDataSource> rd;
                    rd = driver.executeGetRequest("/datasources/uid/" + idDatasource,
                            GrafanaDataSource.class,
                            headers);
                    datasource = rd.getResponseObject();
                    if ( datasource == null )
                    {
                        String path = "/datasources/name/" + idDatasource.trim();
                        path = driver.encodeURIPath(path);
                        rd = driver.executeGetRequest(path, GrafanaDataSource.class, headers);
                        datasource = rd.getResponseObject();
                    }
                    if ( datasource != null )
                    {
                        GrafanaOrg org = getOrgInfo(driver, orgId);
                        if ( org != null )
                        {
                            datasource.setOrgName(org.getName());
                        }
                        if ( datasource.getJsonData() != null && datasource.getJsonData().get(DASHBOARD_TEMPLATE_NAME) != null )
                        {
                            datasource.setDashboardTemplateName(datasource.getJsonData().get(DASHBOARD_TEMPLATE_NAME));
                        }
                        updateDashboardAndPreferences(driver, orgId, datasource.getUid(), datasource.getOrgName(), datasource.getDashboardTemplateName());
                    }
                }
                else
                {
                    throw new ConnectorException("Grafana DataSource orgId not numeric. Expected numerical OrgId prefix in Id Value");
                }
            }
            else
            {
                throw new ConnectorException("Grafana DataSource ID not decomposable. Expected numerical Org Prefix");
            }
        }
        else
        {
            throw new ConnectorException("Grafana DataSource ID not specified");
        }
        return datasource;
    }

    /**
     * Get a Grafana Datasource by Name
     * @param driver
     * @param name
     * @return
     * @throws ConnectorException
     */
    @Override
    public GrafanaDataSource getOneByName(GrafanaDriver driver, String name) throws ConnectorException
    {
        Map<String, String> headers = driver.getAdminHeaders();
        LOG.info(String.format("Processing request to Lookup Grafana Datasource by name %s", name));
        GrafanaDataSource datasource = null;
        if ( name != null && name.trim().length() > 0 )
        {
            String[] ids = decomposeID(name);
            if ( ids != null && ids.length == 2)
            {
                if (StringUtils.isNumeric(ids[0]))
                {
                    String orgId = ids[0];
                    String idName= ids[1];
                    headers.put(ORG_HEADER, orgId);
                    String path = "/datasources/name/" + idName.trim();
                    path = driver.encodeURIPath(path);
                    RestResponseData<GrafanaDataSource> rd;
                    rd = driver.executeGetRequest(path, GrafanaDataSource.class, headers);
                    datasource = rd.getResponseObject();
                    if ( datasource == null )
                    {
                        rd = driver.executeGetRequest("/datasources/uid/" + idName.trim(),
                                GrafanaDataSource.class,
                                headers);
                        datasource = rd.getResponseObject();
                    }
                    if ( datasource != null )
                    {
                        GrafanaOrg org = getOrgInfo(driver, orgId);
                        if ( org != null )
                        {
                            datasource.setOrgName(org.getName());
                        }
                        if ( datasource.getJsonData() != null && datasource.getJsonData().get(DASHBOARD_TEMPLATE_NAME) != null )
                        {
                            datasource.setDashboardTemplateName(datasource.getJsonData().get(DASHBOARD_TEMPLATE_NAME));
                        }
                        updateDashboardAndPreferences(driver, orgId, datasource.getUid(), datasource.getOrgName(), datasource.getDashboardTemplateName());
                    }
                }
                else
                {
                    throw new ConnectorException("Grafana DataSource orgId not numeric. Expected numerical OrgId prefix in Name Value");
                }
            }
            else
            {
                throw new ConnectorException("Grafana DataSource ID not decomposable. Expected numerical Org Prefix");
            }
        }
        else
        {
            throw new ConnectorException("Grafana DataSource name not specified");
        }
        return datasource;
    }

    /**
     * Get a Single Org from the Grafana Service
     * @param driver The Rest Driver for the Connector
     * @param id the unique id of the User
     * @return A Single Org or null
     * @throws ConnectorException when a failure occurs
     */
    public GrafanaOrg getOrgInfo(GrafanaDriver driver, String id) throws ConnectorException
    {
        GrafanaOrg org = null;

        if ( id != null && id.trim().length() > 0 && StringUtils.isNumeric(id.trim()) )
        {
            LOG.info("Lookup Org with ID {0} ", id);
            RestResponseData<GrafanaOrg> rd;
            rd = driver.executeGetRequest("/orgs/" + id.trim(),
                    GrafanaOrg.class,
                    driver.getAdminHeaders());
            org = rd.getResponseObject();
        }
        return org;
    }
}
