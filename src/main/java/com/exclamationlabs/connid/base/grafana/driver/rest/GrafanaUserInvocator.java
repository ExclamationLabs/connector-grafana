package com.exclamationlabs.connid.base.grafana.driver.rest;

import com.exclamationlabs.connid.base.connector.driver.DriverInvocator;
import com.exclamationlabs.connid.base.connector.driver.rest.RestResponseData;
import com.exclamationlabs.connid.base.connector.results.ResultsFilter;
import com.exclamationlabs.connid.base.connector.results.ResultsPaginator;
import com.exclamationlabs.connid.base.grafana.model.GrafanaAddUserToOrg;
import com.exclamationlabs.connid.base.grafana.model.GrafanaUser;
import com.exclamationlabs.connid.base.grafana.model.GrafanaUserOrg;
import com.exclamationlabs.connid.base.grafana.model.response.GrafanaStandardResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

import java.util.*;

public class GrafanaUserInvocator implements DriverInvocator<GrafanaDriver, GrafanaUser>
{
    private static final Log LOG = Log.getLog(GrafanaUserInvocator.class);

    /**
     * Adds an existing user to an organization
     * @param driver The Grafana Driver object
     * @param user The User who will be added to the organization
     * @param orgId The OrdId to which the user will be added
     */
    public boolean addUserToOrg(GrafanaDriver driver, GrafanaUser user, int orgId)
    {
        boolean success = false;
        GrafanaStandardResponse response;
        RestResponseData<GrafanaStandardResponse> rd;
        // determine the role to be used
        String role = user.getRole();
        if ( role == null || role.trim().length() == 0)
        {
            role = driver.getConfiguration().getDefaultOrgRole();
        }
        // Determine the login or email to be used
        String login = user.getLogin();
        if ( login == null || login.trim().length() == 0 )
        {
            login = user.getEmail();
        }
        // Determine whether the request can be executed successfully
        if ( user.getUserId() != null  )
        {
            success = addUserToOrg(driver, login, role, user.getUserId(), orgId);
        }
        else
        {
            LOG.error("Cannot add userId = {0} with login = {1} to organization = {2}", user.getUserId(), login, orgId);
        }
        return success;
    }
    /**
     * Adds an existing user to an organization
     * @param driver The Grafana Driver object
     * @param login The login name or the email address of the user to be added to the organization
     * @param orgId The OrdId to which the user will be added
     */
    public boolean addUserToOrg(GrafanaDriver driver, String login, String role, int userId, int orgId)
    {
        boolean success = false;
        GrafanaStandardResponse response;
        RestResponseData<GrafanaStandardResponse> rd;

        // Not going to attempt unless userId is valid
        if ( userId > 0 )
        {
            // Validate role
            if ( role == null || role.trim().length() == 0 )
            {
                // Substitute default role
                role = driver.getConfiguration().getDefaultOrgRole();
            }
            // Validate user login or email
            if ( login == null || login.trim().length() == 0 )
            {
                // since this information is not supplied we need to go get it
                GrafanaUser user = getOne(driver, String.valueOf(userId), null);
                if ( user != null )
                {
                    login = user.getLogin();
                }
                else
                {
                    // This should never happen unless the service is offline
                    return false;
                }
            }
            GrafanaAddUserToOrg addUser = new GrafanaAddUserToOrg();
            addUser.setRole(role);
            addUser.setLoginOrEmail(login);
            // proceeding with the service request
            StringBuilder sb = new StringBuilder();
            sb.append( "{ \n\"role\" : ");
            sb.append( "\"" + role + "\", \n");
            sb.append( "\"LoginOrEmail\" : ");
            sb.append( "\"" + login + "\" \n");
            sb.append("}");
            String body = sb.toString();
            rd = driver.executePostRequest("/orgs/" + orgId + "/users",
                    GrafanaStandardResponse.class, body, driver.getAdminHeaders());

            response = rd.getResponseObject();

            if ( rd.getResponseStatusCode() == 200 )
            {
                LOG.warn( "HTTP StatusCode {0}, login {1}, OrgId {2}, Message {3}",
                        rd.getResponseStatusCode(), login, orgId, response.getMessage());
                success = true;
            }
            else
            {
                LOG.warn( "HTTP StatusCode {0}, login {1}, OrgId {2}, Message {3}",
                        rd.getResponseStatusCode(), login, orgId, response.getMessage());
            }
        }
        else
        {
            LOG.error("Cannot add userId = {0} with login = {1} to organization = {2}", userId, login, orgId);
        }
        return success;
    }

