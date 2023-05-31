package com.exclamationlabs.connid.base.grafana.adapter;

import com.exclamationlabs.connid.base.connector.adapter.AdapterValueTypeConverter;
import com.exclamationlabs.connid.base.connector.adapter.BaseAdapter;
import com.exclamationlabs.connid.base.connector.attribute.ConnectorAttribute;
import com.exclamationlabs.connid.base.grafana.attribute.GrafanaOrgAttribute;
import com.exclamationlabs.connid.base.grafana.configuration.GrafanaConfiguration;
import com.exclamationlabs.connid.base.grafana.model.GrafanaOrg;
import org.apache.commons.lang3.StringUtils;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.ObjectClass;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.exclamationlabs.connid.base.connector.attribute.ConnectorAttributeDataType.STRING;


public class GrafanaOrgAdapter extends BaseAdapter<GrafanaOrg, GrafanaConfiguration>
{
    private static final Log LOG = Log.getLog(GrafanaOrgAdapter.class);
    @Override
    public ObjectClass getType()
    {
        return new ObjectClass("GrafanaOrganization");
    }

    @Override
    public Class<GrafanaOrg> getIdentityModelClass()
    {
        return GrafanaOrg.class;
    }

    /**
     * @return Organization Schema
     */
    @Override
    public Set<ConnectorAttribute> getConnectorAttributes()
    {
        Set<ConnectorAttribute> result = new HashSet<>();
        result.add(new ConnectorAttribute(GrafanaOrgAttribute.name.name(), STRING, AttributeInfo.Flags.REQUIRED));
        result.add(new ConnectorAttribute(GrafanaOrgAttribute.orgId.name(), STRING, AttributeInfo.Flags.NOT_UPDATEABLE));
        result.add(new ConnectorAttribute(GrafanaOrgAttribute.dashboards.name(), STRING,
                AttributeInfo.Flags.MULTIVALUED,
                AttributeInfo.Flags.NOT_UPDATEABLE,
                AttributeInfo.Flags.NOT_CREATABLE));
        result.add(new ConnectorAttribute(GrafanaOrgAttribute.homeDashboardUID.name(), STRING,
                AttributeInfo.Flags.NOT_UPDATEABLE,
                AttributeInfo.Flags.NOT_CREATABLE));
        return result;
    }

    /**
     * @param org The Grafana Organization Model is converted to ConnId attributes
     * @return The Organization Attributes returned from the connector
     */
    @Override
    protected Set<Attribute> constructAttributes(GrafanaOrg org)
    {
        Set<Attribute> attributes = new HashSet<>();
        attributes.add(AttributeBuilder.build(GrafanaOrgAttribute.name.name(), org.getName()));
        attributes.add(AttributeBuilder.build(GrafanaOrgAttribute.orgId.name(), org.getIdentityIdValue()));
        List<String> list = org.getDashboards();
        if ( list != null && list.size() > 0 )
        {
            attributes.add(AttributeBuilder.build(GrafanaOrgAttribute.dashboards.name(), list));
        }
        if ( org.getHomeDashboardUID() != null && org.getHomeDashboardUID().trim().length() > 0 )
        {
            attributes.add(AttributeBuilder.build(GrafanaOrgAttribute.homeDashboardUID.name(), org.getHomeDashboardUID()));
        }
        return attributes;
    }

    /**
     * Construct a Grafana Organization Model for use by the connector
     * @param attributes
     * @param addedMultiValued
     * @param removedMultivalued
     * @param create
     * @return A grafana Organization Model to be used by the connector for Create or Update operations
     */
    @Override
    protected GrafanaOrg constructModel(Set<Attribute> attributes, Set<Attribute> addedMultiValued, Set<Attribute> removedMultivalued, boolean create)
    {
        GrafanaOrg org = new GrafanaOrg();
        ArrayList<Attribute> list = new ArrayList<>(attributes);
        String UID = null;
        String NAME= null;
        for (Attribute item: list )
        {
            LOG.info("Attribute {0} = {1}", item.getName(), item.getValue());
            if (item.getName().equalsIgnoreCase("__UID__"))
            {
                UID = item.getValue().get(0).toString();
            }
            if (item.getName().equalsIgnoreCase("__NAME__"))
            {
                NAME = item.getValue().get(0).toString();
            }
        }

        String orgName = AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, GrafanaOrgAttribute.name);
        if ( orgName != null )
        {
            org.setName(orgName);
        }
        else if ( NAME != null )
        {
            org.setName(NAME);
        }

        Integer id = AdapterValueTypeConverter.getSingleAttributeValue(Integer.class, attributes, GrafanaOrgAttribute.orgId);
        if ( id != null )
        {
            org.setId(id);
        }
        else if ( UID != null && StringUtils.isNumeric(UID.trim()))
        {
            org.setId( Integer.valueOf(UID.trim()));
        }

        String homeDashBoardUID = AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, GrafanaOrgAttribute.homeDashboardUID);
        if ( homeDashBoardUID != null )
        {
            org.setHomeDashboardUID(homeDashBoardUID);
        }
        return org;
    }
}
