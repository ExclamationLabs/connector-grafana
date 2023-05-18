# Grafana Connector


# 1	Overview

Open source connector for [Grafana API](https://grafana.com/docs/grafana/latest/developers/http_api/) that uses the [ConnId Framework from Tirasa](http://connid.tirasa.net/) for integration with Identity and Access Management (IAM) systems such as [Midpoint](https://evolveum.com/midpoint/).

Leverages the Connector Base Framework located at [https://github.com/ExclamationLabs/connector-base](https://github.com/ExclamationLabs/connector-base)

Developed and tested in [Midpoint](https://evolveum.com/midpoint/), but also could be utilized in any [ConnId](https://connid.tirasa.net/) framework.

The Grafana software allows you to Query, visualize, alert on, and understand data no matter where it’s stored.

This connector allows an IAM system to Create, Read, Update, and Delete information from Grafana Open Source or Grafana Enterprise systems for automated provisioning of Users, Organizations, Data Sources, and Dashboards.

The current release was tested on Midpoint 4.4 and 4.6 with Grafana 9.0 and 9.3

This software is Copyright 2023 Exclamation Graphics.  Licensed under the Apache License, Version 2.0.


# 2	Features

The Grafana connector has the following features:



* The connector configuration is specified in the user interface
* The connector supports 3 types related to Grafana. These are Users, Organizations, and Datasources. There is a provision to update Dashboards which has not been implemented.
* A User can be associated with one or more Grafana Organizations.
* A Grafana Organization may be associated with one or more DataSources such as Grafana Loki
* The connector can Create, Update, Delete and Search Grafana Users. These users can be added or removed from a Grafana Organization.
* The connector can Create Update, Delete and Search Grafana Organizations.
* The connector can Create, Update, Delete, and Retrieve information about Grafana Datasources.
* The connector supports automatic creation of Grafana Data Sources through Configuration
* The connector supports automatic creation of a Grafana Dashboard when a Data Source is created. This is done through a Dashboard template defined in the  connector configuration.


# 3	Getting Started

To begin using the connector you should have a Grafana Web Service instance up and running. Such instances typically employ the SSL protocol over HTTPS with basic authentication.

Once you have acquired access to a Grafana instance you are ready to configure your connector. With Midpoint you must first copy the connector jar file to the **&lt;MIDPOINT_HOME>/icf-connectors** directory.


# 4	Connector Configuration

The actual method of configuring a connector is largely dependent on the interface(s) provided by your Identity and Access management system. Midpoint provides a convenient user interface method to enter these values. The configuration parameters are specified as follows:


<table>
  <tr>
   <td><strong>Item</strong>
   </td>
   <td><strong>Req’d</strong>
   </td>
   <td><strong>Description</strong>
   </td>
  </tr>
  <tr>
   <td>Service URL
   </td>
   <td>Yes
   </td>
   <td>The base URL of the Grafana Web Service
   </td>
  </tr>
  <tr>
   <td>Basic Auth Username
   </td>
   <td>Yes
   </td>
   <td>Username assigned to access the Grafana Web Service. This user is a Grafana Administrator who has the authority to execute provisioning operations
   </td>
  </tr>
  <tr>
   <td>Basic Auth Password
   </td>
   <td>Yes
   </td>
   <td>Password assigned to access the Grafana Web Service
   </td>
  </tr>
  <tr>
   <td>Loki Service base URL
   </td>
   <td>No
   </td>
   <td>A URL for your Grafana Loki server without the trailing slash (/)
   </td>
  </tr>
  <tr>
   <td>Loki Service Username
   </td>
   <td>No
   </td>
   <td>The username of Loki Service administrator
   </td>
  </tr>
  <tr>
   <td>Loki Service password
   </td>
   <td>No
   </td>
   <td>The user password needed to authenticate on The Loki Service
   </td>
  </tr>
  <tr>
   <td>Default Role
   </td>
   <td>No
   </td>
   <td>The default role a user will have in a Grafana organization (Viewer or Admin). 
   </td>
  </tr>
  <tr>
   <td>Separate Org Association
   </td>
   <td>Yes
   </td>
   <td>Specifies whether Organization Association occurs separately from User Creation.
   </td>
  </tr>
  <tr>
   <td>Update Grafana Loki Dashboards
   </td>
   <td>Yes
   </td>
   <td>Designed to allow the provisioning system to update a Grafana Dashboard.
   </td>
  </tr>
  <tr>
   <td>Grafana Dashboard Template
   </td>
   <td>No
   </td>
   <td>When a dashboard template is supplied the connector will attempt to create a new Grafana Dashboard for an organization containing the uid of the newly created or updated Datasource. This is done through substitution of any string sequence “&lt;DataSourceUID>” or “__DataSourceUID__” with the actual UID of the newly created Datasource.  
   </td>
  </tr>
  <tr>
   <td>IO Error Retries
   </td>
   <td>No
   </td>
   <td>Number of retries that will be attempted when an IO error occurs
   </td>
  </tr>
  <tr>
   <td>Deep Get Enabled
   </td>
   <td>No
   </td>
   <td>If true when a search operation is executed, the connector will retrieve each individual record returned. This is useful when a search operation does not return all the available fields in an object.
   </td>
  </tr>
  <tr>
   <td>Deep Import Enables
   </td>
   <td>No
   </td>
   <td>If true when an import operation is executed the connector will perform a get operation on each individual record returned. This is useful when the api does not return all the available fields
   </td>
  </tr>
  <tr>
   <td>Pagination Enabled
   </td>
   <td>No
   </td>
   <td>The Grafana Connector supports pagination on all the supported objects
   </td>
  </tr>
  <tr>
   <td>Duplicate Record Returns Id
   </td>
   <td>No
   </td>
   <td>When a create is attempted and an AlreadyExistsException is generated by the driver/invocator, the adapter shall attempt to call getOneByName() driver/invocator method to return the id of the existing record matching the current name value.
   </td>
  </tr>
</table>



# 5	Connector Operations

The Grafana connector implements the following connId SPI operations:



* **SchemaOp** - Allows the Connector to describe which types of objects the Connector manages on the target resource. This includes the options supported for each type of object.
* **TestOp** - Allows testing of the resource configuration to verify that the target environment is available. (ie. to validate the connection to the Grafana Web Service)
* **SearchOp** - Allows the connector to search the Grafana Web Service for resource objects.
* **CreateOp** - Allows the connector to create Grafana Users, Grafana Organizations, and Grafana Datasources
* **DeleteOp** - Allows the connector to delete Grafana Users, Grafana Organizations, and Grafana Datasources
* **UpdateDeltaOp** - Allows for updates of the supported Object Types. Grafana Users, Grafana Organizations, and Grafana Datasources


## Deep Get Explained

The connector supports a **deep get** functionality which returns detailed information for each item returned from a query. This feature is necessary because a query may return partial fields for a record.This is the case with the Grafana User lookup and the GrafanaOrg lookup API calls. **Deep get** is invoked when the query contains paging parameters such as page size and page offset. **Deep get is recommended to be true for this connector.**


## Deep Import Explained

The connector’s deep import option is similar to the deep get option. The deep import option is invoked when a query does not have paging parameters. **Deep Import is recommended to be true for this connector.**


## Duplicate Record Returns Id Explained

The duplicate record returns Id configuration option is invoked when an HTTP POST request, used to create a record, returns HTTP 409 (Conflict). This typically indicates that the record we are attempting to create already exists. When this option is true the connector will attempt to get the record by name and return the record’s ID value to the caller. In this way a record can be seamlessly imported when it already exists on the target server. Unfortunately the Grafana API does not return HTTP 409 instead it returns HTTP 412. Because this is the case the connector will always do a lookup for an existing object type before creating the type.


## Separate Org Association Explained

Separate org association is used when a new user is created. When **false** the user’s organization will be specified at the time of user creation. When **true** the user’s organization will be implemented as a separate api call. Depending on the Grafana configuration a true setting may cause the user to be associated with a default organization which may not be desirable. **We recommend that the setting should be false.**


# 6	Connector Schema

As mentioned in an earlier section, the Grafana connector supports 3 object classes. These are User Objects, OrganizationObjects, and DataSource Objects.


## GrafanaUser Objects


<table>
  <tr>
   <td><strong>Attribute </strong>
   </td>
   <td><strong>Type</strong>
   </td>
   <td><strong>Comment</strong>
   </td>
  </tr>
  <tr>
   <td>avatarUrl
   </td>
   <td>String
   </td>
   <td>The Grouper assigned path of the stem/folder 
   </td>
  </tr>
  <tr>
   <td>disabled
   </td>
   <td>String
   </td>
   <td>The Grouper assigned uuid of the stem/folder
   </td>
  </tr>
  <tr>
   <td>email
   </td>
   <td>String
   </td>
   <td>The last part of the Grouper path. Also known as the folder name. 
   </td>
  </tr>
  <tr>
   <td>lastSeenAt
   </td>
   <td>String
   </td>
   <td>The description of the Grouper folder
   </td>
  </tr>
  <tr>
   <td>lastSeenAtAge
   </td>
   <td>String
   </td>
   <td>A JSON formatted map of name value pairs containing the attribute assignments for the stem
   </td>
  </tr>
  <tr>
   <td>login
   </td>
   <td>String
   </td>
   <td>The user name used for login
   </td>
  </tr>
  <tr>
   <td>name
   </td>
   <td>String
   </td>
   <td>The user’s full name
   </td>
  </tr>
  <tr>
   <td>orgId
   </td>
   <td>String
   </td>
   <td>Multivalued list of Grafana Organization IDs associated with the use 
   </td>
  </tr>
  <tr>
   <td>password
   </td>
   <td>String
   </td>
   <td>The user's password on creation. When not supplied the connector will generate a random 10 character alphanumeric password.
   </td>
  </tr>
  <tr>
   <td>role
   </td>
   <td>String
   </td>
   <td>The user’s role within a Grafana Organization. This is populated by default on user creation.
   </td>
  </tr>
  <tr>
   <td>userId
   </td>
   <td>String
   </td>
   <td>An Integer representing the Users Primary Key within the Grafana System 
   </td>
  </tr>
</table>


ConnId UID -> id

ConnId Name -> login


## GrafanaOrganization Objects


<table>
  <tr>
   <td><strong>Attribute </strong>
   </td>
   <td><strong>Type</strong>
   </td>
   <td><strong>Comment</strong>
   </td>
  </tr>
  <tr>
   <td>id
   </td>
   <td>String
   </td>
   <td>The automatically assigned Integer ID of a Grafana Organization converted to String
   </td>
  </tr>
  <tr>
   <td>name
   </td>
   <td>String
   </td>
   <td>The Name assigned to a Grafana Organization 
   </td>
  </tr>
  <tr>
   <td>address
   </td>
   <td>String
   </td>
   <td>Not Returned (TBD)
   </td>
  </tr>
  <tr>
   <td>dashboards
   </td>
   <td>String
   </td>
   <td>Multivalued JSON dashboard data. Readonly. This field contains the dashboard configuration for an organization.
   </td>
  </tr>
</table>


ConnId UID -> id

ConnId Name -> name


## GrafanaDatasource Objects


<table>
  <tr>
   <td><strong>Attribute </strong>
   </td>
   <td><strong>Type</strong>
   </td>
   <td><strong>Comment</strong>
   </td>
  </tr>
  <tr>
   <td>datasourceId
   </td>
   <td>String
   </td>
   <td>The automatically assigned Integer ID of a Grafana Datasource converted to String. This value is deprecated in V9.0 and replaced with <strong>uid </strong>for lookups
   </td>
  </tr>
  <tr>
   <td>id
   </td>
   <td>String
   </td>
   <td>A construction used for the <strong>ConnId UID</strong> which combines the orgId and the uid separated with an underscore. Eg “223_3V8zQt14k”
   </td>
  </tr>
  <tr>
   <td>name
   </td>
   <td>String
   </td>
   <td>The name assigned to a Grafana Datasource 
   </td>
  </tr>
  <tr>
   <td>uid
   </td>
   <td>String
   </td>
   <td>The automatically generated uid of a Grafana Datasource. This value is modifiable
   </td>
  </tr>
  <tr>
   <td>orgId
   </td>
   <td>String
   </td>
   <td>The Grafana Organization ID associated with a Grafana Datasource
   </td>
  </tr>
  <tr>
   <td>type
   </td>
   <td>String
   </td>
   <td>The datasource type defaults to “loki” when not specified on create
   </td>
  </tr>
  <tr>
   <td>access
   </td>
   <td>String 
   </td>
   <td>The datasource access defaults to “proxy” when not specified on create
   </td>
  </tr>
  <tr>
   <td>url
   </td>
   <td>String
   </td>
   <td>The datasource URL. When not specified on create the connector uses the configured value
   </td>
  </tr>
  <tr>
   <td>isDefault
   </td>
   <td>Boolean
   </td>
   <td>Set to true when not specified on create. 
   </td>
  </tr>
  <tr>
   <td>basicAuth
   </td>
   <td>Boolean
   </td>
   <td>Specifies whether the Grafana Datasource uses basic authentication 
   </td>
  </tr>
  <tr>
   <td>basicAuthUser
   </td>
   <td>String
   </td>
   <td>The basic authentication user name
   </td>
  </tr>
  <tr>
   <td>basicAuthPassword
   </td>
   <td>String
   </td>
   <td>The basic authentication password used on create or update
   </td>
  </tr>
  <tr>
   <td>user
   </td>
   <td>String
   </td>
   <td>The data source user name. Not used by Loki but supports other types of datasources
   </td>
  </tr>
  <tr>
   <td>password
   </td>
   <td>String
   </td>
   <td>The datasource user password. Not used by Loku but support other types of Grafana Datasources
   </td>
  </tr>
  <tr>
   <td>database
   </td>
   <td>String 
   </td>
   <td>Not supported at this time. The Grafana datasource update operation will reflect back this value to the service when supplied with a get operation 
   </td>
  </tr>
  <tr>
   <td>jsonData
   </td>
   <td>String
   </td>
   <td>Additional Information needed to create or update a Grafana Datasource
   </td>
  </tr>
  <tr>
   <td>secureJsonData
   </td>
   <td>String
   </td>
   <td>Used to provide data which Grafana should encrypt internally. This information is not returned on read operations. In later versions Grafana returns secureJsonFields
   </td>
  </tr>
</table>


ConnId UID -> orgId + “_” + uid

ConnId Name -> orgId + “_” + name


## Grafana Dashboards

There is no particular schema for grafana dashboards except that the data is supplied from a configuration


# 7	Connector Query Capabilities

The Grafana Connector supports the following Query Operations


## GrafanaUser



* Get User by id
* Get User by Name (ie login)
* Get User by Email address
* Get list of users by page number and page size
* Get all Users


## Grafana Organization



* Get Organization by id
* Get Organization by name
* Get list of Organizations by zero based page number and page size
* Get all Organizations


## Grafana Datasources



* Get Datasource by ConnId UID
* Get Datasource by ConnId Name
* Get list of Datasources by Organization Id (orgId)
* Get list of Datasources by zero based page number and page size
* Get all Data Sources


# 8 Connector Create and Update Operations

This section describes the specific details of the Grafana Connector Create and Update operation.


## Creating a Grafana Organization

When creating a Grafana Organization the only item that can be supplied is the Organization name. The connector automatically responds with the automatically generated integer ID supplied by the Grafana API. This value is converted to a string on return.


## Updating a Grafana Organization

The only item that can be updated at this time is the Organization Name. Newer versions of the Grafana API allow the supply of an address which the connector can easily be upgraded to support.


## Creating a Grafana User

When creating a new Grafana User you must supply a name, login name, email address, and optionally a password. If the password is not supplied the connector generates a random 10 character alphanumeric password. A Grafana User can be associated with one or more organizations at creation time. When an organization is supplied but a role is not supplied the connector uses the configuration **Default Role** value to specify the user’s role.


## Updating a Grafana User

On update the name, login, email address, and avatarUrl may be changed. Also the connector supports adding or removing a user’s associated organization(s).


## Creating a Grafana DataSource

Grafana Data sources are required to be associated with an Organization. As such an organization and a name must be supplied on creation. The Connector uses the configuration to supply values for automatically creating a loki datasource when other details are not supplied.  In this way an administrator can use specific values on creation to specify other types of datasource besides loki.

See [https://grafana.com/docs/grafana/latest/developers/http_api/data_source/](https://grafana.com/docs/grafana/latest/developers/http_api/data_source/) for details.


## Updating a Grafana DataSource

The Grafana Datasource api does not support a delta update operation by default. As a result the connector implements Delta updates by feeding back the current values to the datasource api on update. The connector administrator is therefore able to update specific fields as needed.


## Creating a Grafana Dashboard

A grafana dashboard is automatically created when a datasource is created and the connector configuration contains a dashboard template. The connector will make a case sensitive substitution of  “&lt;DataSourceUID>” or “__DataSourceUID__” in the template with the uid of the Datasource. The dashboard is actually associated with an organization so the value is stored in the organization schema.


## Updating a Grafana Dashboard

The connector will update the Grafana Dashboard for a Datasource whenever the Datasource is updated, The Dashboard Template exists, and the Dashboard Update option is set to true.


# Using Grouper to Provision Grafana with Midpoint

This use-case relies on a higher education scenario where Grouper is used to manage the fundamental data having a business intent of Grafana “viewer” administrators. That data for such administrators is represented in Grouper as a group located under a stem. That group possesses a naming convention and contains members comprising the list as required by Grafana, the downstream platform.  midPoint serves as the provisioning and identity management hub between Grouper and Grafana, and it requires installation and configuration of the respective Grouper connector and Grafana connector.  See the separate publications about both those connectors.

This document describes the expected configuration within midPoint to manifest this use-case.  Note that:



1. Group memberships may eventually be managed within midPoint, however as of this publication, that management is conducted exclusively within Grouper and an ldap directory


# Initial Design Consideration

Organization focus objects are used in this solution to represent Grafana “viewer” administrator lists within midPoint. It is advised that consideration be given to a design for management of these organization objects. For example, the simplest approach would be to store all organization objects under one root in an org hierarchy.  However, this simplest of approaches may not meet your needs if you are administering one midPoint instance that manages multiple Grafana Data sources’ lists or that manages orgs which aren’t Grafana lists at all.  In this scenario, it would not be uncommon to adopt a design where each Grafana datasource will be represented as organization objects to be managed under a distinct org root: one org root for each kind of datasource. It bears repeating that it may be important to also consider discerning orgs between those representing Grafana lists and orgs that have nothing to do with Grafana. The choice is yours to make in order to best meet your enterprise’s needs.  If you are indeed managing multiple Grafana Data sources and prefer to hierarchically manage the organization focus objects under a single root, then please consider making use of midPoint’s “intent” feature along with use of org attribute(s) that convey each org’s Grafana datasource.


# Grafana: Configuring the midPoint Resource Connection

Although one instance of any given version of a connector software package (i.e. a .jar file) at a time is installed on midPoint, typically an administrator is at liberty to create multiple resource connection configurations that make use of that software. For example, midPoint could be configured to connect to multiple Active Directory resources and/or multiple relational database resources, each of those types respectively relying on a single software package that handles the data communications over the wire.  The same applies to Grafana.  Therefore, administrators can expect to define a separate resource connection configuration that is dedicated to each Grafana instance for which you are provisioning user lists.

Within midPoint, the treatment of data coming from a resource or destined for a resource can be discerned by declaring that data by what is known as a combination of both midPoint kind and intent. In the resource configuration for Grafana, the resource schema’s “GrafanaOrganization” object and its “GrafanaDatasource” object can be associated with a selection from the kinds: account, entitlement, generic, or unknown.  Entitlements are most appropriate for resource objects that are meant to have manageable membership lists of identities.  For example, an entitlement can represent the permitted ability to physically enter a secured building, and that permitted ability has a membership of those identities (e.g. mobile devices, people, etc) given that ability. An appropriate selection for the “GrafanaOrganization” would be entitlement, and this document assumes a selection of entitlement. Within this document, we also assume a selection of entitlement for the “GrafanaDatasource” object. The entitlement (group intent) data representing GrafanaOrganization is then configured in midPoint as synchronized to an “organization” focus object, and also synchronizing to that same “organization” focus object is the entitlement (DataSource intent) representing the GrafanaDatasource.  So, think of the midPoint organization focus object as possessing two facades that are its projections to the Grafana resource: a GrafanaOrganization and a GrafanaDatasource.


## Schema Extensions

For clarity over what various attributes’ values mean, it is advised to make use of schema extensions. Regarding these Grafana “viewer” lists, add the following attributes as indexed org schema extensions:



* grafanaOrgId


## Configuring the schema handling

Create the following outbound and inbound mappings, each list shown separately.  Ensure you add the org schema extensions prior to configuring the connection’s schema handling.

**For the entitlement (group intent) representing GrafanaOrganization**

**Outbound Attribute Mappings:**

identifier → ri:name (in some deployments, name formatting changes may be required)

**Inbound Attribute Mappings:**

ri:orgId → extension/grafanaOrgId


## Configure the synchronization

Within the objectSynchronization configuration, select “OrgType” for focus object.  Then define the correlation filter to equate the projection’s name with the organization object’s identifier. However, note that in some deployments, name formatting changes may be required.  In this document, we supply such an example filter for a correlation:


```
<correlation>
<q:equal>
     <q:path>identifier</q:path>
     <expression>
          <script>
          <code>
          def theWorkingName=basic.getAttributeValue(shadow, 'name')
          return theWorkingName.replace(".","_")
          </code>
          </script>
    </expression>
    </q:equal>
</correlation>
```


_Note the q namespace is declared xmlns:q="http://prism.evolveum.com/xml/ns/public/query-3"_


## For the entitlement (DataSource intent) representing GrafanaDatasource

**Outbound Attribute Mappings:**

A composition of extension/grafanaOrgId and identifier → ri:name (in some deployments, name formatting changes may be required) The datasource’s ri:name above is composed by the Grafana org ID, an underscore, and the institution’s identifier in midPoint which holds the institution name. This approach is used only during insert of a new Grafana datasource.  Grafana may adjust the datasource name. Therefore, this mapping’s strength should be set to weak.


```
extension/grafanaOrgId → ri:orgId
true → ri:basicAuth
<the password string> → ri:basicAuthPassword
'{"httpHeaderName1": "X-Scope-OrgID"}' → ri:jsonData
"http://loki.dev.infra.eduroam.us" → ri:url
'{"httpHeaderValue1": "' + identifier + '"}'  → ri:secureJsonData  
```


In some deployments, name formatting changes may be required.

The datasource’s ri:secureJsonData above is composed of some string literals and the institution’s identifier in midPoint which holds the institution name. This approach is used only during creation of a new Grafana org.  Grafana may adjust the datasource secureJsonData. Therefore, this mapping’s strength should be set to weak and should be conditionally applied. The condition is warranted in Grafana versions prior to 9.3. The condition is:


```
<condition>
<script>
 	<code>
     theUid = basic.getAttributeValue(shadow, 'uid')
     return ((null == theUid) || ("" == theUid))
</code>
</script>
</condition>

true → ri:isDefault
"grafana" → ri:basicAuthUser
"loki" → ri:type
"proxy" → ri:access
```


**Inbound Attribute Mappings:**

&lt;none>

**Configure the synchronization**

Within the synchronization configuration, select “OrgType” for focus object.  Then define the correlation filter to equate the projection’s orgId with the organization object’s extension/grafanaOrgId.  This is an example filter for such a correlation:


```
<q:equal>
<q:path>extension/grafanaOrgId</q:path>
    		<expression>
    			<path>$shadow/attributes/orgId</path>
    		</expression>
</q:equal>
```


_Note the q namespace is declared xmlns:q="http://prism.evolveum.com/xml/ns/public/query-3"_

**For the account (Viewer intent) representing GrafanaUser**

This is a somewhat straightforward configuration for schema handling. Please see the artifacts from the example demonstration.


# Grouper: Configuring the midPoint Resource Connection

Although one instance of any given version of a connector software package (i.e. a .jar file) at a time is installed on midPoint, typically an administrator is at liberty to create multiple resource connection configurations that make use of that software. For example, midPoint could be configured to connect to multiple Active Directory resources and/or multiple relational database resources, each of those types respectively relying on a single software package that handles the data communications over the wire.  The same applies to Grouper.  Therefore, administrators can expect to define a separate resource connection configuration that is dedicated to each Grouper base stem for which you are provisioning groups or lists.

The example demonstration leveraged the pattern already present on the InCommon ABC Workbench.  The schema handling for the Grouper resource was modified in a very minor fashion in order to accommodate the branch of the Grouper tree that holds institutions meant to be provisioned for Grafana access.

As per the specifications cited during Grafana connector design, the Grouper branch follows this naming convention: app:fm:ref:orgrole:&lt;name of institution>:eduroamadmin

Therefore, stems and groups with this naming convention are imported into midPoint following the pattern already present in the Workbench.  Your specific midPoint deployment may vary.  The objective is to have midPoint organization focus objects possessing attributes, such as the extension/grafanaOrgId, and possessing members, all of which reflect the data necessary to provision Grafana.
