package com.exclamationlabs.connid.base.grafana.adapter;

import com.exclamationlabs.connid.base.connector.adapter.AdapterValueTypeConverter;
import com.exclamationlabs.connid.base.connector.adapter.BaseAdapter;
import com.exclamationlabs.connid.base.connector.attribute.ConnectorAttribute;
import com.exclamationlabs.connid.base.grafana.attribute.GrafanaOrgAttribute;
import com.exclamationlabs.connid.base.grafana.configuration.GrafanaConfiguration;
import com.exclamationlabs.connid.base.grafana.model.GrafanaOrg;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.ObjectClass;

import java.util.HashSet;
import java.util.Set;

import static com.exclamationlabs.connid.base.connector.attribute.ConnectorAttributeDataType.INTEGER;
import static com.exclamationlabs.connid.base.connector.attribute.ConnectorAttributeDataType.STRING;




public class GrafanaOrgAdapter extends BaseAdapter<GrafanaOrg, GrafanaConfiguration>
{
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

    @Override
    public Set<ConnectorAttribute> getConnectorAttributes()
    {
        Set<ConnectorAttribute> result = new HashSet<>();
        result.add(new ConnectorAttribute(GrafanaOrgAttribute.name.name(), STRING, AttributeInfo.Flags.REQUIRED));
        // result.add(new ConnectorAttribute(uuid.name(), STRING));
        result.add(new ConnectorAttribute(GrafanaOrgAttribute.orgId.name(), STRING, AttributeInfo.Flags.NOT_UPDATEABLE));
        return result;
    }

    @Override
    protected Set<Attribute> constructAttributes(GrafanaOrg org)
    {
        Set<Attribute> attributes = new HashSet<>();
        attributes.add(AttributeBuilder.build(GrafanaOrgAttribute.name.name(), org.getName()));
        attributes.add(AttributeBuilder.build(GrafanaOrgAttribute.orgId.name(), org.getIdentityIdValue()));
        return attributes;
    }

    @Override
    protected GrafanaOrg constructModel(Set<Attribute> attributes, Set<Attribute> set1, Set<Attribute> set2, boolean b)
    {
        GrafanaOrg org = new GrafanaOrg();
        org.setName(AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, GrafanaOrgAttribute.name));
        Integer id = AdapterValueTypeConverter.getSingleAttributeValue(Integer.class, attributes, GrafanaOrgAttribute.orgId);
        if ( id != null )
        {
            org.setId(id);
        }
        return org;
    }
}
