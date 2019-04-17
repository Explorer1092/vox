/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.mizar.controller;


import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.service.mizar.api.constants.MizarGoodsStatus;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarGoodsItem;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShopGoods;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yuechen.wang on 2016/10/13.
 */
@Slf4j
public class AbstractMizarGoodsController extends AbstractMizarController {

    protected MizarShopGoods getRequestMizarGoods() {
        String goodsName = getRequestString("goodsName"); // 课程名称
        String title = getRequestString("title"); // 课程标题
        String desc = getRequestString("desc"); // 课程简介
        String goodsHours = getRequestString("goodsHours"); // 课时
        String duration = getRequestString("duration"); // 时长
        String goodsTime = getRequestString("goodsTime"); // 上课时间
        String target = getRequestString("target"); // 年龄段
        String category = getRequestString("category"); // 课程分类
        String audition = getRequestString("audition"); // 试听
        Double price = getRequestDouble("price"); // 课程现价
        Double originalPrice = getRequestDouble("originalPrice"); // 课程原价
        String appointGift = getRequestString("appointGift"); // 预约礼
        String welcomeGift = getRequestString("welcomeGift"); // 到店礼
        List<String> tags = requestStringList("tags"); // 课程标签
        List<String> detailImg = requestStringList("detailImg"); // 课程详情图片
        List<String> bannerImg = requestStringList("bannerImg"); // 头图
        String topImg = requestString("topImg"); // 顶图
        Integer totalLimit = requestInteger("totalLimit");
        Integer dayLimit = requestInteger("dayLimit");
        String smsMessage = getRequestString("smsMessage");
        String buttonColor = getRequestString("buttonColor");
        String buttonText = getRequestString("buttonText");
        String buttonTextColor = getRequestString("buttonTextColor");
        String successText = getRequestString("successText");
        String productId = getRequestString("productId");
        String offlineText = getRequestString("offlineText");
        String inputBGColor = getRequestString("inputBGColor");
        String clazzLevel = getRequestString("clazzLevel");
        String schoolAreas = getRequestString("schoolAreas");
        Integer requireAddress = requestInteger("requireAddress");
        Integer dealSuccess = requestInteger("dealSuccess");
        Integer requireSchool = requestInteger("requireSchool"); // 1是 2否
        Integer requireStudentName = requestInteger("requireStudentName"); // 1是 2否
        Integer requireRegion = requestInteger("requireRegion"); // 1是 2否
        MizarShopGoods goods = new MizarShopGoods();
        goods.setGoodsName(goodsName);
        goods.setTitle(title);
        goods.setDesc(desc);
        goods.setGoodsHours(goodsHours);
        goods.setDuration(duration);
        goods.setGoodsTime(goodsTime);
        goods.setTarget(target);
        goods.setCategory(category);
        goods.setAudition(audition);
        goods.setPrice(convertDouble(price));
        goods.setOriginalPrice(convertDouble(originalPrice));
        goods.setAppointGift(appointGift);
        goods.setWelcomeGift(welcomeGift);
        goods.setTags(tags);
        goods.setStatus(MizarGoodsStatus.OFFLINE);
        goods.setDetail(detailImg);
        goods.setTopImage(topImg);
        goods.setBannerPhoto(bannerImg);
        goods.setDayLimit(dayLimit);
        goods.setTotalLimit(totalLimit);
        goods.setSmsMessage(smsMessage);
        goods.setRequireAddress(requireAddress);
        goods.setSuccessText(successText);
        goods.setButtonColor(buttonColor);
        goods.setButtonText(buttonText);
        goods.setButtonTextColor(buttonTextColor);
        goods.setProductId(productId);
        goods.setOfflineText(offlineText);
        goods.setDealSuccess(dealSuccess);
        goods.setInputBGColor(inputBGColor);
        goods.setRequireSchool(requireSchool);
        goods.setClazzLevel(clazzLevel);
        goods.setSchoolAreas(schoolAreas);
        goods.setRequireStudentName(requireStudentName);
        goods.setRequireRegion(requireRegion);
        return goods;
    }

    protected MizarShopGoods getRequestFamilyActivity() {
        String shopId = getRequestString("sid");
        String goodsName = getRequestString("goodsName").replaceAll("\\s", ""); // 活动名称
        String desc = getRequestString("desc").replaceAll("\\s", ""); // 活动简介
        List<String> tags = requestStringList("tags"); // 活动标签
        String reportDesc = getRequestString("reportDesc").replaceAll("\\s", ""); // 体验报告
        List<String> bannerImg = requestStringList("bannerImg"); // 封面图
        String title = getRequestString("title").replaceAll("\\s", ""); // 活动标题
        Double price = getRequestDouble("price"); // 活动现价
        String contact = getRequestString("contact"); //联系方式
        String category = getRequestString("category"); // 活动分类
        String appointGift = getRequestString("appointGift"); // 支付提示
        String redirectUrl = getRequestString("redirectUrl"); // 跳转链接
        List<String> detailImg = requestStringList("detailImg"); // 活动详情图片
        String address = getRequestString("address"); // 位置
        Double longitude = getRequestDouble("longitude");
        Double latitude = getRequestDouble("latitude");
        String productType = getRequestString("productType"); // 产品类型
        String activityDesc = getRequestString("activityDesc"); // 活动说明
        String expenseDesc = getRequestString("expenseDesc"); // 活动说明
        String goodsType = getRequestString("goodsType");
        String successUrl = getRequestString("successUrl");   // 报名成功后的跳转链接
        MizarShopGoods activity = new MizarShopGoods();
        activity.setShopId(shopId);
        activity.setGoodsName(goodsName);
        activity.setDesc(desc);
        activity.setTags(tags);
        activity.setReportDesc(reportDesc);
        activity.setBannerPhoto(bannerImg);
        activity.setTitle(title);
        activity.setPrice(convertDouble(price));
        activity.setContact(contact);
        activity.setCategory(category);
        activity.setAppointGift(appointGift);
        activity.setRedirectUrl(redirectUrl);
        activity.setDetail(detailImg);
        activity.setAddress(address);
        activity.setLongitude(longitude);
        activity.setLatitude(latitude);
        activity.setActivityDesc(activityDesc);
        activity.setExpenseDesc(expenseDesc);
        activity.setSuccessUrl(successUrl);
        List<MizarGoodsItem> items = JSON.parseArray(productType, MizarGoodsItem.class);
        if (CollectionUtils.isNotEmpty(items)) {
            for (int i = 0; i < items.size(); ++i) {
                MizarGoodsItem item = items.get(i);
                // 校正处理Item中的一些内容
                item.setPrice(convertDouble(item.getPrice()));
                if (StringUtils.isBlank(item.getItemId())) item.setItemId(RandomUtils.randomString(6) + i);
            }
        }
        activity.setItems(items);
        activity.setStatus(MizarGoodsStatus.OFFLINE);
        activity.setGoodsType(goodsType);
        // 默认是亲子活动吧
        if (!activity.isFamilyActivity() && !activity.isUSTalkActivity()) {
            activity.setGoodsType(MizarShopGoods.familyActivityType());
        }
        return activity;
    }

