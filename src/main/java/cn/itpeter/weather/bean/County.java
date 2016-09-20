package cn.itpeter.weather.bean;

/**
 * Created by annpeter on 11/14/15.
 */
public class County{
    String id;
    String name;
    String weatherCode;

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

    public String getWeatherCode() {
        return weatherCode;
    }

    public void setWeatherCode(String weatherCode) {
        this.weatherCode = weatherCode;
    }
}