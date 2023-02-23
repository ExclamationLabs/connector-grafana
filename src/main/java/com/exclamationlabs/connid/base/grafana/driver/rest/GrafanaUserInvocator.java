package com.exclamationlabs.connid.base.grafana.driver.rest;

import com.exclamationlabs.connid.base.connector.driver.DriverInvocator;
import com.exclamationlabs.connid.base.connector.driver.rest.RestResponseData;
import com.exclamationlabs.connid.base.connector.results.ResultsFilter;
import com.exclamationlabs.connid.base.connector.results.ResultsPaginator;
import com.exclamationlabs.connid.base.grafana.model.GrafanaAddUserToOrg;
import com.exclamationlabs.connid.base.grafana.model.GrafanaUser;
import com.exclamationlabs.connid.base.grafana.model.GrafanaUserOrg;
import com.exclamationlabs.connid.base.grafana.model.response.GrafanaStandardResponse;
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
        if ( user.getUserId() != 0  )
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
            sb.append(" }");
            String body = sb.toString();
            rd = driver.executePostRequest("/api/orgs/" + orgId + "/users",
                    GrafanaStandardResponse.class, addUser, driver.getAdminHeaders());

            response = rd.getResponseObject();

            if ( rd.getResponseStatusCode() == 200 )
            {
                LOG.info( "HTTP StatusCode {0}, login {1}, OrgId {2}, Message {3}",
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

        response = driver.executePostRequest("/api/admin/users",
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

        return createInfo.getId();
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
                        "/api/admin/users/"+ user.getIdentityIdValue(),
                        GrafanaStandardResponse.class,
                        driver.getAdminHeaders()).getResponseObject();
                String message = response.getMessage();

                if ( message != null && (message.contains("delete") || message.contains("Delete") ) )
                {
                    LOG.info("Grafana User " + user.getName() + " deleted");
                }
                else
                {
                    LOG.info("Request to Delete Grafana user " + userId + ": "+ message);
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

    public boolean deleteUserFromOrg(GrafanaDriver driver, GrafanaUser user, String orgId)
    {
        boolean success = false;
        GrafanaStandardResponse response;
        RestResponseData<GrafanaStandardResponse> rd;
        rd = driver.executeDeleteRequest("/api/orgs/" + orgId + "/users/" + user.getUserId(),
                GrafanaStandardResponse.class, driver.getAdminHeaders());

        response = rd.getResponseObject();

        if ( rd.getResponseStatusCode() == 200 )
        {
            success = true;
        }
        else
        {
            LOG.warn( "HTTP StatusCode {0}, UserId {1), OrgId {2}, Message {3}",
                    rd.getResponseStatusCode(), user.getUserId(), orgId, response.getMessage());
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

        RestResponseData<GrafanaUser[]> rd = driver.executeGetRequest("/api/users" + queryParameters,
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
            if (StringUtils.isNumeric(id.trim()))
            {
                // Get user By Numeric ID
                RestResponseData<GrafanaUser> rd = driver.executeGetRequest("/api/users/" + id.trim(),
                        GrafanaUser.class,
                        driver.getAdminHeaders());
                user = rd.getResponseObject();
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
        // If we found the user we now need to discover what Org they reside in and the role they occupy
        if ( user != null )
        {
            ArrayList<GrafanaUserOrg> orgs = getUserOrganizations(driver, user.getUserId());
            if ( orgs != null && orgs.size() > 0 )
            {
                for ( GrafanaUserOrg userOrg: orgs )
                {
                    user.setRole(userOrg.getRole());
                    user.setOrgId(userOrg.getOrgId());
                    if ( userOrg.getOrgId() != 1 )
                    {
                        // We are assuming that the orgId 1 is a default
                        break;
                    }
                }
            }
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
            // Get user By Numeric ID
            RestResponseData<GrafanaUser> rd = driver.executeGetRequest(
                    "/api/users/lookup?loginOrEmail=" + name.trim(),
                    GrafanaUser.class,
                    driver.getAdminHeaders());
            user = rd.getResponseObject();
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
        rd = driver.executeGetRequest("/api/orgs/"+orgId+"/users", users.getClass(), driver.getAdminHeaders());
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
        rd = driver.executeGetRequest("/api/users/"+userId+"/orgs", GrafanaUserOrg[].class, driver.getAdminHeaders());
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
    public GrafanaUserOrg isUserInOrg(ArrayList<GrafanaUserOrg> userOrgs, int orgId)
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
            // Grafana User allows one or more of theses 3 items to be updated
            if ( user.getName() != null
                    || user.getEmail() != null
                    || user.getLogin() != null )
            {

                responseData = driver.executePutRequest(
                        "/api/users/" + id,
                        GrafanaStandardResponse.class,
                        user,
                        driver.getAdminHeaders());
                response = responseData.getResponseObject();
            }
            else
            {
                LOG.warn("Bypassed User Update since neither name, login, or email was specified");
            }

            // Organization Functions use integer Ids
            userId = Integer.parseInt(id);
            // A delta update may not specify the userId inside the model object
            if ( user.getOrgId() > 0  )
            {
                user.setUserId(userId);
                user.setId(userId);
                // Determine whether the user is already in the organization
                ArrayList<GrafanaUserOrg> userOrgs = getUserOrganizations(driver, userId);
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
