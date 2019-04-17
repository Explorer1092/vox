package com.voxlearning.utopia.service.reward.mapper;

import com.voxlearning.utopia.service.reward.constant.RewardCouponResource;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Summer Yang on 2016/7/28.
 */
@Data
public class RewardOrderMapper implements Serializable {
    private static final long serialVersionUID = -1132001435826103861L;

    private Long id;
    private String productName;
    private Long productId;
    private Long skuId;
    private String skuName;
    private Integer quantity;
    private Double price;
    private Double totalPrice;
    private Double discount;
    private String unit;
    private Long buyerId;
    private String buyerName;
    private String status;
    private String saleGroup;
    private Long logisticsId;
    private String receiverName;
    private Long receiverId;
    private String companyName;         // 物流公司
    private String logisticNo;          // 物流单号
    private Date createDatetime;
    private String productImg;          // 订单关联的商品图片
    private String exchangeDateStr;     // 兑换时间，格式化后的
    private String endDateStr;          // 虚拟头饰的到期时间
    private String productType;         // 产品类型
    private boolean convertible;        // 是否可以兑换
    private int totalAttendNums;        // 总参加次数，公益活动数据需要的字段
    private String couponNo;            // 兑换优惠码
    private String source;              // 订单来源
    private Boolean returnable;         // 可退换
    private RewardCouponResource couponResource;
    private String couponUrl;
    private Integer leftValidTime;// 剩余有效天数
    private String productCategory;
    private Integer spendType; //花费类型，默认0是学豆，1是碎片
    private Integer oneLevelCategoryType;//一级分类类型
    private Boolean disabled;

}