package com.exclamationlabs.connid.base.grafana.model;

/**
 * Class to hold the Common JSON response for a grafana dashboard request
 */
public class GrafanaOrgPreferences
{
    private String homeDashboardUID;
    private String startWeek;
    private String theme;
    private String timezone;

    public String getHomeDashboardUID()
    {
        return homeDashboardUID;
    }

    public String getStartWeek()
    {
        return startWeek;
    }

    public String getTheme()
    {
        return theme;
    }

    public String getTimezone()
    {
        return timezone;
    }

    public void setHomeDashboardUID(String homeDashboardUID)
    {
        this.homeDashboardUID = homeDashboardUID;
    }

    public void setStartWeek(String startWeek)
    {
        this.startWeek = startWeek;
    }

    public void setTheme(String theme)
    {
        this.theme = theme;
    }

    public void setTimezone(String timezone)
    {
        this.timezone = timezone;
    }
}
