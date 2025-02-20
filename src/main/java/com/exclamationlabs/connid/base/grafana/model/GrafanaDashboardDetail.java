package com.exclamationlabs.connid.base.grafana.model;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GrafanaDashboardDetail
{

    private String annotations;
    private Boolean editable;
    private Integer fiscalYearStartMonth;
    private Integer graphTooltip;
    private String id;
    private String links;
    private String panels;
    private String refresh;
    private String schemaVersion;
    private String style;
    private String tags;
    private String templating;
    private String time;
    private String timepicker;
    private String timezone;
    private String title;
    private String uid;
    private String version;

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        GrafanaDashboardDetail that = (GrafanaDashboardDetail) o;
        return Objects.equals(id, that.id) && Objects.equals(title, that.title) && Objects.equals(uid, that.uid) && Objects.equals(version, that.version);
    }

    public String getAnnotations() {
        return annotations;
    }

    public Boolean getEditable() {
        return editable;
    }

    public Integer getFiscalYearStartMonth()
    {
        return fiscalYearStartMonth;
    }

    public Integer getGraphTooltip()
    {
        return graphTooltip;
    }

    public String getId() {
        return id;
    }

    public String getLinks() {
        return links;
    }

    public String getPanels() {
        return panels;
    }
    // Getters and Setters

    public String getRefresh() {
        return refresh;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public String getStyle()
    {
        return style;
    }

    public String getTags() {
        return tags;
    }

    public String getTemplating() {
        return templating;
    }

    public String getTime() {
        return time;
    }

    public String getTimepicker() {
        return timepicker;
    }

    public String getTimezone() {
        return timezone;
    }

    public String getTitle() {
        return title;
    }

    public String getUid() {
        return uid;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, title, uid, version);
    }

    public void setAnnotations(String annotations) {
        this.annotations = annotations;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    public void setFiscalYearStartMonth(Integer fiscalYearStartMonth)
    {
        this.fiscalYearStartMonth = fiscalYearStartMonth;
    }

    public void setGraphTooltip(Integer graphTooltip)
    {
        this.graphTooltip = graphTooltip;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLinks(String links) {
        this.links = links;
    }

    public void setPanels(String panels) {
        this.panels = panels;
    }

    public void setRefresh(String refresh) {
        this.refresh = refresh;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public void setStyle(String style)
    {
        this.style = style;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setTemplating(String templating) {
        this.templating = templating;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setTimepicker(String timepicker) {
        this.timepicker = timepicker;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}