    /**
     * Creates a New Grafana User or throws a ConnectorException.
     * If the user information contains an orgId then the user will automatically be added to that organization
     * If the user has a list of Organizations they well be added to each specified org
     * @param driver the Grafana Driver
     * @param user The user information to be created
     * @return The user's Id
     * @throws ConnectorException
     */
    @Override
    public String create(GrafanaDriver driver, GrafanaUser user) throws ConnectorException
    {
        RestResponseData<GrafanaStandardResponse> response;
        GrafanaStandardResponse createInfo;
        int userId = 0;
        String origRole = user.getRole();
        if ( user.getOrgId() == null
                && user.getOrganizations() != null
                && user.getOrganizations().size() > 0
                && !driver.getConfiguration().getSeparateOrgAssociation()  )
        {
            user.setOrgId(decomposeOrgId(user.getOrganizations().get(0)));
            LOG.warn("Creating User with Org Association {0}", user.getOrgId());
            if ( user.getRole() == null || user.getRole().trim().length() == 0 )
            {
                user.setRole(driver.getConfiguration().getDefaultOrgRole());
            }
        }
        else
        {
            user.setRole(null);
            LOG.warn("Creating user without Org Association");
        }

        if ( user.getPassword()== null || user.getPassword().trim().length() == 0 )
        {
            user.setPassword(RandomStringUtils.randomAlphanumeric(10));
        }

        GrafanaUser actual = getOneByName(driver, user.getEmail());
        if ( actual == null)
        {
            response = driver.executePostRequest("/admin/users",
                    GrafanaStandardResponse.class,
                    user,
                    driver.getAdminHeaders());

            createInfo = response.getResponseObject();

            if ( createInfo == null || createInfo.getId() == null || createInfo.getId().trim().length() == 0 )
            {
                String message = "HTTP status" + response.getResponseStatusCode() +
                        ". Failed to create Global Grafana User: " + createInfo.getMessage() ;
                LOG.warn( message);
                throw new ConnectorException(message);
            }
            else if (StringUtils.isNumeric(createInfo.getId().trim()))
            {
                userId = Integer.valueOf(createInfo.getId().trim());
            }
        }
        else
        {
            userId = actual.getId();
            createInfo = new GrafanaStandardResponse();
            createInfo.setId(String.valueOf(actual.getId()));
            LOG.warn("User already exists returning ID");
        }

        // If the User Model includes has a list or organizations, then add them when not already associated
        if (user.getOrganizations() != null && user.getOrganizations().size() > 0 && userId > 0 )
        {
            // Verify that all Orgs have been associated
            List<GrafanaUserOrg> userOrgs = getUserOrganizations(driver, userId);
            for ( String organization:  user.getOrganizations() )
            {
                int orgId = decomposeOrgId(organization);
                if (isUserInOrg(userOrgs, orgId) == null )
                {
                    String role = decomposeRole(organization);
                    if ( role == null || role.trim().length() == 0 )
                    {
                        if ( origRole != null && origRole.trim().length() > 0 )
                        {
                            role = origRole;
                        }
                        else
                        {
                            role = driver.getConfiguration().getDefaultOrgRole();
                        }
                    }
                    addUserToOrg(driver, user.getLogin(), role, userId, orgId);
                }
            }
        }
        return createInfo.getId();
    }

