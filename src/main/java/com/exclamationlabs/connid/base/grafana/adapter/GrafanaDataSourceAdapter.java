package com.exclamationlabs.connid.base.grafana.adapter;

import com.exclamationlabs.connid.base.connector.adapter.AdapterValueTypeConverter;
import com.exclamationlabs.connid.base.connector.adapter.BaseAdapter;
import com.exclamationlabs.connid.base.connector.attribute.ConnectorAttribute;
import com.exclamationlabs.connid.base.grafana.configuration.GrafanaConfiguration;
import com.exclamationlabs.connid.base.grafana.driver.rest.GrafanaDataSourceInvocator;
import com.exclamationlabs.connid.base.grafana.model.GrafanaDataSource;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.ObjectClass;

import java.util.*;

import static com.exclamationlabs.connid.base.connector.attribute.ConnectorAttributeDataType.BOOLEAN;
import static com.exclamationlabs.connid.base.connector.attribute.ConnectorAttributeDataType.STRING;
import static com.exclamationlabs.connid.base.grafana.attribute.GrafanaDataSourceAttribute.*;
import static org.identityconnectors.framework.common.objects.AttributeInfo.Flags.*;

public class GrafanaDataSourceAdapter extends BaseAdapter<GrafanaDataSource, GrafanaConfiguration>
{
    private static final Log LOG = Log.getLog(GrafanaDataSourceAdapter.class);
    @Override
    protected Set<Attribute> constructAttributes(GrafanaDataSource dataSource)
    {
        Gson gson = new Gson();
        Set<Attribute> attributes = new HashSet<>();
        attributes.add(AttributeBuilder.build(id.name(), dataSource.getIdentityIdValue()));
        attributes.add(AttributeBuilder.build(name.name(), dataSource.getIdentityNameValue()));
        Integer orgValue = dataSource.getOrgId();
        if ( orgValue != null )
        {
            attributes.add(AttributeBuilder.build(orgId.name(), String.valueOf(dataSource.getOrgId())));
        }
        else
        {
            String aVal = null;
            attributes.add(AttributeBuilder.build(orgId.name(), aVal));
        }
        attributes.add(AttributeBuilder.build(type.name(), dataSource.getType()));
        attributes.add(AttributeBuilder.build(access.name(), dataSource.getAccess()));
        attributes.add(AttributeBuilder.build(isDefault.name(), dataSource.getDefault()));
        attributes.add(AttributeBuilder.build(url.name(), dataSource.getUrl()));
        attributes.add(AttributeBuilder.build(basicAuth.name(), dataSource.getBasicAuth()));
        attributes.add(AttributeBuilder.build(basicAuthUser.name(), dataSource.getBasicAuthUser()));
        if ( dataSource.getBasicAuthPassword() != null && dataSource.getBasicAuthPassword().trim().length() > 0  )
        {
            String encoded = Base64.getEncoder().encodeToString(dataSource.getBasicAuthPassword().getBytes());
            attributes.add(AttributeBuilder.build(basicAuthPassword.name(), "OBF:"+encoded));
        }
        else
        {
            attributes.add(AttributeBuilder.build(basicAuthPassword.name(), dataSource.getBasicAuthPassword()));
        }

        attributes.add(AttributeBuilder.build(database.name(), dataSource.getDatabase()));
        attributes.add(AttributeBuilder.build(uid.name(), dataSource.getUid()));
        attributes.add(AttributeBuilder.build(user.name(), dataSource.getUser()));

        if ( dataSource.getPassword() != null && dataSource.getPassword().trim().length() > 0 )
        {
            String encoded = Base64.getEncoder().encodeToString(dataSource.getPassword().getBytes());
            attributes.add(AttributeBuilder.build(password.name(), "OBF:"+encoded));
        }
        else
        {
            attributes.add(AttributeBuilder.build(password.name(), dataSource.getPassword()));
        }

        attributes.add(AttributeBuilder.build(dataSourceId.name(), String.valueOf(dataSource.getId())));

        if ( dataSource.getJsonData() != null )
        {
            String json = gson.toJson(dataSource.getJsonData());
            attributes.add(AttributeBuilder.build(jsonData.name(), json));
            if( dataSource.getJsonData().get("dashboardTemplateName") != null )
            {
                attributes.add(AttributeBuilder.build(dashboardTemplateName.name(), dataSource.getJsonData().get("dashboardTemplateName")));
            }
            else if ( dataSource.getDashboardTemplateName() != null )
            {
                attributes.add(AttributeBuilder.build(dashboardTemplateName.name(), dataSource.getDashboardTemplateName()));
            }
        }

        if ( dataSource.getSecureJsonData() != null )
        {
            String secureJson = gson.toJson(dataSource.getSecureJsonData());
            attributes.add(AttributeBuilder.build(secureJsonData.name(), secureJson));
        }

        if ( dataSource.getSecureJsonFields() != null )
        {
            String secureJson = gson.toJson(dataSource.getSecureJsonFields());
            attributes.add(AttributeBuilder.build(secureJsonFields.name(), secureJson));
        }
        return attributes;
    }

