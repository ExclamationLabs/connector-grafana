
package com.exclamationlabs.connid.base.grafana.adapter;

import com.exclamationlabs.connid.base.connector.adapter.AdapterValueTypeConverter;
import com.exclamationlabs.connid.base.connector.adapter.BaseAdapter;
import com.exclamationlabs.connid.base.connector.attribute.ConnectorAttribute;
import com.exclamationlabs.connid.base.connector.model.IdentityModel;
import com.exclamationlabs.connid.base.connector.util.GuardedStringUtil;
import com.exclamationlabs.connid.base.grafana.configuration.GrafanaConfiguration;
import com.exclamationlabs.connid.base.grafana.model.GrafanaUser;
import org.apache.commons.lang3.StringUtils;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.ObjectClass;

import java.util.*;

import static com.exclamationlabs.connid.base.connector.attribute.ConnectorAttributeDataType.STRING;
import static com.exclamationlabs.connid.base.grafana.attribute.GrafanaUserAttribute.*;
import static org.identityconnectors.framework.common.objects.AttributeInfo.Flags.*;


/**
 * The Grafana User adapter manages typing, schema, inbound mapping, and outbound mapping
 * between the connector client and the service endpoint.
 */
public class GrafanaUserAdapter extends BaseAdapter<GrafanaUser, GrafanaConfiguration>
{
    private static final Log LOG = Log.getLog(GrafanaUserAdapter.class);
    /**
     * Populates the connector client attributes with inbound data from the service
     * @param user inbound service data model record representing a user
     * @return
     */
    @Override
    protected Set<Attribute> constructAttributes(GrafanaUser user)
    {
        Set<Attribute> attributes = new HashSet<>();
        attributes.add(AttributeBuilder.build(avatarUrl.name(), user.getAvatarUrl()));
        attributes.add(AttributeBuilder.build(disabled.name(), user.getIsDisabled()));
        attributes.add(AttributeBuilder.build(email.name(), user.getEmail()));
        attributes.add(AttributeBuilder.build(name.name(), user.getName()));
        attributes.add(AttributeBuilder.build(lastSeenAt.name(), user.getLastSeenAt()));
        attributes.add(AttributeBuilder.build(lastSeenAtAge.name(), user.getLastSeenAtAge()));
        attributes.add(AttributeBuilder.build(login.name(), user.getLogin()));
        List<String> organizations = user.getOrganizations();
        if ( organizations != null && organizations.size() > 0 )
        {
            attributes.add(AttributeBuilder.build(orgId.name(), organizations));
        }
        else if ( user.getOrgId() != null  )
        {
            attributes.add(AttributeBuilder.build(orgId.name(), String.valueOf(user.getOrgId())));
        }
        attributes.add(AttributeBuilder.build(password.name(), user.getPassword()));
        attributes.add(AttributeBuilder.build(role.name(), user.getRole()));
        attributes.add(AttributeBuilder.build(userId.name(), user.getIdentityIdValue()));
        return attributes;
    }

    /**
     * populate a GrafanaUser object with attributes from connector client.
     * This is typically used to create or update a record at the endpoint
     * @param attributes
     * @param addedMultiValued
     * @param removedMultivalued
     * @param creation
     * @return
     */
    @Override
    protected GrafanaUser constructModel(Set<Attribute> attributes, Set<Attribute> addedMultiValued, Set<Attribute> removedMultivalued, boolean creation)
    {
        GrafanaUser user = new GrafanaUser();
        String pwd = null;
        // print attributes inbound
        ArrayList<Attribute> list = new ArrayList<>(attributes);
        for (Attribute item: list )
        {
            LOG.info("Attribute {0} = {1}", item.getName(), item.getValue());
            if ( item.getName() == "__PASSWORD__")
            {
                pwd = GuardedStringUtil.read(AttributeUtil.getGuardedStringValue(item));
            }
        }
        user.setAvatarUrl(AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, avatarUrl));
        user.setIsDisabled(AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, disabled));
        user.setEmail(AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, email));
        user.setLastSeenAt(AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, lastSeenAt));
        user.setLastSeenAtAge(AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, lastSeenAtAge));
        user.setLogin(AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, login));
        user.setName(AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, name));
        user.setPassword(AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, password));
        if ( pwd != null && pwd.trim().length() > 0 && user.getPassword() == null )
        {
            user.setPassword(pwd);
        }
        user.setRole(AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, role));

        String userValue = AdapterValueTypeConverter.getSingleAttributeValue(String.class, attributes, userId);
        if ( userValue != null && StringUtils.isNumeric(userValue.trim()) )
        {
            user.setUserId(Integer.valueOf(userValue.trim()));
            user.setId(Integer.valueOf(userValue.trim()));
        }

        Set<String> organizations = readAssignments(attributes, orgId);
        if ( organizations != null && organizations.size() > 0 )
        {
            ArrayList<String> orgList = new ArrayList<>(organizations);
            user.setOrganizations(orgList);
            /* Bypass OrgId Initialization it is handled in the invocator
            if ( orgList.size() == 1 && orgList.get(0) != null && StringUtils.isNumeric(orgList.get(0).trim()))
            {
                user.setOrgId(Integer.valueOf(orgList.get(0).trim()));
            }
            */
        }
        if ( addedMultiValued != null && addedMultiValued.size() > 0 )
        {
            // Only Multivalued Attributes are Organization
            Set<String> orgsAdd = readAssignments(addedMultiValued, orgId);
            user.setOrgsAdd(new ArrayList<>(orgsAdd));
        }
        if ( removedMultivalued != null && removedMultivalued.size() > 0 )
        {
            Set<String> orgsRemove = readAssignments(removedMultivalued, orgId);
            user.setOrgsRemove(new ArrayList<>(orgsRemove));
        }
        return user;
    }

    /**
     * @return The set of connector attributes that define the schema the connector client will interact with.
     */
    @Override
    public Set<ConnectorAttribute> getConnectorAttributes()
    {
        Set<ConnectorAttribute> result = new HashSet<>();
        result.add(new ConnectorAttribute(avatarUrl.name(), STRING, NOT_UPDATEABLE, NOT_CREATABLE));
        result.add(new ConnectorAttribute(disabled.name(), STRING));
        result.add(new ConnectorAttribute(email.name(), STRING));
        result.add(new ConnectorAttribute(lastSeenAt.name(), STRING, NOT_UPDATEABLE, NOT_CREATABLE));
        result.add(new ConnectorAttribute(lastSeenAtAge.name(), STRING, NOT_UPDATEABLE, NOT_CREATABLE));
        result.add(new ConnectorAttribute(login.name(), STRING));
        result.add(new ConnectorAttribute(name.name(), STRING));
        result.add(new ConnectorAttribute(orgId.name(), STRING, MULTIVALUED));
        result.add(new ConnectorAttribute(password.name(), STRING));
        result.add(new ConnectorAttribute(role.name(), STRING));
        result.add(new ConnectorAttribute(userId.name(), STRING, NOT_UPDATEABLE));
        return result;
    }

    /**
     * @return Species the type of object the service with produce
     */
    @Override
    public Class<GrafanaUser> getIdentityModelClass()
    {
        return GrafanaUser.class;
    }

    /**
     * Specifies the Object Class that the connector will advertise
     * @return an object class containing the name of the Object
     */
    @Override
    public ObjectClass getType()
    {
        return new ObjectClass("GrafanaUser");
    }
}

