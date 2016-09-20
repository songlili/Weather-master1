package cn.itpeter.weather.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by annpeter on 11/14/15.
 */
public class Province{
    String id;
    String name;
    List<City> city = new ArrayList<City>();

    public List<City> getCityList() {
        return city;
    }

    public void setCityList(List<City> cityList) {
        this.city = cityList;
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

    @Override
    public String toString() {
        return "id:" +id+",  name"+name;
    }
}