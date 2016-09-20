package cn.itpeter.weather.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by annpeter on 11/15/15.
 */
public class WeatherInfo {

    public class Future{
        int success;
        List<Result> result;

        @Override
        public String toString() {
            return "success:" + success +", result"+ result.toString();
        }

        public List<Result> getResult() {
            return result;
        }

        public void setResult(List<Result> result) {
            this.result = result;
        }

        public int getSuccess() {
            return success;
        }

        public void setSuccess(int success) {
            this.success = success;
        }
    }

    public class Today{
        int success;
        Result result;

        @Override
        public String toString() {
            return "success:" + success +", result"+ result.toString();
        }

        public Result getResult() {
            return result;
        }

        public void setResult(Result result) {
            this.result = result;
        }

        public int getSuccess() {
            return success;
        }

        public void setSuccess(int success) {
            this.success = success;
        }


    }

    public class Result{
        String days;
        String week;
        String citynm;
        String temperature;
        String cityid;
        String humidity;
        String weather;
        String weather_icon;
        String weather_icon1;
        String wind;
        String winp;
        String temp_high;
        String temp_low;
        String humi_high;
        String humi_low;
        String weatid;
        String weatid1;
        String windid;
        String winpid;

        @Override
        public String toString() {
            return "city:"+citynm +",days:"+days+", week:"+week+",weather:"+weather;
        }

        public String getCityid() {
            return cityid;
        }

        public void setCityid(String cityid) {
            this.cityid = cityid;
        }

        public String getCitynm() {
            return citynm;
        }

        public void setCitynm(String citynm) {
            this.citynm = citynm;
        }

        public String getDays() {
            return days;
        }

        public void setDays(String days) {
            this.days = days;
        }

        public String getHumi_high() {
            return humi_high;
        }

        public void setHumi_high(String humi_high) {
            this.humi_high = humi_high;
        }

        public String getHumi_low() {
            return humi_low;
        }

        public void setHumi_low(String humi_low) {
            this.humi_low = humi_low;
        }

        public String getHumidity() {
            return humidity;
        }

        public void setHumidity(String humidity) {
            this.humidity = humidity;
        }

        public String getTemp_high() {
            return temp_high;
        }

        public void setTemp_high(String temp_high) {
            this.temp_high = temp_high;
        }

        public String getTemp_low() {
            return temp_low;
        }

        public void setTemp_low(String temp_low) {
            this.temp_low = temp_low;
        }

        public String getTemperature() {
            return temperature;
        }

        public void setTemperature(String temperature) {
            this.temperature = temperature;
        }

        public String getWeather() {
            return weather;
        }

        public void setWeather(String weather) {
            this.weather = weather;
        }

        public String getWeather_icon1() {
            return weather_icon1;
        }

        public void setWeather_icon1(String weather_icon1) {
            this.weather_icon1 = weather_icon1;
        }

        public String getWeather_icon() {
            return weather_icon;
        }

        public void setWeather_icon(String weather_icon) {
            this.weather_icon = weather_icon;
        }

        public String getWeatid1() {
            return weatid1;
        }

        public void setWeatid1(String weatid1) {
            this.weatid1 = weatid1;
        }

        public String getWeatid() {
            return weatid;
        }

        public void setWeatid(String weatid) {
            this.weatid = weatid;
        }

        public String getWeek() {
            return week;
        }

        public void setWeek(String week) {
            this.week = week;
        }

        public String getWind() {
            return wind;
        }

        public void setWind(String wind) {
            this.wind = wind;
        }

        public String getWindid() {
            return windid;
        }

        public void setWindid(String windid) {
            this.windid = windid;
        }

        public String getWinp() {
            return winp;
        }

        public void setWinp(String winp) {
            this.winp = winp;
        }

        public String getWinpid() {
            return winpid;
        }

        public void setWinpid(String winpid) {
            this.winpid = winpid;
        }
    }
}
