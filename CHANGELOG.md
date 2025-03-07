# connector-grafana

## Change Log
+ **2.0.3**  - Fix issue with Dashboard OrgId expecting string.
+ **2.0.2**  - Added Multiple Grafana Dashboard Support.  
+ **1.8.8**  - Fix User Updates when email or login is not specified
+ **1.8.7**  - Update to Bases connector 4.1.8
+ **1.8.6**  - Uses Bases Connector V3.1.3
+ **1.7.11** - Added readonly timezone and homeDashboardId attributes to GrafanaOrganization Object type
+ **1.7.10** - Changed Org preference update to add the homeDashboardId to support Grafana 8.X. Added default timezone option in configuration
+ **1.7.9** - Fixed unmatched curly brace when logging org preference update failure
+ **1.7.8** - Set the Grafana loki password as confidential and not required
+ **1.7.6** - Set Home Dashboard Organization Preference when a datasource is created, updated, or read and not set  
+ **1.7.5** - Added "\_\_DataSourceUID\_\_" as an alternate datasource substitution code for the dashboard template.
+ **1.7.4** - Added Dashboard JSON content to organization. An organization can have multiple dashboards. Changed some warning level logs to info level. 
+ **1.7.3** - Updated Create check for User already exists using login rather than email address 
+ **1.7.2** - Added Create check for User already exists and warning messages when it occurs
+ **1.7.1** - Added Grafana Dashboard Create and Update Support
+ **1.7.0** - Initial Stable 
+ **1.5.9** - Add Logging on Org create
+ **1.5.8** - Org Identifier should not be specified on create
+ **1.5.7** - Fixes for USer Create null ID which should not be sent
+ **1.5.5** - Fixes for Data Source Lookup
+ **1.5.3** - Initial development
