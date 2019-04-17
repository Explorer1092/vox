package com.voxlearning.utopia.service.mizar.api.mapper;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by Summer Yang on 2016/9/13.
 * 机构详情页 展示实体
 */
@Data
public class MizarShopDetail implements Serializable {

    private static final long serialVersionUID = -4686478949084346654L;
    private String shopId;
    private String name;
    private String photo;
    private String address;
    private String phone;
    private Double latitude;
    private Double longitude;
    private String brandId;
    private boolean isVip;
    private Integer ratingCount;
    private Integer ratingStar;
    private List<Map<String, Object>> ratingMapList;
    private Integer picCount;
    private String firstPic;
    private String brandDesc;
    private List<Map<String, Object>> faculty;      // 师资力量 name,photo
    private List<String> certificationPhotos;       // 获奖证书 photos
    private String certificationName;               // 获奖证书 描述
    private String welcomeGift;                     // 到店礼
    private List<Map<String, Object>> goodsList;    // 课程列表
    private Integer sameSchoolReserveCount;         // 有多少同学来过
    private String area;                            // 商圈
    private List<String> secondCategory;            // 二级分类
}
