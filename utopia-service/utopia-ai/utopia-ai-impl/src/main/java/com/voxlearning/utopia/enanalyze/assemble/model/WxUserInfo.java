package com.voxlearning.utopia.enanalyze.assemble.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户信息
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Data
public class WxUserInfo implements Serializable {

    private String nickName;
    private String gender;
    private String language;
    private String city;
    private String province;
    private String country;
    private String avatarUrl;
    private String unionId;
    private WaterMark watermark;

    @Data
    public static class WaterMark implements Serializable {
        private String appid;
        private Date timestamp;
    }
}