    protected MapMessage validateActivity(MizarShopGoods activity) {
        // 先校验权限相关
        if (getCurrentUser().isShopOwner()) {
            if (StringUtils.isBlank(activity.getShopId())) {
                return MapMessage.errorMessage("请选择活动机构");
            }
            if (!currentUserShop().contains(activity.getShopId())) {
                return MapMessage.errorMessage("没有编辑该机构下活动的权限");
            }
        } else if (StringUtils.isNotBlank(activity.getShopId())) {
            MizarShop shop = mizarLoaderClient.loadShopById(activity.getShopId());
            if (shop == null) {
                return MapMessage.errorMessage("无效的机构ID: " + activity.getShopId());
            }
        }
        // 再校验内容相关
        StringBuilder validInfo = new StringBuilder();
        // 校验必填项
//        if (StringUtils.isBlank(activity.getGoodsName())) {
//            validInfo.append("【活动名称】不能为空!").append("<br />");
//        }
        if (StringUtils.isBlank(activity.getTitle())) {
            validInfo.append("【活动标题】不能为空!").append("<br />");
        } else if (activity.getTitle().length() > 20) {
            validInfo.append("【活动标题】不要超过20字!").append("<br />");
        }
//        if (StringUtils.isBlank(activity.getDesc())) {
//            validInfo.append("【活动简介】不能为空!").append("<br />");
//        }
        if (StringUtils.isNotBlank(activity.getDesc())) {
            List<String> desc = Arrays.asList(activity.getDesc().split(","));
            if (desc.size() > 3 || desc.stream().anyMatch(t -> t.length() > 10)) {
                validInfo.append("【活动简介】不能超过3段(英文逗号分隔)，每段不能超过10个字!").append("<br />");
            }
        }
        if (CollectionUtils.isNotEmpty(activity.getTags())) {
            List<String> tags = Arrays.asList(activity.getDesc().split(","));
            if (tags.size() > 3 || tags.stream().anyMatch(t -> t.length() > 60)) {
                validInfo.append("【标签】最多3个(英文逗号分隔)，每个不能超过6个字!").append("<br />");
            }
        }
        if (activity.getPrice() == 0D) {
            validInfo.append("【活动价格】不能为0!").append("<br />");
        }
        if (StringUtils.isBlank(activity.getCategory())) {
            validInfo.append("【类型】不能为0!").append("<br />");
        }
        if (CollectionUtils.isEmpty(activity.getDetail())) {
            validInfo.append("【封面图】不能为空!").append("<br />");
        }
        if (CollectionUtils.isEmpty(activity.getDetail())) {
            validInfo.append("【活动详情】不能为空!").append("<br />");
        }
//        if (StringUtils.isEmpty(activity.getContact())) {
//            validInfo.append("【联系方式】不能为空!").append("<br />");
//        }
        if (CollectionUtils.isNotEmpty(activity.getItems())) {
            for (int i = 0; i < activity.getItems().size(); ++i) {
                MizarGoodsItem item = activity.getItems().get(i);
                String line = "";
                if (StringUtils.isBlank(item.getCategoryName())) line += ",【产品类型】不能为空";
                if (StringUtils.isBlank(item.getItemName())) line += ",【出行日期】不能为空";
                if (item.getPrice() <= 0D) line += ",【价格】不能小于0";
                if (item.getInventory() < 0) line += ",【库存】不能小于0";
                if (StringUtils.isNotBlank(line)) {
                    validInfo.append("第").append(i + 1).append("行").append(line).append("<br />");
                }
            }
        }
        if (StringUtils.isBlank(activity.getActivityDesc())) {
            validInfo.append("【活动介绍】不能为空!").append("<br />");
        }
        if (StringUtils.isBlank(activity.getExpenseDesc())) {
            validInfo.append("【费用说明】不能为空!").append("<br />");
        }
        // 活动位置选填
        if (StringUtils.isNotBlank(activity.getAddress()) && (activity.getLatitude() == 0 || activity.getLongitude() == 0)) {
            validInfo.append("请于地图上选择活动位置!").append("<br />");
        }
        if (StringUtils.isBlank(validInfo.toString())) {
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage(validInfo.toString());
    }

    private double convertDouble(double initVal) {
        BigDecimal decimal = new BigDecimal(initVal);
        return decimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}
