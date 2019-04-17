package com.voxlearning.utopia.admin.data;

import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.utopia.service.reward.constant.RewardCouponResource;
import com.voxlearning.utopia.service.reward.entity.RewardCoupon;
import com.voxlearning.utopia.service.reward.entity.RewardProduct;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by XiaoPeng.Yang on 14-7-14.
 */
@Data
public class RewardProductMapper implements Serializable{

    private static final long serialVersionUID = 631646983846110212L;

    public Long productId;
    public String productName;
    public Double priceT;
    public Double priceOldT;
    public Double priceS;
    public Double priceOldS;
    public Integer teacherLevel;
    public Integer ambassadorLevel;
    public String tags;
    public String saleGroup;
    public Boolean teacherVisible;
    public Boolean studentVisible;
    public Boolean primarySchoolVisible;
    public Boolean juniorSchoolVisible;
    public Boolean osVisible;
    public Boolean nsVisible;
    public String categoryIds;
    public String tagIds;
    public String productType;
    public String description;
    public Boolean onlined;
    public String usedUrl;
    public Boolean rebated;
    public Double buyingPrice;
    public Integer studentOrderValue;
    public Integer teacherOrderValue;
    public Integer expiryDate;
    public String relateVirtualItemId;
    public String displayTerminal;
    public String relateVirtualItemContent;
    public String remarks;
    public Integer minBuyNums;
    public boolean repeatExchanged;
    public String categoryCode;
    public Boolean needSendSms;
    public Boolean needSendMsg;
    public String smsContent;
    public String msgContent;
    public String gradeVisible;
    public String couponResource;
    public List<Map<String, Object>> skus;

    public static RewardProduct convert(RewardProductMapper mapper) {
        RewardProduct product = new RewardProduct();

        product.setId(mapper.getProductId());
        product.setProductType(mapper.getProductType());
        product.setTags(mapper.getTags());
        product.setSaleGroup(mapper.getSaleGroup());
        product.setOnlined(mapper.getOnlined());
        product.setProductName(mapper.getProductName());
        product.setStudentVisible(mapper.getStudentVisible());
        product.setTeacherVisible(mapper.getTeacherVisible());
        product.setPrimarySchoolVisible(mapper.getPrimarySchoolVisible());
        product.setJuniorSchoolVisible(mapper.getJuniorSchoolVisible());
        product.setDescription(mapper.getDescription());
        if (mapper.getOnlined()) {
            product.setOnlineDatetime(new Date());
        }
        product.setPriceS(mapper.getPriceS());
        product.setPriceT(mapper.getPriceT());
        product.setPriceOldS(mapper.getPriceOldS());
        product.setPriceOldT(mapper.getPriceOldT());
        product.setTeacherLevel(mapper.getTeacherLevel());
        product.setAmbassadorLevel(mapper.getAmbassadorLevel());
        product.setOsVisible(true);// // FIXME: 2016/3/2  这次奖品中心改版去掉了新老学校的概念 此处默认全部设置为true
        product.setNsVisible(true);
        product.setRebated(mapper.getRebated());
        product.setUsedUrl(mapper.getUsedUrl());
        product.setBuyingPrice(mapper.getBuyingPrice());
        product.setStudentOrderValue(mapper.getStudentOrderValue());
        product.setTeacherOrderValue(mapper.getTeacherOrderValue());
        // 不能为负
        product.setExpiryDate(Math.max(mapper.getExpiryDate(),0));
        product.setRelateVirtualItemId(mapper.getRelateVirtualItemId());
        product.setDisplayTerminal(mapper.getDisplayTerminal());
        product.setRelateVirtualItemContent(mapper.getRelateVirtualItemContent());
        product.setRemarks(mapper.getRemarks());
        product.setMinBuyNums(mapper.getMinBuyNums());
        product.setGradeVisible(mapper.getGradeVisible());
        product.setRepeatExchanged(mapper.repeatExchanged);
        product.setCouponResource(RewardCouponResource.parse(mapper.getCouponResource()));
        return product;
    }

    /**
     * 从mapper中提取优惠券的信息
     * @param mapper
     * @return
     */
    public static RewardCoupon extractCoupon(RewardProductMapper mapper){
        RewardCoupon coupon = new RewardCoupon();
        coupon.setProductId(mapper.getProductId());
        coupon.setName(mapper.getProductName());
        coupon.setSendMsg(mapper.getNeedSendMsg());
        coupon.setSendSms(mapper.getNeedSendSms());
        coupon.setSmsTpl(mapper.getSmsContent());
        coupon.setMsgTpl(mapper.getMsgContent());

        return coupon;
    }
}
