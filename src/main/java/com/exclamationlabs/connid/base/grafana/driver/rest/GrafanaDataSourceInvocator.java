package com.exclamationlabs.connid.base.grafana.driver.rest;

import com.exclamationlabs.connid.base.connector.driver.DriverInvocator;
import com.exclamationlabs.connid.base.connector.results.ResultsFilter;
import com.exclamationlabs.connid.base.connector.results.ResultsPaginator;
import com.exclamationlabs.connid.base.grafana.model.GrafanaDataSource;
import com.exclamationlabs.connid.base.connector.driver.rest.RestResponseData;
import com.exclamationlabs.connid.base.grafana.model.GrafanaOrg;
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
    public String createDashboard(GrafanaDriver driver, String orgId, String dashboard)
    {
        String success = null;
        RestResponseData<GrafanaDashboardResponse> response;
        Map<String, String> headers = driver.getAdminHeaders();
        headers.put(ORG_HEADER, String.valueOf(orgId));

        response = driver.executePostRequest("/dashboards/db",
                GrafanaDashboardResponse.class,
                dashboard,
                headers);
        return success;
    }
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
            headers.put(ORG_HEADER, String.valueOf(dataSource.getOrgId()));
            if (dataSource.getBasicAuth() == null )
            {
                dataSource.setBasicAuth(true);
            }

            if ( dataSource.getType() == null || dataSource.getType().trim().length() == 0)
            {
                dataSource.setType("loki");
            }

            if ( dataSource.getBasicAuth()  )
            {
                if ( dataSource.getBasicAuthUser() == null || dataSource.getBasicAuthUser().trim().length() == 0 )
                {
                    dataSource.setBasicAuthUser(driver.getConfiguration().getLokiUser());
                }
                if ( dataSource.getBasicAuthPassword() == null
                        || dataSource.getBasicAuthPassword().trim().length() == 0 )
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

            if ( dataSource.getUrl() == null || dataSource.getUrl().trim().length() == 0 )
            {
                dataSource.setUrl(driver.getConfiguration().getLokiURL());
            }

            if ( dataSource.getAccess() == null || dataSource.getAccess().trim().length() == 0)
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

        if ( createInfo == null || createInfo.getId() == null || createInfo.getId().trim().length() == 0 )
        {
            String message = "HTTP " + response.getResponseStatusCode() +
                    ". Failed to create Global Grafana User: " + createInfo.getMessage() ;
            LOG.warn( message);
            throw new ConnectorException(message);
        }
        else
        {
            // Construct __UID__
            UID = createInfo.getDatasource().getOrgId() + "_" + createInfo.getDatasource().getUid();
            try
            {
                if ( driver.getConfiguration().getUpdateDashBoards() != null && driver.getConfiguration().getUpdateDashBoards())
                {
                    String template = driver.getConfiguration().getDashboardTemplate();
                    template = template.replace("<DataSourceUID>", createInfo.getDatasource().getUid());
                    template = template.replace("__DataSourceUID__", createInfo.getDatasource().getUid());
                    createDashboard(driver, String.valueOf(createInfo.getDatasource().getOrgId()), template);
                }
            }
            catch (Exception exception)
            {
                LOG.warn(exception.getMessage());
            }
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

                    if ( dataSource.getJsonData() == null || dataSource.getJsonData().size() == 0 )
                    {
                        dataSource.setJsonData(actual.getJsonData());
                    }
                    else if ( actual.getJsonData() != null && actual.getJsonData().size() > 0 )
                    {
                        actual.getJsonData().putAll(dataSource.getJsonData());
                        dataSource.setJsonData(actual.getJsonData());
                    }

                    if ( dataSource.getSecureJsonData() == null || dataSource.getSecureJsonData().size() == 0 )
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
/*
                    if ( dataSource.getReadOnly() == null )
                    {
                        dataSource.setReadOnly(actual.getReadOnly());
                    }

                    if ( dataSource.getVersion() == null || dataSource.getVersion().trim().length() == 0)
                    {
                        String version = actual.getVersion();
                        if ( actual.getVersion() !=  null && StringUtils.isNumeric(actual.getVersion().trim()))
                        {
                            Integer ver = Integer.valueOf(actual.getVersion().trim());
                            if ( ver != null)
                            {
                                ver += 1;
                                version = String.valueOf(ver);
                            }
                        }
                        dataSource.setVersion(version);
                    }
*/
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
                        try
                        {
                            if ( driver.getConfiguration().getUpdateDashBoards() != null && driver.getConfiguration().getUpdateDashBoards())
                            {
                                String template = driver.getConfiguration().getDashboardTemplate();
                                template = template.replace("<DataSourceUID>", dataSource.getUid());
                                template = template.replace("__DataSourceUID__", dataSource.getUid());
                                createDashboard(driver, String.valueOf(dataSource.getOrgId()), template);
                            }
                        }
                        catch (Exception exception)
                        {
                            LOG.warn(exception.getMessage());
                        }
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
}