    /**
     * Outbound mapping of the controller to the Service
     * @param attributes the set of attributes to be used to construct the model
     * @param addedMultiValued the set of attributes that have been added
     * @param removedMultivalued the set of attributes that have been removed
     * @param create whether the model is being created or updated
     * @return the Datasource model constructed from the attributes
     */
    @Override
    protected GrafanaDataSource constructModel(Set<Attribute> attributes, Set<Attribute> addedMultiValued, Set<Attribute> removedMultivalued, boolean create)
    {
        Gson gson = new Gson();
        String gsonData;
        GrafanaDataSource dataSource = new GrafanaDataSource();
        ArrayList<Attribute> list = new ArrayList<>(attributes);
        String UID = null;
        String NAME= null;
        for (Attribute item: list )
        {
            LOG.info("Attribute {0} = {1}", item.getName(), item.getValue());
            if (item.getName().equalsIgnoreCase("__UID__"))
            {
                UID = item.getValue().get(0).toString();
                String[] idParts =  GrafanaDataSourceInvocator.decomposeID(UID);
                if ( idParts != null && idParts.length == 2 && StringUtils.isNumeric(idParts[0].trim()))
                {
                    dataSource.setOrgId(Integer.valueOf(idParts[0]));
                    dataSource.setUid(idParts[1]);
                }
            }
            if (item.getName().equalsIgnoreCase("__NAME__"))
            {
                NAME = item.getValue().get(0).toString();
                String[] nameParts =  GrafanaDataSourceInvocator.decomposeID(NAME);
                if ( nameParts != null && nameParts.length == 2 && StringUtils.isNumeric(nameParts[0].trim()))
                {
                    dataSource.setOrgId(Integer.valueOf(nameParts[0]));
                    dataSource.setName(nameParts[1]);
                }
            }
        }

        String dataSourceName = AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, name);
        if ( dataSourceName != null && dataSourceName.trim().length() > 0 )
        {
            String[] nameParts = GrafanaDataSourceInvocator.decomposeID(dataSourceName);
            if ( nameParts != null && nameParts.length == 2 && StringUtils.isNumeric(nameParts[0].trim()))
            {
                dataSource.setOrgId(Integer.valueOf(nameParts[0]));
                dataSource.setName(nameParts[1]);
            }
            else
            {
                dataSource.setName(dataSourceName);
            }
        }

