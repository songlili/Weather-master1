package cn.itpeter.weather.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by annpeter on 11/14/15.
 */
public class China {

    List<Province> province = new ArrayList<Province>();

    public List<Province> getProvinceList() {
        return province;
    }

    public void setProvinceList(List<Province> provinceList) {
        this.province = provinceList;
    }

}

