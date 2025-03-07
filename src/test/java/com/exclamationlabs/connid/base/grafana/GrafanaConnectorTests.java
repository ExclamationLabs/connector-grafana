package com.exclamationlabs.connid.base.grafana;

import com.exclamationlabs.connid.base.grafana.attribute.GrafanaDashboardAttribute;
import com.exclamationlabs.connid.base.grafana.attribute.GrafanaDataSourceAttribute;
import com.exclamationlabs.connid.base.grafana.attribute.GrafanaOrgAttribute;
import com.exclamationlabs.connid.base.grafana.attribute.GrafanaUserAttribute;
import com.exclamationlabs.connid.base.grafana.configuration.GrafanaConfiguration;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.spi.SearchResultsHandler;
import org.identityconnectors.test.common.ToListResultsHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class GrafanaConnectorTests
{
    private GrafanaConnector connector = new GrafanaConnector();
    private GrafanaConfiguration configuration = new GrafanaConfiguration();
    private final ArrayList<ConnectorObject> results = new ArrayList<>();
    private String dataSourceName = null;
    private Boolean grafana_local = true;
    private String dashboardUid = "6_be6iuisslqkn4e";


    /**
     * Implementation to capture an array list of search results
     */
    SearchResultsHandler handler = new SearchResultsHandler()
    {
        @Override
        public boolean handle(ConnectorObject connectorObject) {
            results.add(connectorObject);
            return true;
        }

        @Override
        public void handleResult(SearchResult result) {
        }
    };

    @BeforeEach
    public void setup()
    {
        connector = new GrafanaConnector();
        configuration = new GrafanaConfiguration();

        configuration.setServiceUrl("http://localhost:3000/api/");
        configuration.setBasicUsername("admin");
        configuration.setBasicPassword("password");

        if ( grafana_local )
        {
            configuration.setServiceUrl("http://localhost:3000/api/");
            configuration.setBasicUsername("admin");
            configuration.setBasicPassword("password");
            configuration.setLokiUser("admin");
            configuration.setLokiPassword("admin");
            configuration.setLokiURL("http://localhost:3100/");
        }
        else
        {
            configuration.setServiceUrl("http://localhost:3000/api/");
            configuration.setBasicUsername("admin");
            configuration.setBasicPassword("password");
            configuration.setLokiUser("admin");
            configuration.setLokiPassword("admin");
            configuration.setLokiURL("http://localhost:3100/");
        }

        configuration.setActive(true);
        configuration.setPagination(true);
        configuration.setUpdateDashBoards(true);
        configuration.setSeparateOrgAssociation(false);
        String defaultTemplate = "{\"dashboard\":{\"id\":__DashboardId__,\"uid\":__DashboardUid__,\"title\":\"__OrgName__ eduroam RADIUS Logs\",\"schemaVersion\":35,\"version\":2,\"timezone\":\"browser\",\"annotations\":{\"list\":[{\"builtIn\":1,\"datasource\":\"-- Grafana --\",\"enable\":true,\"hide\":true,\"iconColor\":\"rgba(0, 211, 255, 1)\",\"name\":\"Annotations & Alerts\",\"target\":{\"limit\":100,\"matchAny\":false,\"tags\":[],\"type\":\"dashboard\"},\"type\":\"dashboard\"}]},\"editable\":true,\"fiscalYearStartMonth\":0,\"graphTooltip\":0,\"links\":[],\"liveNow\":false,\"panels\":[{\"description\":\"\",\"fieldConfig\":{\"defaults\":{\"color\":{\"mode\":\"thresholds\"},\"custom\":{\"align\":\"auto\",\"displayMode\":\"auto\",\"filterable\":true},\"mappings\":[],\"thresholds\":{\"mode\":\"absolute\",\"steps\":[{\"color\":\"green\",\"value\":\"\"},{\"color\":\"red\",\"value\":80}]}},\"overrides\":[{\"matcher\":{\"id\":\"byName\",\"options\":\"TIMESTAMP\"},\"properties\":[{\"id\":\"custom.width\",\"value\":193}]}]},\"gridPos\":{\"h\":11,\"w\":23,\"x\":0,\"y\":0},\"id\":13,\"options\":{\"footer\":{\"fields\":\"\",\"reducer\":[\"sum\"],\"show\":false},\"showHeader\":true,\"sortBy\":[{\"desc\":true,\"displayName\":\"TIMESTAMP\"}]},\"pluginVersion\":\"8.4.3\",\"targets\":[{\"datasource\":{\"type\":\"loki\",\"uid\":\"__DataSourceUID__\"},\"expr\":\"{record_type=\\\"fticks\\\"}\",\"maxLines\":5000,\"refId\":\"A\"}],\"title\":\"Authentication Logs\",\"transformations\":[{\"id\":\"merge\",\"options\":{}},{\"id\":\"extractFields\",\"options\":{\"format\":\"auto\",\"replace\":false,\"source\":\"line\"}},{\"id\":\"organize\",\"options\":{\"excludeByName\":{\"FAILURE\":false,\"NEXTHOP\":false,\"REALM\":false,\"REGION\":true,\"TIMESTAMP\":true,\"Time\":false,\"Time ns\":true,\"UTC\":true,\"VISINST\":true,\"id\":true,\"line\":true,\"line {record_type=\\\"fticks\\\", side=\\\"rp\\\"}\":true,\"ts\":false,\"tsNs\":true},\"indexByName\":{\"14\":14,\"15\":15,\"2022\":16,\"CSI\":9,\"EAPTYPE\":8,\"FAILURE\":6,\"NEXTHOP\":4,\"REALM\":3,\"REGION\":12,\"RESULT\":5,\"Realm\":19,\"TIMESTAMP\":1,\"UTC\":13,\"VISCOUNTRY\":7,\"VISINST\":10,\"VISINSTID\":2,\"at\":23,\"does\":20,\"dot\":26,\"have\":22,\"id\":11,\"least\":24,\"line\":17,\"not\":21,\"one\":25,\"separator\":27,\"ts\":0,\"tsNs\":18},\"renameByName\":{\"Time\":\"TIMESTAMP\",\"ts\":\"TIMESTAMP\"}}},{\"id\":\"filterFieldsByName\",\"options\":{\"include\":{\"names\":[\"VISINSTID\",\"REALM\",\"NEXTHOP\",\"RESULT\",\"FAILURE\",\"VISCOUNTRY\",\"EAPTYPE\",\"CSI\",\"TIMESTAMP\"]}}}],\"type\":\"table\"},{\"gridPos\":{\"h\":12,\"w\":23,\"x\":0,\"y\":11},\"id\":6,\"options\":{\"dedupStrategy\":\"none\",\"enableLogDetails\":true,\"prettifyLogMessage\":false,\"showCommonLabels\":false,\"showLabels\":false,\"showTime\":true,\"sortOrder\":\"Descending\",\"wrapLogMessage\":false},\"targets\":[{\"datasource\":{\"type\":\"loki\",\"uid\":\"__DataSourceUID__\"},\"expr\":\"{record_type=\\\"stdout\\\"}\",\"maxLines\":5000,\"refId\":\"A\"}],\"title\":\"RADIUS Server Logs\",\"type\":\"logs\"}],\"style\":\"dark\",\"tags\":[],\"templating\":{\"list\":[]},\"time\":{\"from\":\"now-15m\",\"to\":\"now\"},\"timepicker\":{},\"weekStart\":\"\"},\"folderId\":0,\"overwrite\":true}";
        String ecoTemplate = "eso{\"dashboard\":{\"id\":__DashboardId__,\"uid\":__DashboardUid__,\"title\":\"__OrgName__ ESO eduroam RADIUS Logs\",\"schemaVersion\":35,\"version\":2,\"timezone\":\"browser\",\"annotations\":{\"list\":[{\"builtIn\":1,\"datasource\":\"-- Grafana --\",\"enable\":true,\"hide\":true,\"iconColor\":\"rgba(0, 211, 255, 1)\",\"name\":\"Annotations & Alerts\",\"target\":{\"limit\":100,\"matchAny\":false,\"tags\":[],\"type\":\"dashboard\"},\"type\":\"dashboard\"}]},\"editable\":true,\"fiscalYearStartMonth\":0,\"graphTooltip\":0,\"links\":[],\"liveNow\":false,\"panels\":[{\"description\":\"\",\"fieldConfig\":{\"defaults\":{\"color\":{\"mode\":\"thresholds\"},\"custom\":{\"align\":\"auto\",\"displayMode\":\"auto\",\"filterable\":true},\"mappings\":[],\"thresholds\":{\"mode\":\"absolute\",\"steps\":[{\"color\":\"green\",\"value\":\"\"},{\"color\":\"red\",\"value\":80}]}},\"overrides\":[{\"matcher\":{\"id\":\"byName\",\"options\":\"TIMESTAMP\"},\"properties\":[{\"id\":\"custom.width\",\"value\":193}]}]},\"gridPos\":{\"h\":11,\"w\":23,\"x\":0,\"y\":0},\"id\":13,\"options\":{\"footer\":{\"fields\":\"\",\"reducer\":[\"sum\"],\"show\":false},\"showHeader\":true,\"sortBy\":[{\"desc\":true,\"displayName\":\"TIMESTAMP\"}]},\"pluginVersion\":\"8.4.3\",\"targets\":[{\"datasource\":{\"type\":\"loki\",\"uid\":\"__DataSourceUID__\"},\"expr\":\"{record_type=\\\"fticks\\\"}\",\"maxLines\":5000,\"refId\":\"A\"}],\"title\":\"Authentication Logs\",\"transformations\":[{\"id\":\"merge\",\"options\":{}},{\"id\":\"extractFields\",\"options\":{\"format\":\"auto\",\"replace\":false,\"source\":\"line\"}},{\"id\":\"organize\",\"options\":{\"excludeByName\":{\"FAILURE\":false,\"NEXTHOP\":false,\"REALM\":false,\"REGION\":true,\"TIMESTAMP\":true,\"Time\":false,\"Time ns\":true,\"UTC\":true,\"VISINST\":true,\"id\":true,\"line\":true,\"line {record_type=\\\"fticks\\\", side=\\\"rp\\\"}\":true,\"ts\":false,\"tsNs\":true},\"indexByName\":{\"14\":14,\"15\":15,\"2022\":16,\"CSI\":9,\"EAPTYPE\":8,\"FAILURE\":6,\"NEXTHOP\":4,\"REALM\":3,\"REGION\":12,\"RESULT\":5,\"Realm\":19,\"TIMESTAMP\":1,\"UTC\":13,\"VISCOUNTRY\":7,\"VISINST\":10,\"VISINSTID\":2,\"at\":23,\"does\":20,\"dot\":26,\"have\":22,\"id\":11,\"least\":24,\"line\":17,\"not\":21,\"one\":25,\"separator\":27,\"ts\":0,\"tsNs\":18},\"renameByName\":{\"Time\":\"TIMESTAMP\",\"ts\":\"TIMESTAMP\"}}},{\"id\":\"filterFieldsByName\",\"options\":{\"include\":{\"names\":[\"VISINSTID\",\"REALM\",\"NEXTHOP\",\"RESULT\",\"FAILURE\",\"VISCOUNTRY\",\"EAPTYPE\",\"CSI\",\"TIMESTAMP\"]}}}],\"type\":\"table\"},{\"gridPos\":{\"h\":12,\"w\":23,\"x\":0,\"y\":11},\"id\":6,\"options\":{\"dedupStrategy\":\"none\",\"enableLogDetails\":true,\"prettifyLogMessage\":false,\"showCommonLabels\":false,\"showLabels\":false,\"showTime\":true,\"sortOrder\":\"Descending\",\"wrapLogMessage\":false},\"targets\":[{\"datasource\":{\"type\":\"loki\",\"uid\":\"__DataSourceUID__\"},\"expr\":\"{record_type=\\\"stdout\\\"}\",\"maxLines\":5000,\"refId\":\"A\"}],\"title\":\"RADIUS Server Logs\",\"type\":\"logs\"}],\"style\":\"dark\",\"tags\":[],\"templating\":{\"list\":[]},\"time\":{\"from\":\"now-15m\",\"to\":\"now\"},\"timepicker\":{},\"weekStart\":\"\"},\"folderId\":0,\"overwrite\":true}";
        String[] templates = {defaultTemplate, ecoTemplate};
        configuration.setDashboardTemplate(templates);
        configuration.setDefaultOrgRole("Viewer");
        connector.init(configuration);
        ConnectorMessages messages = configuration.getConnectorMessages();

    }

    @Test
    public void test100ConfigurationValidate()
    {
        configuration.validate();
    }

    @Test
    public void test110GetSchema()
    {
        Schema schema = connector.schema();
        assertNotNull(schema, "Schema not available");
    }

    @Test
    public void test120Test()
    {
        connector.test();
    }


    @Test
    public void test125CreateOrg()
    {
        Set<Attribute> attributes = new HashSet<>();
        attributes.add(new AttributeBuilder().setName(GrafanaOrgAttribute.name.name()).addValue("provisioniam.edu").build());

        Uid newId = connector.create(new ObjectClass("GrafanaOrganization"), attributes, new OperationOptionsBuilder().build());
        assertNotNull(newId);
        assertNotNull(newId.getUidValue());
    }

    @Test
    public void test130CreateUser()
    {
        Set<Attribute> attributes = new HashSet<>();
        attributes.add(new AttributeBuilder().setName(GrafanaUserAttribute.email.name()).addValue("guest8@internet2.edu").build());
        attributes.add(new AttributeBuilder().setName(GrafanaUserAttribute.name.name()).addValue("guest8@internet2.edu").build());
        attributes.add(new AttributeBuilder().setName(GrafanaUserAttribute.login.name()).addValue("guest8@internet2.edu").build());
        attributes.add(new AttributeBuilder().setName(GrafanaUserAttribute.password.name()).addValue("Secret1234!").build());
        attributes.add(new AttributeBuilder().setName(GrafanaUserAttribute.role.name()).addValue("Viewer").build());
        attributes.add(new AttributeBuilder().setName(GrafanaUserAttribute.orgId.name()).addValue("6").build());


        Uid newId = connector.create(new ObjectClass("GrafanaUser"), attributes, new OperationOptionsBuilder().build());
        assertNotNull(newId);
        assertNotNull(newId.getUidValue());
    }
    @Test
    public void test130CreateDuplicateUser()
    {
        Set<Attribute> attributes = new HashSet<>();
        attributes.add(new AttributeBuilder().setName(GrafanaUserAttribute.email.name()).addValue("user2@graf.com").build());
        attributes.add(new AttributeBuilder().setName(GrafanaUserAttribute.name.name()).addValue("John User2").build());
        attributes.add(new AttributeBuilder().setName(GrafanaUserAttribute.login.name()).addValue("user2").build());
        attributes.add(new AttributeBuilder().setName(GrafanaUserAttribute.password.name()).addValue("Secret1234!").build());
        attributes.add(new AttributeBuilder().setName(GrafanaUserAttribute.role.name()).addValue("Viewer").build());
        Uid newId = connector.create(new ObjectClass("GrafanaUser"), attributes, new OperationOptionsBuilder().build());
        assertNotNull(newId);
        assertNotNull(newId.getUidValue());
    }

    /**
     * Create a Datasource
     */
    @Test
    public void test135CreateDatasource()
    {
        Set<Attribute> attributes = new HashSet<>();
        attributes.add(new AttributeBuilder().setName(GrafanaDataSourceAttribute.name.name()).addValue("ProvisionIAM").build());
        attributes.add(new AttributeBuilder().setName(GrafanaDataSourceAttribute.orgId.name()).addValue("7").build());
        attributes.add(new AttributeBuilder().setName(GrafanaDataSourceAttribute.isDefault.name()).addValue(true).build());
        attributes.add(new AttributeBuilder().setName(GrafanaDataSourceAttribute.basicAuth.name()).addValue(true).build());
        Uid newId = connector.create(new ObjectClass("GrafanaDatasource"), attributes, new OperationOptionsBuilder().build());
        assertNotNull(newId);
        assertNotNull(newId.getUidValue());
    }

    /**
     * Create a Datasource for eco
     */
    @Test
    public void test135CreateEsoDatasource()
    {
        Set<Attribute> attributes = new HashSet<>();
        attributes.add(new AttributeBuilder().setName(GrafanaDataSourceAttribute.name.name()).addValue("ProvisionECO").build());
        attributes.add(new AttributeBuilder().setName(GrafanaDataSourceAttribute.orgId.name()).addValue("7").build());
        attributes.add(new AttributeBuilder().setName(GrafanaDataSourceAttribute.isDefault.name()).addValue(false).build());
        attributes.add(new AttributeBuilder().setName(GrafanaDataSourceAttribute.dashboardTemplateName.name()).addValue("eso").build());
        attributes.add(new AttributeBuilder().setName(GrafanaDataSourceAttribute.basicAuth.name()).addValue(true).build());
        Uid newId = connector.create(new ObjectClass("GrafanaDatasource"), attributes, new OperationOptionsBuilder().build());
        assertNotNull(newId);
        assertNotNull(newId.getUidValue());
    }
    /**
     * Create a dashboard
     */
    @Test
    public void test135CreateDashboard()
    {
        Set<Attribute> attributes = new HashSet<>();
        attributes.add(new AttributeBuilder().setName(GrafanaDashboardAttribute.orgId.name()).addValue("6").build());
        attributes.add(new AttributeBuilder().setName(GrafanaDashboardAttribute.orgName.name()).addValue("GrafanaTest.org").build());
        attributes.add(new AttributeBuilder().setName(GrafanaDashboardAttribute.templateName.name()).addValue("eso").build());
        attributes.add(new AttributeBuilder().setName(GrafanaDashboardAttribute.dataSourceUid.name()).addValue("be6iuisslqkn4e").build());
        Uid newId = connector.create(new ObjectClass("GrafanaDashboard"), attributes, new OperationOptionsBuilder().build());
        dashboardUid = newId.getUidValue();
        assertNotNull(newId);
        assertNotNull(newId.getUidValue());
    }
    /**
     * Get A Single User by Name
     */
    @Test
    public void test140GetUserByName()
    {
        ToListResultsHandler listHandler = new ToListResultsHandler();
        connector.executeQuery(new ObjectClass("GrafanaUser"), "admin", listHandler, new OperationOptionsBuilder().build());
        List<ConnectorObject> users = listHandler.getObjects();
        assertNotNull(users);
    }

    /**
     * Get A Single User by Name
     */
    @Test
    public void test140GetUserById()
    {
        ToListResultsHandler listHandler = new ToListResultsHandler();
        connector.executeQuery(new ObjectClass("GrafanaUser"), "2", listHandler, new OperationOptionsBuilder().build());
        List<ConnectorObject> users = listHandler.getObjects();
        assertNotNull(users);
    }

    /**
     * Get A Single Organization by number
     */
    @Test
    public void test140GetOrgByNumber()
    {
        ToListResultsHandler listHandler = new ToListResultsHandler();
        connector.executeQuery(new ObjectClass("GrafanaOrganization"), "1", listHandler, new OperationOptionsBuilder().build());
        List<ConnectorObject> orgs = listHandler.getObjects();
        assertNotNull(orgs);
        assertEquals(1, orgs.size());
    }

    /**
     * Get A Single Organization by number
     */
    @Test
    public void test140GetOrgByName()
    {
        ToListResultsHandler listHandler = new ToListResultsHandler();
        Attribute aName = new AttributeBuilder().setName(Name.NAME).addValue("Internet2_uuid").build();
        Filter eqName = new EqualsFilter(aName);
        connector.executeQuery(new ObjectClass("GrafanaOrganization"), eqName, listHandler, new OperationOptionsBuilder().build());
        List<ConnectorObject> orgs = listHandler.getObjects();
        assertNotNull(orgs);
        assertEquals(1, orgs.size());
    }

    /**
     * Get A Single Datasource by number
     */
    @Test
    public void test140GetDataSourceByID()
    {
        ToListResultsHandler listHandler = new ToListResultsHandler();
        connector.executeQuery(new ObjectClass("GrafanaDatasource"), "6_be6iuisslqkn4e", listHandler, new OperationOptionsBuilder().build());

        List<ConnectorObject> dataSources = listHandler.getObjects();
        assertNotNull(dataSources);
        assertEquals(1, dataSources.size());
    }

    /**
     * Get a Single Datasource by Name
     */
    @Test
    public void test140GetDataSourceByName()
    {
        ToListResultsHandler listHandler = new ToListResultsHandler();
        Attribute aName = new AttributeBuilder().setName(Name.NAME).addValue("7_ProvisionIAM").build();
        Filter eqName = new EqualsFilter(aName);
        connector.executeQuery(new ObjectClass("GrafanaDatasource"), eqName, listHandler, new OperationOptionsBuilder().build());
        List<ConnectorObject> dataSources = listHandler.getObjects();
        assertNotNull(dataSources);
        assertEquals(1, dataSources.size());
    }

    @Test
    public void test140GetDashboardByUid()
    {
        ToListResultsHandler listHandler = new ToListResultsHandler();
        connector.executeQuery(new ObjectClass("GrafanaDashboard"), dashboardUid, listHandler, new OperationOptionsBuilder().build());
        List<ConnectorObject> dashboards = listHandler.getObjects();
        assertNotNull(dashboards);
        assertEquals(1, dashboards.size());
    }

    /**
     * Get All Dashboards
     */
    @Test
    public void test150GetAllDashboards()
    {
        ToListResultsHandler listHandler = new ToListResultsHandler();
        connector.executeQuery(new ObjectClass("GrafanaDashboard"), "", listHandler, new OperationOptionsBuilder().build());
        List<ConnectorObject> dashboards = listHandler.getObjects();
        assertNotNull(dashboards);
        assertFalse(dashboards.isEmpty());
    }
    /**
     * Get All Users
     */
    @Test
    public void test150GetAllUsers()
    {
        ToListResultsHandler listHandler = new ToListResultsHandler();
        connector.executeQuery(new ObjectClass("GrafanaUser"), "", listHandler, new OperationOptionsBuilder().build());
        List<ConnectorObject> users = listHandler.getObjects();
        assertNotNull(users);
        assertFalse(users.isEmpty());
    }

    /**
     * Get All Users
     */
    @Test
    public void test150GetAllOrgs()
    {
        ToListResultsHandler listHandler = new ToListResultsHandler();
        connector.executeQuery(new ObjectClass("GrafanaOrganization"), "", listHandler, new OperationOptionsBuilder().build());
        List<ConnectorObject> orgs = listHandler.getObjects();
        assertNotNull(orgs);
        assertFalse(orgs.isEmpty());
    }

    /**
     * Get All Paged Orgs
     */
    @Test
    public void test150GetPagedOrgs()
    {
        HashMap<String, Object> map = new HashMap<>();
        OperationOptionsBuilder builder = new OperationOptionsBuilder();
        builder.setPageSize(10);
        builder.setPagedResultsOffset(1);
        map.put(OperationOptions.OP_PAGE_SIZE, "10");
        map.put(OperationOptions.OP_PAGED_RESULTS_OFFSET, "1");
        ToListResultsHandler listHandler = new ToListResultsHandler();
        connector.executeQuery(new ObjectClass("GrafanaOrganization"), "", listHandler, builder.build());
        List<ConnectorObject> orgs = listHandler.getObjects();
        assertNotNull(orgs);
    }

        /**
         * Get All Data Sources
         */
    @Test
    public void test150GetAllDataSources()
    {
        OperationOptionsBuilder builder = new OperationOptionsBuilder();
        builder.setPageSize(10);
        builder.setPagedResultsOffset(1);

        ToListResultsHandler listHandler = new ToListResultsHandler();
        connector.executeQuery(new ObjectClass("GrafanaDatasource"), "", listHandler, builder.build());
        List<ConnectorObject> dataSources = listHandler.getObjects();
        assertNotNull(dataSources);
        assertFalse(dataSources.isEmpty());
    }
    @Test
    public void test150GetAllDashboardsPaged()
    {
        OperationOptionsBuilder builder = new OperationOptionsBuilder();
        builder.setPageSize(10);
        builder.setPagedResultsOffset(1);

        ToListResultsHandler listHandler = new ToListResultsHandler();
        connector.executeQuery(new ObjectClass("GrafanaDashboard"), "", listHandler, builder.build());
        List<ConnectorObject> dashboards = listHandler.getObjects();
        assertNotNull(dashboards);
        assertFalse(dashboards.isEmpty());
    }
    /**
     * Update user by adding to org 4
     */
    @Test
    public void test160UpdateUser()
    {
        ToListResultsHandler listHandler = new ToListResultsHandler();
        ObjectClass objectClass = new ObjectClass("GrafanaUser");
        OperationOptions options = new OperationOptionsBuilder().build();

        // Lookup a User with viewer Role
        connector.executeQuery(new ObjectClass("GrafanaUser"), "guest8@internet2.edu", listHandler, options);
        List<ConnectorObject> users = listHandler.getObjects();
        ConnectorObject user = users.get(0);

        // Get the Id of the user
        Set<Attribute> attributes = user.getAttributes();
        Attribute id = user.getAttributeByName(GrafanaUserAttribute.userId.name());

        String oid = (String)id.getValue().get(0);
        Uid uid = new Uid(oid);
        // Set User to Org 3
        Set<AttributeDelta> delta = new HashSet<>();
        AttributeDeltaBuilder builder = new AttributeDeltaBuilder();
        delta.add(builder.setName(GrafanaUserAttribute.orgId.name()).addValueToRemove("6").build());
        delta.add(builder.setName(GrafanaUserAttribute.orgId.name()).addValueToAdd("7").build());

        builder = new AttributeDeltaBuilder();
        builder.setName(GrafanaUserAttribute.email.name()).addValueToReplace(user.getAttributeByName(GrafanaUserAttribute.email.name()).getValue().get(0));
        delta.add(builder.build());

        builder = new AttributeDeltaBuilder();
        builder.setName(GrafanaUserAttribute.login.name()).addValueToReplace(user.getAttributeByName(GrafanaUserAttribute.login.name()).getValue().get(0));
        delta.add(builder.build());


        Set<AttributeDelta> output = connector.updateDelta(objectClass, uid, delta, options);
        assertNotNull(output);
    }
    @Test
    public void test160UpdateUserName()
    {
        ToListResultsHandler listHandler = new ToListResultsHandler();
        ObjectClass objectClass = new ObjectClass("GrafanaUser");
        OperationOptions options = new OperationOptionsBuilder().build();

        // Lookup a User with viewer Role
        connector.executeQuery(new ObjectClass("GrafanaUser"), "user2@graf.com", listHandler, options);
        List<ConnectorObject> users = listHandler.getObjects();
        ConnectorObject user = users.get(0);

        // Get the Id of the user
        Set<Attribute> attributes = user.getAttributes();
        Attribute id = user.getAttributeByName(GrafanaUserAttribute.userId.name());

        String oid = (String)id.getValue().get(0);
        Uid uid = new Uid(oid);
        // Set User to Org 3
        Set<AttributeDelta> delta = new HashSet<>();
        AttributeDeltaBuilder builder = new AttributeDeltaBuilder();

        builder = new AttributeDeltaBuilder();
        builder.setName(GrafanaUserAttribute.name.name()).addValueToReplace("Another Name");
        delta.add(builder.build());

        Set<AttributeDelta> output = connector.updateDelta(objectClass, uid, delta, options);
        assertNotNull(output);
    }
    /**
     * Update An organization
     */
    @Test
    public void test160UpdateOrg()
    {
        ToListResultsHandler listHandler = new ToListResultsHandler();
        ObjectClass objectClass = new ObjectClass("GrafanaOrganization");
        OperationOptions options = new OperationOptionsBuilder().build();

        // Lookup a User with viewer Role
        connector.executeQuery(objectClass, "provisioniam.edu", listHandler, options);
        List<ConnectorObject> orgs = listHandler.getObjects();
        ConnectorObject org = orgs.get(0);

        // Get the Id of the organization
        Set<Attribute> attributes = org.getAttributes();
        // Save the Id of the organization to identify the record to be updated
        Attribute id = org.getAttributeByName(GrafanaOrgAttribute.orgId.name());
        String oid = (String)id.getValue().get(0);
        Uid uid = new Uid(oid);
        // Change the Org Name
        Set<AttributeDelta> delta = new HashSet<>();
        AttributeDeltaBuilder builder = new AttributeDeltaBuilder();
        delta.add(builder.setName(GrafanaOrgAttribute.name.name()).addValueToReplace("Updatedprovisioniam.edu").build());
        Set<AttributeDelta> output = connector.updateDelta(objectClass, uid, delta, options);

        assertNotNull(output);
    }
    /**
     * Update a Single Data Source by Name
     */
    @Test
    public void test160UpdateDataSource()
    {
        ToListResultsHandler listHandler = new ToListResultsHandler();
        ObjectClass objectClass = new ObjectClass("GrafanaDatasource");
        OperationOptions options = new OperationOptionsBuilder().build();
        Attribute aName = new AttributeBuilder().setName(Name.NAME).addValue("7_ProvisionIAM").build();
        Filter filter = new EqualsFilter(aName);
        connector.executeQuery(objectClass, filter, listHandler, new OperationOptionsBuilder().build());
        List<ConnectorObject> dataSources = listHandler.getObjects();
        ConnectorObject dataSource = dataSources.get(0);

        // Get the current attributes of the datasource
        Set<Attribute> attributes = dataSource.getAttributes();

        // Save the Id of the dataSource to identify the record to be updated
        Attribute id  = dataSource.getAttributeByName(GrafanaDataSourceAttribute.uid.name());
        Attribute org = dataSource.getAttributeByName(GrafanaDataSourceAttribute.orgId.name());
        String oid = (String)org.getValue().get(0) + "_" + (String)id.getValue().get(0);
        Uid uid = new Uid(oid);

        // Change the Data source attributes
        Set<AttributeDelta> delta = new HashSet<>();

        AttributeDeltaBuilder builder = new AttributeDeltaBuilder();
        builder.setName(GrafanaDataSourceAttribute.dashboardTemplateName.name()).addValueToReplace("eco");
        delta.add(builder.build());

        builder = new AttributeDeltaBuilder();
        builder.setName(GrafanaDataSourceAttribute.orgId.name()).addValueToReplace(org.getValue().get(0));
        delta.add(builder.build());

        builder = new AttributeDeltaBuilder();
        String secureJSON = "{\"httpHeaderValue1\": \"internet2_uuid\"}";
        builder.setName(GrafanaDataSourceAttribute.secureJsonData.name()).addValueToReplace(secureJSON);
        delta.add(builder.build());

        Set<AttributeDelta> output = connector.updateDelta(objectClass, uid, delta, options);
        assertNotNull(output);
    }

    /**
     * Update a Single Dashboard by ID
     */
    @Test
    public void test160UpdateDashboard()
    {
        ToListResultsHandler listHandler = new ToListResultsHandler();
        ObjectClass objectClass = new ObjectClass("GrafanaDashboard");
        Uid uid = new Uid(dashboardUid);
        OperationOptions options = new OperationOptionsBuilder().build();
        Set<AttributeDelta> delta = new HashSet<>();
        AttributeDeltaBuilder builder = new AttributeDeltaBuilder();
        builder.setName(GrafanaDashboardAttribute.templateName.name()).addValueToReplace("eso");
        delta.add(builder.build());
        builder = new AttributeDeltaBuilder();
        builder.setName(GrafanaDashboardAttribute.dataSourceUid.name()).addValueToReplace("be6iuisslqkn4e");
        delta.add(builder.build());
        Set<AttributeDelta> output = connector.updateDelta(objectClass, uid, delta, options);
        assertNotNull(output);
    }
    /**
     * Delete a Single User by Id
     */
    @Test
    public void test170DeleteOrg()
    {
        ToListResultsHandler listHandler = new ToListResultsHandler();
        connector.executeQuery(new ObjectClass("GrafanaOrganization"), "Updatedprovisioniam.edu", listHandler, new OperationOptionsBuilder().build());
        List<ConnectorObject> orgs = listHandler.getObjects();
        ConnectorObject org = orgs.get(0);
        // Get the Id of the organization
        Set<Attribute> attributes = org.getAttributes();
        Attribute id = org.getAttributeByName(GrafanaOrgAttribute.orgId.name());

        String oid = (String)id.getValue().get(0);
        Uid uid = new Uid(oid);

        connector.delete(new ObjectClass("GrafanaOrganization"), uid, new OperationOptionsBuilder().build());
    }

    /**
     * Delete a Single User by Id
     */
    @Test
    public void test170DeleteUser()
    {
        ToListResultsHandler listHandler = new ToListResultsHandler();
        connector.executeQuery(new ObjectClass("GrafanaUser"), "guest8@internet2.edu", listHandler, new OperationOptionsBuilder().build());
        List<ConnectorObject> users = listHandler.getObjects();
        ConnectorObject user = users.get(0);

        // Get the Id of the user
        Set<Attribute> attributes = user.getAttributes();
        Attribute id = user.getAttributeByName(GrafanaUserAttribute.userId.name());

        String oid = (String)id.getValue().get(0);
        Uid uid = new Uid(oid);
        connector.delete(new ObjectClass("GrafanaUser"),  uid, new OperationOptionsBuilder().build());
    }
/**
 * Delete a Single dashboard by id
 */
    @Test
    public void test170DeleteDashboard()
    {
        ToListResultsHandler listHandler = new ToListResultsHandler();
        Uid uid = new Uid(dashboardUid);
        connector.delete(new ObjectClass("GrafanaDashboard"),  uid, new OperationOptionsBuilder().build());
    }
    /**
     * Delete a Single Data Source by Name
     */
    @Test
    public void test170DeleteDataSourceByName()
    {
        ToListResultsHandler listHandler = new ToListResultsHandler();
        Attribute aName = new AttributeBuilder().setName(Name.NAME).addValue("7_ProvisionIAM").build();
        Filter filter = new EqualsFilter(aName);
        connector.executeQuery(new ObjectClass("GrafanaDatasource"), filter, listHandler, new OperationOptionsBuilder().build());
        List<ConnectorObject> dataSources = listHandler.getObjects();
        ConnectorObject dataSource = dataSources.get(0);

        // Get the Id of the datasource
        Set<Attribute> attributes = dataSource.getAttributes();
        Attribute dsUid = dataSource.getAttributeByName(GrafanaDataSourceAttribute.uid.name());
        Attribute dsOrg = dataSource.getAttributeByName(GrafanaDataSourceAttribute.orgId.name());

        String sUid = (String)dsUid.getValue().get(0);
        String sOrg = (String)dsOrg.getValue().get(0);
        Uid uid = new Uid(String.format("%s_%s", sOrg, sUid));
        connector.delete(new ObjectClass("GrafanaDatasource"),  uid, new OperationOptionsBuilder().build());
    }
}
