package com.exclamationlabs.connid.base.grafana.adapter;

import com.exclamationlabs.connid.base.connector.adapter.AdapterValueTypeConverter;
import com.exclamationlabs.connid.base.connector.adapter.BaseAdapter;
import com.exclamationlabs.connid.base.connector.attribute.ConnectorAttribute;
import com.exclamationlabs.connid.base.grafana.configuration.GrafanaConfiguration;
import com.exclamationlabs.connid.base.grafana.driver.rest.GrafanaDataSourceInvocator;
import com.exclamationlabs.connid.base.grafana.model.GrafanaDashboard;
import org.apache.commons.lang3.StringUtils;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.ObjectClass;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.exclamationlabs.connid.base.connector.attribute.ConnectorAttributeDataType.BOOLEAN;
import static com.exclamationlabs.connid.base.connector.attribute.ConnectorAttributeDataType.STRING;
import static com.exclamationlabs.connid.base.grafana.attribute.GrafanaDashboardAttribute.*;
import static org.identityconnectors.framework.common.objects.AttributeInfo.Flags.*;

public class GrafanaDashboardAdapter extends BaseAdapter<GrafanaDashboard, GrafanaConfiguration>
{
    private static final Log LOG = Log.getLog(GrafanaDataSourceAdapter.class);
    @Override
    public ObjectClass getType()
    {
        return new ObjectClass("GrafanaDashboard");
    }

    @Override
    public Class<GrafanaDashboard> getIdentityModelClass()
    {
        return GrafanaDashboard.class;
    }

    @Override
    public Set<ConnectorAttribute> getConnectorAttributes()
    {
        Set<ConnectorAttribute> result = new HashSet<>();
        result.add(new ConnectorAttribute(id.name(), STRING, NOT_CREATABLE, NOT_UPDATEABLE));
        result.add(new ConnectorAttribute(orgId.name(), STRING, REQUIRED));
        result.add(new ConnectorAttribute(orgName.name(), STRING, REQUIRED));
        result.add(new ConnectorAttribute(dashboard.name(), STRING));
        result.add(new ConnectorAttribute(isStarred.name(), BOOLEAN, NOT_RETURNED_BY_DEFAULT));
        result.add(new ConnectorAttribute(meta.name(), STRING, NOT_CREATABLE, NOT_UPDATEABLE));
        result.add(new ConnectorAttribute(slug.name(), STRING, NOT_CREATABLE, NOT_UPDATEABLE));
        result.add(new ConnectorAttribute(sortMeta.name(), STRING, NOT_CREATABLE, NOT_UPDATEABLE));
        result.add(new ConnectorAttribute(status.name(), STRING, NOT_CREATABLE, NOT_UPDATEABLE));
        result.add(new ConnectorAttribute(templateName.name(), STRING));
        result.add(new ConnectorAttribute(properties.name(), STRING));
        result.add(new ConnectorAttribute(tags.name(), STRING));
        result.add(new ConnectorAttribute(title.name(), STRING, NOT_CREATABLE, NOT_UPDATEABLE));
        result.add(new ConnectorAttribute(type.name(), STRING, NOT_CREATABLE, NOT_UPDATEABLE));
        result.add(new ConnectorAttribute(uid.name(), STRING, NOT_CREATABLE,  NOT_UPDATEABLE));
        result.add(new ConnectorAttribute(uri.name(), STRING, NOT_CREATABLE, NOT_UPDATEABLE));
        result.add(new ConnectorAttribute(url.name(), STRING, NOT_CREATABLE, NOT_UPDATEABLE));
        return result;
    }

    @Override
    protected Set<Attribute> constructAttributes(GrafanaDashboard model)
    {
        Set<Attribute> attributes = new HashSet<>();
        attributes.add(AttributeBuilder.build(id.name(), model.getId()));
        attributes.add(AttributeBuilder.build(orgId.name(), model.getOrgId()));
        attributes.add(AttributeBuilder.build(orgName.name(), model.getOrgName()));
        attributes.add(AttributeBuilder.build(dashboard.name(), model.getDashboard()));
        attributes.add(AttributeBuilder.build(isStarred.name(), model.getStarred()));
        attributes.add(AttributeBuilder.build(meta.name(), model.getMeta()));
        attributes.add(AttributeBuilder.build(slug.name(), model.getSlug()));
        attributes.add(AttributeBuilder.build(sortMeta.name(), model.getSortMeta()));
        attributes.add(AttributeBuilder.build(status.name(), model.getStatus()));
        attributes.add(AttributeBuilder.build(templateName.name(), model.getTemplateName()));
        attributes.add(AttributeBuilder.build(properties.name(), model.getProperties()));
        attributes.add(AttributeBuilder.build(tags.name(), model.getTags()));
        attributes.add(AttributeBuilder.build(title.name(), model.getTitle()));
        attributes.add(AttributeBuilder.build(type.name(), model.getType()));
        attributes.add(AttributeBuilder.build(uid.name(), model.getUid()));
        attributes.add(AttributeBuilder.build(uri.name(), model.getUri()));
        attributes.add(AttributeBuilder.build(url.name(), model.getUrl()));

        return attributes;
    }

    @Override
    protected GrafanaDashboard constructModel(Set<Attribute> attributes, Set<Attribute> addedMultiValueAttributes, Set<Attribute> removedMultiValueAttributes, boolean isCreate)
    {
        GrafanaDashboard model = new GrafanaDashboard();
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
                    model.setOrgId(Integer.valueOf(idParts[0]));
                    model.setUid(idParts[1]);
                }
            }
            if (item.getName().equalsIgnoreCase("__NAME__"))
            {
                NAME = item.getValue().get(0).toString();
                String[] nameParts =  GrafanaDataSourceInvocator.decomposeID(NAME);
                if ( nameParts != null && nameParts.length == 2 && StringUtils.isNumeric(nameParts[0].trim()))
                {
                    model.setOrgId(Integer.valueOf(nameParts[0]));
                    model.setUid(nameParts[1]);
                }
            }
            model.setOrgId(AdapterValueTypeConverter.getSingleAttributeValue(Integer.class, attributes, orgId));
            model.setOrgName(AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, orgName));
            model.setDashboard(AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, dashboard));
            model.setTemplateName(AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, templateName));
            model.setProperties(AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, properties));
        }
        return model;
    }
}
