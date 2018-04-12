package com.example.gentl.superapp.DataClasses;

/**
 * Created by Gentl on 3/21/2018.
 */

public class City
{
    private int idCity;
    private String titleCity;

    public City(){}

    public City(int idCity, String titleCity)
    {
        this.idCity = idCity;
        this.titleCity = titleCity;
    }

    public void setIdCity(int idCity)
    {
        this.idCity = idCity;
    }

    public void setTitleCity(String titleCity)
    {
        this.titleCity = titleCity;
    }

    public int getIdCity()
    {
        return idCity;
    }

    public String getTitleCity()
    {
        return titleCity;
    }
}
