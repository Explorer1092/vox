package com.voxlearning.utopia.service.mizar.api.entity.shop;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.mizar.api.constants.MizarGoodsStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Summer Yang on 2016/8/15.
 * 机构导流——机构课程表
 */
@Setter
@Getter
@DocumentDatabase(database = "vox_o2o")
@DocumentCollection(collection = "vox_shop_goods")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20161020")
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
public class MizarShopGoods implements Serializable, Cloneable {

    private static final long serialVersionUID = -5392813863222202180L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.OBJECT_ID)
    private String id;
    @DocumentField("shop_id") private String shopId;                   // 机构ID
    @DocumentField("goods_name") private String goodsName;             // 课程名称
    @DocumentField("goods_title") private String title;                // 课程标题
    @DocumentField("goods_desc") private String desc;                  // 课程简介
    @DocumentField("goods_hours") private String goodsHours;           // 课时
    @DocumentField("goods_duration") private String duration;          // 时长
    @DocumentField("goods_time") private String goodsTime;             // 上课时间
    @DocumentField("goods_target") private String target;              // 年龄段
    @DocumentField("goods_category") private String category;          // 课程分类
    @DocumentField("goods_audition") private String audition;          // 试听
    @DocumentField("goods_price") private Double price;                // 课程现价
    @DocumentField("original_price") private Double originalPrice;     // 课程原价
    @DocumentField("banner_photo") private List<String> bannerPhoto;   // banner图片
    @DocumentField("tags") private List<String> tags;                  // 课程标签
    @DocumentField("goods_detail") private List<String> detail;        // 课程详情图片
    @DocumentField("appoint_gift") private String appointGift;         // 预约礼
    @DocumentField("welcome_gift") private String welcomeGift;         // 到店礼
    @DocumentField("redirect_url") private String redirectUrl;         // 跳转链接
    @DocumentField("recommended") private Boolean recommended;         // 是否推荐到首页
    @DocumentField private MizarGoodsStatus status;                    // 课程状态
    private String topImage; // 顶部图
    private String smsMessage; // 发送短信
    private Integer totalLimit; // 课程总量
    private Integer dayLimit; // 天限量
    private Integer sellCount; // 总已售
    private Integer daySellCount; // 天已售
    private String productId; // 一起学商品id
    private Integer requireAddress; // 是否需要收货地址 2 不需要 1需要
    private String buttonColor; // 按钮颜色
    private String buttonText; // 按钮文案
    private String buttonTextColor; // 按钮颜色
    private String successText; // 报名成功文案
    private String offlineText;
    private Integer dealSuccess; // 1到店 2 到店并退款
    private String inputBGColor; // 输入框背景色
    private String clazzLevel;  // 年级
    private Integer requireSchool; // 是否填写学校
    private String schoolAreas; // 校区逗号分隔
    private Integer requireStudentName; // 学生姓名
    private Integer requireRegion; // 地区

    private String goodsType;                                          // 课程类型，family_activity-亲子活动
    // 亲子活动相关
    private String expenseDesc;                                        // 费用说明 富文本
    private String reportDesc;                                         // 体验报告 富文本 自己填写Url
    private String activityDesc;                                       // 活动介绍 富文本
    private List<MizarGoodsItem> items;                                // 产品类型 List<Object>
    private String address;                                            // 活动位置
    private Double longitude;                                          // 位置经度
    private Double latitude;                                           // 位置纬度
    private String contact;                                            // 联系方式
    private String successUrl;                                         // 报名成功过后的上课URl

    @DocumentCreateTimestamp private Date createAt;
    @DocumentUpdateTimestamp private Date updateAt;

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(MizarShopGoods.class, id);
    }

    public static String ck_shopId(String shopId) {
        return CacheKeyGenerator.generateCacheKey(MizarShopGoods.class, "shopId", shopId);
    }

    public static String familyActivityType() {
        return "family_activity";
    }

    public static String usTalkType() {
        return "ustalk";
    }

    @Override
    public MizarShopGoods clone() {
        try {
            return (MizarShopGoods) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new UnsupportedOperationException(ex);
        }
    }

    @JsonIgnore
    public boolean isFamilyActivity() {
        return familyActivityType().equals(getGoodsType());
    }

    @JsonIgnore
    public boolean isUSTalkActivity() {
        return usTalkType().equals(getGoodsType());
    }

    public Integer featureRequireAddress() {
        return requireAddress == null ? 2 : requireAddress;
    }
    public Integer featureDealSuccess() {
        return dealSuccess == null ? 1 : dealSuccess;
    }
    public Integer featureRequireSchool() {
        return requireSchool == null ? 2 : requireSchool;
    }
    public Integer featureRequireStudentName() {
        return requireStudentName == null ? 2 : requireStudentName;
    }
    public Integer featureRequireRegion() {
        return requireRegion == null ? 2 : requireRegion;
    }
    public static List<Integer> featureRange(String s) {
        if (StringUtils.isBlank(s)) {
            return null;
        }
        List<Integer> list = Arrays.stream(s.split("-")).map(SafeConverter::toInt).collect(Collectors.toList());
        if (list.isEmpty()) {
            return null;
        }
        List<Integer> iList = new ArrayList<>();
        for (int i = list.get(0); i <= list.get(list.size() - 1); i++) {
            iList.add(i);
        }
        return iList;
    }

    public static void main(String[] args) {
        System.out.println(ck_id("1111"));
    }
}

