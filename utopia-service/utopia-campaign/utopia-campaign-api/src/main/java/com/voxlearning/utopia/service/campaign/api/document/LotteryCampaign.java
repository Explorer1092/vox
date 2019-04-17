//package com.voxlearning.utopia.service.campaign.api.document;
//
//import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
//import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
//import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
//import com.voxlearning.alps.annotation.dao.DocumentConnection;
//import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
//import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
//import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
//import com.voxlearning.utopia.core.ObjectIdEntityWithDisabledField;
//import lombok.Getter;
//import lombok.Setter;
//
//import java.util.Date;
//
///**
// * Lottery campaign configuration
// * Created by alex on 2018/3/20.
// */
//@Getter
//@Setter
//@DocumentConnection(configName = "hs_misc")
//@DocumentTable(table = "VOX_LOTTERY_CAMPAIGN")
//@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
//@UtopiaCacheRevision("20180320")
//public class LotteryCampaign extends ObjectIdEntityWithDisabledField implements CacheDimensionDocument {
//    private static final long serialVersionUID = 1266664143345161315L;
//
//    @UtopiaSqlColumn private String campaignName;       // 活动名称
//    @UtopiaSqlColumn private Date campaignStartTime;    // 活动开始时间
//    @UtopiaSqlColumn private Date campaignEndTime;      // 活动结束时间
//    // 以下是一些抽奖处理相关的通用控制字段
//    @UtopiaSqlColumn private Integer bigAwardRewin;     // 重复中大奖控制，// 0：无控制，1：不允许同一个用户中多个大奖，2：不允许同校用户中多个大奖，3：不允许同一个地区用户中多个大奖
//
//    @Override
//    public String[] generateCacheDimensions() {
//        return new String[]{
//                newCacheKey(getId())
//        };
//    }
//
//    // 是否允许同一个用户中多个大奖
//    public boolean sameUserRewinAllowed() {
//        return bigAwardRewin == null || bigAwardRewin < 1;
//    }
//
//    // 是否允许同一个学校中多个用户中大奖
//    public boolean sameSchoolRewinAllowed() {
//        return bigAwardRewin != null && bigAwardRewin < 2;
//    }
//
//    // 是否允许同一个地区中多个用户中大奖
//    public boolean sameCountyRewinAllowed() {
//        return bigAwardRewin != null && bigAwardRewin < 3;
//    }
//
//}