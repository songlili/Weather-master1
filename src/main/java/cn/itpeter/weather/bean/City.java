package cn.itpeter.weather.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by annpeter on 11/14/15.
 */
public class City{
    String id;
    String name;
    List<County> county = new ArrayList<County>();

    public List<County> getCountyList() {
        return county;
    }

    public void setCountyList(List<County> countyList) {
        this.county = countyList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}