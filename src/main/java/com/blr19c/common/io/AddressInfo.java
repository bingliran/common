package com.blr19c.common.io;

import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Subdivision;

/**
 * 地址信息
 *
 * @author blr
 */
public class AddressInfo {
    /**
     * getDefaultXXXName 获取中文版的地区名称
     */
    public static final String ZH_CN_ASN = "zh-CN";
    /**
     * 国家
     */
    private final Country country;
    /**
     * 省
     */
    private final Subdivision subdivision;
    /**
     * 市
     */
    private final City city;

    public AddressInfo(CityResponse response) {
        this.country = response.getCountry();
        this.subdivision = response.getMostSpecificSubdivision();
        this.city = response.getCity();
    }

    public Country getCountry() {
        return country;
    }

    public Subdivision getSubdivision() {
        return subdivision;
    }

    public City getCity() {
        return city;
    }

    public String getDefaultCountryName() {
        return country.getNames().get(ZH_CN_ASN);
    }

    public String getDefaultSubdivisionName() {
        return subdivision.getNames().get(ZH_CN_ASN);
    }

    public String getDefaultCityName() {
        return city.getNames().get(ZH_CN_ASN);
    }
}