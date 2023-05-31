package com.exclamationlabs.connid.base.grafana.model.response;

/**
 * Class to hold the Common JSON response for a grafana dashboard request
 */
public class GrafanaOrgPreferencesResponse
{
    private String homeDashboardId;
    private String homeDashboardUID;
    private String locale;
    private String theme;
    private String timezone;
    private String weekStart;

    public String getHomeDashboardId()
    {
        return homeDashboardId;
    }

    public String getHomeDashboardUID()
    {
        return homeDashboardUID;
    }

    public String getLocale()
    {
        return locale;
    }

    public String getTheme()
    {
        return theme;
    }

    public String getTimezone()
    {
        return timezone;
    }

    public String getWeekStart()
    {
        return weekStart;
    }

    public void setHomeDashboardId(String homeDashboardId)
    {
        this.homeDashboardId = homeDashboardId;
    }

    public void setHomeDashboardUID(String homeDashboardUID)
    {
        this.homeDashboardUID = homeDashboardUID;
    }

    public void setLocale(String locale)
    {
        this.locale = locale;
    }

    public void setTheme(String theme)
    {
        this.theme = theme;
    }

    public void setTimezone(String timezone)
    {
        this.timezone = timezone;
    }

    public void setWeekStart(String weekStart)
    {
        this.weekStart = weekStart;
    }
}
