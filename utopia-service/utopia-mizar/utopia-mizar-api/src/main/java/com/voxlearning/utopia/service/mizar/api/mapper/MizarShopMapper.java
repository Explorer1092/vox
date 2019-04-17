package com.voxlearning.utopia.service.mizar.api.mapper;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Summer Yang on 2016/8/29.
 */
@Data
public class MizarShopMapper implements Serializable {

    private static final long serialVersionUID = -6974697652802080975L;
    private String id;
    private String name;
    private String photo;
    private Integer ratingCount;
    private Integer ratingStar;
    private String brandDesc;
    private Double distance;  // 距离  km
    private String tradeArea;
    private List<String> secondCategory;
    private String welcomeGift; // 到店礼
    private boolean vip;
    private String address;
    private Double longitude;
    private Double latitude;

    private Integer orderScore; //智能排序得分值,越高越靠前
    private String firstCategory;
    private boolean sameSchoolFlag; // 有同学去过
}
