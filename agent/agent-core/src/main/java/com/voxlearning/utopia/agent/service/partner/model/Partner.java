package com.voxlearning.utopia.agent.service.partner.model;

import lombok.Data;

import java.util.List;

/**
 * @description: 合作机构业务模型
 * @author: kaibo.he
 * @create: 2019-04-02 11:42
 **/
@Data
public class Partner {
    private Long id;                    //id
    private String name;                // 名称
    private Integer provinceCode;       // 省份CODE
    private String provinceName;        // 省份名称
    private Integer cityCode;           // 城市CODE
    private String cityName;            // 城市名称
    private Integer countyCode;         // 地区CODE
    private String countyName;          // 地区名称
    private String longitude;           // 地理坐标：经度
    private String latitude;            // 地理坐标：纬度
    private String address;             //地址
    private String type;//机构类别
    private String homePhotoUrl;     // 门头照片
    private List<String> otherPhotoUrls; //其他图片
    private Long createUserId;          //创建人
    private String createUserName;      //创建人姓名
    private String createTime;
    private List<PartnerLinkMan> linkMans;  //联系人列表
}
