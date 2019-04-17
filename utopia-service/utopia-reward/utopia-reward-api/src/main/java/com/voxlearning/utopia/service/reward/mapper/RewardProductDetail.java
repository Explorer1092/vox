package com.voxlearning.utopia.service.reward.mapper;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.action.api.support.PrivilegeOrigin;
import com.voxlearning.utopia.service.reward.constant.RewardCouponResource;
import com.voxlearning.utopia.service.reward.constant.RewardProductType;
import com.voxlearning.utopia.service.reward.entity.RewardImage;
import com.voxlearning.utopia.service.reward.entity.RewardSku;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

/**
 * Created by XiaoPeng.Yang on 14-7-15.
 */
@Data
public class RewardProductDetail implements Serializable {
    private static final long serialVersionUID = -3784757236490499754L;

    private Long id;
    private String productName;
    private String productType;
    private String description;
    private Double price;     // 原价
    private Double originPrice;// 奖品中心配置的原始价格
    private Double discountPrice;//对应折后价
    private Double discount;//对应折扣率
    private Double vipPrice; //VIP价格
    private String unit;  //学豆  金币
    private Integer soldQuantity; //已售数量
    private Integer wishQuantity; //愿望盒数量
    private String tags; //
    private String saleGroup; // vip
    private Date createDatetime; //
    private String image;
    private String usedUrl;
    private Boolean rebated; //
    private Boolean repeatExchanged;//是否重复兑换
    private List<RewardImage> images; //
    private List<RewardSku> skus; //
    private Map<String, Object> extenstionAttributes;
    private String ambassadorLevelName;
    private Integer teacherLevel;
    private Integer ambassadorLevel;
    private Integer sellAbleCount;//第一个sku还剩余的库存.
    private Integer studentOrderValue;
    private Integer teacherOrderValue;
    private Integer expiryDate;// 有效期
    private String relateVirtualItemId; // 奖品关联的虚拟物品id
    private String displayTerminal;// 展示端
    private Boolean online;
    private String relateVirtualItemContent; // 奖品关联的虚拟物品内容
    private Integer minBuyNums;
    private RewardCouponResource couponResource;
    private Boolean have; // 是否已拥有(为了超级变变变而添加)
    private List<PriceDay> priceDay; // 头饰价格列表 (为了超级变变变而添加)
    private PrivilegeOrigin origin; // 来源
    private String headWearImg; // 来源
    //private Boolean prohibit; // 是否禁止兑换 (为小卖部而添加 参加公益未满3次不允许兑换公益专区的物品)
    private Integer spendType; //花费类型，默认0是学豆，1是碎片
    private Long oneLevelCategoryId; //一级分类Id
    private Long twoLevelCategoryId; //二级分类
    private Boolean isNewProduct; //新品标志
    private Integer oneLevelCategoryType;

    // 判断是否零库存
    public boolean isZeroStock(){
        List<RewardSku> skuList = getSkus();
        if (CollectionUtils.isNotEmpty(skuList)) {
            int totalCount = 0;
            for (RewardSku sku : skuList) {
                totalCount = totalCount + sku.getInventorySellable();
            }
            if (totalCount <= 0) {
                return true;
            }
        }

        return false;
    }

    public void addSku(RewardSku sku){
        if(CollectionUtils.isEmpty(skus))
            skus = new ArrayList<>();

        skus.add(sku);
    }

    public boolean isExperiential(){
        return Objects.equals(this.productType, RewardProductType.JPZX_TIYAN.name());
    }

    /**
     * 是否包含公益标签
     */
    public boolean isPublicGood() {
        if (tags == null) {
            return false;
        }
        if (tags.indexOf("公益") >= 0) {
            return true;
        }
        return false;
    }

    @Getter
    @Setter
    public static class PriceDay implements java.io.Serializable {
        private static final long serialVersionUID = -3784757236490499754L;

        private Integer day;
        private Double price;
        private Integer quantity;
    }
}
