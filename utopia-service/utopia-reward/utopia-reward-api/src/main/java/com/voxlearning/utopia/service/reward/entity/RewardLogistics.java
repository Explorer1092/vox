package com.voxlearning.utopia.service.reward.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * Created by Summer Yang on 2016/7/26.
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_reward")
@DocumentTable(table = "VOX_REWARD_LOGISTICS")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20171030")
public class RewardLogistics extends AbstractDatabaseEntity {
    private static final long serialVersionUID = -1819694035042654385L;

    @DocumentField("LOGISTIC_NO") private String logisticNo;        // 物流编码
    @DocumentField("COMPANY_NAME") private String companyName;      // 物流公司
    @DocumentField("TYPE") private Type type;                       // 类型
    @DocumentField("LOGISTIC_TYPE") private String logisticType;    // 配送方式
    @DocumentField("IS_BACK") private Boolean isBack;               // 是否倒回
    @DocumentField("PRICE") private Double price;
    @DocumentField("MONTH") private String month;
    @DocumentField("RECEIVER_ID") private Long receiverId;
    @DocumentField("RECEIVER_NAME") private String receiverName;
    @DocumentField("SCHOOL_ID") private Long schoolId;              // 学校ID
    @DocumentField("SCHOOL_NAME") private String schoolName;        // 学校名称
    @DocumentField("PHONE") private String sensitivePhone;          // 联系电话
    @DocumentField("DETAIL_ADDRESS") private String detailAddress;  // 详细地址
    @DocumentField("POST_CODE") private String postCode;            // 邮政编码
    @DocumentField("PROVINCE_CODE") private Long provinceCode;      // 省编码
    @DocumentField("PROVINCE_NAME") private String provinceName;    // 省
    @DocumentField("CITY_CODE") private Long cityCode;              // 市编码
    @DocumentField("CITY_NAME") private String cityName;            // 市
    @DocumentField("COUNTY_CODE") private Long countyCode;          // 区县编码
    @DocumentField("COUNTY_NAME") private String countyName;        // 区县
    @DocumentField("DISABLED") private Boolean disabled;
    @DocumentField("DELIVERED_TIME") private Date deliveredTime;    // 已发货的时间

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum Type {
        TEACHER("老师快递"),
        STUDENT("学生快递");

        @Getter
        private final String description;
    }

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(RewardLogistics.class, id);
    }

    public static String ck_receiver_type(Long uid, Type type) {
        return CacheKeyGenerator.generateCacheKey(RewardLogistics.class,
                new String[]{"UID", "TY"},
                new Object[]{uid, type});
    }

}
