package com.voxlearning.utopia.admin.data;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author xuerui.zhang
 * @since 2018/9/17 下午8:51
 */
@Data
public class CourseStructSkuMapper implements Serializable {
    private static final long serialVersionUID = 864710581164952068L;

    private Long skuId;
    private Long spuId;
    private Integer phase;
    private String openDate;
    private String closeDate;
    private String price;
    private String discountPrice;
    private Integer personLimited;
    private String productId;
    private Integer cardDisplay;
    private String ebookId;
    private Integer ebookGetWay;
    private Integer joinWay;
    private String showDate;
    private String sighUpEndDate;
    private Integer activeType;
    private String activePagePic;//非必须
    private Integer qrcodeType;
    private Integer envLevel;
    private String remark; //非必须
    private String createUser;
    private List<Map<String, Object>> rankRewards;
}