    /**
     * Strips the numeric OrgId from the Formatted Org Name
     * @param organization The ORG formatted as "IDNUMBER_ORGNAME_ROLE"
     * @return
     */
    public static int decomposeOrgId(String organization)
    {
        int orgId = 0;
        if ( organization != null )
        {
            int idx = organization.indexOf("_");
            if (idx > 0 && organization.length() >= idx + 1)
            {
                String org = organization.substring(0, idx);
                if (StringUtils.isNumeric(org.trim()))
                {
                    orgId = Integer.valueOf(org.trim());
                }
            }
            else if (StringUtils.isNumeric(organization.trim()))
            {
                orgId = Integer.valueOf(organization.trim());
            }
        }
        return orgId;
    }
    /**
     * Strips the numeric OrgId from the Formatted Org Name
     * @param organization The ORG formatted as "IDNUMBER_ORGNAME_ROLE"
     * @return
     */
    public static String decomposeRole(String organization)
    {
        String role = null;
        if ( organization != null )
        {
            int idx = organization.lastIndexOf("_");
            if (idx > 0 && organization.length() >= idx + 1)
            {
                String aRole = organization.substring(idx+1);
                if ( aRole != null
                        && (aRole.trim().equalsIgnoreCase("Viewer")
                                || aRole.trim().equalsIgnoreCase("Admin")))
                {
                    role = aRole.trim();
                }
            }
        }
        return role;
    }
    @Override
    public void delete(GrafanaDriver driver, String userId) throws ConnectorException
    {
        if ( userId != null && userId.trim().length() > 0 )
        {
            GrafanaUser user = getOne(driver, userId, new HashMap<String, Object>());
            if ( user != null && (user.getIdentityIdValue().equalsIgnoreCase(userId.trim()) ||
                            user.getIdentityNameValue().equalsIgnoreCase(userId.trim())))
            {
                GrafanaStandardResponse response;
                response = driver.executeDeleteRequest(
                        "/admin/users/"+ user.getIdentityIdValue(),
                        GrafanaStandardResponse.class,
                        driver.getAdminHeaders()).getResponseObject();
                String message = response.getMessage();

                if ( message != null && (message.contains("delete") || message.contains("Delete") ) )
                {
                    LOG.warn("Grafana User " + user.getName() + " deleted");
                }
                else
                {
                    LOG.warn("Request to Delete Grafana user " + userId + ": "+ message);
                }
            }
            else
            {
                // Ignore case when user is not found
                LOG.warn("User with Id " + userId + "does not exist");
            }
        }
        else
        {
            throw new ConnectorException("Cannot delete Grafana User when Id not specified");
        }
        return;
    }

    public boolean deleteUserFromOrg(GrafanaDriver driver, int userId, String orgId)
    {
        boolean success = false;
        GrafanaStandardResponse response;
        RestResponseData<GrafanaStandardResponse> rd;
        rd = driver.executeDeleteRequest("/orgs/" + orgId + "/users/" + userId,
                GrafanaStandardResponse.class, driver.getAdminHeaders());

        response = rd.getResponseObject();

        if ( rd.getResponseStatusCode() == 200 )
        {
            success = true;
        }
        else
        {
            LOG.warn( "HTTP StatusCode {0}, UserId {1), OrgId {2}, Message {3}",
                    rd.getResponseStatusCode(), userId, orgId, response.getMessage());
        }
        return success;
    }

    @Override
    public Set<GrafanaUser> getAll(GrafanaDriver driver, ResultsFilter resultsFilter, ResultsPaginator paginator, Integer maxResultsRecords) throws ConnectorException
    {
        String queryParameters = "";
        if (paginator.hasPagination())
        {
            queryParameters = "?perpage=" + paginator.getPageSize() + "&page=" + paginator.getCurrentPageNumber();
        }
        GrafanaUser[] userArray = null;
        Set<GrafanaUser> users = new HashSet<GrafanaUser>();

        RestResponseData<GrafanaUser[]> rd = driver.executeGetRequest("/users" + queryParameters,
                                                                        GrafanaUser[].class,
                                                                        driver.getAdminHeaders());
        userArray = rd.getResponseObject();
        users = new HashSet<>(Arrays.asList(userArray));
        return users;
    }