        String dataSourceUID = AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, id);
        if ( dataSourceUID != null && dataSourceUID.trim().length() > 0 && !create)
        {
            String[] idParts =  GrafanaDataSourceInvocator.decomposeID(dataSourceUID);
            if ( idParts != null && idParts.length == 2 && StringUtils.isNumeric(idParts[0].trim()))
            {
                dataSource.setOrgId(Integer.valueOf(idParts[0]));
                dataSource.setUid(idParts[1]);
            }
        }

        dataSource.setAccess(AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, access));
        dataSource.setBasicAuth(AdapterValueTypeConverter.getSingleAttributeValue(Boolean.class, attributes, basicAuth));
        dataSource.setBasicAuthUser(AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, basicAuthUser));

        String basicPassword = AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, basicAuthPassword);
        if ( basicPassword != null && basicPassword.startsWith("OBF:"))
        {
            basicPassword = basicPassword.substring(4);
            byte[] decoded = Base64.getDecoder().decode(basicPassword);
            String decodedString = new String(decoded);
            dataSource.setBasicAuthPassword(decodedString);
        }
        else
        {
            dataSource.setBasicAuthPassword(basicPassword);
        }

        dataSource.setDefault(AdapterValueTypeConverter.getSingleAttributeValue(Boolean.class, attributes, isDefault));
        dataSource.setType(AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, type));
        dataSource.setUid(AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, uid));
        dataSource.setUrl(AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, url));
        dataSource.setUser(AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, user));
        dataSource.setDatabase(AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, database));
        dataSource.setDashboardTemplateName(AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, dashboardTemplateName));
        String pwd = AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, password);
        if ( pwd != null && pwd.startsWith("OBF:"))
        {
            pwd = pwd.substring(4);
            byte[] decoded = Base64.getDecoder().decode(pwd);
            String decodedString = new String(decoded);
            dataSource.setPassword(decodedString);
        }
        else
        {
            dataSource.setPassword(pwd);
        }

        String did = AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, dataSourceId);
        if ( did != null && StringUtils.isNumeric(did.trim()))
        {
            dataSource.setId(Integer.valueOf(did.trim()));
        }

        String oid = AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, orgId);
        if ( oid != null && StringUtils.isNumeric(oid.trim()))
        {
            dataSource.setOrgId(Integer.valueOf(oid.trim()));
        }

        gsonData = AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, jsonData);
        Map jData = gson.fromJson(gsonData, Map.class);
        dataSource.setJsonData(jData);

        gsonData = AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, secureJsonData);
        Map secureData = gson.fromJson(gsonData, Map.class);
        dataSource.setSecureJsonData(secureData);

        return dataSource;
    }

    @Override
    public Set<ConnectorAttribute> getConnectorAttributes()
    {
        Set<ConnectorAttribute> result = new HashSet<>();
        result.add(new ConnectorAttribute(id.name(), STRING, NOT_CREATABLE));
        result.add(new ConnectorAttribute(uid.name(), STRING, NOT_CREATABLE));
        result.add(new ConnectorAttribute(orgId.name(), STRING, REQUIRED));
        result.add(new ConnectorAttribute(name.name(), STRING, REQUIRED));
        result.add(new ConnectorAttribute(type.name(), STRING, NOT_UPDATEABLE));
        result.add(new ConnectorAttribute(access.name(), STRING));
        result.add(new ConnectorAttribute(url.name(), STRING, NOT_UPDATEABLE));
        result.add(new ConnectorAttribute(isDefault.name(), BOOLEAN));
        result.add(new ConnectorAttribute(basicAuth.name(), BOOLEAN));
        result.add(new ConnectorAttribute(basicAuthUser.name(), STRING));
        result.add(new ConnectorAttribute(basicAuthPassword.name(), STRING));
        result.add(new ConnectorAttribute(jsonData.name(), STRING));
        result.add(new ConnectorAttribute(secureJsonData.name(), STRING, NOT_READABLE, NOT_RETURNED_BY_DEFAULT, NOT_UPDATEABLE));
        result.add(new ConnectorAttribute(user.name(), STRING));
        result.add(new ConnectorAttribute(database.name(), STRING));
        result.add(new ConnectorAttribute(password.name(), STRING));
        result.add(new ConnectorAttribute(dataSourceId.name(), STRING, NOT_UPDATEABLE, NOT_CREATABLE));
        result.add(new ConnectorAttribute(secureJsonFields.name(), STRING, NOT_RETURNED_BY_DEFAULT, NOT_UPDATEABLE));
        result.add(new ConnectorAttribute(dashboardTemplateName.name(), STRING));
        return result;
    }

    /**
     * @return Species the type of object the service with produce
     */
    @Override
    public Class<GrafanaDataSource> getIdentityModelClass()
    {
        return GrafanaDataSource.class;
    }

    /**
     * Specifies the Object Class that the connector will advertise
     * @return an object class containing the name of the Object
     */
    @Override
    public ObjectClass getType()
    {
        return new ObjectClass("GrafanaDatasource");
    }
}
