package com.exclamationlabs.connid.base.grafana;

import com.exclamationlabs.connid.base.grafana.attribute.GrafanaOrgAttribute;
import com.exclamationlabs.connid.base.grafana.attribute.GrafanaUserAttribute;
import com.exclamationlabs.connid.base.grafana.configuration.GrafanaConfiguration;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.spi.SearchResultsHandler;
import org.identityconnectors.test.common.ToListResultsHandler;


import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class GrafanaConnectorTest
{
    private GrafanaConnector connector = new GrafanaConnector();
    private GrafanaConfiguration configuration = new GrafanaConfiguration();
    private final ArrayList<ConnectorObject> results = new ArrayList<>();


    /**
     * Implementation to capture an array list of search results
     */
    SearchResultsHandler handler = new SearchResultsHandler() {
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
        configuration.setServiceUrl("http://localhost:3000");
        configuration.setBasicUsername("admin");
        configuration.setBasicPassword("password");
        configuration.setActive(true);
        configuration.setPagination(true);
        configuration.setUpdateDashBoards(false);
        configuration.setLokiUser("admin");
        configuration.setLokiPassword("password");
        configuration.setLokiURL("http://localhost:3100");
        configuration.setDefaultOrgRole("Viewer");
        connector.init(configuration);
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
    public void test130CreateUsers()
    {
        Set<Attribute> attributes = new HashSet<>();
        attributes.add(new AttributeBuilder().setName(GrafanaUserAttribute.email.name()).addValue("user3@example.com").build());
        attributes.add(new AttributeBuilder().setName(GrafanaUserAttribute.name.name()).addValue("Mary User3").build());
        attributes.add(new AttributeBuilder().setName(GrafanaUserAttribute.login.name()).addValue("user3").build());
        attributes.add(new AttributeBuilder().setName(GrafanaUserAttribute.password.name()).addValue("password").build());
        //attributes.add(new AttributeBuilder().setName(GrafanaUserAttribute.orgId.name()).addValue("3").build());


        Uid newId = connector.create(new ObjectClass("GrafanaUser"), attributes, new OperationOptionsBuilder().build());
        assertNotNull(newId);
        assertNotNull(newId.getUidValue());
    }

    @Test
    public void test130CreateOrg()
    {
        Set<Attribute> attributes = new HashSet<>();
        attributes.add(new AttributeBuilder().setName(GrafanaOrgAttribute.name.name()).addValue("Grafana Test Org").build());

        Uid newId = connector.create(new ObjectClass("GrafanaOrganization"), attributes, new OperationOptionsBuilder().build());
        assertNotNull(newId);
        assertNotNull(newId.getUidValue());
    }

    /**
     * Get A Single User by Name
     */
    @Test
    public void test140GetUser()
    {
        ToListResultsHandler listHandler = new ToListResultsHandler();
        connector.executeQuery(new ObjectClass("GrafanaUser"), "admin", listHandler, new OperationOptionsBuilder().build());
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
        connector.executeQuery(new ObjectClass("GrafanaOrganization"), "Main Org.", listHandler, new OperationOptionsBuilder().build());
        List<ConnectorObject> orgs = listHandler.getObjects();
        assertNotNull(orgs);
        assertEquals(1, orgs.size());
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
        assertTrue(users.size() > 0);
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
        assertTrue(orgs.size() >= 1 );
    }

    /**
     * Update user by adding to org 3
     */
    @Test
    public void test160UpdateUser()
    {
        ToListResultsHandler listHandler = new ToListResultsHandler();
        ObjectClass objectClass = new ObjectClass("GrafanaUser");
        OperationOptions options = new OperationOptionsBuilder().build();

        // Lookup a User with viewer Role
        connector.executeQuery(new ObjectClass("GrafanaUser"), "user2", listHandler, options);
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
        delta.add(builder.setName(GrafanaUserAttribute.orgId.name()).addValueToReplace(3).build());
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
        connector.executeQuery(objectClass, "Grafana Test Org", listHandler, options);
        List<ConnectorObject> orgs = listHandler.getObjects();
        ConnectorObject org = orgs.get(0);

        // Get the Id of the organization
        Set<Attribute> attributes = org.getAttributes();
        Attribute id = org.getAttributeByName(GrafanaOrgAttribute.orgId.name());

        String oid = (String)id.getValue().get(0);
        Uid uid = new Uid(oid);
        // Change the Org Name
        Set<AttributeDelta> delta = new HashSet<>();
        AttributeDeltaBuilder builder = new AttributeDeltaBuilder();
        delta.add(builder.setName(GrafanaOrgAttribute.name.name()).addValueToReplace("New Grafana Org Name").build());
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
        connector.executeQuery(new ObjectClass("GrafanaOrganization"), "New Grafana Org Name", listHandler, new OperationOptionsBuilder().build());
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
        connector.executeQuery(new ObjectClass("GrafanaUser"), "user3", listHandler, new OperationOptionsBuilder().build());
        List<ConnectorObject> users = listHandler.getObjects();
        ConnectorObject user = users.get(0);

        // Get the Id of the user
        Set<Attribute> attributes = user.getAttributes();
        Attribute id = user.getAttributeByName(GrafanaUserAttribute.userId.name());

        String oid = (String)id.getValue().get(0);
        Uid uid = new Uid(oid);
        connector.delete(new ObjectClass("GrafanaUser"),  uid, new OperationOptionsBuilder().build());
    }
}