    /**
     * Get a Single user from the Grafana Service
     * @param driver The Rest Driver for the Connector
     * @param id the unique id of the User
     * @param map Operation Options Map
     * @return
     * @throws ConnectorException
     */
    @Override
    public GrafanaUser getOne(GrafanaDriver driver, String id, Map<String, Object> map) throws ConnectorException
    {
        GrafanaUser user = null;
        Set<GrafanaUser> users = new HashSet<GrafanaUser>();
        if ( id != null && id.trim().length() > 0 )
        {
            LOG.warn("Lookup user with ID {0} ", id);
            if (StringUtils.isNumeric(id.trim()))
            {
                // Get user By Numeric ID
                RestResponseData<GrafanaUser> rd = driver.executeGetRequest("/users/" + id.trim(),
                        GrafanaUser.class,
                        driver.getAdminHeaders());
                user = rd.getResponseObject();
                populateUserOrganizations(driver, user);
            }
            else
            {
                user = getOneByName(driver, id);
            }
        }
        else
        {
            throw new ConnectorException("Grafana user id, login name, or email not specified");
        }

        return user;
    }

    /**
     * Search for a user by login or email address
     * @param driver This Grafana Driver instance
     * @param name The login or email address of the user to be returned
     * @return A GrafanaUser, null, or throws exception
     * @throws ConnectorException
     */
    @Override
    public GrafanaUser getOneByName(GrafanaDriver driver, String name) throws ConnectorException
    {
        GrafanaUser user;
        if ( name != null && name.trim().length() > 0 )
        {
            LOG.warn("Lookup user with login or email ", name);
            // Get user By Numeric ID
            RestResponseData<GrafanaUser> rd = driver.executeGetRequest(
                    "/users/lookup?loginOrEmail=" + name.trim(),
                    GrafanaUser.class,
                    driver.getAdminHeaders());
            user = rd.getResponseObject();
            populateUserOrganizations(driver, user);
        }
        else
        {
            throw new ConnectorException("Grafana user id, login name, or email not specified");
        }
        return user;
    }

    /**
     * Query the Grafana Server for a list of users that belong to the specified Organization
     * @param driver Grafana Driver instance
     * @param orgId  The org to be whose users are to be returned
     * @return
     */
    public Set<GrafanaUser> getUsersInOrganization(GrafanaDriver driver, int orgId)
    {
        Set<GrafanaUser> users = new HashSet<GrafanaUser>();
        RestResponseData<? extends Set> rd;
        rd = driver.executeGetRequest("/orgs/"+orgId+"/users", users.getClass(), driver.getAdminHeaders());
        users = rd.getResponseObject();
        return users;
    }

    /**
     * Query the Grafana Server for a list of orgs that a user is member belong
     * @param driver Grafana Driver instance
     * @param userId  The userId whose organizations are to be returned
     * @return A set or Organizations associated with the user specified
     */
    public ArrayList<GrafanaUserOrg> getUserOrganizations(GrafanaDriver driver, int userId)
    {

        GrafanaUserOrg[] orgs = null;
        RestResponseData<GrafanaUserOrg[]> rd;
        rd = driver.executeGetRequest("/users/"+userId+"/orgs", GrafanaUserOrg[].class, driver.getAdminHeaders());
        orgs = rd.getResponseObject();
        ArrayList<GrafanaUserOrg> list = new ArrayList<>(Arrays.asList(orgs));
        return list;
    }

    /**
     * Searches for a user within an org and returns the role name
     * @param driver The Grafana Driver instance
     * @param userId The userId to be found
     * @param orgId The organization to be searched
     * @return The role of the user within the org or null when the user is not found to be part of the org
     */
    public String isUserInOrg(GrafanaDriver driver, int userId, int orgId)
    {
        String role = null;
        if ( userId  > 0)
        {
            Set<GrafanaUser> users = getUsersInOrganization(driver, orgId);
            if ( users != null && users.size() > 0 )
            {
                for (GrafanaUser user : users)
                {
                    if ( user.getUserId() == userId )
                    {
                        role = user.getRole();
                    }
                }
            }
        }
        return role;
    }

    /**
     * Searches for an org within a set of Organizations associated with a user
     * @param userOrgs A Set of Organizations
     * @param orgId The orgId to be found
     * @return The Organization's Definition including the users role
     */
    public GrafanaUserOrg isUserInOrg(List<GrafanaUserOrg> userOrgs, int orgId)
    {
        GrafanaUserOrg theUserOrg = null;
        if ( userOrgs != null && userOrgs.size() > 0)
        {
            for (GrafanaUserOrg userOrg : userOrgs)
            {
                if ( userOrg.getOrgId() == orgId )
                {
                    theUserOrg = userOrg;
                    break;
                }
            }
        }
        return theUserOrg;
    }

