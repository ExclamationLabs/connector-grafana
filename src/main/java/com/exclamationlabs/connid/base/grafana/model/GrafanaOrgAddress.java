package com.exclamationlabs.connid.base.grafana.model;

public class GrafanaOrgAddress
{
    String address1;
    String address2;
    String city;
    String country;
    String state;
    String zipCode;


    public String getAddress1()
    {
        return address1;
    }

    public String getAddress2()
    {
        return address2;
    }

    public String getCity()
    {
        return city;
    }

    public String getCountry()
    {
        return country;
    }

    public String getState()
    {
        return state;
    }

    public String getZipCode()
    {
        return zipCode;
    }

    public void setAddress1(String address1)
    {
        this.address1 = address1;
    }

    public void setAddress2(String address2)
    {
        this.address2 = address2;
    }

    public void setCity(String city)
    {
        this.city = city;
    }

    public void setCountry(String country)
    {
        this.country = country;
    }

    public void setState(String state)
    {
        this.state = state;
    }

    public void setZipCode(String zipCode)
    {
        this.zipCode = zipCode;
    }

}
