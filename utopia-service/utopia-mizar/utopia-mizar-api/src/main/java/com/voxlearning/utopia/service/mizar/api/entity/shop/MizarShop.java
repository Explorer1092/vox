package com.voxlearning.utopia.service.mizar.api.entity.shop;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Summer Yang on 2016/8/15.
 * 机构导流——机构基础信息
 * modify xiang.lv      2016/9/13.
 * 添加matchGrade,cooperationLevel,adjustScore,online
 */
@Setter
@Getter
@DocumentDatabase(database = "vox_o2o")
@DocumentCollection(collection = "vox_shop")
@DocumentIndexes({
        @DocumentIndex(def = "{'full_name':1}", background = true),
        @DocumentIndex(def = "{'region_code':1}", background = true),
        @DocumentIndex(def = "{'trade_area':1}", background = true),
        @DocumentIndex(def = "{'first_category':1}", background = true),
        @DocumentIndex(def = "{'type':1}", background = true),
        @DocumentIndex(def = "{'brand_id':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20161029")
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
public class MizarShop implements Serializable {
    public static final String FACULTY_NAME = "name";  // 名字
    public static final String FACULTY_PHOTO = "photo"; // 头像
    public static final String FACULTY_COURSE = "course";   // 科目
    public static final String FACULTY_EXPERIENCE = "experience"; // 教龄
    public static final String FACULTY_DESCRIPTION = "description"; // 描述

    private static final long serialVersionUID = -6014171582624060784L;
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.OBJECT_ID)
    private String id;
    @DocumentField("full_name") private String fullName;                     // 机构全称
    @DocumentField("short_name") private String shortName;                   // 机构简称
    @DocumentField("introduction") private String introduction;              // 机构介绍
    @DocumentField("shop_type") private String shopType;                     // 机构类型
    @DocumentField("region_code") private Integer regionCode;                // 所属地区
    @DocumentField("trade_area") private String tradeArea;                   // 所属商圈
    @DocumentField("address") private String address;                        // 详细地址
    @DocumentField("gps_longitude") private Double longitude;                // GPS经度
    @DocumentField("gps_latitude") private Double latitude;                  // GPS纬度
    @DocumentField("baidu_gps") private Boolean baiduGps;
    @DocumentField("contact_phone") private List<String> contactPhone;       // 联系电话
    @DocumentField("photo") private List<String> photo;                      // 机构图片
    @DocumentField("vip") private Boolean vip;                               // 是否付费商家
    @DocumentField("first_category") private List<String> firstCategory;     // 一级分类
    @DocumentField("second_category") private List<String> secondCategory;   // 二级分类
    @DocumentField("brand_id") private String brandId;                       // 所属品牌ID
    @DocumentField("rating_count") private Integer ratingCount;              // 评论条数  延迟一天 任务刷新
    @DocumentField("rating_star") private Integer ratingStar;                // 评论星级  延迟一天 任务刷新
    @DocumentField("welcome_gift") private String welcomeGift;               // 到店礼
    @DocumentField("like_count") private Long likeCount;                     // 点赞数量 延迟一天 任务刷新

    @DocumentCreateTimestamp private Date createAt;
    @DocumentUpdateTimestamp private Date updateAt;
    @DocumentField("match_grade") private String matchGrade;                 // 适配年级,多个以逗号分隔
    @DocumentField("cooperator") private Boolean cooperator;                 // 是否合作机构
    @DocumentField("cooperation_level") private Integer cooperationLevel;    // 合作等级分数
    @DocumentField("adjust_score") private Integer adjustScore;              // 人工调整分数  (合作等级分数,人工调整分数这两个字段是给大数据用于计算机构得分的)

    @DocumentField("type") private Integer type;                             // 是否线上机构(1-是,0-否,2-亲子,)

    @DocumentField("shop_status") private String shopStatus;                 // 机构审核状态 MizarShopStatusType

    @DocumentField("faculty") private List<Map<String, Object>> faculty;     // 师资力量 name,photo,后加的,品牌中也有


    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(MizarShop.class, id);
    }

    public static String ck_region(Integer region_code) {
        return CacheKeyGenerator.generateCacheKey(MizarShop.class, "RC", region_code);
    }

    public static String ck_brand(String brandId) {
        return CacheKeyGenerator.generateCacheKey(MizarShop.class, "BID", brandId);
    }

    public static String ck_type(Integer type) {
        return CacheKeyGenerator.generateCacheKey(MizarShop.class, "T", type);
    }

    public boolean matchCooperator(Boolean coop) {
        return (coop == null || (coop && cooperator != null && cooperator) || (!coop && (cooperator == null || !cooperator)));
    }

    public boolean matchVip(Boolean avip) {
        return (avip == null || (avip && vip != null && vip) || (!avip && (vip == null || !vip)));
    }

    @JsonIgnore
    public boolean isBaiduGps() {
        return Boolean.TRUE.equals(baiduGps);
    }

    @JsonIgnore
    public boolean isVip() {
        return Boolean.TRUE.equals(vip);
    }


    public Map<String, Object> simpleInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("shopId", id);
        info.put("shopName", fullName);
        return info;
    }

}