    /**
     * Add the Organizations that a user is a member of to GrafanaUser model
     * @param driver The Grafana Driver
     * @param user the user whose orgainization need to be known
     */
    public void populateUserOrganizations(GrafanaDriver driver, GrafanaUser user)
    {
        // once we found the user we now need to discover what Org(s) they reside in and the role they occupy
        // in each of those Org(s)
        if ( user != null )
        {
            LOG.warn("Lookup Organizations for user {0} with email {1} ", user.getLogin(), user.getEmail());
            ArrayList<GrafanaUserOrg> orgs = getUserOrganizations(driver, user.getUserId());
            if ( orgs != null && orgs.size() > 0 )
            {
                List<String> organization = new ArrayList<>();
                for ( GrafanaUserOrg userOrg: orgs )
                {
                    user.setRole(userOrg.getRole());
                    user.setOrgId(userOrg.getOrgId());
                    // organization.add(String.format("%d_%s_%s", userOrg.getOrgId(), userOrg.getName(), userOrg.getRole()));
                    organization.add(String.format("%d", userOrg.getOrgId()));
                }
                user.setOrganizations(organization);
            }
        }
    }
    /**
     * Updates a User. For example to set their orgId. This method may need to update the global user account as well
     * as the organizational user account.
     * @param driver The driver tied to this connector
     * @param id userId of the User being updated
     * @throws ConnectorException
     */
    @Override
    public void update(GrafanaDriver driver, String id, GrafanaUser user) throws ConnectorException
    {
        GrafanaStandardResponse response;
        RestResponseData<GrafanaStandardResponse> responseData;
        int userId = 0;

        if ( StringUtils.isNumeric(id))
        {
            // Grafana User allows one or more of these  items to be specified
            if ( user.getEmail() == null && user.getLogin() == null )
            {
                GrafanaUser existing = getOne(driver, id, null );
                if ( existing != null )
                {
                    user.setEmail(existing.getEmail());
                    user.setLogin(existing.getLogin());
                }
            }


            responseData = driver.executePutRequest(
                    "/users/" + id,
                    GrafanaStandardResponse.class,
                    user,
                    driver.getAdminHeaders());
            response = responseData.getResponseObject();



            // Retrieve the Organizations associated with the User
            userId = Integer.valueOf(id);
            ArrayList<GrafanaUserOrg> userOrgs = getUserOrganizations(driver, userId);

            // Check for Delta update to add the User to an Org
            if ( user.getOrgsAdd() != null && user.getOrgsAdd().size() > 0)
            {
                for (String organization: user.getOrgsAdd())
                {
                    int orgId = decomposeOrgId(organization);
                    String role = decomposeRole(organization);
                    if ( isUserInOrg(userOrgs, orgId) == null )
                    {
                        if ( role != null && role.trim().length() > 0  )
                        {
                            role = driver.getConfiguration().getDefaultOrgRole();
                        }
                        addUserToOrg(driver, user.getLogin(), role, userId, orgId );
                    }
                }
            }

            // Check for delta update to remove the user from an org
            if ( user.getOrgsRemove() != null && user.getOrgsRemove().size() > 0)
            {
                for (String organization: user.getOrgsRemove())
                {
                    int orgId = decomposeOrgId(organization);
                    if ( isUserInOrg(userOrgs, orgId) != null )
                    {
                        deleteUserFromOrg(driver, userId, String.valueOf(orgId));
                    }
                }
            }
            // Perhaps the user org is specified otherwise
            if ( user.getOrgId() != null  )
            {
                user.setUserId(userId);
                user.setId(userId);
                // Determine whether the user is already in the organization

                GrafanaUserOrg userOrg = isUserInOrg(userOrgs, user.getOrgId());
                if ( userOrg == null )
                {
                    addUserToOrg(driver, user.getLogin(), user.getRole(), userId, user.getOrgId() );
                }
            }
        }
        else
        {
            LOG.warn("UserId " + id + "not supplied or not Numeric ");
        }
    }
}
